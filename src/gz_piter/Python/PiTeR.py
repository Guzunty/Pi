#!/usr/bin/python
#
# PiTeR.py
#
# Author: Derek Campbell
# Date  : 22/10/2014
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
# This is the PiTeR avatar main control program
#
# Enables control with a Wii remote. See the startup message near 
# the bottom of the script for button meanings.
#  

import cwiid
import serial
import time
import struct
import os
import sys
import random
import GZ
import copy
import spidev
import scriptPlayer
import ledController
import faceFinder

def float2hex(f):
  return struct.pack('>f', f)

def hex2int(val):
  return struct.unpack('h', val)

def setWiiMoteLEDs(value):
  ledValue = 1
  if (value < 4):
    ledValue = 1 << value
  else:
    ledValue = 15 - (1 << (value - 4))
  wii.led = ledValue

parameters = ['k', 'p','i','d','K', 'P','I','D']
parmDefault = [0.5, 6.6, 1.9, 5.5, 0.8, 4.5, 3.0, 2.0] # Must match default values in arduino.
parmValue = copy.deepcopy(parmDefault)

def decrementParameter(currentParameter):
  parmValue[currentParameter] = parmValue[currentParameter] - 0.01
  writeParameter(currentParameter)
  displayParameter(currentParameter)

def incrementParameter(currentParameter):
  parmValue[currentParameter] = parmValue[currentParameter] + 0.01
  writeParameter(currentParameter)
  displayParameter(currentParameter)

def resetParameter(currentParameter):
  parmValue[currentParameter] = parmDefault[currentParameter]
  writeParameter(currentParameter)
  displayParameter(currentParameter)

def writeParameter(currentParameter):
  ser.write(parameters[currentParameter] + ':')
  ser.write(float2hex(parmValue[currentParameter]))
  ser.flush()

def displayParameter(currentParameter):
  output = parameters[currentParameter] + ": " + str(parmValue[currentParameter])
  print output

def writePWM(addr, value):
  spi.xfer([addr, value])

def dumpSettings():
  output = ""
  for i in range (0, 8):
    output = output + parameters[i] + ": " + str(parmValue[i]) + " "
  print output
  output = "TWR: " + str(handleCommand.targetWheelRate) + " TTR: " + str(targetTurnRate) + " BSP: " + str(handleCommand.balanceSetPoint)
  print output
  output = "MOS: " + str(handleCommand.motorOffset) + " STA: " + str(handleCommand.state) + " CPM: " + str(handleCommand.currentParameter)
  print output
  output = "PAN: " + str(handleCommand.headPan) + " TLT: " + str(handleCommand.headTilt)
  print output

def handleSerial():
  while (ser.inWaiting() >=4):
    frameType = ser.read(2)
    if (frameType == 'v:'):
      voltage = hex2int(ser.read(2))
      if (voltage[0] <= 740):
        print("Warning: Low voltage.")   # We'd like to halt
      #  os.system("sudo halt")          # eventually to protect
      #  quit()                          # the LiPo.
    elif (frameType == 'r:'):
      wheelRate = hex2int(ser.read(2))
      #print "Wheel rate: " + str(wheelRate[0])
    elif (frameType == 'd:'):
      distance = hex2int(ser.read(2))
      #print "Distance: " + str(distance[0])
    elif (frameType == 'a:'):
      handleSerial.angle = ((handleSerial.angle + hex2int(ser.read(2))[0]) / 2.0)
      if (abs(handleSerial.angle) < 20.0):
        tilt = handleCommand.headTilt - int(handleSerial.angle)
        if (tilt < 0):
          tilt = 0
        if (tilt > 63):
          tilt = 63
        writePWM(1, tilt)
    else:
      ser.flushInput()
handleSerial.angle = 0.0

