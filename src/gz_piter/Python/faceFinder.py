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
      #discardFrames = 10
      #while (discardFrames > 0):
      #  self.cap.read()
      #  discardFrames = discardFrames - 1
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
