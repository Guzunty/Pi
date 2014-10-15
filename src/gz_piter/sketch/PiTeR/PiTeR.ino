/*
 * PiTeR.ino
 *
 * Pi Terrestrial Robot
 * 
 * Copyright 2014  guzunty
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 * 
 * This program is the main setup and loop for PiTeR's ATMega firmware.
 * It is responsible for reading the Inertial Measurement Unit (IMU) and
 * driving the motors so that the robot stays upright. It also reads the
 * battery state and reports that to the host Raspberry Pi computer.
 *
 * The controller uses two cascaded Proportional Integral Derivative (PID)
 * algorithms. The lowest level one compares a target angle with the actual
 * measured angle from the IMU. Its output drives the motors so that the
 * target angle is achieved. When standing still, the target angle is zero,
 * i.e. upright.
 *
 * The second PID controller compares the desired wheel rotation rate with
 * the actual wheel rotation rate as measured by the motor encoders. When
 * standing still, the desired wheel rate is zero. When moving forwards, the
 * desired wheel rate is positive and it is negative when reversing. Its
 * output is the target angle input to the first PID controller.
 * 
 */

#include <math.h>
#include <Wire.h>

#include"Pid.h"

// *************************  IMU Array offsets  *************************
#define   GYR_Y                 0                             // Gyro Y
#define   ACC_Z                 1                             // Acc  Z
#define   ACC_X                 2                             // Acc  X

// **************************  Pin Assignments  **************************
#define   InA_R                 6                             // INA right 
#define   InB_R                 7                             // INB right
#define   PWM_R                 10                            // PWM right
#define   InA_L                 8                             // INA left
#define   InB_L                 11                            // INB left
#define   PWM_L                 9                             // PWM left

#define encodPinA_L             2                             // encoder A left  - Interrupt 0
#define encodPinA_R             3                             // encoder A right - Interrupt 1
#define encodPinB_L             4                             // encoder B left
#define encodPinB_R             5                             // encoder B right

#define LED                     13                            // LED

// **************************  Separators  **************************
#define   LINE_END              10                            // \n
#define   SPLIT                 58                            // :

#define STANDING_RATE           4.0
#define MOTOR_DISARM_TO         25                            // Approximately 100ms at 4ms per cycle
#define MOTOR_ARM_TO            50                            // Approximately 200ms at 4ms per cycle
int   STD_LOOP_TIME  =          10000;

int sensorValue[3] = { 0, 0, 0 };
int sensorZero[3] = { 0, 0, 0 }; 
unsigned long lastLoopTime = STD_LOOP_TIME;
unsigned long lastLoopUsefulTime = STD_LOOP_TIME;
unsigned long loopStartTime = 0;
unsigned long lastFusionTime = 0;
unsigned long lastWheelTime = 0;
unsigned long lastPidTime = 0;

float fusedAngle = 0.0;          // angles in QUIDS (360° = 2PI = 1204 QUIDS   <<<
float ACC_angle = 0.0;
float GYRO_rate = 0.0;
float setPoint = 0.0;
float lastSetPoint = 0.0;
float basePoint = 0.0;
int   drive = 0;
int   updateRate = 5;            // Nr of loop to skip sending and receving info from PC
float motorOffsetL = 1;          // The offset for left motor
float motorOffsetR = 1;          // The offset for right motor
float wheelRate = 0.0;
float targetWheelRate = 0.0;
float targetTurnRate= 0.0;
int   posnError = 0;
long  targetPosn = 0;

float rate_R = 0;                // Rates are computed in updateEncoders()
float rate_L = 0;
volatile long posn_R = 0;        // These are updated by the encoder interrupt defined in motors module.
volatile long posn_L = 0;        // Must be volatile to tell the compiler not to optimise into registers.

boolean calibrated = false;
boolean armed = true;

// *****************  Proportional Integral Derivative  *****************
//Pid    angleCtrl(0.75, 6.6, 1.9, 5.5, false);         // 7.4v, 124mm wheels, linear response
//Pid    wheelCtrl(0.6, 4.5, 3.0, 3.3, false);
Pid    angleCtrl(0.5, 6.6, 1.9, 5.5, false);            // 11.1v, 124mm wheels, linear response
Pid    wheelCtrl(0.8, 4.5, 3.0, 2.0, false);