# Commands
CMD_NONE =     0
CMD_EXIT =     1
CMD_SHUTDOWN = 2
CMD_DOWN =     3
CMD_UP =       4
CMD_LEFT =     5
CMD_RIGHT =    6
CMD_BRAKE =    7
CMD_ACCEL =    8
CMD_CUE =      9
CMD_AUTO =    10
CMD_NXTST =   11
CMD_NEXT =    12
CMD_PREV =    13
CMD_RESTRT =  14

# States
ST_DRIVE =     0
ST_BAL =       1
ST_TUNE =      2
ST_AUTO =      3

def getCommand():
  coasting = True
  buttons = wii.state['buttons']
  if (buttons - cwiid.BTN_PLUS - cwiid.BTN_MINUS == 0):
    return coasting, CMD_EXIT
  if (buttons - cwiid.BTN_PLUS - cwiid.BTN_MINUS - cwiid.BTN_A == 0):
    return coasting, CMD_SHUTDOWN
  if (buttons - cwiid.BTN_PLUS - cwiid.BTN_B == 0):
    return coasting, CMD_RESTRT
  # These look odd, wii-mote is used on its side, steering wheel style
  if (buttons & cwiid.BTN_LEFT):
    return coasting, CMD_DOWN
  if (buttons & cwiid.BTN_RIGHT):
    return coasting, CMD_UP
  if (buttons & cwiid.BTN_UP):
    return coasting, CMD_LEFT
  if (buttons & cwiid.BTN_DOWN):
    return coasting, CMD_RIGHT
  if (buttons & cwiid.BTN_1):
    coasting = False
    return coasting, CMD_BRAKE
  if (buttons & cwiid.BTN_2):
    coasting = False
    return coasting, CMD_ACCEL
  if (buttons & cwiid.BTN_A):
    return coasting, CMD_AUTO
  if (buttons & cwiid.BTN_B):
    return coasting, CMD_CUE
  if (buttons & cwiid.BTN_HOME):
    return coasting, CMD_NXTST
  if (buttons & cwiid.BTN_MINUS):
    return coasting, CMD_PREV
  if (buttons & cwiid.BTN_PLUS):
    return coasting, CMD_NEXT
  return coasting, CMD_NONE

