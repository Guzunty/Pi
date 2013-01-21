/*
 * gz_16i8o.c
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

void exercise_outputs(unsigned char a, unsigned char b) {
	  unsigned char output[2];
	  output[0] = a;
	  gz_spi_write(output);          // pass output bytes to SPI
      sleep(1);
      output[0] = b;
	  gz_spi_write(output);          // pass output bytes to SPI
	  sleep(1);
}

int main(int argc, char* argv[])
{
    initscr();                        // initialize ncurses display
    nodelay(stdscr, 1);               // don't wait for key presses
    noecho();                         // don't echo key presses
    gz_spi_set_width(2);              // Pass blocks of 2 bytes on SPI
    printw("Toggling all outputs.\n");
	printw("Press 'n' for next test, any other key to stop.\n");
	int key = 0;
    while(1) {
	  exercise_outputs(0xff, 0x00);
      key = getch();
      if (key != -1) {
		  break;
	  }
	}
	if (key == 'n') {
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
    gz_spi_close();                   // close SPI channel
    reset_shell_mode();               // turn off ncurses
    return 0;
}
