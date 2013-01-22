/*
 * gz_spi.h
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
#ifndef GZ_SPI_H
#define GZ_SPI_H

#define byte unsigned char

#include<bcm2835.h>

int gz_spi_set(int bit_to_set);
int gz_spi_reset(int bit_to_reset);
int gz_spi_write(byte* to_write);
void gz_spi_read(byte* result);
void gz_spi_set_width(int new_width);
void gz_spi_close();

#endif // GZ_SPI_H
