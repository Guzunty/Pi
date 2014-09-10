/*
 * distance.ino
 *
 * Pi Terrestrial Robot - Distance sensor
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
 * The algorithms defined in this file are used to drive the Ultrasound
 * distance sensor.
 */

#define PING 13
#define ECHO_MAX 100000

int state = 0;
float distance = 0.0;

void setUpDistanceSensor() {
  PCMSK0 |= (1 << PCINT4);           // Enable pin 12 as the echo return
  PCIFR  |= (1 << PCIF0);            // Clear the interrupt if we had one already     
  PCICR  |= (1 << PCIE0);            // Enable pin change interrupts on pins 8 - 13 (Port B)
  pinMode(PING, OUTPUT);             // Pin 13 will initiate the ping signal
}

int pingThrottle = 0;
unsigned long pingTime = 0L;
unsigned long echoTime = 0L;

void startPing() {
  if (pingThrottle++ > 25) {         // Gives approx. 100ms ping period at 4ms cycle time 
    digitalWrite(PING, HIGH);
    if (state != 0) {
      echoTime = ECHO_MAX;            // We never had a return signal, record maximum distance
    }
    state = 1;
    pingThrottle = 0;
  }
}

void endPing() {
  if (state == 1) {
    digitalWrite(PING, LOW);
    state = 2;
  }
}

ISR(PCINT0_vect) {
  if (state == 2) {                // Rising edge of pin 12
    pingTime = micros();
    state = 3;
  }
  else {                           // Falling edge of pin 12
    echoTime = getElapsedMicros(micros(), pingTime);
    state = 0;
  } 
}

float getDistance() {
  return ((float)echoTime) / 58.0; // Result in centimetres
}
