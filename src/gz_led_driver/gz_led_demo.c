/*
 * gz_led_demo.c
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
 * This program implements a digital clock displayed on a 4 digit
 * 7 segment LED. It also incorporates a font for sevent segment
 * displays, so that messages can be displayed. Toggle between the
 * clock and message with the 'c' and 'm' keys.
 * 
 */

#include <unistd.h>
#include <time.h>
#include <ncurses.h>
#include <gz_clk.h>
#include <gz_spi.h>

const unsigned char font[128] = {
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0, // All
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0, // unprintable
0x00, 0x30, 0x22, 0x76, // SP, !, ", # (H)
0x6d, 0x7f, 0x7d, 0x20, // $ (5), % (8), & (6), '
0x39, 0x0f, 0x76, 0x76, // (, ), * (H), + (H)
0x10, 0x40, 0x00, 0x52, // ',', -, ., /
0x3f, 0x06, 0x5b, 0x4f, // 0, 1, 2, 3
0x66, 0x6d, 0x7d, 0x07, // 4, 5, 6, 7
0x7f, 0x6f, 0x30, 0x30, // 8, 9, :, ;
0x58, 0x48, 0x4c, 0x53, // <, =, >, ?
0x7b, 0x77, 0x7f, 0x39, // @, A, B, C
0x3f, 0x79, 0x71, 0x3d, // D (0), E, F, G
0x76, 0x06, 0x1e, 0x75, // H, I (1), J, K
0x38, 0x55, 0x37, 0x3f, // L, M, N, O (0)
0x73, 0x7d, 0x31, 0x6d, // P, Q, R, S (5)
0x07, 0x3e, 0x2a, 0x6a, // T, U, V, W
0x76, 0x6e, 0x5b, 0x39, // X (H), Y (y), Z, [
0x64, 0x0f, 0x23, 0x08, // backslash, ], ^, _
0x02, 0x77, 0x7c, 0x58, // ', a (A), b, c
0x5e, 0x7b, 0x71, 0x6f, // d, e, f (F), g
0x74, 0x04, 0x0c, 0x75, // h, i (1), j, k (K)
0x06, 0x55, 0x54, 0x5c, // l, m (M), n, o
0x73, 0x67, 0x50, 0x6d, // p (P), q, r, s (5)
0x78, 0x1c, 0x1c, 0x6a, // t, u, v (u), w (W)
0x76, 0x6e, 0x5b, 0x39, // x (H), y, Z (2), { ([)
0x30, 0x0f, 0x01, 0x00  // |, } (]), ~, DEL
};

int main(int argc, char* argv[])
{
    gz_clock_ena(GZ_CLK_5MHz, 0xfff); // Turn on the slowest clock we can
    unsigned char colon = 0;
    unsigned char disp_mode = 'c';
    char * message = "Guzunty LED driver - ";
    char * start_at = message;
    char * cursor = message;
    unsigned char payload[4];
    
    initscr();                        // initialize ncurses display
    nodelay(stdscr, 1);               // don't wait for key presses
    noecho();                         // don't echo key presses
    gz_spi_set_width(4);              // Pass blocks of 4 bytes on SPI
	printw("Press 'm' for message, 'c' for clock, any other key to stop.\n");
    while (1) {
      int i = 0;
      if (disp_mode == 'c') {
        time_t t;
        time(&t);
        char time_buf[4];
        char time_str[25];
        sprintf(time_str, "%s",ctime(&t));
        time_buf[0] = time_str[11];
        time_buf[1] = time_str[12];
        time_buf[2] = time_str[14];
        time_buf[3] = time_str[15];
        for (; i < 4; i++) {
		  payload[i] = ~font[(unsigned char)time_buf[i]];
	    }
	    if (colon != 0) {
		  payload[1] &= ~0x80;
		  colon = 0;
        }
        else {
		  colon = 1;
	    }
	  }
	  if (disp_mode == 'm') {
		cursor = start_at;
		for (; i < 4; i++) {
		  if (*cursor == '\0') {
			cursor = message;
		  }
		  payload[i] = ~font[(unsigned char)*cursor];
		  cursor++;
		}
	    start_at++;
	    if (*start_at == '\0') {
		  start_at = message;
	    }
	  }
	  gz_spi_write(payload);          // pass display bytes to SPI
      sleep(1);
      int key = getch();
      if (key !=-1) {
		if (key == 'c') {
		  disp_mode = (unsigned char)key;
		}
		else {
		  if (key == 'm') {
			  disp_mode = (unsigned char)key;
		  }
		  else {
		    break;
		  }
	    }
	  }
    }
    gz_spi_close();                   // close SPI channel
    reset_shell_mode();               // turn off ncurses
    gz_clock_dis();                   // turn off GPIO clock
    return 0;
}
