#!/usr/bin/python
#
# scriptPlayer.py
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
# This is the class which plays the script.py user program.
#
# The class uses the python 'exec' command to realise the "program
# within a program" feature of script.py. It adds a number of built
# in functions and objects the script can use.
# 
# 'say(string)' allows PiTeR to speak a text string passed to it.
# 'ledCtrllr' is an instance of the avatars LED control program.
# It allows the script to send timed sequences of commands to the LEDs.
#
 
import os

def say(phrase):
  message = 'flite -voice slt -t "' + phrase.replace('\n', '').replace('\r', '') + '"&'
  os.system(message)

class scriptPlayer:
  
  def __init__(self, f, ledCtrlr):
    self.script = list(f)
    self.reset()
    self.locals = {'ledCtrlr': ledCtrlr}
    self.globals = {'say' : say}

  def cue(self):
    self.curLine = self.curLine + 1
    lines = ''
    while (self.curLine < len(self.script) and self.script[self.curLine].replace('\n', '').replace('\r', '') != "waitForCue()"):
      lines = lines + self.script[self.curLine]
      self.curLine = self.curLine + 1
    if (len(lines) > 0):
      exec lines in self.locals, self.globals

  def reset(self):
    self.curLine = -1

