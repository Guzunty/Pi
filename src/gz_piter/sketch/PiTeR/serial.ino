/*
 * serial.ino
 *
 * Pi Terrestrial Robot - PiTeR - Serial Drivers
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
 * This file contains the functions needed to exchange serial data with
 * the host Raspberry Pi. Incoming data consists of commands for
 * movement and balance trimming. Outgoing data includes current voltage
 * and any other telemetry needed by the designer.
 *
 */

int skipOut=0;

void serialOut_GUI() {  
  
  if(skipOut++>=updateRate) {                                                        
    skipOut = 0;
    //Filtered and unfiltered angle
    Serial.print(ACC_angle, DEC);  Serial.print(",");
    Serial.print(fusedAngle, DEC);   Serial.print(",");
    
    //Raw sensor data
    Serial.print(sensorValue[ACC_X], DEC);   Serial.print(",");
    Serial.print(sensorValue[ACC_Z], DEC);   Serial.print(",");
    Serial.print(sensorValue[GYR_Y], DEC);   Serial.print(",");
   
    writeAngleData();
    //loop
    Serial.print(STD_LOOP_TIME, DEC);        Serial.print(",");
    Serial.print(angleCtrl.getIntegratedError(), DEC);            Serial.print(",");
    Serial.print(lastLoopTime, DEC);         Serial.print(",");
    Serial.print(updateRate, DEC);           Serial.print(",");
    
    Serial.print(motorOffsetL, DEC);         Serial.print(",");
    Serial.print(motorOffsetR, DEC);         Serial.print(",");

    //encoder
    Serial.print(rate_R  * 5000, DEC);       Serial.print(",");
    Serial.print((rate_L * 5000) -10.0, DEC); Serial.print(","); // Offset lets us see two values separately
    Serial.print((wheelRate * 5) -20.0, DEC); Serial.print(",");
    Serial.print((targetWheelRate * 50) -30.0, DEC); Serial.print(",");
    writeWheelData();
     Serial.print("\n");
  }
}

void writeWheelData() {
/*    Serial.print(wheelCtrl.getPTerm(), DEC); Serial.print(",");
    Serial.print(wheelCtrl.getITerm(), DEC); Serial.print(",");
    Serial.print(wheelCtrl.getDTerm(), DEC); Serial.print(",");
    Serial.print(drive, DEC);                Serial.print(",");
    Serial.print(wheelCtrl.getError(), DEC); Serial.print(",");
    Serial.print(setPoint, DEC);             Serial.print(",");
*/    
    //PID Parameters
    Serial.print(wheelCtrl.getK(), DEC);     Serial.print(",");
    Serial.print(wheelCtrl.getKp(), DEC);    Serial.print(",");
    Serial.print(wheelCtrl.getKi(), DEC);    Serial.print(",");
    Serial.print(wheelCtrl.getKd(), DEC);
}

void writeAngleData() {
    Serial.print(angleCtrl.getPTerm(), DEC); Serial.print(",");
    Serial.print(angleCtrl.getITerm(), DEC); Serial.print(",");
    Serial.print(angleCtrl.getDTerm(), DEC); Serial.print(",");
    Serial.print(drive, DEC);                Serial.print(",");
    Serial.print(angleCtrl.getError(), DEC); Serial.print(",");
    Serial.print(setPoint, DEC);             Serial.print(",");
    
    //PID Parameters
    Serial.print(angleCtrl.getK(), DEC);     Serial.print(",");
    Serial.print(angleCtrl.getKp(), DEC);    Serial.print(",");
    Serial.print(angleCtrl.getKi(), DEC);    Serial.print(",");
    Serial.print(angleCtrl.getKd(), DEC);    Serial.print(",");
}

union u_tag {
  byte b[8];
  float fval;
} u;

void serialIn() {
  if (Serial.available() > 0 ) {
    char param = Serial.read(); 
    delay(1);
    int av = Serial.available();
    byte inByte[av];
    if(Serial.read() == SPLIT) {
      if (Serial.available() >= 4) {
        u.b[3] = Serial.read();
        u.b[2] = Serial.read();
        u.b[1] = Serial.read();
        u.b[0] = Serial.read();
        switch (param) {
          case 'P':
            wheelCtrl.setKp(u.fval);
            break;
          case 'I':
            wheelCtrl.setKi(u.fval);
            wheelCtrl.resetIntegratedError();
            break;
          case 'D':
            wheelCtrl.setKd(u.fval);
            break;
          case 'K':
            wheelCtrl.setK(u.fval);
            break;
          case 'p':
            angleCtrl.setKp(u.fval);
            break;
          case 'i':
            angleCtrl.setKi(u.fval);
            angleCtrl.resetIntegratedError();
            break;
          case 'd':
            angleCtrl.setKd(u.fval);
            break;
          case 'k':
            angleCtrl.setK(u.fval);
            break;
          case 's':
            basePoint = u.fval;
            break;
          case 'u':
            updateRate = int(u.fval);
            break;
          case 'l':
            motorOffsetL = u.fval;
            break;
          case 'r':
            motorOffsetR = u.fval;
            break;
          case 'w':
            targetWheelRate = u.fval * 0.05;
            break;
          case 't':
            targetTurnRate = u.fval;
            break;
          case 'b':
            basePoint = u.fval;
            break;
        }
      }
    }
  }
  else { // Formatting issue, flush the buffer
    Serial.flush();
  }
}

void serialOut_raw() {
  static int skip=0;
  if(skip++==40) {                                                        
    skip = 0;
    Serial.print("ACC_X:");	       Serial.print(sensorValue[ACC_X]);  
    Serial.print("  ACC_Z:");	       Serial.print(sensorValue[ACC_Z]);
    Serial.print("  GYR_Y:");	       Serial.println(sensorValue[GYR_Y]);
    Serial.print("Acc Angle: ");       Serial.println(getAccAngle());
    
  }
}

void serialOut_timing() {
  static int skip=0;
  if(skip++==5) { // display every 500 ms (at 100 Hz)
    skip = 0;
    Serial.print(lastLoopUsefulTime); Serial.print(",");
    Serial.print(lastLoopTime);       Serial.print("\n");
  }
}

typedef union {
  byte out[2];
  int in;
}  convertRead;
unsigned int throttle = 0;

void serialOut_runtime() {
  convertRead result;
  throttle++;
  if ((throttle & 0xfff) == 0) {
    result.in = analogRead(0);            // Report voltage
    Serial.write("v:");
    Serial.write(result.out, 2);
  }
  unsigned int throttleLSB = throttle & 0xff;
  if (throttleLSB == 0x80 || throttleLSB == (0x80 - 0x55) || throttleLSB == (0x80 + 0x55)) {
    result.in = analogRead(1);            // Report distance
    Serial.write("d:");
    Serial.write(result.out, 2);
  }
  if (throttleLSB == 0) {
    result.in = int(wheelRate);
    Serial.write("r:");
    Serial.write(result.out, 2);
  }
}
