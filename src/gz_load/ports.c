/*******************************************************/
/* file: ports.c                                       */
/* abstract:  This file contains the routines to       */
/*            output values on the JTAG ports, to read */
/*            the TDO bit, and to read a byte of data  */
/*            from the prom                            */
/* Revisions:                                          */
/* 12/01/2008:  Same code as before (original v5.01).  */
/*              Updated comments to clarify instructions.*/
/*              Add print in setPort for xapp058_example.exe.*/
/*******************************************************/
#include "ports.h"
#include "uart.h"
#include<stdio.h>
#include<stdlib.h>
#include<unistd.h>

#ifdef DEBUG_MODE
#include "stdio.h"
#endif

#ifdef WIN95PP
#include "conio.h"

#define DATA_OFFSET    (unsigned short) 0
#define STATUS_OFFSET  (unsigned short) 1
#define CONTROL_OFFSET (unsigned short) 2

typedef union outPortUnion {
    unsigned char value;
    struct opBitsStr {
        unsigned char tdi:1;
        unsigned char tck:1;
        unsigned char tms:1;
        unsigned char zero:1;
        unsigned char one:1;
        unsigned char bit5:1;
        unsigned char bit6:1;
        unsigned char bit7:1;
    } bits;
} outPortType;

typedef union inPortUnion {
    unsigned char value;
    struct ipBitsStr {
        unsigned char bit0:1;
        unsigned char bit1:1;
        unsigned char bit2:1;
        unsigned char bit3:1;
        unsigned char tdo:1;
        unsigned char bit5:1;
        unsigned char bit6:1;
        unsigned char bit7:1;
    } bits;
} inPortType;

static inPortType in_word;
static outPortType out_word;
static unsigned short base_port = 0x378;
static int once = 0;
#endif
#ifndef DEBUG_MODE
extern FILE * input;
#else
extern FILE * in;
#endif
#ifndef DEBUG_MODE
const char*   const xsvf_pzErrorName[]  =
{
    "No error",
    "ERROR:  Unknown",
    "ERROR:  TDO mismatch",
    "ERROR:  TDO mismatch and exceeded max retries",
    "ERROR:  Unsupported XSVF command",
    "ERROR:  Illegal state specification",
    "ERROR:  Data overflows allocated MAX_LEN buffer size"
};
#else
extern const char* xsvf_pzErrorName[];
#endif

//----------------------------------------------------------------------
// Access from ARM Running Linux

#include <sys/mman.h>
#include <fcntl.h>
#include <time.h>

#define BCM2708_PERI_BASE        0x20000000
#define BCM2709_PERI_BASE        0x3f000000
#define GPIO_BASE                (peri_base + 0x200000) /* GPIO controller */

#define PAGE_SIZE (4*1024)
#define BLOCK_SIZE (4*1024)

int  mem_fd;
void *gpio_map;

// I/O access
volatile unsigned *gpio;
__off_t peri_base = BCM2708_PERI_BASE;

// GPIO setup macros. Always use INP_GPIO(x) before using OUT_GPIO(x) or SET_GPIO_ALT(x,y)
#define INP_GPIO(g) *(gpio+((g)/10)) &= ~(7<<(((g)%10)*3))
#define OUT_GPIO(g) *(gpio+((g)/10)) |=  (1<<(((g)%10)*3))
#define SET_GPIO_ALT(g,a) *(gpio+(((g)/10))) |= (((a)<=3?(a)+4:(a)==4?3:2)<<(((g)%10)*3))

#define GPIO_SET *(gpio+7)  // sets   bits which are 1 ignores bits which are 0
#define GPIO_CLR *(gpio+10) // clears bits which are 1 ignores bits which are 0

#define GET_GPIO(g) (*(gpio+13)&(1<<g)) // 0 if LOW, (1<<g) if HIGH

#define GPIO_PULL *(gpio+37) // Pull up/pull down
#define GPIO_PULLCLK0 *(gpio+38) // Pull up/pull down clock

void setup_io();

//----------------------------------------------------------------------

#define JTAG_TDI 23
#define JTAG_TCK 17
#define JTAG_TMS 24
#define JTAG_TDO 22

