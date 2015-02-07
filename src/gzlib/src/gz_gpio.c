/*
 * gz_gpio.c
 *
 * Copyright 2015  guzunty
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
#include <fcntl.h>
#include <sys/mman.h>
#include <gz_gpio.h>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>

char                gpio_initialized = 0;
__off_t             peri_base = BCM2708_PERI_BASE;
volatile unsigned * gpio_base;
volatile unsigned * clock_base;

void initialize_peripherals() {
  if (!gpio_initialized) {
	int proc_fd;
	int mem_fd;
	char proc_model[32];
    if ((mem_fd = open("/dev/mem", O_RDWR|O_SYNC) ) < 0) {
      printf("\rError initializing IO. Consider using sudo.\n");
      exit(-1);
    }

    if ((proc_fd = open("/proc/device-tree/model", O_RDONLY|O_SYNC) ) >= 0) {
	  // if we can read the device tree, we may be running in a more
	  // recent Raspberry Pi model which has a different peripheral
	  // memory address.
	  int i = 0;
	  int len = 0;
      len = read(proc_fd, proc_model, sizeof proc_model);
      for (i = 0; i < len; i++) {
		if (proc_model[i] == '2') {  // Yes this is a rev 2 Pi ...
			peri_base = BCM2709_PERI_BASE;
			break;
	    }
      }
    }
    /* memory map the GPIO */
    void *gpio_map = mmap(
      NULL,             //Any address in our space will do
      MAP_BLOCK_SIZE,       //Map length
      PROT_READ|PROT_WRITE,// Enable reading & writting to mapped memory
      MAP_SHARED,       //Shared with other processes
      mem_fd,           //File to map
      GPIO_BASE         //Offset to GPIO peripheral
    );

    void *clock_map = mmap(
      NULL,             //Any address in our space will do
      MAP_BLOCK_SIZE,       //Map length
      PROT_READ|PROT_WRITE,// Enable reading & writting to mapped memory
      MAP_SHARED,       //Shared with other processes
      mem_fd,           //File to map
      CLOCK_BASE         //Offset to hardware clock peripheral
    );

    close(mem_fd); //No need to keep mem_fd open after mmap
    if (gpio_map == MAP_FAILED) {
      printf("error mapping gpio%d\n", (int)gpio_map);//errno also set!
      exit(-1);
    }
    if (clock_map == MAP_FAILED) {
      printf("error mapping clocks%d\n", (int)clock_map);//errno also set!
      exit(-1);
    }
    gpio_base = (volatile unsigned *)gpio_map;
    clock_base = (volatile unsigned *)clock_map;
    gpio_initialized = 1;
  }
}

__off_t get_peripheral_base() {
  if (!gpio_initialized) {
    initialize_peripherals();
  }
  return peri_base;
}

volatile unsigned * get_gpio_base() {
  if (!gpio_initialized) {
    initialize_peripherals();
  }
  return gpio_base;
}

volatile unsigned * get_clock_base() {
  if (!gpio_initialized) {
    initialize_peripherals();
  }
  return clock_base;
}