def handleCommand():
  millis = int(round(time.time() * 1000))
  elapsed = millis - handleCommand.lastTime
  for i in range (0, len(handleCommand.throttle)):
    if (handleCommand.throttle[i] > 0):
      handleCommand.throttle[i] = handleCommand.throttle[i] - elapsed
      if (handleCommand.throttle[i] < 0):
        handleCommand.throttle[i] = 0
  handleCommand.lastTime = millis
  coasting, command = getCommand()
  if (command == CMD_EXIT):
    dumpSettings()
    print '\nClosing connection ...'
    wii.rumble = 1
    time.sleep(0.25)
    wii.rumble = 0
    ledCtrl.resetLEDs()
    finder.stop()
    finder.join()
    exit(wii)  

  if (command == CMD_SHUTDOWN):
    dumpSettings()
    print '\nHalting ...'
    wii.rumble = 1
    time.sleep(0.5)
    wii.rumble = 0
    ledCtrl.resetLEDs()
    finder.stop()
    finder.join()
    os.system("sudo halt")
    exit(wii)  

  if (command == CMD_DOWN and handleCommand.throttle[CMD_DOWN] == 0):
    if (handleCommand.state == ST_DRIVE):
      if (handleCommand.headTilt > 0):
        handleCommand.headTilt = handleCommand.headTilt - 1
        tilt = handleCommand.headTilt
        if (abs(handleSerial.angle) < 20.0):
          tilt = handleCommand.headTilt - int(handleSerial.angle)
        if (tilt < 0):
          tilt = 0
        if (tilt > 63):
          tilt = 63
        writePWM(1, tilt)
        handleCommand.throttle[CMD_DOWN] = 25
    elif (handleCommand.state == ST_BAL):
      handleCommand.balanceSetPoint = handleCommand.balanceSetPoint - 0.1
      ser.write('b:')
      ser.write(float2hex(handleCommand.balanceSetPoint))
      ser.flush()
      print('Pitch: ' + str(handleCommand.balanceSetPoint))
      handleCommand.throttle[CMD_DOWN] = 50
    elif (handleCommand.state == ST_TUNE):
      decrementParameter(handleCommand.currentParameter)
      handleCommand.throttle[CMD_DOWN] = 250

  if (command == CMD_UP and handleCommand.throttle[CMD_UP] == 0):
    if (handleCommand.state == ST_DRIVE):
      if (handleCommand.headTilt < 63):
        handleCommand.headTilt = handleCommand.headTilt + 1
        tilt = handleCommand.headTilt
        if (abs(handleSerial.angle) < 20.0):
          tilt = handleCommand.headTilt - int(handleSerial.angle)
        if (tilt < 0):
          tilt = 0
        if (tilt > 63):
          tilt = 63
        writePWM(1, tilt)
        handleCommand.throttle[CMD_UP] = 25
    elif (handleCommand.state == ST_BAL):
      handleCommand.balanceSetPoint = handleCommand.balanceSetPoint + 0.1
      ser.write('b:')
      ser.write(float2hex(handleCommand.balanceSetPoint))
      ser.flush()
      print('Pitch: ' + str(handleCommand.balanceSetPoint))
      handleCommand.throttle[CMD_UP] = 50
    elif (handleCommand.state == ST_TUNE):
      incrementParameter(handleCommand.currentParameter)
      handleCommand.throttle[CMD_UP] = 250

  if (command == CMD_LEFT and handleCommand.throttle[CMD_LEFT] == 0):
    if (handleCommand.state == ST_DRIVE):
      if (handleCommand.headPan < 63):
        handleCommand.headPan = handleCommand.headPan + 1
        writePWM(0, handleCommand.headPan)
        handleCommand.throttle[CMD_LEFT] = 25
    elif (handleCommand.state == ST_BAL):
      handleCommand.motorOffset = handleCommand.motorOffset + 0.05
      ser.write('l:')
      ser.write(float2hex(1.0 + handleCommand.motorOffset))
      ser.write('r:')
      ser.write(float2hex(1.0 - handleCommand.motorOffset))
      ser.flush()
      print('Yaw: ' + str(handleCommand.motorOffset))
      handleCommand.throttle[CMD_LEFT] = 50
    elif (handleCommand.state == ST_TUNE):
      resetParameter(handleCommand.currentParameter)
      handleCommand.throttle[CMD_LEFT] = 250

  if (command == CMD_RIGHT and handleCommand.throttle[CMD_RIGHT] == 0):
    if (handleCommand.state == ST_DRIVE):
      if (handleCommand.headPan > 0):
        handleCommand.headPan = handleCommand.headPan - 1
        writePWM(0, handleCommand.headPan)
        handleCommand.throttle[CMD_RIGHT] = 25
    elif (handleCommand.state == ST_BAL):
      handleCommand.motorOffset = handleCommand.motorOffset - 0.05
      ser.write('l:')
      ser.write(float2hex(1.0 + handleCommand.motorOffset))
      ser.write('r:')
      ser.write(float2hex(1.0 - handleCommand.motorOffset))
      ser.flush()
      print('Yaw: ' + str(handleCommand.motorOffset))
      handleCommand.throttle[CMD_RIGHT] = 50
    elif (handleCommand.state == ST_TUNE):
      resetParameter(handleCommand.currentParameter)
      handleCommand.throttle[CMD_RIGHT] = 250

  if (command == CMD_BRAKE and handleCommand.throttle[CMD_BRAKE] == 0):
    if (handleCommand.targetWheelRate > -30.0):
      if (handleCommand.targetWheelRate > 0.1):
        handleCommand.targetWheelRate = handleCommand.targetWheelRate * 0.5
      else:
        handleCommand.targetWheelRate = handleCommand.targetWheelRate - 0.5
      ser.write('w:')
      ser.write(float2hex(handleCommand.targetWheelRate))
      ser.flush()
    handleCommand.throttle[CMD_BRAKE] = 50

  if (command == CMD_ACCEL and handleCommand.throttle[CMD_ACCEL] == 0):
    if (handleCommand.targetWheelRate < 40.0):
      handleCommand.targetWheelRate = handleCommand.targetWheelRate + 1.5
      ser.write('w:')
      ser.write(float2hex(handleCommand.targetWheelRate))
      ser.flush()
    handleCommand.throttle[CMD_ACCEL] = 50

  if (command == CMD_AUTO and handleCommand.throttle[CMD_AUTO] == 0):
    if (handleCommand.state == ST_DRIVE):
      finder.enable()
      print("Entering Auto mode")
      handleCommand.state = ST_AUTO
    elif (handleCommand.state == ST_AUTO):
      finder.disable()
      print("Leaving Auto mode")
      handleCommand.headTilt = 35
      writePWM(1, handleCommand.headTilt)
      handleCommand.headPan = 30
      writePWM(0, handleCommand.headPan)
      handleCommand.state = ST_DRIVE
    handleCommand.throttle[CMD_AUTO] = 250

  if (command == CMD_CUE and handleCommand.throttle[CMD_CUE] == 0):
    actor.cue()
    handleCommand.throttle[CMD_CUE] = 500

  if (command == CMD_RESTRT and handleCommand.throttle[CMD_CUE] == 0):
    actor.reset()
    handleCommand.throttle[CMD_CUE] = 500

  if (command == CMD_NXTST and handleCommand.throttle[CMD_NXTST] == 0):
    handleCommand.state = handleCommand.state + 1
    if (handleCommand.state == ST_BAL):
      # Turning is disabled in this state, so centre the robot.
      targetTurnRate = 121.0
      ser.write('t:')
      ser.write(float2hex(targetTurnRate - 121.0))
      ser.flush()
    if (handleCommand.state == ST_TUNE):
      handleCommand.currentParameter = 0;
      setWiiMoteLEDs(handleCommand.currentParameter)
    if (handleCommand.state == ST_TUNE + 1):
      wii.led = 0
      handleCommand.state = ST_DRIVE           
    wii.rumble = 1
    time.sleep(0.25)
    wii.rumble = 0
    handleCommand.throttle[CMD_NXTST] = 50
    
  if (command == CMD_PREV and handleCommand.throttle[CMD_PREV] == 0):
    if (handleCommand.state == ST_TUNE):
      handleCommand.currentParameter = handleCommand.currentParameter - 1
      if (handleCommand.currentParameter == -1):
        handleCommand.currentParameter = 7
      setWiiMoteLEDs(handleCommand.currentParameter)
    handleCommand.throttle[CMD_PREV] = 250
    
  if (command == CMD_NEXT and handleCommand.throttle[CMD_NEXT] == 0):
    if (handleCommand.state == ST_TUNE):
      handleCommand.currentParameter = handleCommand.currentParameter + 1
      if (handleCommand.currentParameter == 8):
        handleCommand.currentParameter = 0
      setWiiMoteLEDs(handleCommand.currentParameter)
    handleCommand.throttle[CMD_NEXT] = 250

  if (coasting and handleCommand.targetWheelRate != 0.0):
    # Decelerate
    handleCommand.targetWheelRate = handleCommand.targetWheelRate * 0.95
    if (abs(handleCommand.targetWheelRate) < 0.2):
      handleCommand.targetWheelRate = 0.0
    ser.write('w:')
    ser.write(float2hex(handleCommand.targetWheelRate))
    ser.flush()
