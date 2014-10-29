#!/usr/bin/python
#
# actionController.py
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
# This class allows the robot LEDs to be flashed and servos to be
# operated without resorting to sleep calls. We do this so that driving
# controls remain responsive during periods of activity.
#  

import time
import threading
import spidev
import GZ

class ActionController(threading.Thread):
  
  def __init__(self):
    super(ActionController, self).__init__()
    GZ.clock_ena(GZ.GZ_CLK_5MHz, 180)
    self.spi = spidev.SpiDev()
    self.spi.open(0,1)
    self.lastTime = int(round(time.time() * 1000))
    self.actionList = []
    self.active = True
    self.gate = threading.Condition()

  def newLEDAction(self, ledID, colour, delay):
    self.gate.acquire()
    self.actionList.append([ledID + 2, colour, delay])
    self.gate.notify()
    self.gate.release()

  def newServoAction(self, servoID, angle, delay):
    self.gate.acquire()
    if (len(self.actionList) == 0):
      self.lastTime = int(round(time.time() * 1000))
    self.actionList.append([servoID, angle, delay])
    self.gate.notify()
    self.gate.release()

  def run(self):
    self.gate.acquire()
    while(self.active == True):
      curTime = int(round(time.time() * 1000))
      elapsed = curTime - self.lastTime
      self.lastTime = curTime
      if (len(self.actionList) > 0):
        for x in self.actionList:
          if (x[2] <= elapsed):
            self.writePWM(x[0], x[1])
            self.actionList.remove(x)
          else:
            x[2] = x[2] - elapsed
        self.gate.release()
        time.sleep(0.001)
        self.gate.acquire()
      else:
        self.gate.wait()
        self.lastTime = int(round(time.time() * 1000))
    self.gate.release()

  def writePWM(self, addr, value):
    self.spi.xfer([addr, value])

  def resetLEDs(self):
    self.writePWM(2, 0)
    self.writePWM(3, 0)

  def stop(self):
    self.gate.acquire()
    self.active = False
    self.gate.notify()
    self.gate.release()
