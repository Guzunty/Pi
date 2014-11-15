import cv2
import sys, getopt
import numpy as np

obj = cv2.imread('./symbols/home_85x120w.png')

detector = cv2.SURF(1000)
kp_object, des_object = detector.detectAndCompute(obj, None)

FLANN_INDEX_KDTREE = 0
index_params = dict(algorithm = FLANN_INDEX_KDTREE, trees = 5)
search_params = dict(checks = 50)
matcher = cv2.FlannBasedMatcher(index_params, search_params)

cap = cv2.VideoCapture(-1)

if(not cap.isOpened()):
  print("Cannot open camera")
else:
  cap.set(cv2.cv.CV_CAP_PROP_FRAME_WIDTH, 320)
  cap.set(cv2.cv.CV_CAP_PROP_FRAME_HEIGHT, 240)
  
  spotFilter = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5, 5))
  maskMorph = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (10, 10))  

  lowH = 20
  highH = 69

  lowS = 120
  highS = 255

  lowV = 50
  highV = 185

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
        
      mask = cv2.inRange(imgHSV, np.array([lowH, lowS, lowV]), np.array([highH, highS, highV]))

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

      # Double size of rectangle
      x = x-(w/2)
      y = y-(h/2)
      w = w*2
      h = h*2

      # Get grayscale image by taking red channel.
      # Its faster and green will appear black as a bonus
      image = cv2.equalizeHist(cv2.split(frame)[2])

      # Crop the frame to the rectangle found from the mask
      image = image[y:y+h, x:x+w]
      if image.size <> 0:
        kp_image, des_image = detector.detectAndCompute(image, None)
      
        if not isinstance(des_image, type(None)):
          # Prevent knnMatch defect when the image
          # descriptor list contains only one point
          if len(des_image) > 1:
            matches = matcher.knnMatch(des_object, des_image, 2)
      
            good_matches = []
            for match in matches:
              if len(match) == 2 and match[0].distance < match[1].distance * 0.7:
                good_matches.append(match)

            if len(good_matches) > 4:
              src_pts = np.float32([ kp_object[m[0].queryIdx].pt for m in good_matches ])
              dst_pts = np.float32([ kp_image[m[0].trainIdx].pt for m in good_matches ])

              M, mask2 = cv2.findHomography(src_pts, dst_pts, cv2.RANSAC)

              obj_h,obj_w,obj_dep = obj.shape
              pts = np.float32([ [0,0],[0,obj_h-1],[obj_w-1,obj_h-1],[obj_w-1,0] ]).reshape(-1,1,2)
              dst = cv2.perspectiveTransform(pts,M)
              offset = np.float32([ [x,y],[x,y],[x,y],[x,y] ]).reshape(-1,1,2)
              dst = dst + offset
              #frame = cv2.bitwise_and(frame, frame, mask = mask)
              cv2.polylines(frame, [np.int32(dst)], True, 255, 3)
      cv2.rectangle(frame, (x,y), (x+w, y+h), (0,255,0), 2)
      cv2.imshow("Camera View", frame)
    if(cv2.waitKey(1) == 27):
      break
  cap.release()
cv2.destroyAllWindows()

