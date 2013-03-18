#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#
# gz_16i8o.py
# 
# Copyright 2013  guzunty
# 
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
# MA 02110-1301, USA.
# 
# This program demonstrates and tests the capabilities of the
# gz_16i8o core. 16 inputs, 8 outputs.
#
#

import curses
import GZ
import time

def exercise_outputs(a, b):
  GZ.spi_write(a & 0xffff)            # pass output bytes to SPI
  time.sleep(0.1)
  GZ.spi_write(b & 0xffff)            # pass output bytes to SPI
  time.sleep(0.1)

def display_inputs():
  size = stdscr.getmaxyx()            # get the screen boundaries
  center_x = size[1] / 2
  center_y = size[0] / 2
  start_y = center_y - 2
  start_x = center_x - 16
  stdscr.addstr(start_y, center_x - 8, "Input bit status")
  start_y += 1
  stdscr.addstr(start_y, start_x, "F E D C B A 9 8 7 6 5 4 3 2 1 0")
  inputs = GZ.spi_read()
  mask = 0x01
  for i in range(16):
    if (inputs & mask):
      stdscr.addstr(center_y, start_x + 30 - (2 * i), "1")
    else:
      stdscr.addstr(center_y, start_x + 30 - (2 * i), "0")
    mask = mask << 1

def main():
    global stdscr
    dir(curses)
    stdscr = curses.initscr()         # initialize ncurses display
    stdscr.nodelay(1)                 # dont wait for key presses
    curses.noecho()                   # dont echo key presses
    GZ.spi_set_width(2)               # Pass blocks of 2 bytes on SPI
    stdscr.erase() 
    stdscr.addstr("Toggling all outputs.\n")
    stdscr.addstr("Press 'n' for next test, any other key to stop.\n")
    key = 0
    while(True):
      exercise_outputs(0xff, 0x00);
      key = stdscr.getch()
      if (key != -1):
        break;
    if (str(unichr(key)) == "n"):
      stdscr.erase()
      stdscr.addstr("Toggling alternate outputs.\n")
      stdscr.addstr("Press 'n' for next test, any other key to stop.\n")
      while(True):
        exercise_outputs(0xaa, 0x55)
        key = stdscr.getch()
        if (key != -1):
          break;
    if (str(unichr(key)) == "n"):
      stdscr.erase()
      stdscr.addstr("Walking outputs.\n")
      stdscr.addstr("Press 'n' for next test, any other key to stop.\n")
      current = 0xfffe
      while(True):
        exercise_outputs(current, (current << 1) | 0x0001)
        current = (current << 2) | 0x03
        if ((current & 0xff) == 0xff):
          current = 0xfe
        key = stdscr.getch();
        if (key != -1):
          break;
    if (str(unichr(key)) == "n"):
      stdscr.erase()
      curses.curs_set(0)              # Hide the cursor
      stdscr.addstr("Reading inputs.\n")
      stdscr.addstr("Press any key to stop.\n")
      while(True):
        display_inputs()
        key = stdscr.getch()
        if (key != -1):
          break;
      stdscr.move(stdscr.getyx()[0] + 1 ,0)
      curses.curs_set(1)
      stdscr.refresh()
    GZ.spi_close()                    # close SPI channel
    curses.reset_shell_mode()         # turn off ncurses
    return 0

if __name__ == '__main__':
  main()
