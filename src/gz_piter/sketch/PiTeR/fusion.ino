/*
 * fusion.ino
 *
 * Pi Terrestrial Robot - Sensor fusion algorithms
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
 * The algorithms defined in this file are used to fuse the sensor data
 * from the PiTeR onboard gyro and accelerometer. This fusion is needed
 * because the accelerometer data is extremely sensitive to vibration,
 * while the gyro output drifts over time. By combining the output from
 * both sensors, the accelerometer keeps the output from drifting, while
 * the gyro reacts to changes of angle quickly enough to allow the motors
 * to balance the robot.
 * 
 */

    // Kalman filter
    double Q_angle  =  0.001; //0.001
    double Q_gyro   =  0.003; //0.002;  //0.003
    double R_angle  =  0.038;  //0.04;  //0.03

    double x_angle = 0;
    double x_bias = 0;
    double P_00 = 0, P_01 = 0, P_10 = 0, P_11 = 0;

  float fuse(float newAngle, float newRate,int looptime) {
    double dt, y, S;
    double K_0, K_1;

    dt = double(looptime)/1000000.0;
    x_angle += dt * (newRate - x_bias);
    P_00 += dt * (dt * P_11 - P_01 - P_10 + Q_angle);
    P_01 +=  - dt * P_11;
    P_10 +=  - dt * P_11;
    P_11 +=  + Q_gyro * dt;
    
    y = newAngle - x_angle;
    S = P_00 + R_angle;
    K_0 = P_00 / S;
    K_1 = P_10 / S;
    
    x_angle +=  K_0 * y;
    x_bias  +=  K_1 * y;
    P_00 -= K_0 * P_00;
    P_01 -= K_0 * P_01;
    P_10 -= K_1 * P_00;
    P_11 -= K_1 * P_01;
    
    return x_angle;
  }

void resetFilter() {
    x_angle = 0;
    x_bias = 0;
    P_00 = 0; P_01 = 0; P_10 = 0; P_11 = 0;	
}

/* Alternate implementation slightly faster, but slightly more noisy 
// Complementary filter
// newAngle = angle measured with atan2 using the accelerometer
// newRate = angle measured using the gyro
// looptime = loop time in microseconds.
double x1 = 0.0;
double y1 = 0.0;
double x2 = 0.0;
double x_angle2C = 0.0;

float fuse(float newAngle, float newRate,int looptime) {
  double k=2.5;
  double dtc2=double(looptime)/1000000.0;

  x1 = (newAngle -   x_angle2C)*k*k;
  y1 = dtc2*x1 + y1;
  x2 = y1 + (newAngle -   x_angle2C)*2*k + newRate;
  x_angle2C = dtc2*x2 + x_angle2C;

  return x_angle2C;
}
*/
