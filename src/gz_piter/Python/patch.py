import cv2

cap = cv2.VideoCapture(-1)

if(not cap.isOpened()):
  print("Cannot open camera")
else:
  cap.set(cv2.cv.CV_CAP_PROP_FRAME_WIDTH, 320)
  cap.set(cv2.cv.CV_CAP_PROP_FRAME_HEIGHT, 240)
  
  spotFilter = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5, 5))
  maskMorph = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (10, 10))  

  lowH = 20
  highH = 59;

  lowS = 150
  highS = 255

  lowV = 0
  highV = 120

  while(1):
    frameCount = 0
    while frameCount < 5:
      cap.read()
      frameCount = frameCount + 1
    success, frame = cap.read()
    if (not success):
      print("Cannot read a frame from the camera")
    else:
      imgHSV = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
        
      mask = cv2.inRange(imgHSV, (lowH, lowS, lowV), (highH, highS, highV))

      # Remove spots in image        
      mask = cv2.erode(mask, spotFilter)
      # Create mask
      mask = cv2.dilate(mask, maskMorph)
      
      # Find the contours in the mask
      contours, hierarchy = cv2.findContours(mask, 1, 2)

      # Find the contour with the greatest area
      area = 0.0
      contour = None
      for candidateContour in contours:
        candidateArea = cv2.contourArea(candidateContour)
        if candidateArea > area:
          area = candidateArea
          contour = candidateContour

      # Get the bounding rectangle for the contour
      x = y = w = h = 0
      if len(contours) > 0:
        x, y, w, h = cv2.boundingRect(contour)

      cv2.rectangle(frame, (x,y), (x+w, y+h), (0,255,0), 2)
      cv2.imshow("Patch area", frame)
    if(cv2.waitKey(1) == 27):
      break
  cap.release()
cv2.destroyAllWindows()

