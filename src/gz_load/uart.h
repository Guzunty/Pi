/*
 * This file is part of the MSP430 hardware UART example.
 *
 * Copyright (C) 2012 Stefan Wendler <sw@kaltpost.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/******************************************************************************
 * Hardware UART example for MSP430.
 *
 * Stefan Wendler
 * sw@kaltpost.de
 * http://gpio.kaltpost.de
 *
 *****************************************************************************/

#ifndef __UART_H
#define __UART_H

/**
 * Initialize soft UART
 */
void uart_init(void);

/**
 * Read one character from UART blocking.
 *
 * @return	character received 
 */
unsigned char uart_getc();

/**
 * Write one chracter to the UART blocking.
 *
 * @param[in]	*c	the character to write
 */
void uart_putc(unsigned char c);

/**
 * Write string to the UART blocking.
 *
 * @param[in]	*str	the 0 terminated string to write
 */
void uart_puts(const char *str);

#endif

