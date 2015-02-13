/*
 * gz_gpio.h
 *
 * Copyright 2015  guzunty
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

// Peripheral and GPIO base memory definitions
#define BCM2708_PERI_BASE        0x20000000
#define BCM2709_PERI_BASE        0x3f000000
#define GPIO_BASE                (peri_base + 0x200000) /* GPIO controller */
#define CLOCK_BASE               (peri_base + 0x101000) /* Clocks */

#define MAP_BLOCK_SIZE (4*1024)

// GPIO setup macros. Always use INP_GPIO(x) before using OUT_GPIO(x) or SET_GPIO_ALT(x,y)
#define INP_GPIO(g) *(get_gpio_base()+((g)/10)) &= ~(7<<(((g)%10)*3))
#define OUT_GPIO(g) *(get_gpio_base()+((g)/10)) |=  (1<<(((g)%10)*3))
#define SET_GPIO_ALT(g,a) *(get_gpio_base()+(((g)/10))) |= (((a)<=3?(a)+4:(a)==4?3:2)<<(((g)%10)*3))

#define GPIO_SET *(get_gpio_base()+7)  // sets   bits which are 1 ignores bits which are 0
#define GPIO_CLR *(get_gpio_base()+10) // clears bits which are 1 ignores bits which are 0

#define GET_GPIO(g) (*(get_gpio_base()+13)&(1<<g)) // 0 if LOW, (1<<g) if HIGH

#define GPIO_PULL *(get_gpio_base()+37) // Pull up/pull down
#define GPIO_PULLCLK0 *(get_gpio_base()+38) // Pull up/pull down clock

__off_t get_peripheral_base();
volatile unsigned * get_gpio_base();
volatile unsigned * get_clock_base();
