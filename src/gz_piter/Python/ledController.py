import time
import spidev

class ledController:
  
  def __init__(self, spi):
    self.lastTime = int(round(time.time() * 1000))
    self.actionList = []
    self.spi = spi

  def newLEDAction(self, ledID, colour, delay):
    self.actionList.append([ledID, colour, delay])

  def poll(self):
    curTime = int(round(time.time() * 1000))
    elapsed = curTime - self.lastTime
    for x in self.actionList:
      if (x[2] <= elapsed):
        self.writePWM(x[0] + 2, x[1])
        self.actionList.remove(x)
      else:
        x[2] = x[2] - elapsed
    self.lastTime = curTime

  def writePWM(self, addr, value):
    self.spi.xfer([addr, value])

  def resetLEDs(self):
    self.writePWM(2, 0)
    self.writePWM(3, 0)