void portsInitialize()
{
  setup_io();
  INP_GPIO(JTAG_TDI);
  INP_GPIO(JTAG_TCK);
  INP_GPIO(JTAG_TMS);
  INP_GPIO(JTAG_TDO);
  OUT_GPIO(JTAG_TDI);
  OUT_GPIO(JTAG_TCK);
  OUT_GPIO(JTAG_TMS);
}



/* setPort:  Implement to set the named JTAG signal (p) to the new value (v).*/
/* if in debugging mode, then just set the variables */
void setPort(short p,short val)
{
#ifdef WIN95PP
    /* Old Win95 example that is similar to a GPIO register implementation.
       The old Win95 example maps individual bits of the 
       8-bit register (out_word) to the JTAG signals: TCK, TMS, TDI. 
       */

    /* Initialize static out_word register bits just once */
    if (once == 0) {
        out_word.bits.one = 1;
        out_word.bits.zero = 0;
        once = 1;
    }

    /* Update the local out_word copy of the JTAG signal to the new value. */
    if (p==TMS)
        out_word.bits.tms = (unsigned char) val;
    if (p==TDI)
        out_word.bits.tdi = (unsigned char) val;
    if (p==TCK) {
        out_word.bits.tck = (unsigned char) val;
        (void) _outp( (unsigned short) (base_port + 0), out_word.value );
        /* To save HW write cycles, this example only writes the local copy
           of the JTAG signal values to the HW register when TCK changes. */
    }
#endif
    /* Printing code for the xapp058_example.exe.  You must set the specified
       JTAG signal (p) to the new value (v).  See the above, old Win95 code
       as an implementation example. */
    if (val == 1) {
      if (p==TMS) {
        GPIO_SET = 1 << JTAG_TMS;
      }
      if (p==TDI) {
        GPIO_SET = 1 << JTAG_TDI;
      }
      if (p==TCK) {
        GPIO_SET = 1 << JTAG_TCK;
       // usleep(1);
      }
    }
    else { // val == 0
      if (p==TMS) {
        GPIO_CLR = 1 << JTAG_TMS;
      }
      if (p==TDI) {
        GPIO_CLR = 1 << JTAG_TDI;
      }
      if (p==TCK) {
        GPIO_CLR = 1 << JTAG_TCK;
        //usleep(1);
      }
#ifdef DEBUG_MODE
    //    printf( "TCK = %d;  TMS = %d;  TDI = %d\n", g_iTCK, g_iTMS, g_iTDI );
#endif
    }
}


/* toggle tck LH.  No need to modify this code.  It is output via setPort. */
void pulseClock()
{
    setPort(TCK,0);  /* set the TCK port to low  */
    setPort(TCK,1);  /* set the TCK port to high */
}


/* readByte:  Implement to source the next byte from your XSVF file location */
/* read in a byte of data from the file */

void readByte(unsigned char *data)
{
#ifndef DEBUG_MODE
   *data = fgetc(input);
#else
   *data = fgetc(in);
#endif
}

/* readTDOBit:  Implement to return the current value of the JTAG TDO signal.*/
/* read the TDO bit from port */
unsigned char readTDOBit()
{
#ifdef WIN95PP
    /* Old Win95 example that is similar to a GPIO register implementation.
       The old Win95 reads the hardware input register and extracts the TDO
       value from the bit within the register that is assigned to the
       physical JTAG TDO signal. 
       */
    in_word.value = (unsigned char) _inp( (unsigned short) (base_port + STATUS_OFFSET) );
    if (in_word.bits.tdo == 0x1) {
        return( (unsigned char) 1 );
    }
#endif
	if(GET_GPIO(JTAG_TDO))	{
		return( (unsigned char) 1 );
	}
	else {
		return( (unsigned char) 0 );
	}
}

void output_error(int error_code) {

	printf("\n%s\n",xsvf_pzErrorName[error_code]);

}

