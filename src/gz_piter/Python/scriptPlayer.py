import os

def say(phrase):
  message = 'flite -voice slt -t "' + phrase.replace('\n', '').replace('\r', '') + '"&'
  os.system(message)

class scriptPlayer:
  
  def __init__(self, f, ledCtrlr):
    self.script = list(f)
    self.reset()
    self.locals = {'ledCtrlr': ledCtrlr}
    self.globals = {'say' : say}

  def cue(self):
    self.curLine = self.curLine + 1
    lines = ''
    while (self.curLine < len(self.script) and self.script[self.curLine].replace('\n', '').replace('\r', '') != "waitForCue()"):
      lines = lines + self.script[self.curLine]
      self.curLine = self.curLine + 1
    if (len(lines) > 0):
      exec lines in self.locals, self.globals

  def reset(self):
    self.curLine = -1

