/*
 * sensors.ino
 *
 * Pi Terrestrial Robot - PiTeR - Sensor Drivers
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
 * This file contains the functions needed to initialize and read the IMU
 * the accompanying MPU6050.ino file contains all the lowest level interface
 * routines. This one handles the next level up, calibration and conversion
 * from a digital integer values from the acceleromter axis values to a real
 * angle.
 */

boolean calibrateSensors() {                                       // Set zero sensor values
  long accSensorValue[3] = {0,0,0};
  int tempSensorValue[3] = {0,0,0};
  
  for (int i=0; i<50; i++) {
    readSensors(tempSensorValue);
    for (int j=0; j<3; j++) {
      accSensorValue[j] += tempSensorValue[j];
    }
  }
  
  for(int i=0; i<3; i++) {
    sensorZero[i] = (int)(accSensorValue[i]/50);
  }
  sensorZero[ACC_Z] -= 16384;                                   // Zl - Zr = (1684 - (-876))/2 = 2560 / 2
  boolean result = true;
  return (accSensorValue[ACC_Z] > abs(accSensorValue[ACC_X]));  // Disable motor drive if not upright
}

void updateSensors() {                                          // data acquisition
  long accSensorValue[3] = {0,0,0};
  int tempSensorValue[3] = {0,0,0};
  
  for (int i=0; i<5; i++) {
    readSensors(tempSensorValue);
    for (int j=0; j<3; j++) {
      accSensorValue[j] += tempSensorValue[j];
    }
  }  
  for(int i=0; i<3; i++) {
    sensorValue[i] = ((accSensorValue[i]/5) - sensorZero[i]);
  }
}

int readSensors(int *value) {
  accel_t_gyro_union accel_t_gyro;
  int error;
  
  error = MPU6050_read (MPU6050_ACCEL_XOUT_H, (uint8_t *) &accel_t_gyro, sizeof(accel_t_gyro));

  // Swap all high and low bytes.
  // After this, the registers values are swapped, 
  // so the structure member x_accel_l no 
  // longer contains the lower byte.
  uint8_t swap;
  #define SWAP(x,y) swap = x; x = y; y = swap

  SWAP (accel_t_gyro.reg.x_accel_h, accel_t_gyro.reg.x_accel_l);
  SWAP (accel_t_gyro.reg.y_accel_h, accel_t_gyro.reg.y_accel_l);
  SWAP (accel_t_gyro.reg.z_accel_h, accel_t_gyro.reg.z_accel_l);
  SWAP (accel_t_gyro.reg.t_h, accel_t_gyro.reg.t_l);
  SWAP (accel_t_gyro.reg.x_gyro_h, accel_t_gyro.reg.x_gyro_l);
  SWAP (accel_t_gyro.reg.y_gyro_h, accel_t_gyro.reg.y_gyro_l);
  SWAP (accel_t_gyro.reg.z_gyro_h, accel_t_gyro.reg.z_gyro_l);
  
  value[GYR_Y] = accel_t_gyro.value.y_gyro;
  value[ACC_X] = accel_t_gyro.value.x_accel;
  value[ACC_Z] = accel_t_gyro.value.z_accel;
}

float getGyroRate() {                                          // Gyro sensitivity=131 LSB/(deg/sec)
  return sensorValue[GYR_Y] * 0.021709923;                     // in quid/sec:((1/131) * 1024/360)
}

float getAccAngle() {                      
  return arctan2(-sensorValue[ACC_Z], -sensorValue[ACC_X]) + 256;  // in Quid: 1024/(2*PI))
}

float arctan2(int y, int x) {                                  // http://www.dspguru.com/comp.dsp/tricks/alg/fxdatan2.htm
   int coeff_1 = 128;                                          // angle in Quids (1024 Quids=360°) <<<<<<<<<<<<<<
   int coeff_2 = 3*coeff_1;
   float abs_y = fabs(y)+1e-10;                                // prevent 0/0 condition
   float r, angle;
   
   if (x >= 0) {
     r = (x - abs_y) / (x + abs_y);
     angle = coeff_1 - coeff_1 * r;
   }  else {
     r = (x + abs_y) / (abs_y - x);
     angle = coeff_2 - coeff_1 * r;
   }
   if (y < 0) {
     return int(-angle);                         // negate if in quad III or IV
   }
   else {
     return int(angle);
   }
}

