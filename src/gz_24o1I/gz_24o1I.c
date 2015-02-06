/*
 * gz_24o1I.c
 * 
 * Copyright 2013  guzunty
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
  unsigned char output[3];
  output[0] = a & 0xff;
  output[1] = (a & 0xff00) >> 8;
  output[2] = (a & 0xff0000) >> 16;
  gz_spi_write(output);               // pass output bytes to SPI
  usleep(100000);
  output[0] = b & 0xff;
  output[1] = (b & 0xff00) >> 8;
  output[2] = (b & 0xff0000) >> 16;
  gz_spi_write(output);               // pass output bytes to SPI
  usleep(100000);
}

void display_input() {
  int row, col;
  getmaxyx(stdscr, row, col);   /* get the screen boundaries */
  int center_x = col / 2;
  int center_y = row / 2;
  int start_y = center_y - 2;

  mvprintw(start_y, center_x - 8, "Input bit status");
  start_y++;
  uint8_t value = GET_GPIO(PIN);

  if (value) {
    mvprintw(center_y, center_x, "1");
  }
  else {
    mvprintw(center_y, center_x, "0");
  }
}

int main(int argc, char* argv[])
{
  initscr();                          // initialize ncurses display
  nodelay(stdscr, 1);                 // don't wait for key presses
  noecho();                           // don't echo key presses
  gz_spi_set_width(3);                // Pass blocks of 3 bytes on SPI
  erase();
  printw("Toggling all outputs.\n");
  printw("Press 'n' for next test, any other key to stop.\n");
  int key = 0;
  while(1) {
    exercise_outputs(0xffffff, 0x000000);
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
      exercise_outputs(0xaaaaaa, 0x555555);
      key = getch();
      if (key != -1) {
        break;
      }
    }
  }
  if (key == 'n') {
    erase();
    printw("Walking outputs.\n");
    printw("Press 'n' for next test, any other key to stop.\n");
    uint32_t current = 0xfffffe;
    while(1) {
      exercise_outputs(current, (current << 1) | 0x01);
      current = (current << 2) | 0x03;
      if ((current & 0xffffff) == 0xffffff) {
        current = 0xfffffe;
      }
      key = getch();
      if (key != -1) {
        break;
      }
    }
  }
  if (key == 'n') {
    erase();
    curs_set(0);                      // Hide the cursor
    // Set RPI pin P1-07 to be an input
    INP_GPIO(PIN);
    //  with a pullup
    GPIO_PULL = 2;
    GPIO_PULLCLK0 = 1 << PIN;
    printw("Reading input.\n");
    printw("Press any key to stop.\n");
    while(1) {
      display_input();
      key = getch();
      if (key != -1) {
        break;
      }
    }
    move(getcury(stdscr) + 1 ,0);
    curs_set(1);
    refresh();
  }
  gz_spi_close();                     // close SPI channel
  erase();
  reset_shell_mode();                 // turn off ncurses
  return 0;
}
