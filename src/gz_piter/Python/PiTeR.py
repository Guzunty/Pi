#!/usr/bin/python
#
# PiTeR.py
# Control PiTeR with a Wii-mote
#
# Author: Derek Campbell
# Date  : 30/07/2014

import cwiid
import serial
import time
import struct
import os
import GZ
import copy

def float2hex(f):
  return struct.pack('>f', f)

def hex2int(val):
  return struct.unpack('h', val)

def setLEDs(value):
  ledValue = 1
  if (value < 4):
    ledValue = 1 << value
  else:
    ledValue = 15 - (1 << (value - 4))
  wii.led = ledValue

parameters = ['k', 'p','i','d','K', 'P','I','D']
parmDefault = [0.5, 6.6, 1.9, 5.5, 0.45, 4.5, 3.0, 3.0] # Must match default values in arduino.
parmValue = copy.deepcopy(parmDefault)

def decrementParameter():
  parmValue[currentParameter] = parmValue[currentParameter] - 0.01
  writeParameter()
  displayParameter()

def incrementParameter():
  parmValue[currentParameter] = parmValue[currentParameter] + 0.01
  writeParameter()
  displayParameter()

def resetParameter():
  parmValue[currentParameter] = parmDefault[currentParameter]
  writeParameter()
  displayParameter()

def writeParameter():
  ser.write(parameters[currentParameter] + ':')
  ser.write(float2hex(parmValue[currentParameter]))
  ser.flush()

def displayParameter():
  output = parameters[currentParameter] + ": " + str(parmValue[currentParameter])
  print output

def writePWM(addr, value):
  value = ((addr & 0x07) << 7) | (value & 0x7f)
  GZ.spi_write(value)

targetWheelRate = 0.0
targetTurnRate = 121.0
balanceSetPoint = 0.0
motorOffset = 0.0
state = 0
currentParameter = 0

headPan = 64
headTilt = 64

button_delay = 0.05
GZ.clock_ena(GZ.GZ_CLK_5MHz, 180)
GZ.spi_open_port('/dev/spidev0.1')
GZ.spi_set_width(1)

# Centre the servos
writePWM(0, headPan)
writePWM(1, headTilt)

print 'Press 1 + 2 on your Wii Remote now ...'
time.sleep(1)

# Connect to the Wii Remote. If it times out
# then quit.
try:
  wii=cwiid.Wiimote()
except RuntimeError:
  print "Error opening wiimote connection"
  quit()

print 'Wii Remote connected...'
print '2     - Accelerate'
print '1     - Brake/Reverse'
print 'Tilt  - Steer'
print 'Cross - Camera Pan/Tilt'
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