/* waitTime:  Implement as follows: */
/* REQUIRED:  This function must consume/wait at least the specified number  */
/*            of microsec, interpreting microsec as a number of microseconds.*/
/* REQUIRED FOR SPARTAN/VIRTEX FPGAs and indirect flash programming:         */
/*            This function must pulse TCK for at least microsec times,      */
/*            interpreting microsec as an integer value.                     */
/* RECOMMENDED IMPLEMENTATION:  Pulse TCK at least microsec times AND        */
/*                              continue pulsing TCK until the microsec wait */
/*                              requirement is also satisfied.               */
void waitTime(long microsec)
{
	struct timespec ts, dummy;
	
	//clock_gettime(CLOCK_REALTIME, &ts);
	//long n_time = (ts.tv_sec * 1000000000) + ts.tv_nsec;
	
   //static long tckCyclesPerMicrosec  = 1; /* must be at least 1 */
   //long        tckCycles   = microsec * tckCyclesPerMicrosec;
   //long        i;
    
    /* This implementation is highly recommended!!! */
    /* This implementation requires you to tune the tckCyclesPerMicrosec 
       variable (above) to match the performance of your embedded system
       in order to satisfy the microsec wait time requirement. */
    //for ( i = 0; i < tckCycles; ++i )
    //{
    //    pulseClock();
    //}
	//clock_gettime(CLOCK_REALTIME, &ts);
	//long n_now = (ts.tv_sec * 1000000000) + ts.tv_nsec;
    //while (n_now - n_time < microsec * 1000) {
	//	pulseClock();
	//    clock_gettime(CLOCK_REALTIME, &ts);
	//    n_now = (ts.tv_sec * 1000000000) + ts.tv_nsec;
	//}
    setPort(TCK, 0);
    ts.tv_sec = 0;
    ts.tv_nsec = microsec * 1000L;
    nanosleep(&ts, &dummy);
    //for (i = 0; i < tckCycles; i++) {
	//	pulseClock();
	//} 

#if 0
    /* Alternate implementation */
    /* For systems with TCK rates << 1 MHz;  Consider this implementation. */
    /* This implementation does not work with Spartan-3AN or indirect flash
       programming. */
    if ( microsec >= 50L )
    {
        /* Make sure TCK is low during wait for XC18V00/XCFxxS */
        /* Or, a running TCK implementation as shown above is an OK alternate */
        setPort( TCK, 0 );

        /* Use Windows Sleep().  Round up to the nearest millisec */
        _sleep( ( microsec + 999L ) / 1000L );
    }
    else    /* Satisfy FPGA JTAG configuration, startup TCK cycles */
    {
        for ( i = 0; i < microsec;  ++i )
        {
            pulseClock();
        }
    }
#endif

#if 0
    /* Alternate implementation */
    /* This implementation is valid for only XC9500/XL/XV, CoolRunner/II CPLDs, 
       XC18V00 PROMs, or Platform Flash XCFxxS/XCFxxP PROMs.  
       This implementation does not work with FPGAs JTAG configuration. */
    /* Make sure TCK is low during wait for XC18V00/XCFxxS PROMs */
    /* Or, a running TCK implementation as shown above is an OK alternate */
    setPort( TCK, 0 );
    /* Use Windows Sleep().  Round up to the nearest millisec */
    _sleep( ( microsec + 999L ) / 1000L );
#endif
}

//
// Set up a memory region to access GPIO
//
void setup_io()
{
	int proc_fd;
	char proc_model[32];
   if ((mem_fd = open("/dev/mem", O_RDWR|O_SYNC) ) < 0) {
      printf("\rError initializing IO. Consider using sudo.\n");
      exit(-1);
   }

   if ((proc_fd = open("/proc/device-tree/model", O_RDONLY|O_SYNC) ) >= 0) {
	  // if we can read the device tree, we may be dealing with a more
	  // recent Raspberry Pi model which has a different peripheral
	  // memory address.
	  int i = 0;
	  int len = 0;
      len = read(proc_fd, proc_model, sizeof proc_model);
      for (i = 0; i < len; i++) {
		if (proc_model[i] == '2') {  // Yes this is a rev 2 Pi ...
			peri_base = BCM2709_PERI_BASE;
			break;
	    }
      }
   }

   /* memory map the GPIO */
   gpio_map = mmap(
      NULL,             //Any address in our space will do
      BLOCK_SIZE,       //Map length
      PROT_READ|PROT_WRITE,// Enable reading & writting to mapped memory
      MAP_SHARED,       //Shared with other processes
      mem_fd,           //File to map
      GPIO_BASE         //Offset to GPIO peripheral
   );

   close(mem_fd); //No need to keep mem_fd open after mmap

   if (gpio_map == MAP_FAILED) {
      printf("mmap error %d\n", (int)gpio_map);//errno also set!
      exit(-1);
   }

   // Always use volatile pointer!
   gpio = (volatile unsigned *)gpio_map;


} // setup_io

