/*
 * gz_spi.c
 * 
 * Copyright 2012  campbellsan
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
#include <gz_spi.h>
#include <linux/spi/spidev.h>
#include <fcntl.h>
#include <stdio.h>
#include <sys/ioctl.h>

#define SPI_MODE              SPI_MODE_0
#define SPI_BITS_PER_WORD     8
#define SPI_MAX_SPEED         10000000       // 10 Mhz

void gz_spi_initialize();
int spi_open(char* dev);
unsigned char transfer(unsigned char data, int delay);
int spi_fd;
unsigned short int spi_cache = 0;
char initialized = 0;

int gz_spi_set(int bit_to_set) {
  if (!initialized) {
    gz_spi_initialize();
  }
  spi_cache |= (1 << bit_to_set);
  transfer(spi_cache & 0xff, 0);
  transfer((spi_cache & 0xff00) >> 8, 0);
  return 0;
}

int gz_spi_reset(int bit_to_reset){
  if (!initialized) {
    gz_spi_initialize();
  }
  spi_cache &= ~(1 << bit_to_reset);
  transfer(spi_cache & 0xff, 0);
  transfer((spi_cache & 0xff00) >> 8, 0);
  return 0;
}

int gz_spi_write(unsigned short int to_write) {
  if (!initialized) {
    gz_spi_initialize();
  }
  spi_cache = to_write;
  transfer(spi_cache & 0xff, 0);
  transfer((spi_cache & 0xff00) >> 8, 0);
  return 0;
}

unsigned short int gz_spi_read() {
  unsigned short result = 0;
  if (!initialized) {
    gz_spi_initialize();
  }
  result = transfer(spi_cache & 0xff, 0);
  result |= transfer((spi_cache & 0xff00) >> 8, 0) << 8;
  return result;
}

void gz_spi_initialize() {
	spi_open("/dev/spidev.0.0");
	initialized = 1;
}

int spi_open(char* dev) {
  int _mode  = SPI_MODE;
  int _bpw   = SPI_BITS_PER_WORD;
  int _speed = SPI_MAX_SPEED;

  if((spi_fd = open(dev, O_RDWR)) < 0){
    printf("error opening %s\n",dev);
    return -1;
  }

  if (ioctl (spi_fd, SPI_IOC_WR_MODE, &_mode) < 0) 
      return -1 ;
  if (ioctl (spi_fd, SPI_IOC_WR_BITS_PER_WORD, &_bpw) < 0) 
      return -1 ;
  if (ioctl (spi_fd, SPI_IOC_WR_MAX_SPEED_HZ, &_speed)   < 0) 
      return -1 ;
  return 0;
}

unsigned char transfer(unsigned char data, int delay) {
    struct spi_ioc_transfer spi;
    unsigned char outbuf;
    unsigned char inbuf;

    outbuf = data;
    spi.tx_buf        = (unsigned long)&outbuf;
    spi.rx_buf        = (unsigned long)&inbuf;
    spi.len           = 1;
    spi.delay_usecs   = delay;
    spi.speed_hz      = SPI_MAX_SPEED;
    spi.bits_per_word = SPI_BITS_PER_WORD;

    if(ioctl(spi_fd, SPI_IOC_MESSAGE(1), &spi) < 0){
        printf("ERROR while sending\n");
    }
    return inbuf;
}