handleCommand.state = ST_DRIVE
handleCommand.headTilt = 35
handleCommand.headPan = 30
handleCommand.balanceSetPoint = 0.0
handleCommand.motorOffset = 0.0
handleCommand.currentParameter = 0
handleCommand.targetWheelRate = 0.0
handleCommand.throttle = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
handleCommand.lastTime = 0
handleCommand.curUtterance = 0

targetTurnRate = 121.0

GZ.clock_ena(GZ.GZ_CLK_5MHz, 180)
spi = spidev.SpiDev()
spi.open(0,1)

# Centre the servos
writePWM(0, handleCommand.headPan)
writePWM(1, handleCommand.headTilt)
path = os.path.dirname(os.path.realpath(sys.argv[0]))
f = open(path + '/speech.txt')
speechList= list(f)
f = open(path + '/script.py')
ledCtrl = ledController.ledController(spi)
actor = scriptPlayer.scriptPlayer(f, ledCtrl)
finder = faceFinder.faceFinder()
finder.start()

print 'Press 1 + 2 on your Wii Remote now ...'
time.sleep(1)

# Connect to the Wii Remote. If it times out keep trying.
while True:
  try:
    wii=cwiid.Wiimote()
    break
  except RuntimeError:
    time.sleep(1)
print 'Wii Remote connected...'
print '2     - Accelerate'
print '1     - Brake/Reverse'
print 'Tilt  - Steer'
print 'Cross - Camera Pan/Tilt'
print 'A     - In Drive mode, enter/exit autonomous mode'
print 'B     - Cue next script actions'
print 'Home  - Step through modes:'
print 'Mode 0 - Drive'
print 'Mode 1 - Tune balance point and steering. (using cross control)'
print 'Mode 2 - Tune PID parameters. (using cross control)'
print 'Press PLUS and MINUS together to disconnect and quit.\n'

