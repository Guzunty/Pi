#!/usr/bin/python
#
# wii-mote.py
# Control PiTeR with a Wii-mote
#
# Author: Derek Campbell
# Date  : 30/07/2014

import cwiid
import time
import serial
import struct
import os
import GZ

def float2hex(f):
  return struct.pack('>f', f)

def hex2int(val):
  return struct.unpack('h', val)

targetWheelRate = 0.0
targetTurnRate = 121.0
balanceSetPoint = 0.0
motorOffset = 0.0
state = 0
headPan = 64
headTilt = 64

button_delay = 0.05
GZ.clock_ena(GZ.GZ_CLK_5MHz, 180)
GZ.spi_open_port('/dev/spidev0.1')
GZ.spi_set_width(1)

# Centre the servos
GZ.spi_write(headPan & 0x7f)
GZ.spi_write(0x80 | (headTilt & 0x7f))

print 'Press 1 + 2 on your Wii Remote now ...'
time.sleep(1)

# Connect to the Wii Remote. If it times out
# then quit.
try:
  wii=cwiid.Wiimote()
except RuntimeError:
  print "Error opening wiimote connection"
  quit()

print 'Wii Remote connected...\n'
print 'Press some buttons!\n'
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
        GZ.spi_write(headTilt & 0x7f)
        time.sleep(button_delay/2)
    elif (state == 1):
      balanceSetPoint = balanceSetPoint - 0.1
      ser.write('b:')
      ser.write(float2hex(balanceSetPoint))
      ser.flush()
      print('Pitch: ' + str(balanceSetPoint))
      time.sleep(button_delay)     

  if (buttons & cwiid.BTN_RIGHT):
    if (state == 0):
      if (headTilt < 127):
        headTilt = headTilt + 1
        GZ.spi_write(headTilt & 0x7f)
        time.sleep(button_delay/2)
    elif (state == 1):
      balanceSetPoint = balanceSetPoint + 0.1
      ser.write('b:')
      ser.write(float2hex(balanceSetPoint))
      ser.flush()
      print('Pitch: ' + str(balanceSetPoint))
      time.sleep(button_delay)          

  if (buttons & cwiid.BTN_UP):
    if (state == 0):
      if (headPan < 127):
        headPan = headPan + 1
        GZ.spi_write(0x80 | (headPan & 0x7f))
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
    
  if (buttons & cwiid.BTN_DOWN):
    if (state == 0):
      if (headPan > 0):
        headPan = headPan - 1
        GZ.spi_write(0x80 | (headPan & 0x7f))
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
    if (targetWheelRate < 60.0):
      targetWheelRate = targetWheelRate + 1.5
      ser.write('w:')
      ser.write(float2hex(targetWheelRate))
      ser.flush()
    time.sleep(button_delay)
          
  if (not buttons & (cwiid.BTN_2 + cwiid.BTN_1) and targetWheelRate != 0.0):
    targetWheelRate = targetWheelRate * 0.9
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
    if (state == 0):
      targetTurnRate = 121.0
      ser.write('t:')
      ser.write(float2hex(targetTurnRate - 121.0))
      ser.flush()
      state = 1
    else:
      state = 0           
    wii.rumble = 1
    time.sleep(button_delay * 5)
    wii.rumble = 0
    
  if (buttons & cwiid.BTN_MINUS):
    time.sleep(button_delay)   
    
  if (buttons & cwiid.BTN_PLUS):
    time.sleep(button_delay)

