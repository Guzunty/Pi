/*
 * gz_ppm_irq_.c
 * 
 * echo $gpio > /sys/class/gpio/export
 * echo $trigger > /sys/class/gpio/gpio$gpio/edge
 * ./gpio-irq-demo $gpio
 * 
 * where $gpio is the number of the pin you want to trigger with, and
 * $trigger = none | rising | falling | both
 * 
 * Uses linux sysfs interface
 * 
 */


#include <stdio.h>
#include <poll.h>
#include <stdlib.h>
#include <fcntl.h>
#include <string.h>
#include <gz_clk.h>
#include <ncurses.h>
#include <gz_spi.h>
#include <pthread.h>

#define GPIO_FN_MAXLEN 32
#define POLL_TIMEOUT   1000
#define RDBUF_LEN      5

typedef struct {
  unsigned * buf;
  struct pollfd * pfd;
  unsigned short stop;
} i_info;

void *read_ppm(void* shared);

int main(int argc, char **argv) {
  unsigned input[8];
  char fn[GPIO_FN_MAXLEN];
  int fd,ret;
  struct pollfd pfd;
  char rdbuf[RDBUF_LEN];
  int key = 0;
  pthread_attr_t attr;
  i_info irq_info;
  pthread_t thread_p;
  int row, col;

  ret = pthread_attr_init(&attr);
  if (ret < 0) {
    perror("read()");
    return 4;
  }

  initscr();                          // initialize ncurses display
  nodelay(stdscr, 1);                 // don't wait for key presses
  noecho();                           // don't echo key presses
  erase();
  getmaxyx(stdscr, row, col);         // get the screen boundaries
  int center_x = col / 2;
  int center_y = row / 2;

  memset(rdbuf, 0x00, RDBUF_LEN);
  memset(fn, 0x00, GPIO_FN_MAXLEN);

  if (argc != 2) {
		printf("Usage: %s <GPIO>\nGPIO must be exported to sysfs and have enabled edge detection\n", argv[0]);
		return 1;
  }
  snprintf(fn, GPIO_FN_MAXLEN-1, "/sys/class/gpio/gpio%s/value", argv[1]);
  fd=open(fn, O_RDONLY);              // open sysfs for gpio
  if (fd < 0) {
    perror(fn);
    return 2;
  }
  pfd.fd=fd;
  pfd.events=POLLPRI;
  ret = read(fd, rdbuf, RDBUF_LEN-1);
  if (ret < 0) {
    perror("read()");
    return 3;
  }
  printf("value is: %s\n", rdbuf);

  irq_info.buf = input;               // Set up shared sysfs data with
  irq_info.pfd = &pfd;                // interrupt handling thread and
  irq_info.stop = 0;                  // start the thread ...
  ret = pthread_create(&thread_p, &attr, &read_ppm, &irq_info);
  if (ret < 0) {
    perror("read_ppm()");
    return 4;
  }

  gz_clock_ena(GZ_CLK_125MHz, 0x0c4); // Start clocking the CPLD
  int i = 0;                          // Clear the array to be used
  for (i = 0; i < 8; i++) {           // for the recieved data from SPI
    input[i] = 0;
  }
  while(1) {                          // Main loop, replace with
    key = getch();                    // application code
    if (key != -1) {
      break;
    }
    int i = 0;
    for (i=0; i < 8; i++) {
      mvprintw(center_y - 4 + i, center_x - 8,
                                        "%d - %u    ", i, input[i]);
    }
  }
  close(fd);                          // clean up
  gz_clock_dis();
  irq_info.stop = 1;
  erase();
  reset_shell_mode();                 // turn off ncurses
  return 0;
}

void *read_ppm(void* arg)
{
  i_info * shared = (i_info *)arg;
  void * exit_status = NULL;
  char rdbuf[RDBUF_LEN];

  memset(rdbuf, 0x00, RDBUF_LEN);
  gz_spi_set_width(2);                // Pass blocks of 2 bytes on SPI
  while(shared->stop == 0) {          // INterrupt thread main loop
    unsigned char input[2];
    int ret;
    memset(rdbuf, 0x00, RDBUF_LEN);
    lseek(shared->pfd->fd, 0, SEEK_SET);
                                      // poll() blocks until interrupted
    ret=poll(shared->pfd, 1, POLL_TIMEOUT);
    if (ret < 0) {
      perror("poll()");
      close(shared->pfd->fd);
      exit_status = (void *)1;
      break;
    }
    if (ret == 0) {
      printf("timeout\n");
      continue;
    }
    gz_spi_read(input);               // Read data from CPLD
    ret = read(shared->pfd->fd, rdbuf, RDBUF_LEN-1);
    if (ret < 0) {
      perror("read()");
      exit_status = (void *)2;
      break;
    }
                                      // Parse into channel and value
    int channel = (input[0] & 0x70) >> 4;
    unsigned value = ((input[0] & 0x0f) << 8) + input[1];
    shared->buf[channel] = value;     // Store in shared buffer
  }
  gz_spi_close();                     // close SPI channel
  pthread_exit(exit_status);
}
