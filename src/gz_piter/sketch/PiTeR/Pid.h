#ifndef _pid_h_
#define _pid_h_
/*
 * Pid.h
 *
 * Pi Terrestrial Robot - Proportional Integral Derivative algorithm
 * 
 * Copyright 2014  guzunty
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
 * The class definition in this file encapsulates the PID algorithm for
 * the PiTeR robot firmware. There are two instantiations of this class,
 * one takes as input the desired balance angle and outputs the motor
 * drive. The second takes the desired wheel speed and outputs the desired
 * balance angle. Hence, the two instantiations form a control cascade.
 * 
 */

#include<Arduino.h>

#define   GUARD_GAIN   20.0

class Pid {
  private:
    float   K ;
    float   Kp;   
    float   Ki;
    float   Kd;
    float   error;
    float   last_position;
    float   integrated_error;
    float   pTerm, iTerm, dTerm;
    boolean nonlinear = false;
  public:
    Pid(float K, float Kp, float Ki, float Kd, boolean nl): K(K), Kp(Kp), Ki(Ki), Kd(Kd), nonlinear(nl) {
      error = 0.0;
      last_position = 0.0;
      integrated_error = 0.0;
      pTerm = 0.0; iTerm = 0.0; dTerm = 0.0;
    };
    
    float updatePid(float targetPosition, float currentPosition, unsigned long dt)   {
      float rdt = (float)dt / 1000000.0;
      error = targetPosition - currentPosition;
      pTerm = Kp * error;
      integrated_error += error * rdt * 100;
      
      iTerm = Ki * constrain(integrated_error, -GUARD_GAIN, GUARD_GAIN);
      integrated_error *= 0.9; // Decay integrated error
      integrated_error = constrain(integrated_error, -100.0, 100.0);
      dTerm = (- Kd * (currentPosition - last_position) / (rdt  * 100));                            
      last_position = currentPosition;
      float result = K*(pTerm + iTerm + dTerm);
      if (nonlinear) {
        float rads = abs(((float)error/512.0));
        if (rads > 0.5) {
          rads = 0.5;
        }
        result *= 0.5 + (1.0 - cos(rads * PI));
      }
      return -constrain(result, -255, 255);
    };

    float getK() {
      return K;
    }

    float getKp() {
      return Kp;
    }

    float getKi() {
      return Ki;
    }

    float getKd() {
      return Kd;
    }

    float getPTerm() {
      return pTerm;
    }

    float getITerm() {
      return iTerm;
    }

    float getDTerm() {
      return dTerm;
    }

    float getError() {
      return error;
    }
    
    float getIntegratedError() {
      return integrated_error;
    }

    void setK(float K) {
      this->K = K;
    }

    void setKp(float Kp) {
      this->Kp = Kp;
    }

    void setKi(float Ki) {
      this->Ki = Ki;
    }

    void setKd(float Kd) {
      this->Kd = Kd;
    }
    
    void resetIntegratedError() {
      integrated_error = 0.0;
    }
    
    void resetDerivativeError(float newLastInput) {
      last_position = newLastInput;
    }
};
#endif

