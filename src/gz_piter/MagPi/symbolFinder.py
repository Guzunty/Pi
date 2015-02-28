#!/usr/bin/python
#
# symbolFinder.py
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
# This class uses OpenCV to detect symbols of the specified colour in
# the image captured from the Raspberry Pi camera.
#
# The position of the detected symbol in the image is available via the
# 'getPatch()' method.

import cv2
import threading
import time
import os
import numpy as np

class SymbolFinder(threading.Thread):

  def __init__(self):
    super(SymbolFinder, self).__init__()
    self.cap = cv2.VideoCapture()
    self.active = True
    self.dataReady = False
    self.patch = None
    self.gate = threading.RLock()
    self.gate.acquire()
    self.spotFilter = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5, 5))
    self.maskMorph = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (10, 10))  
    self.low = (20, 120, 40)
    self.high = (89, 255, 160)

  def run(self):
    self.throttle = 5
    self.gate.acquire()
    while (self.active == True):
      acqTime = 0
      while acqTime < 0.01:
        now = time.time()
        ret, self.frame = self.cap.read()
        acqTime = time.time() - now
      now = time.time()
      imgHSV = cv2.cvtColor(self.frame, cv2.COLOR_BGR2HSV)
      cvtTime = time.time() - now
      now = time.time()
      mask = cv2.inRange(imgHSV, self.low, self.high)
      inRangeTime = time.time() - now
      now = time.time()
      mask = cv2.erode(mask, self.spotFilter)    # Remove spots in image
      mask = cv2.dilate(mask, self.maskMorph)    # Merge holes in image
      maskTime = time.time() - now
      # Find the contours in the mask
      now = time.time()
      contours, hierarchy = cv2.findContours(mask, 1, 2)
      contourTime = time.time() - now
      # Find the contour with the greatest area
      now = time.time()
      area = 0.0
      contour = None
      for candidateContour in contours:
        candidateArea = cv2.contourArea(candidateContour)
        if candidateArea > area:
          area = candidateArea
          contour = candidateContour
      # Save the bounding rectangle for the contour
      if len(contours) > 0:
        self.patch = cv2.boundingRect(contour)
        rectTime = time.time() - now
        self.symbolTimes = (acqTime, cvtTime, inRangeTime, maskTime, contourTime, rectTime)
        self.dataReady = True
      self.gate.release()
      time.sleep(0.01)
      self.gate.acquire()

  def enable(self):
    #os.system("v4l2-ctl --set-fmt-video=width=320,height=240,pixelformat=10")
    self.cap.open(0)
    if (self.cap.isOpened()):
      self.cap.set(cv2.cv.CV_CAP_PROP_FRAME_WIDTH, 320)
      self.cap.set(cv2.cv.CV_CAP_PROP_FRAME_HEIGHT, 240)
      #self.cap.set(cv2.cv.CV_CAP_PROP_FPS, 4)
      discardFrameCount = 0
      while discardFrameCount < 25:
        self.cap.read()
        discardFrameCount = discardFrameCount + 1 
      self.gate.release()
    else:
      print("ERROR: symbolFinder.py 99 : Failed to open camera")
      self.active = False

  def disable(self):
    self.gate.acquire()
    self.cap.release()

  def stop(self):
    self.disable()
    self.active = False
    self.gate.release()

  def getPatch(self):
    self.dataReady = False
    return self.patch, self.frame, self.symbolTimes
