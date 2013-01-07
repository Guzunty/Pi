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
 ******************************************************************************/

#include <msp430.h>
#include <legacymsp430.h>
#include  <msp430g2553.h>

#include "uart.h"

/**
 * Receive Data (RXD) at P1.1
 */
#define RXD		BIT1

/**
 * Transmit Data (TXD) at P1.2
 */
#define TXD		BIT2

void uart_init(void)
{
	P1SEL  = RXD + TXD;                       
  	P1SEL2 = RXD + TXD;                       
  	UCA0CTL1 |= UCSSEL_2;                     // SMCLK
  	UCA0BR0 = 0x80;                           // 16MHz 9600
  	UCA0BR1 = 0x06;                           // 16MHz 9600
  	UCA0MCTL = UCBRS0;                        // Modulation UCBRSx = 1
  	UCA0CTL1 &= ~UCSWRST;                     // Initialize USCI state machine
  	IE2 |= UCA0RXIE;                          // Enable USCI_A0 RX interrupt
    __bis_SR_register(GIE);                   // global interrrupt enable
}

#define BUFFER_SIZE 64
int read_cursor = 0;
unsigned char buffer[BUFFER_SIZE];
int write_cursor = 0;

unsigned char uart_getc()
{
    while (!(read_cursor < write_cursor));	// Is there data to read?
    unsigned char result = buffer[read_cursor++];
	if (read_cursor == BUFFER_SIZE) {
		read_cursor = 0;
		write_cursor = 0;
		uart_putc(0x06);					// send ACK for next block
	}
	return result;
}

void uart_putc(unsigned char c)
{
	while (!(IFG2&UCA0TXIFG));              // USCI_A0 TX buffer ready?
  	UCA0TXBUF = c;                    		// TX
}

void uart_puts(const char *str)
{
     while(*str) uart_putc(*str++);
}

interrupt(USCIAB0RX_VECTOR) USCI0RX_ISR(void)
{
	buffer[write_cursor++] = (unsigned char)(UCA0RXBUF & 0xff);     // (clears interrupt flag)
}
