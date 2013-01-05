/*
 * gz_clk.c
 * 
 * Copyright 2012  campbellsan
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
 */
#include<gz_clk.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#define GZ_CLK_BUSY (1 << 7)

int gz_clock_ena(int speed, int divisor) {
  int speed_id = 6;
  if (speed < GZ_CLK_5MHz || speed > GZ_CLK_125MHz) {
    printf("gz_clock_ena: Unsupported clock speed selected.\n");
    printf("Supported speeds: GZ_CLK_5MHz (0) and GZ_CLK_125MHz (1).");
    exit(-1);
  }
  if (speed == 0) {
    speed_id = 1;
  }
  if (divisor < 2) {
    printf("gz_clock_ena: Minimum divisor value is 2.");
    exit(-1);
  }
  if (divisor > 0xfff) {
    printf("gz_clock_ena: Maximum divisor value is %d.", 0xfff);
    exit(-1);
  }
  if (bcm2835_init() !=1) {
    printf("gz_clock_ena: Failed to initialize I/O\n");
    exit(-1);
  }
  usleep(5);
  bcm2835_gpio_fsel(RPI_GPIO_P1_07, BCM2835_GPIO_FSEL_ALT0);
  *(bcm2835_clk + 0x1C) = 0x5A000000 | speed_id;    // GPCLK0 off
  while (*(bcm2835_clk + 0x1C) & GZ_CLK_BUSY) {}    // Wait for BUSY low
  *(bcm2835_clk + 0x1D) = 0x5A002000 | (divisor << 12); // set DIVI
  *(bcm2835_clk + 0x1C) = 0x5A000010 | speed_id;    // GPCLK0 on
  return 0;
  
}

int gz_clock_dis() {
  if (bcm2835_init() !=1) {
    printf("gz_clock_dis: Failed to initialize I/O\n");
    exit(-1);
  }
  bcm2835_gpio_fsel(RPI_GPIO_P1_07, BCM2835_GPIO_FSEL_INPT);
  return 0;
}
