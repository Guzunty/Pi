#!/usr/bin/python
#
# ledController.py
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
# This class allows the robot LEDs to be flashed without resorting to
# sleep calls. We do this so that driving controls remain responsive
# during periods of LED activity.
#  

import time
import spidev

class ledController:
  
  def __init__(self, spi):
    self.lastTime = int(round(time.time() * 1000))
    self.actionList = []
    self.spi = spi

  def newLEDAction(self, ledID, colour, delay):
    self.actionList.append([ledID, colour, delay])

  def poll(self):
    curTime = int(round(time.time() * 1000))
    elapsed = curTime - self.lastTime
    for x in self.actionList:
      if (x[2] <= elapsed):
        self.writePWM(x[0] + 2, x[1])
        self.actionList.remove(x)
      else:
        x[2] = x[2] - elapsed
    self.lastTime = curTime

  def writePWM(self, addr, value):
    self.spi.xfer([addr, value])

  def resetLEDs(self):
    self.writePWM(2, 0)
    self.writePWM(3, 0)
