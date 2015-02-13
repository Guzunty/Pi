/*
 * gz_4p8o8i.c
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
#include <ncurses.h>
#include <gz_spi.h>
#include <gz_clk.h>

unsigned char dir = 1;
unsigned char reg = 2;
unsigned char values[4] = {0xf0,0xc0,0x80,0x40};
int val_dir[4] = {1,1,1,1};

void outputs_off() {
  unsigned char payload[2];
  payload[0] = 4;
  payload[1] = 0xff;
  gz_spi_write(payload);
}

void exercise_pwms() {
  unsigned char payload[2];
  int i;
  
  for(i = 0; i < 4; i++) {
    values[i] += val_dir[i];
  if (values[i] == 0xfe || values[i] == 0x01) {
      val_dir[i] = -val_dir[i];
  }
    payload[0] = i;
    payload[1] = values[i];
    gz_spi_write(payload);
  }
  usleep(10000);
}

void exercise_outputs(unsigned char a, unsigned char b) {
  unsigned char output[2];
  output[0] = 4;
  output[1] = a;
  gz_spi_write(output);          // pass output bytes to SPI
  usleep(100000);
  output[1] = b;
  gz_spi_write(output);          // pass output bytes to SPI
  usleep(100000);
}

void display_inputs() {
  int row, col;
  unsigned char inputs[2];
  getmaxyx(stdscr, row, col);   /* get the screen boundaries */
  int center_x = col / 2;
  int center_y = row / 2;
  int start_y = center_y - 2;
  int start_x = center_x - 16;
  mvprintw(start_y, center_x - 8, "Input bit status");
  start_y++;
  mvprintw(start_y, start_x + 8, "7 6 5 4 3 2 1 0");
  gz_spi_read(inputs);
  unsigned char mask = 0x01;
  int i = 0;
  for (; i < 8; i++) {
    int cur_byte = 0;
    if (i > 7) {
      cur_byte = 1;
    }
    if (inputs[cur_byte] & mask) {
      mvprintw(center_y, start_x + 22 - (2 * i), "1");
    }
    else {
      mvprintw(center_y, start_x + 22 - (2 * i), "0");
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
  gz_spi_set_width(2);              // Pass blocks of 2 bytes on SPI
  gz_clock_ena(GZ_CLK_5MHz, 0x02);  // 2.5 MHz
  erase();
  outputs_off();
  printw("Modulating PWMs.\n");
  printw("Press 'n' for next test, any other key to stop.\n");
  int key = 0;
  while(1) {
    exercise_pwms();
    key = getch();
    if (key != -1) {
      break;
    }
  }
  if (key == 'n') {
    erase();
    printw("Toggling all outputs.\n");
    printw("Press 'n' for next test, any other key to stop.\n");
    while(1) {
      exercise_outputs(0xff, 0x00);
      key = getch();
      if (key != -1) {
        break;
      }
    }
  }
  if (key == 'n') {
    erase();
    printw("Toggling alternate outputs.\n");
    printw("Press 'n' for next test, any other key to stop.\n");
    while(1) {
      exercise_outputs(0xaa, 0x55);
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
    unsigned char current = 0xfe;
    while(1) {
      exercise_outputs(current, (current << 1) | 0x01);
      current = (current << 2) | 0x03;
      if (current == 0xff) {
        current = 0xfe;
      }
      key = getch();
      if (key != -1) {
        break;
      }
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
