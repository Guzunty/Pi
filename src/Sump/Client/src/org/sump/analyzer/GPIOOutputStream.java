package org.sump.analyzer;

import java.io.IOException;
import java.io.OutputStream;

public class GPIOOutputStream extends OutputStream {
	
    // Copied from Device for now to disturb as little as possible

	/** set trigger mask */
	private final static int SETTRIGMASK = 0xc0;
	/** set trigger value */
	private final static int SETTRIGVAL = 0xc1;
	/** set trigger configuration */
	private final static int SETTRIGCFG = 0xc2;
	/** set clock divider */
	private final static int SETDIVIDER = 0x80;
	/** set sample counters */
	private final static int SETSIZE = 0x81;
	/** set flags */
	private final static int SETFLAGS = 0x82;

	/** reset analyzer */
	private final static int RESET = 0x00;
	/** arm trigger / run device */
	private final static int RUN = 0x01;
	/** ask for device id */
	private final static int ID = 0x02;

	private static GPIOInputStream is = null;
	@Override
	public void write(int shortCmd) throws IOException {
      if(shortCmd == ID) {
    	  System.out.println("ID Requested.");
    	  is.privateWrite(0x534c4131);
      } else
      if (shortCmd == RESET) {
    	  System.out.println("Reset Requested.");
      } else
      if (shortCmd == RUN) {
    	  System.out.println("Run Requested.");
      } else
      // If we were supporting the multi stage trigger we would want to mask
      // the command with 0xF3 so that the trigger level bits are removed here.
      if (shortCmd == SETTRIGMASK) {
    	  System.out.println("Trig Mask set.");
      } else
      if (shortCmd == SETTRIGVAL) {
    	  System.out.println("Trig set.");
      } else
      if (shortCmd == SETTRIGCFG) {
    	  System.out.println("Trig Cfg set.");
      } else
      if (shortCmd == SETDIVIDER) {
    	  System.out.println("Divider set.");
      } else
      if (shortCmd == SETSIZE) {
    	  System.out.println("Size set.");
      } else
      if (shortCmd == SETFLAGS) {
    	  System.out.println("Flags set.");
      }
      else {
    	  System.out.println("Unrecognised command" + Integer.toHexString(shortCmd));
      }
	}
	
	public void write(byte[] longCmd) throws IOException {
		write(longCmd[0] & 0xff);
		
	}
	
	public static void setIs(GPIOInputStream is) {
		GPIOOutputStream.is = is;
	}

}
