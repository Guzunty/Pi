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

typedef union {
  byte out[2];
  int in;
}  convertRead;
unsigned int throttle = 0;

void serialOut() {
  convertRead result;
  throttle++;
  if ((throttle & 0xfff) == 0) {
    result.in = analogRead(0);            // Report voltage
    Serial.write("v:");
    Serial.write(result.out, 2);
  }
  unsigned int throttleLSB = throttle & 0xff;
  if (throttleLSB == 0x80 || throttleLSB == (0x80 - 0x55) || throttleLSB == (0x80 + 0x55)) {
    result.in = getDistance();            // Report distance
    Serial.write("d:");
    Serial.write(result.out, 2);
  }
  if (throttleLSB == 0) {
    result.in = int(wheelRate);
    Serial.write("r:");
    Serial.write(result.out, 2);
  }
}
