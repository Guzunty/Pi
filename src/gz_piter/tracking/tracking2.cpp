/*
 * tracking.cpp
 * 
 * Copyright 2014  <pi@raspberrypi>
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 * 
 * Based on example code provided by the OpenCV community.
 * 
 * http://docs.opencv.org/doc/tutorials/features2d/feature_homography
 * 
 * Thank you for all you do.
 */
 
#include <stdio.h>
#include <iostream>
#include "opencv2/core/core.hpp"
#include "opencv2/features2d/features2d.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/calib3d/calib3d.hpp"
#include "opencv2/nonfree/nonfree.hpp"
#include "opencv2/legacy/legacy.hpp"

using namespace cv;

void readme();

/** @function main */
int main( int argc, char** argv )
{
  if( argc != 3 )
  { readme(); return -1; }

  Mat img1 = imread( argv[1], CV_LOAD_IMAGE_GRAYSCALE );
  Mat img2 = imread( argv[2], CV_LOAD_IMAGE_GRAYSCALE );

  if( !img1.data || !img2.data )
  { std::cout<< " --(!) Error reading images " << std::endl; return -1; }

  imshow( "Raw target", img2 );
  waitKey(0);

  // detecting keypoints
  FastFeatureDetector detector(50);
  vector<KeyPoint> keypoints1;
  detector.detect(img1, keypoints1);

  vector<KeyPoint> keypoints2;
  detector.detect(img2, keypoints2);

  // computing descriptors
  SurfDescriptorExtractor extractor;

  Mat descriptors1;
  extractor.compute(img1, keypoints1, descriptors1);

  Mat descriptors2;
  extractor.compute(img2, keypoints2, descriptors2);

  // matching descriptors
  //BruteForceMatcher<L2<float> > matcher;
  FlannBasedMatcher matcher;
  vector<DMatch> matches;
  matcher.match(descriptors1, descriptors2, matches);

  double max_dist = 0; double min_dist = 100;

  //-- Quick calculation of max and min distances between keypoints
  for( int i = 0; i < descriptors1.rows; i++ )
  { double dist = matches[i].distance;
    if( dist < min_dist ) min_dist = dist;
    if( dist > max_dist ) max_dist = dist;
  }

  printf("-- Max dist : %f \n", max_dist );
  printf("-- Min dist : %f \n", min_dist );

  //-- Draw only "good" matches (i.e. whose distance is less than 3*min_dist )
  std::vector< DMatch > good_matches;

  for( int i = 0; i < descriptors1.rows; i++ )
  { if( matches[i].distance <= 3*min_dist )
     { good_matches.push_back( matches[i]); }
  }

  // drawing the results
  namedWindow("matches", 1);
  Mat img_matches;
  drawMatches(img1, keypoints1, img2, keypoints2, good_matches, img_matches);

  imshow( "Matches", img_matches );
  waitKey(0);

  // fill the arrays with the points
  vector<Point2f> points1, points2;

  for( int i = 0; i < good_matches.size(); i++ )
  {
    //-- Get the keypoints from the good matches
    points1.push_back( keypoints1[ good_matches[i].queryIdx ].pt );
    points2.push_back( keypoints2[ good_matches[i].trainIdx ].pt );
  }

  Mat H = findHomography(Mat(points1), Mat(points2), CV_RANSAC);

  std::cout << "Homography complete." << std::endl;

  Mat points1Projected;  
  perspectiveTransform(Mat(points1), points1Projected, H);

  std::cout << "Transform complete." << std::endl;

  Mat proj_matches;
  //drawMatches(img1, keypoints1, img2, points1Projected, good_matches, proj_matches);
  drawMatches(img1, Mat(points1), img2, points1Projected, good_matches, proj_matches);


  //-- Show detected matches
  imshow( "Projected Matches", proj_matches );

  waitKey(0);
  return 0;
  }

  /** @function readme */
  void readme()
  { std::cout << " Usage: ./SURF_descriptor <img1> <img2>" << std::endl; }
