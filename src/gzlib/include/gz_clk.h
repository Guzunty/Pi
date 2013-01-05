/*
 * gz_clk.h
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
#ifndef GZ_CLK_H
#define GZ_CLK_H

#include<bcm2835.h>

#define GZ_CLK_5MHz 0
#define GZ_CLK_125MHz 1

int gz_clock_ena(int speed, int divider);
int gz_clock_dis();

#endif // GZ_CLK_H
