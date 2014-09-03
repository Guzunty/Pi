/*
 * motors.ino
 *
 * Pi Terrestrial Robot - PiTeR - Motor Control
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
 * This file contains the functions needed to send signals to the motor
 * controller hardware to drive the motors. It also includes the interrupt
 * routines used to capture the encoder output from the motors.
 */

#define GUARD_DRIVE 16
#define DEAD_BAND 2

int lastTorque = 0;

int driveMotor(int torque)  {
  if (abs(torque - lastTorque) > GUARD_DRIVE) {               // Ramp motor if necessary
    if ((torque - lastTorque) > 0) {
      torque = lastTorque + GUARD_DRIVE;
    }
    else {
      torque = lastTorque - GUARD_DRIVE;
    }
  }
  lastTorque = torque;
  // At 7.2 volts, PiTeR has a mechanical deadband of about +/- 13. We don't want anything that
  // big or else the robot will oscillate. We do want a smaller deadband, so we chose 2.
  // At 12 volts, there is no measurable deadband.
  float wheelFactor = 1.0;
  int torqueR = torque * motorOffsetR + (targetTurnRate / wheelFactor);
  torqueR = constrain(torqueR, -255, 255);
  if (torqueR >= DEAD_BAND)  {                                        // drive motor forward
    digitalWrite(InA_R, LOW);                        
    digitalWrite(InB_R, HIGH);
  }
  else if (torqueR <= -DEAD_BAND) {                                   // drive motor backward
    digitalWrite(InA_R, HIGH);                       
    digitalWrite(InB_R, LOW);
    torqueR = abs(torqueR);
  }
  if (torqueR >= DEAD_BAND) {
    //torqueR = map(torqueR,2,255,6,255);
    analogWrite(PWM_R,torqueR);
  }
  else {
    digitalWrite(InA_R, HIGH);
    digitalWrite(InB_R, HIGH);
    analogWrite(PWM_R, 255);                                  // brake right
  }
  int torqueL = torque * motorOffsetL - (targetTurnRate / wheelFactor);
  torqueL = constrain(torqueL, -255, 255);
  if (torqueL >= DEAD_BAND)  {                                        // drive motor forward
    digitalWrite(InA_L, LOW);                     
    digitalWrite(InB_L, HIGH);
  }
  else if (torqueL <= -DEAD_BAND) {                                   // drive motor backward
    digitalWrite(InA_L, HIGH);                      
    digitalWrite(InB_L, LOW);
    torqueL = abs(torqueL);
  }
  if (torqueL >= DEAD_BAND) {
    //torqueL = map(torqueL,2,255,6,255);
    analogWrite(PWM_L,torqueL);
  }
  else {    
    digitalWrite(InA_L, HIGH);
    digitalWrite(InB_L, HIGH);
    analogWrite(PWM_L, 255);                                  // brake left
  }
}

int brakeMotor()  {
  digitalWrite(InA_R, HIGH);
  digitalWrite(InB_R, HIGH);
  analogWrite(PWM_R, 255);
  digitalWrite(InA_L, HIGH);
  digitalWrite(InB_L, HIGH);
  analogWrite(PWM_L, 255);
}

void setupEncoders()  {
  pinMode(encodPinA_L, INPUT); 
  pinMode(encodPinB_L, INPUT); 
  pinMode(encodPinA_R, INPUT); 
  pinMode(encodPinB_R, INPUT);
  digitalWrite(encodPinA_L, HIGH);                      // turn on pullup resistors
  digitalWrite(encodPinB_L, HIGH);
  digitalWrite(encodPinA_R, HIGH);
  digitalWrite(encodPinB_R, HIGH);
  attachInterrupt(1, encoderRInterrupt, CHANGE);
  attachInterrupt(0, encoderLInterrupt, CHANGE);
}

volatile unsigned long lastEncoderRDTime = 0;
volatile unsigned long lastEncoderLDTime = 0;
volatile int encoderRDir = 1;
volatile int encoderLDir = 1;
volatile boolean encoderRSeen = false;
volatile boolean encoderLSeen = false;
unsigned long lastEncoderUpdateTime = 0;

void updateEncoders() {
  unsigned long time = micros();
  unsigned long deltaTime = time - lastEncoderUpdateTime;
  if (encoderRSeen) {
    encoderRSeen = false;
  }
  else {
    lastEncoderRDTime += deltaTime;
  }
  rate_R = 1.0 / (float)lastEncoderRDTime;
  if (encoderRDir < 0) {
    rate_R = -rate_R;
  }
  if (encoderLSeen) {
    encoderLSeen = false;
  }
  else {
    lastEncoderLDTime += deltaTime;
  }
  rate_L = 1.0 / (float)lastEncoderLDTime;
  if (encoderLDir < 0) {
    rate_L = -rate_L;
  }
  lastEncoderUpdateTime = time;
}

unsigned long lastEncoderRTime = 0;

void encoderRInterrupt()  {
  unsigned long time = micros();
  lastEncoderRDTime = time - lastEncoderRTime;
  encoderRDir = 1;
  if (PIND & 0b00001000) {                            // RISING
    if (PIND & 0b00100000) {
      encoderRDir = -1;
    }
  }
  else {                                              // FALLING
    if (not(PIND & 0b00100000)) {
      encoderRDir = -1;
    }
  }
  posn_R += encoderRDir;
  lastEncoderRTime = time;
  encoderRSeen = true;
}

unsigned long lastEncoderLTime = 0;

void encoderLInterrupt()  {
  unsigned long time = micros();
  lastEncoderLDTime = time - lastEncoderLTime;
  encoderLDir = 1;
  if (PIND & 0b00000100) {                            // RISING
    if (not(PIND & 0b00010000)) {
      encoderLDir = -1;
    }
  }
  else {                                              // FALLING
    if (PIND & 0b00010000) {
       encoderLDir = -1;
    }
  }
  posn_L += encoderLDir;
  lastEncoderLTime = time;
  encoderLSeen = true;
}
