#
# Makefile:

DEBUG	= -g -O0
#DEBUG	= -O2
CC	= gcc
INCLUDE	= -I/usr/local/include
CFLAGS	= $(DEBUG) -Wall $(INCLUDE) -Winline -pipe -fPIC -DDEBUG_MODE

LDFLAGS	= -L/usr/local/lib
LIBS    = -lbcm2835 -lrt

SRC	=	lenval.c micro.c ports.c

OBJ	=	lenval.o micro.o ports.o

BIN	=	./gz_load

all: $(BIN)

#$(BIN):	$(OBJ)
#	@gcc -Wl -o $@ $(OBJ)

.c.o:
	@echo [CC] $<
	@$(CC) -c $(CFLAGS) $< -o $@

#%.bin: $(OBJS)
$(BIN): $(OBJ)
	$(CC) -o $@ -Wl $(OBJ) $(LDFLAGS) $(LIBS)

clean:
	rm -f $(OBJ) *~ lenval uart micro ports main

tags:	$(SRC)
	@echo [ctags]
	@ctags $(SRC)

depend:
	makedepend -Y $(SRC)

