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
 * 7 segment LED.
 * 
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <linux/spi/spidev.h>
#include <sys/mman.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <time.h>
#include <gz_clk.h>

// SPI config definitions
#define SPI_MODE              SPI_MODE_0
#define SPI_BITS_PER_WORD     8
#define SPI_MAX_SPEED         1000000       // 1 Mhz

int spi_fd;                                 // file descriptor

void transfer(unsigned char* data, 
               int delay)
{
    struct spi_ioc_transfer spi;
 
    unsigned char outbuf[4];
    unsigned char inbuf[4];

    outbuf[0] = data[0];
    outbuf[1] = data[1];
    outbuf[2] = data[2];
    outbuf[3] = data[3];
    spi.tx_buf        = (unsigned long)outbuf;
    spi.rx_buf        = (unsigned long)&inbuf;
    spi.len           = 4;
    spi.delay_usecs   = delay;
    spi.speed_hz      = SPI_MAX_SPEED;
    spi.bits_per_word = SPI_BITS_PER_WORD;

    if(ioctl (spi_fd, SPI_IOC_MESSAGE(1), &spi) < 0){
        printf("ERROR while sending\n");
    }
}

/*spi_open
*      - Open the given SPI channel and configure it.
*      - there are normally two SPI devices on your PI:
*        /dev/spidev0.0: activates the CS0 pin during transfer
*        /dev/spidev0.1: activates the CS1 pin during transfer
*
*/
int spi_open(char* dev)
{
  int _mode  = SPI_MODE;
  int _bpw   = SPI_BITS_PER_WORD;
  int _speed = SPI_MAX_SPEED;

  if((spi_fd = open(dev, O_RDWR)) < 0){
    printf("error opening %s\n",dev);
    return -1;
  }

  if (ioctl (spi_fd, SPI_IOC_WR_MODE, &_mode) < 0) 
      return -1 ;
  if (ioctl (spi_fd, SPI_IOC_RD_MODE, &_mode) < 0) 
      return -1 ;

  if (ioctl (spi_fd, SPI_IOC_WR_BITS_PER_WORD, &_bpw) < 0) 
      return -1 ;
  if (ioctl (spi_fd, SPI_IOC_RD_BITS_PER_WORD, &_bpw) < 0) 
      return -1 ;

  if (ioctl (spi_fd, SPI_IOC_WR_MAX_SPEED_HZ, &_speed)   < 0) 
      return -1 ;
  if (ioctl (spi_fd, SPI_IOC_RD_MAX_SPEED_HZ, &_speed)   < 0) 
      return -1 ;

  return 0;

}

const unsigned char font[128] = {
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0x00, // SP
0x30, // !
0x22, // "
0x76, // # (H)
0x6d, // $ (5)
0x7f, // % (8)
0x7d, // & (6)
0x20, // '
0x39, // (
0x0f, // )
0x76, // * (H)
0x76, // + (H)
0x10, // ,
0x40, // -
0x00, // .
0x52, // /
0x3f, // 0
0x06, // 1
0x5b, // 2
0x4f, // 3
0x66, // 4
0x6d, // 5
0x7d, // 6
0x07, // 7
0x7f, // 8
0x6f, // 9
0x30, // : (!)
0x30, // ; (!)
0x58, // <
0x48, // =
0x4c, // >
0x53, // ?
0x7b, // @
0x77, // A
0x7f, // B
0x39, // C
0x3f, // D (0)
0x79, // E
0x71, // F
0x3d, // G
0x76, // H
0x06, // I (1)
0x1e, // J
0x75, // K
0x38, // L
0x55, // M
0x37, // N
0x3f, // O (0)
0x73, // P
0x7d, // Q
0x31, // R
0x6d, // S (5)
0x07, // T
0x3e, // U
0x2a, // V
0x6a, // W
0x76, // X (H)
0x6e, // Y
0x5b, // Z
0x39, // [
0x64, // backslash
0x0f, // ]
0x23, // ^
0x08, // _
0x02, // '
0x77, // A
0x7c, // b
0x58, // c
0x5e, // d
0x7b, // e
0x71, // F
0x6f, // g
0x74, // h
0x04, // i
0x0c, // J
0x75, // K
0x06, // l
0x55, // M
0x54, // n
0x5c, // o
0x73, // P
0x67, // q
0x50, // r
0x6d, // S (5)
0x78, // t
0x1c, // u
0x1c, // v
0x6a, // W
0x76, // X (H)
0x6e, // Y
0x5b, // Z
0x39, // { ([)
0x30, // |
0x0f, // } (])
0x00
};

int main(int argc, char* argv[])
{
    gz_clock_ena(GZ_CLK_5MHz, 0xfff); // Turn on the slowest clock we can
    unsigned char colon = 0;
    char * message;
    char time_buf[4];
    unsigned char payload[4];
    
    if(spi_open("/dev/spidev0.0") < 0){
        printf("spi_open failed\n");
        return -1;
    }
    while (1) {
      memset(payload, 0, 4);
      int i = 0;
      time_t t;
      time(&t);
      char time_str[25];
      sprintf(time_str, "%s",ctime(&t));
      message = time_buf;
      message[0] = time_str[11];
      message[1] = time_str[12];
      message[2] = time_str[14];
      message[3] = time_str[15];
      for (; i < 4; i++) {
		  payload[i] = ~font[(unsigned char)message[i]];
	  }
	  if (colon != 0) {
		  payload[1] &= ~0x80;
		  colon = 0;
      }
      else {
		  colon = 1;
	  }
      transfer(payload,0);
      sleep(1);
    }

    // close SPI channel
    close(spi_fd);

    return 0;
}