void setup() {
  Serial.begin(115200);
  for(int pin=InA_R; pin<=InB_L; pin++) {
    pinMode(pin, OUTPUT);                               // set output mode
  }
  TCCR1B = TCCR1B & 0b11111000 | 0x02;                  // Set 3921.16 Hz PWM frequency
  bitSet(TCCR1B, WGM12);                                // Set timer 1 to Fast PWM mode (f = 7842.32)
  delay(100);
  MPU6050_setup();  
  calibrated = calibrateSensors();
  pinMode(LED, OUTPUT);                                 // Set up to post state output
  if (!calibrated) {
    digitalWrite(LED, HIGH);
  }
  setupEncoders();
  targetPosn = (posn_L + posn_R);
  setUpDistanceSensor();
}

int motorDisableCount = 0;
int motorEnableCount = 0;

void loop() {
  // ********************* Sensor aquisition & filtering *******************
  updateSensors();
  updateEncoders();
  
  ACC_angle = getAccAngle();                            // in Quids (1024 Quids=360°)
  GYRO_rate = getGyroRate();                            // in Quids per second
  unsigned long time = micros();
  fusedAngle = fuse(ACC_angle, GYRO_rate, getElapsedMicros(time, lastFusionTime));   // calculate Absolute Angle
  lastFusionTime = time;
  wheelRate = ((rate_R + rate_L) / 2.0) * 1000;         // in interrupts per millisecond
  if (calibrated) {                                     // 1 interrupt is 1/960 of a revolution
    if (armed) {
      // ***************************** Distance ********************************
      startPing();
      // ************************** Wheel movement *****************************
      time = micros();
      endPing();                                          // End trigger > 10 uS after starting
      if (getDistance() < 15 && targetWheelRate > 0) {    // Protect robot from collision
        setPoint = basePoint - wheelCtrl.updatePid(0.0, wheelRate, getElapsedMicros(time, lastWheelTime));
      }
      else if (getDistance() < 20 && targetWheelRate > 0) {    // Slow robot before collision
        setPoint = basePoint - wheelCtrl.updatePid(targetWheelRate / 2.0, wheelRate, getElapsedMicros(time, lastWheelTime));
      }
      else {                                              // Normal wheel rate control path
        setPoint = basePoint - wheelCtrl.updatePid(targetWheelRate, wheelRate, getElapsedMicros(time, lastWheelTime));
      }
      lastWheelTime = time;
      // *********************** PID and motor drive ***************************
      time = micros();
      drive = angleCtrl.updatePid(setPoint, fusedAngle,  getElapsedMicros(time, lastPidTime));
      lastPidTime = time;
      if (fusedAngle < (basePoint - 190) || fusedAngle > (basePoint + 190)) {
        if (motorDisableCount == MOTOR_DISARM_TO) {
          armed = false;                                  // Disable motors if we
          digitalWrite(LED, HIGH);                        // cannot recover.
          motorDisableCount = 0;
        }
        else {
          motorDisableCount++;
        }
      }
      else {                                              // Reset the count if we were not outside
        motorDisableCount = 0;                            // the target band.
      }
    }
    else {                                                // not armed
      if (fusedAngle > (basePoint - 20) && fusedAngle < (basePoint + 20)) {
        if (motorEnableCount == MOTOR_ARM_TO) {
          armed = true;                                   // Re-enable motors within a narrow band
          angleCtrl.resetIntegratedError();               // of vertical (avoids voltage drop -
          wheelCtrl.resetIntegratedError();               // motors never surge to full)
          digitalWrite(LED, LOW);
          motorEnableCount = 0;
          drive = 0;
        }
        else {
          motorEnableCount++;
        }
      }
      else {                                              // Reset the count if we were not within
        motorEnableCount = 0;                             // the target band.
      }
    }
  }
  if (calibrated && armed) {
    driveMotor(drive);
  }
  else {
    brakeMotor();
  }

// ************************* Serial ****************************
  serialOut();

// ************************* Timing ****************************
  time = micros();
  lastLoopTime = getElapsedMicros(time, loopStartTime);
  loopStartTime = time;
}

void serialEvent() {
  serialIn();
}

unsigned long getElapsedMicros(unsigned long time, unsigned long lastValue) {
   if (lastValue < time) {
     return time - lastValue;
   }
   else {                                               // Arduino microsecond counter overflowed
     return time + (0xffffffffL - lastValue);
   }
}
