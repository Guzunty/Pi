/*
 * gz_25o.c
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
 * This program demonstrates and tests the capabilities of the
 * gz_16i8o core. 16 inputs, 8 outputs.
 *
 */
#include <unistd.h>
#include <time.h>
#include <stdint.h>
#include <ncurses.h>
#include <gz_gpio.h>
#include <gz_spi.h>
#include <stdio.h>

// Input on RPi pin GPIO 04 (Pin 7)
#define PIN 4

void exercise_outputs(uint32_t a, uint32_t b) {
  unsigned char output[4];
  output[0] = a & 0xff;
  output[1] = (a & 0xff00) >> 8;
  output[2] = (a & 0xff0000) >> 16;
  output[3] = (a & 0x1000000) >> 24;
  gz_spi_write(output);               // pass output bytes to SPI
  usleep(100000);
  output[0] = b & 0xff;
  output[1] = (b & 0xff00) >> 8;
  output[2] = (b & 0xff0000) >> 16;
  output[3] = (b & 0x1000000) >> 24;
  gz_spi_write(output);               // pass output bytes to SPI
  usleep(100000);
}

int main(int argc, char* argv[])
{
  initscr();                          // initialize ncurses display
  nodelay(stdscr, 1);                 // don't wait for key presses
  noecho();                           // don't echo key presses
  gz_spi_set_width(4);                // Pass blocks of 4 bytes on SPI
  erase();
  printw("Toggling all outputs.\n");
  printw("Press 'n' for next test, any other key to stop.\n");
  int key = 0;
  while(1) {
    exercise_outputs(0x1ffffff, 0x0000000);
    key = getch();
    if (key != -1) {
      break;
    }
  }
  if (key == 'n') {
    erase();
    printw("Toggling alternate outputs.\n");
    printw("Press 'n' for next test, any other key to stop.\n");
    while(1) {
      exercise_outputs(0x0aaaaaa, 0x1555555);
      key = getch();
      if (key != -1) {
        break;
      }
    }
  }
  if (key == 'n') {
    erase();
    printw("Walking outputs.\n");
    printw("Press any key to stop.\n");
    uint32_t current = 0x1fffffe;
    while(1) {
      exercise_outputs(current, (current << 1) | 0x01);
      current = (current << 2) | 0x03;
      if ((current & 0x1ffffff) == 0x1ffffff) {
        current = 0x1fffffe;
      }
      key = getch();
      if (key != -1) {
        break;
      }
    }
  }
  gz_spi_close();                     // close SPI channel
  erase();
  reset_shell_mode();                 // turn off ncurses
  return 0;
}
