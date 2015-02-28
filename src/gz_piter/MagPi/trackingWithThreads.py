import cv2
import sys, getopt
import numpy as np
import time
import symbolFinder

accumulator = [0.0 for j in range(20)]
count = 0.0
def avg(values):
  global count
  avgs = [0.0 for j in range(0, len(values))]
  count = count + 1
  for x in range(0, len(values)):
    accumulator[x] = accumulator[x] + values[x]
    avgs[x] = accumulator[x] / count
  return tuple(avgs)

maximums = [0.0 for j in range(20)]
def maximum(values):
  for x in range(0, len(values)):
    if maximums[x] < values[x]:
      maximums[x] = values[x]
  return tuple(maximums[0:len(values)])

minimums = [10000.0 for j in range(20)]
def minimum(values):
  for x in range(0, len(values)):
    if minimums[x] > values[x]:
      minimums[x] = values[x]
  return tuple(minimums[0:len(values)])

obj = cv2.imread('./symbols/turn_right_85x120w.png')

detector = cv2.SURF(1000)
kp_object, des_object = detector.detectAndCompute(obj, None)

FLANN_INDEX_KDTREE = 0
index_params = dict(algorithm = FLANN_INDEX_KDTREE, trees = 5)
search_params = dict(checks = 50)
matcher = cv2.FlannBasedMatcher(index_params, search_params)

symFinder = symbolFinder.SymbolFinder()
symFinder.start()
symFinder.enable()

if(not symFinder.active):
  print("Cannot start Symbol Finder")
else:
  now=time.time()
  start = now
  while(1):
    if symFinder.dataReady == True:
      now = time.time()
      cycleTime = now
      patch, frame, symTimes = symFinder.getPatch()
      x, y, w, h = patch
      # Double size of rectangle
      x = x-(w/2)
      y = y-(h/2)
      w = w * 2
      h = h * 2
      
      if x < 0:
        x = 0
      if y < 0:
        y = 0
      # Crop the frame to the rectangle found from the mask
      now = time.time()
      cpy = frame[y:y+h, x:x+w]
      cropTime = time.time() - now
      histTime = detectTime = matchTime = dispTime = decorateTime = 0
      if cpy.size <> 0:
        # Get grayscale image by taking red channel.
        # Its faster and green will appear black as a bonus
        now=time.time()
        image = cv2.equalizeHist(cv2.split(cpy)[2])
        histTime = time.time() - now
        now=time.time()
        kp_image, des_image = detector.detectAndCompute(image, None)
        detectTime = time.time() - now
        if not isinstance(des_image, type(None)):
          # Prevent knnMatch defect when the image
          # descriptor list contains only one point
          if len(des_image) > 1:
            now=time.time()
            matches = matcher.knnMatch(des_object, des_image, 2)
      
            good_matches = []
            for match in matches:
              if len(match) == 2 and match[0].distance < match[1].distance * 0.7:
                good_matches.append(match)
            matchTime = time.time() - now
            
            if len(good_matches) > 4:
              src_pts = np.float32([ kp_object[m[0].queryIdx].pt for m in good_matches ])
              dst_pts = np.float32([ kp_image[m[0].trainIdx].pt for m in good_matches ])

              now=time.time()
              M, mask2 = cv2.findHomography(src_pts, dst_pts, cv2.RANSAC)

              obj_h,obj_w,obj_dep = obj.shape
              pts = np.float32([ [0,0],[0,obj_h-1],[obj_w-1,obj_h-1],[obj_w-1,0] ]).reshape(-1,1,2)
              dst = cv2.perspectiveTransform(pts,M)
              offset = np.float32([ [x,y],[x,y],[x,y],[x,y] ]).reshape(-1,1,2)
              dst = dst + offset
              #frame = cv2.bitwise_and(frame, frame, mask = mask)
              cv2.polylines(frame, [np.int32(dst)], True, 255, 3)
              decorateTime = time.time() - now
      cv2.rectangle(frame, (x,y), (x+w, y+h), (0,255,0), 2)
      now=time.time()
      cv2.imshow("Camera View", frame)
      dispTime = time.time() - now
      now = time.time()
      times = symTimes + (cropTime, histTime, detectTime, matchTime, decorateTime, dispTime, now - cycleTime, now - start)
      avgs = avg(times)
      print "AVG -> AQU: %f, CVT: %f, IRG: %f, MSK: %f, CTR: %f, RCT: %f, CRP: %f, HST: %f, DCT: %f, MCH %f, DEC: %f, DSP: %f, TTL: %f, SPF: %f" % avgs
      maxs = maximum(times)
      print "MAX -> AQU: %f, CVT: %f, IRG: %f, MSK: %f, CTR: %f, RCT: %f, CRP: %f, HST: %f, DCT: %f, MCH %f, DEC: %f, DSP: %f, TTL: %f, SPF: %f" % maxs
      mins = minimum(times)
      print "MIN -> AQU: %f, CVT: %f, IRG: %f, MSK: %f, CTR: %f, RCT: %f, CRP: %f, HST: %f, DCT: %f, MCH %f, DEC: %f, DSP: %f, TTL: %f, SPF: %f" % mins
      start = now
    if(cv2.waitKey(1) == 27):
      break
  symFinder.stop()
cv2.destroyAllWindows()

