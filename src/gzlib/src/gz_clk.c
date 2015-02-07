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
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <gz_gpio.h>
#include <gz_clk.h>

#define GZ_CLK_BUSY    (1 << 7)
#define GP_CLK0_CTL *(get_clock_base() + 0x1C)
#define GP_CLK0_DIV *(get_clock_base() + 0x1D)

void *clk_map;
volatile unsigned * clk;

int gz_clock_ena(int speed, int divisor) {
  int speed_id = 6;
  int mem_fd;
  if ((mem_fd = open("/dev/mem", O_RDWR|O_SYNC) ) < 0) {
     printf("\rError initializing IO. Consider using sudo.\n");
     exit(-1);
  }
  if (speed < GZ_CLK_5MHz || speed > GZ_CLK_125MHz) {
    printf("gz_clock_ena: Unsupported clock speed selected.\n");
    printf("Supported speeds: GZ_CLK_5MHz (0) and GZ_CLK_125MHz (1).\n");
    exit(-1);
  }
  if (speed == 0) {
    speed_id = 1;
  }
  if (divisor < 2) {
    printf("gz_clock_ena: Minimum divisor value is 2.\n");
    exit(-1);
  }
  if (divisor > 0xfff) {
    printf("gz_clock_ena: Maximum divisor value is %d.\n", 0xfff);
    exit(-1);
  }
  close(mem_fd); //No need to keep mem_fd open after mmap
  usleep(1000);
  INP_GPIO(4);
  SET_GPIO_ALT(4,0);
  GP_CLK0_CTL = 0x5A000000 | speed_id;    // GPCLK0 off
  while (GP_CLK0_CTL & GZ_CLK_BUSY) {}    // Wait for BUSY low
  GP_CLK0_DIV = 0x5A002000 | (divisor << 12); // set DIVI
  GP_CLK0_CTL = 0x5A000010 | speed_id;    // GPCLK0 on
  return 0;
  
}

int gz_clock_dis() {
  INP_GPIO(4);
  return 0;
}
