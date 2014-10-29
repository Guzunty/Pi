#!/usr/bin/python
#
# symbolIdentifier.py
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
# This class uses OpenCV to identify a symbol in an image captured from
# the Raspberry Pi camera.
#
# The name of the detected symbol in the image is returned from the
# 'computeBestMatch()' method.

import os
import cv2
import numpy as np

class SymbolIdentifier:

  def __init__(self, symbolPath):
    self.symbols = []
    self.symbolNames = []
    self.detector = cv2.SURF(1000)
    list = os.listdir(symbolPath)
    # Build a list of symbols (keypoint and descriptor pairs for each)
    for symbolFile in list:
      self.symbols.append(self.detector.detectAndCompute(cv2.imread(symbolPath + '/' + symbolFile), None))
      self.symbolNames.append(symbolFile)
    FLANN_INDEX_KDTREE = 0
    index_params = dict(algorithm = FLANN_INDEX_KDTREE, trees = 5)
    search_params = dict(checks = 50)
    self.matcher = cv2.FlannBasedMatcher(index_params, search_params)

  def computeBestMatch(self, frame, rect):
    foundSymbolName = ""
    x, y, w, h = rect
    # Enlarge rectangle, we don't want to crop out anything useful
    x = x-(w/2)
    y = y-(h/2)
    w = w*2
    h = h*2
    # Get grayscale image by taking red channel.
    # Its faster and green will appear black as a bonus
    # IF YOU DECIDE TO PRINT THE SYMBOLS IN ANOTHER COLOUR, CHANGE THIS
    image = cv2.equalizeHist(cv2.split(frame)[2])
    # Crop the frame to the rectangle passed
    image = image[y:y+h, x:x+w]
    if (image.size <> 0):
      kp_image, des_image = self.detector.detectAndCompute(image, None)
      if not isinstance(des_image, type(None)):
        # Prevent knnMatch defect when the image
        # descriptor list contains only one point
        if (len(des_image) > 1):
          best_match = 0
          for cur in range (0, len(self.symbols)):
            matches = self.matcher.knnMatch(self.symbols[cur][1], des_image, 2)
            good_matches = []
            for match in matches:
              if (len(match) == 2 and match[0].distance < match[1].distance * 0.7):
                good_matches.append(match)
            if (len(good_matches) > 4 and len(good_matches) > best_match):
              best_match = len(good_matches)
              foundSymbolName = self.symbolNames[cur]
    return foundSymbolName
