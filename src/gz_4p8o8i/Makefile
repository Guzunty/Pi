#
# Makefile:
#       gz_led_dem0 - Guzunty test program
#
#	 Copyright (c) 2013 campbellsan
########################################################################
#
#    Guzunty is free software: you can redistribute it and/or modify
#    it under the terms of the GNU Lesser General Public License as
#    published by the Free Software Foundation, either version 3 of the
#    License, or (at your option) any later version.
#
#    Guzunty is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU Lesser General Public License for more details.
#
#    You should have received a copy of the GNU Lesser General Public
#    License along with the Guzunty distribution.  If not, please see
#    <http://www.gnu.org/licenses/>.
########################################################################

DEBUG	= -g -O0
CC	= gcc
INCLUDE	= -I/usr/local/include
CFLAGS	= $(DEBUG) -Wall $(INCLUDE) -Winline -pipe -fPIC -DDEBUG_MODE
LIBS    = -lgz -lrt -lncurses

SRC	=	gz_4p8o8i.c

BIN	=	gz_4p8o8i

all:
	$(CC) $(CFLAGS) $(SRC) -o $(BIN) $(LIBS) 
    
clean:
	rm -f *~ $(BIN)