wii.rpt_mode = cwiid.RPT_BTN | cwiid.RPT_ACC

try:
  ser = serial.Serial('/dev/ttyAMA0', 115200, timeout=1)
  ser.open()
except RuntimeError:
  print "Error opening serial port"
  quit()

# Let the user know PiTeR is connected and ready to command
wii.rumble = 1
time.sleep(1)
wii.rumble = 0

ledCtrl.poll()
ledCtrl.newLEDAction(0, 0x25, 0)
ledCtrl.newLEDAction(0, 0x00, 250)
ledCtrl.newLEDAction(1, 0x25, 250)
ledCtrl.newLEDAction(1, 0x00, 500)

os.system("v4l2-ctl -p 4")

while True:
  ledCtrl.poll()
  handleSerial()
  handleCommand()
  if (wii.state['acc'][1] != targetTurnRate and (handleCommand.state == ST_DRIVE or handleCommand.state == ST_AUTO)):
    targetTurnRate = wii.state['acc'][1]
    ser.write('t:')
    ser.write(float2hex(targetTurnRate - 121.0))
    ser.flush()
  if (finder.dataReady == True):
    faces = finder.getFaces()
    if (len(faces) != 0):
      face = faces[random.randint(0, len(faces) - 1)]
      x = face[0] + face[2]/2
      y = face[1] + face[3]/2
      if (x > 165 or x < 155):
        handleCommand.headPan = handleCommand.headPan - int((x - 160.0) / 30.0)
        if (handleCommand.headPan > 63):
          handleCommand.headPan = 63
        if (handleCommand.headPan < 0):
          handleCommand.headPan = 0
        handleCommand.headPan = 31 - int(((x - 160)/5.0))
        writePWM(0, handleCommand.headPan)
      if (y > 125 or y < 115):
        handleCommand.headTilt = handleCommand.headTilt + int((y - 120.0) / 15.0)
        if (handleCommand.headTilt > 63):
          handleCommand.headTilt = 63
        if (handleCommand.headTilt < 0):
          handleCommand.headTilt = 0
        writePWM(1, handleCommand.headTilt)
