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
 * This program demonstrates the 8 way 6 bit pwm plus six input Guzunty
 * core.
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
#include <ncurses.h>

// SPI config definitions
#define SPI_MODE              SPI_MODE_0
#define SPI_BITS_PER_WORD     8
#define SPI_MAX_SPEED         1000000       // 1 Mhz

int spi_fd;                                 // file descriptor

void transfer(unsigned char* data, 
               int delay)
{
    struct spi_ioc_transfer spi;
 
    unsigned char outbuf[2];
    unsigned char inbuf[2];

    outbuf[0] = data[0];
    outbuf[1] = data[1];
    spi.tx_buf        = (unsigned long)outbuf;
    spi.rx_buf        = (unsigned long)&inbuf;
    spi.len           = 2;
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

unsigned char intensity[256];

int main(int argc, char* argv[])
{
    unsigned char payload[2];
    unsigned char reg = 4;
    char dir = 1;
    unsigned char values[8] = {0,0,0,0,0,0,0,0};

    if(spi_open("/dev/spidev0.0") < 0){
        printf("spi_open failed\n");
        return -1;
    }

    int i = 0;
    while (1) {
	  reg += dir;
      if (reg == 7 || reg == 0) {
		  dir = -dir;
      } 
      values[reg] = 0x7;
      
      for(i = 0; i < 8; i++) {
        payload[0] = i;
        payload[1] = values[i];
        if (values[i] > 0) {
          values[i]--;
        }
        transfer(payload,0);
      }
      usleep(50000);
      if (getch() != 0) {
		  break;
	  }
    }
    // close SPI channel
    close(spi_fd);

    return 0;
}
