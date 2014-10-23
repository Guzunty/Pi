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

def police():
  for i in range(0, 63):
    ledCtrlr.newLEDAction(0, 0x3, i*100)
    ledCtrlr.newLEDAction(0, 0xff, (i*100) + 50)
    ledCtrlr.newLEDAction(0, 0x00, (i*100) + 80) 
    ledCtrlr.newLEDAction(1, 0x3, (i*100) + 50)
    ledCtrlr.newLEDAction(1, 0xff, (i*100) + 100)
    ledCtrlr.newLEDAction(1, 0x00, (i*100) + 130) 
  ledCtrlr.newLEDAction(0, 0x00, 6500)
  ledCtrlr.newLEDAction(1, 0x00, 6500)


def reversing():
  for i in range(0, 15):
    ledCtrlr.newLEDAction(0, 0xf0, i*400)
    ledCtrlr.newLEDAction(0, 0x00, (i*400) + 200)
    ledCtrlr.newLEDAction(1, 0xf0, (i*400) + 200)
    ledCtrlr.newLEDAction(1, 0x00, (i*400) + 400)
  ledCtrlr.newLEDAction(0, 0x00, 6500)
  ledCtrlr.newLEDAction(1, 0x00, 6500)

say("Hello, my name is Piter")
waitForCue()
say("I can be a police robot")
police()
waitForCue()
say("I can be a dumper truck")
reversing()