while True:
  if (ser.inWaiting() >=4):
    cmd = ser.read(2)
    if (cmd == 'v:'):
      voltage = hex2int(ser.read(2))
      print ('Voltage: ' + str(voltage[0]))
      if (voltage <= 760):
        print("Low voltage: Halting.")
        os.system("sudo halt")
        quit()
    elif (cmd == 'r:'):
      wheelRate = hex2int(ser.read(2))
      print( 'Wheel Rate: ' + str(wheelRate[0]))
    elif (cmd == 'd:'):
      distance = hex2int(ser.read(2))
      print ('Distance: ' + str(distance[0]))
    else:
      ser.flushInput()

  buttons = wii.state['buttons']

  # If Plus and Minus buttons pressed
  # together then rumble and quit.
  if (buttons - cwiid.BTN_PLUS - cwiid.BTN_MINUS == 0):  
    print '\nClosing connection ...'
    wii.rumble = 1
    time.sleep(0.25)
    wii.rumble = 0
    exit(wii)  
  
  if (wii.state['acc'][1] != targetTurnRate and state == 0):
    targetTurnRate = wii.state['acc'][1]
    ser.write('t:')
    ser.write(float2hex(targetTurnRate - 121.0))
    ser.flush()
  # Check if other buttons are pressed by
  # doing a bitwise AND of the buttons number
  # and the predefined constant for that button.
  if (buttons & cwiid.BTN_LEFT):
    if (state == 0):
      if (headTilt > 0):
        headTilt = headTilt - 1
        writePWM(1, headTilt)
        time.sleep(button_delay/2)
    elif (state == 1):
      balanceSetPoint = balanceSetPoint - 0.1
      ser.write('b:')
      ser.write(float2hex(balanceSetPoint))
      ser.flush()
      print('Pitch: ' + str(balanceSetPoint))
      time.sleep(button_delay)
    elif (state == 2):
      decrementParameter()
      time.sleep(button_delay * 5)
  if (buttons & cwiid.BTN_RIGHT):
    if (state == 0):
      if (headTilt < 127):
        headTilt = headTilt + 1
        writePWM(1, headTilt)
        time.sleep(button_delay/2)
    elif (state == 1):
      balanceSetPoint = balanceSetPoint + 0.1
      ser.write('b:')
      ser.write(float2hex(balanceSetPoint))
      ser.flush()
      print('Pitch: ' + str(balanceSetPoint))
      time.sleep(button_delay)          
    elif (state == 2):
      incrementParameter()
      time.sleep(button_delay * 5)
  if (buttons & cwiid.BTN_UP):
    if (state == 0):
      if (headPan < 127):
        headPan = headPan + 1
        writePWM(0, headPan)
        time.sleep(button_delay/2)
    elif (state == 1):
      motorOffset = motorOffset + 0.05
      ser.write('l:')
      ser.write(float2hex(1.0 + motorOffset))
      ser.write('r:')
      ser.write(float2hex(1.0 - motorOffset))
      ser.flush()
      print('Yaw: ' + str(motorOffset))
      time.sleep(button_delay)
    elif (state == 2):
      resetParameter()
      time.sleep(button_delay * 5)          

  if (buttons & cwiid.BTN_DOWN):
    if (state == 0):
      if (headPan > 0):
        headPan = headPan - 1
        writePWM(0, headPan)
        time.sleep(button_delay/2)
    elif (state == 1):
      motorOffset = motorOffset - 0.05
      ser.write('l:')
      ser.write(float2hex(1.0 + motorOffset))
      ser.write('r:')
      ser.write(float2hex(1.0 - motorOffset))
      ser.flush()
      print('Yaw: ' + str(motorOffset))
      time.sleep(button_delay)  
    elif (state == 2):
      resetParameter()
      time.sleep(button_delay * 5)          
  if (buttons & cwiid.BTN_1):
    if (targetWheelRate > -30.0):
      if (targetWheelRate > 0.1):
        targetWheelRate = targetWheelRate * 0.5
      else:
        targetWheelRate = targetWheelRate - 0.5
      ser.write('w:')
      ser.write(float2hex(targetWheelRate))
      ser.flush()
      print(targetWheelRate)
    time.sleep(button_delay)

  if (buttons & cwiid.BTN_2):
    if (targetWheelRate < 40.0):
      targetWheelRate = targetWheelRate + 1.5
      ser.write('w:')
      ser.write(float2hex(targetWheelRate))
      ser.flush()
    time.sleep(button_delay)
          
  if (not buttons & (cwiid.BTN_2 + cwiid.BTN_1) and targetWheelRate != 0.0):
    # Decelerate
    targetWheelRate = targetWheelRate * 0.95
    if (abs(targetWheelRate) < 0.2):
      targetWheelRate = 0.0
    ser.write('w:')
    ser.write(float2hex(targetWheelRate))
    ser.flush()

  if (buttons & cwiid.BTN_A):
    time.sleep(button_delay)          

  if (buttons & cwiid.BTN_B):
    time.sleep(button_delay)          

  if (buttons & cwiid.BTN_HOME):
    state = state + 1
    if (state == 1):
      # Turning is disabled in this state, so centre the robot.
      targetTurnRate = 121.0
      ser.write('t:')
      ser.write(float2hex(targetTurnRate - 121.0))
      ser.flush()
    if (state == 2):
      currentParameter = 0;
      setLEDs(currentParameter)
    if (state == 3):
      wii.led = 0
      state = 0           
    wii.rumble = 1
    time.sleep(button_delay * 5)
    wii.rumble = 0
    
  if (buttons & cwiid.BTN_MINUS):
    if (state == 2):
      currentParameter = currentParameter - 1
      if (currentParameter == -1):
        currentParameter = 7
      setLEDs(currentParameter)
    time.sleep(button_delay * 5)   
    
  if (buttons & cwiid.BTN_PLUS):
    if (state == 2):
      currentParameter = currentParameter + 1
      if (currentParameter == 8):
        currentParameter = 0
      setLEDs(currentParameter)
    time.sleep(button_delay * 5)

