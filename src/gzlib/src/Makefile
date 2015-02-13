#
# Makefile:
#       gzlib - Guzunty access library
#
#	 Copyright (c) 2012 campbellsan
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
INCLUDE	= -I/usr/local/include -I../include
CFLAGS	= $(DEBUG) -Wall $(INCLUDE) -Winline -pipe -fPIC -DDEBUG_MODE

LDFLAGS	= -L/usr/local/lib
LIBS    = -lrt

SRC	=	gz_clk.c gz_spi.c gz_gpio.c

OBJ	=	gz_clk.o gz_spi.o gz_gpio.o

BIN	=	libgz.a

all: $(BIN)

.c.o:
	@echo [CC] $<
	@$(CC) -c $(CFLAGS) $< -o $@

$(BIN): $(OBJ)
	@ar rcs $(BIN) $(OBJ)
	@ranlib $(BIN)

clean:
	rm -f $(OBJ) *~ libgz.a

tags:	$(SRC)
	@echo [ctags]
	@ctags $(SRC)

depend:
	makedepend -Y $(SRC)

install:
	@install -m 0755 -d /usr/local/lib
	@install -m 0755 -d /usr/local/include
	@install -m 0644 ../include/gz_clk.h /usr/local/include
	@install -m 0644 ../include/gz_spi.h /usr/local/include
	@install -m 0644 ../include/gz_gpio.h /usr/local/include
	@install -m 0644 libgz.a /usr/local/lib
	@ldconfig

uninstall:
	@echo [uninstall]
	@rm -f /usr/local/include/gz_clk.h
	@rm -f /usr/local/include/gz_spi.h
	@rm -f /usr/local/include/gz_gpio.h
	@rm -f /usr/local/lib/libgz.a
	@ldconfig
