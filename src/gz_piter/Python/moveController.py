#!/usr/bin/python
#
# moveController.py
#
# Author: Derek Campbell
# Date  : 23/10/2014
#
#  Copyright 2014  <guzunty@gmail.com>
#  
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#  
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#  
#  You should have received a copy of the GNU General Public License
#  along with this program; if not, write to the Free Software
#  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
#  MA 02110-1301, USA.
#  
# This class allows the user to control PiTeRs movement.
#
import time
import threading
import struct
import serial

def float2hex(f):
  return struct.pack('>f', f)

class MoveController(threading.Thread):
  
  def __init__(self):
    super(MoveController, self).__init__()
    self.lastTime = int(round(time.time() * 1000))
    self.actionList = []
    # Initialise the serial port
    try:
      self.ser = serial.Serial('/dev/ttyAMA0', 115200, timeout=1)
      self.ser.open()
    except RuntimeError:
      print "Error opening serial port"
      quit()
    self.currentRate = 0.0
    self.currentTurn = 0.0
    self.userInPlaceTurnEnabled = True
    self.active = True
    self.gate = threading.Condition()

  def newDriveAction(self, rate, delay, turn = 0.0):
    self.gate.acquire()
    if (len(self.actionList) == 0):
      self.lastTime = int(round(time.time() * 1000))
    self.actionList.append([rate, delay, turn])
    self.gate.notify()
    self.gate.release()
  
  def newUserTurnAction(self, rate, delay, turn):
    if (self.userInPlaceTurnEnabled == False):
      turn = self.currentTurn
    self.newDriveAction(rate, delay, turn)

  def disableUserInPlaceTurn(self):
    self.userInPlaceTurnEnabled = False

  def enableUserInPlaceTurn(self):
    self.userInPlaceTurnEnabled = True

  def run(self):
    self.gate.acquire()
    while(self.active == True):
      curTime = int(round(time.time() * 1000))
      elapsed = curTime - self.lastTime
      self.lastTime = curTime
      if (len(self.actionList) > 0):
        for x in self.actionList:
          if (x[1] <= elapsed):
            self.currentRate = x[0]
            self.currentTurn = x[2]
            self.writeSerial(x[0], x[2])
            self.actionList.remove(x)
          else:
            x[1] = x[1] - elapsed
        self.gate.release()
        time.sleep(0.001)
        self.gate.acquire()
      else:
        self.gate.wait()
        self.lastTime = int(round(time.time() * 1000))
    self.gate.release()

  def writeSerial(self, rate, turn):
    self.ser.write('w:')
    self.ser.write(float2hex(rate))
    self.ser.write('t:')
    self.ser.write(float2hex(turn))
    self.ser.flush()

  def stop(self):
    self.gate.acquire()
    self.active = False
    self.gate.notify()
    self.gate.release()

  def getCurrentRate(self):
    return self.currentRate
