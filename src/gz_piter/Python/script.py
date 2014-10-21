def police():
  for i in range(0, 63):
    ledCtrlr.newLEDAction(0, 0x3, i*100)
    ledCtrlr.newLEDAction(0, 0xff, (i*100) + 50)
    ledCtrlr.newLEDAction(0, 0x00, (i*100) + 80) 
    ledCtrlr.newLEDAction(1, 0x3, (i*100) + 50)
    ledCtrlr.newLEDAction(1, 0xff, (i*100) + 100)
    ledCtrlr.newLEDAction(1, 0x00, (i*100) + 130) 
  ledCtrlr.newLEDAction(0, 0x00, 6500)
  ledCtrlr.newLEDAction(1, 0x00, 6500)


def reversing():
  for i in range(0, 15):
    ledCtrlr.newLEDAction(0, 0xf0, i*400)
    ledCtrlr.newLEDAction(0, 0x00, (i*400) + 200)
    ledCtrlr.newLEDAction(1, 0xf0, (i*400) + 200)
    ledCtrlr.newLEDAction(1, 0x00, (i*400) + 400)
  ledCtrlr.newLEDAction(0, 0x00, 6500)
  ledCtrlr.newLEDAction(1, 0x00, 6500)

say("Hello, my name is Piter")
waitForCue()
say("I can be a police robot")
police()
waitForCue()
say("I can be a dumper truck")
reversing()
