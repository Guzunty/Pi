/*
 * gz_8p8i.c
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
 * This program demonstrates the 8 way 5 bit pwm plus eight input
 * Guzunty core.
 * 
 */
#include <unistd.h>
#include <time.h>
#include <ncurses.h>
#include <gz_spi.h>
#include <gz_clk.h>

unsigned char reg = 4;
char dir = 1;
unsigned char values[8] = {0,0,0,0,0,0,0,0};

void exercise_pwms() {
  unsigned char payload[2];

  reg += dir;
  if (reg == 7 || reg == 0) {
    dir = -dir;
  }
  values[reg] = 0x1f;
  int i;
  for(i = 0; i < 8; i++) {
    payload[0] = i;
    payload[1] = 0x1f - values[i];
    if (values[i] > 0) {
      values[i] -= 2;
    }
    else {
      values[i] = 0;
    }
    gz_spi_write(payload);
  }
  usleep(100000);
}

void display_inputs() {
  int row, col;
  unsigned char inputs[2];
  getmaxyx(stdscr, row, col);   /* get the screen boundaries */
  int center_x = col / 2;
  int center_y = row / 2;
  int start_y = center_y - 2;
  int start_x = center_x - 8;
  mvprintw(start_y, center_x - 8, "Input bit status");
  start_y++;
  mvprintw(start_y, start_x, "7 6 5 4 3 2 1 0");
  gz_spi_read(inputs);
  unsigned char mask = 0x01;
  int i = 0;
  for (; i < 8; i++) {
    int cur_byte = 0;
    if (i > 7) {
      cur_byte = 1;
    }
    if (inputs[cur_byte] & mask) {
      mvprintw(center_y, start_x + 14 - (2 * i), "1");
    }
    else {
      mvprintw(center_y, start_x + 14 - (2 * i), "0");
    }
    mask = mask << 1;
    if (i == 7) {
      mask = 0x01;
    }
  }
}

int main(int argc, char* argv[])
{
  initscr();                        // initialize ncurses display
  nodelay(stdscr, 1);               // don't wait for key presses
  noecho();                         // don't echo key presses
  erase();
  gz_spi_set_width(2);              // Pass blocks of 2 bytes on SPI
  gz_clock_ena(GZ_CLK_5MHz, 0x02);  // 2.5 Mhz

  int key = 0;
  printw("Modulating PWMs.\n");
  printw("Press 'n' for next test, any other key to stop.\n");
  while (1) {
    exercise_pwms();
    key = getch();
    if (key != -1) {
      break;
    }
  }
  if (key == 'n') {
    erase();
    curs_set(0);                     // Hide the cursor
    printw("Reading inputs.\n");
    printw("Press any key to stop.\n");
    while(1) {
      display_inputs();
      key = getch();
      if (key != -1) {
        break;
      }
    }
    move(getcury(stdscr) + 1 ,0);
    curs_set(1);
    refresh();
  }
  gz_spi_close();                   // close SPI channel
  erase();
  reset_shell_mode();               // turn off ncurses
  return 0;
}
