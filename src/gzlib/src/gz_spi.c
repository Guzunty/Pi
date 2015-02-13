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
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>

#define SPI_MODE              SPI_MODE_0
#define SPI_BITS_PER_WORD     8
#define SPI_MAX_SPEED         10000000       // 10 Mhz

void gz_spi_initialize();
int spi_open(char* dev);
byte* transfer(byte* data, int delay);
int spi_fd;
byte spi_cache[4] = {0, 0, 0, 0};
byte inbuf[4];

char initialized = 0;
char width = 2;

int gz_spi_set(int bit_to_set) {
  if (!initialized) {
    gz_spi_initialize();
  }
  int byte_cnt = bit_to_set >> 3;
  if (byte_cnt > width) {
	  printf("Bit %d is out of range.", bit_to_set);
	  exit(-1);
  }
  else {
    spi_cache[byte_cnt] |= (1 << (bit_to_set & 0x7));
    transfer(spi_cache, 0);
  }
  return 0;
}

int gz_spi_reset(int bit_to_reset){
  if (!initialized) {
    gz_spi_initialize();
  }
  int byte_cnt = bit_to_reset >> 3;
  if (byte_cnt > width) {
	printf("Bit %d is out of range.", bit_to_reset);
	exit(-1);
  }
  else {
    spi_cache[byte_cnt] &= ~(1 << (bit_to_reset & 0x7));
    transfer(spi_cache, 0);
  }
  return 0;
}

int gz_spi_write(byte* to_write) {
  if (!initialized) {
    gz_spi_initialize();
  }
  int i = 0;
  for (; i < width; i++) {
	spi_cache[i] = *to_write++;
  }
  transfer(spi_cache, 0);
  return 0;
}

void gz_spi_read(byte* result) {
  byte* buffer;
  if (!initialized) {
    gz_spi_initialize();
  }
  buffer = transfer(spi_cache, 0);
  int i= 0;
  for (; i < width; i++) {
	  result[i] = buffer[i];
  }
}

int gz_spi_get(int bit_to_read) {
  byte* buffer;
  int byte_cnt = bit_to_read >> 3;
  if (byte_cnt > width) {
	printf("Bit %d is out of range.", bit_to_read);
	exit(-1);
  }
  else {
	buffer = transfer(spi_cache, 0);
	return 0 == (buffer[byte_cnt] & (1 << (bit_to_read & 0x7))) ? 0 : 1;
  }
  return 0;
}

int gz_output_get(int bit_to_read) {
  int byte_cnt = bit_to_read >> 3;
  if (byte_cnt > width) {
	printf("Bit %d is out of range.", bit_to_read);
	exit(-1);
  }
  else {
    return 0 == (spi_cache[byte_cnt] & (1 << (bit_to_read & 0x7))) ? 0 : 1;
  }
  return 0;
}

void gz_spi_initialize() {
  spi_open("/dev/spidev0.0");
  initialized = 1;
}

/*
*     - Open a specific device
*/
int gz_spi_open_port(char * port) {
  int result;
  result = spi_open(port);
  initialized = 1;
  return result;
}

/*spi_open
*      - Open the given SPI channel and configure it.
*      - there are normally two SPI devices on your PI:
*        /dev/spidev0.0: activates the CS0 pin during transfer
*        /dev/spidev0.1: activates the CS1 pin during transfer
*
*/
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

byte* transfer(byte* data, int delay) {
  struct spi_ioc_transfer spi;
  byte outbuf[4];
  int result;

  int i = 0;
  for(; i < width; i++) {
    outbuf[i] = data[i];
  }
  memset(&spi,0,sizeof(spi));
  spi.tx_buf        = (unsigned long)&outbuf;
  spi.rx_buf        = (unsigned long)&inbuf;
  spi.len           = width;
  spi.delay_usecs   = delay;
  spi.speed_hz      = SPI_MAX_SPEED;
  spi.bits_per_word = SPI_BITS_PER_WORD;
  result = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &spi);
  if( result < 0){
        printf("spi: ERROR while sending - %s\r\n", strerror(errno));
  }
  return inbuf;
}

void gz_spi_set_width(int new_width) {
  if (new_width < 5 && new_width > 0) {
	width = new_width;
  }
  else {
	printf(
	  "Illegal buffer width %d. SPI buffer width must be 1, 2, 3 or 4.",
	  new_width);
	exit(-1);
  }
}

void gz_spi_close() {
  if (close(spi_fd) < 0) {
	  printf("ERROR closing spi device");
  }
  initialized = 0;
}
