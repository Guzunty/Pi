#!/usr/bin/python
#
# script.py
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
# This is the PiTeR avatar action script
#
# It is a python program within a python program. In other words, this 
# script is executed under the control of the main avatar control
# program, PiTeR.py.
#
# We do this so that we can special case the function, 'waitForCue'.
# Each time this function is called in this script, PiTeR's activity
# will pause and wait for the cue button to be pressed on the
# controller. In the case of the Wii-mote, this is the 'B' button.
#  

import time

def police():
  for i in range(0, 63):
    actionCtrlr.newLEDAction(0, 0x3, i*100)
    actionCtrlr.newLEDAction(0, 0xff, (i*100) + 50)
    actionCtrlr.newLEDAction(0, 0x00, (i*100) + 80) 
    actionCtrlr.newLEDAction(1, 0x3, (i*100) + 50)
    actionCtrlr.newLEDAction(1, 0xff, (i*100) + 100)
    actionCtrlr.newLEDAction(1, 0x00, (i*100) + 130) 
  actionCtrlr.newLEDAction(0, 0x00, 6500)
  actionCtrlr.newLEDAction(1, 0x00, 6500)


def reversing():
  for i in range(0, 15):
    actionCtrlr.newLEDAction(0, 0xf0, i*400)
    actionCtrlr.newLEDAction(0, 0x00, (i*400) + 200)
    actionCtrlr.newLEDAction(1, 0xf0, (i*400) + 200)
    actionCtrlr.newLEDAction(1, 0x00, (i*400) + 400)
  actionCtrlr.newLEDAction(0, 0x00, 6500)
  actionCtrlr.newLEDAction(1, 0x00, 6500)

def drive(speed, length):
  moveCtrlr.newDriveAction(speed, 0)
  moveCtrlr.newDriveAction(-speed, length)
  moveCtrlr.newDriveAction(0, length * 2)

say("Hello, my name is Piter")
waitForCue()
say("I can be a police robot")
police()
waitForCue()
say("I can be a dumper truck")
reversing()
waitForCue()
say("five")
drive(5.0, 1000)
waitForCue()
say("ten")
drive(10.0, 1000)
waitForCue()
say("fifteen")
drive(15.0, 1000)
waitForCue()
say("forward")
moveCtrlr.newDriveAction(5.0, 0)
moveCtrlr.newDriveAction(0.0, 3000)
waitForCue()
say("backwards")
moveCtrlr.newDriveAction(-5.0, 0)
moveCtrlr.newDriveAction(0.0, 3000)
waitForCue()
say("turn")
moveCtrlr.disableUserInPlaceTurn()
moveCtrlr.newDriveAction(0.0, 0, 20.0)
moveCtrlr.newDriveAction(0.0, 3000)
time.sleep(3)
moveCtrlr.enableUserInPlaceTurn()

