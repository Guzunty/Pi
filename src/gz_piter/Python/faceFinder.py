#!/usr/bin/python
#
# faceFinder.py
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
# This class uses OpenCV to detect faces in the image captured from the
# Raspberry Pi camera.
#
# The detected faces are available via the 'getFaces()' method.

import cv2
import time
import threading
import os

class faceFinder(threading.Thread):

  def __init__(self):
    super(faceFinder, self).__init__()
    self.faces = []
    self.cap = cv2.VideoCapture()
    self.faceCascade = cv2.CascadeClassifier('lbpcascade_frontalface.xml')
    self.active = True
    self.enabled = False
    self.dataReady = False

  def run(self):
    self.throttle = 5
    while (self.active == True):
      if (self.enabled == True):
        ret, frame = self.cap.read()
        if (self.throttle == 0):
          gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
          self.faces = self.faceCascade.detectMultiScale(
            gray,
            scaleFactor=1.3,
            minNeighbors=5,
            minSize=(20, 20))
          self.throttle = 5
          self.dataReady = True
        else:
          self.throttle = self.throttle - 1
      else:
        self.faces = []
      time.sleep(0.1)

  def enable(self):
    self.cap.open(0)
    if (self.cap.isOpened()):
      os.system("v4l2-ctl -p 4")
      self.cap.set(cv2.cv.CV_CAP_PROP_FRAME_WIDTH, 320)
      self.cap.set(cv2.cv.CV_CAP_PROP_FRAME_HEIGHT, 240)
      time.sleep(0.5)
      self.enabled = True
    else:
      print("ERROR: faceDet.py 35 : Failed to open camera")
      self.active = False

  def disable(self):
    self.enabled = False
    time.sleep(0.5)
    self.cap.release()

  def stop(self):
    self.disable()
    self.active = False

  def getFaces(self):
    self.dataReady = False
    return self.faces
