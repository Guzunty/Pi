/*
 *  Copyright (C) 2006 Michael Poppitz
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or (at
 *  your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin St, Fifth Floor, Boston, MA 02110, USA
 *
 */
package org.sump.analyzer;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.LinkedList;

/**
 * Device provides access to the physical logic analyzer device.
 * It requires the rxtx package from http://www.rxtx.org/ to
 * access the serial port the analyzer is connected to.
 * 
 * @version 0.7
 * @author Michael "Mr. Sump" Poppitz
 *
 */
public class Device extends Object {
	/** use internal clock */
	public final static int CLOCK_INTERNAL = 0;
	/** use external clock rising edge */
	public final static int CLOCK_EXTERNAL_RISING = 1;
	/** use external clock falling edge */
	public final static int CLOCK_EXTERNAL_FALLING = 2;
	
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
	/** continue data transmission to host */
	private final static int XON = 0x11;
	/** pause data transmission to host */
	private final static int XOFF = 0x13;

	
	private final static int FLAG_DEMUX = 0x00000001;		// demultiplex
	private final static int FLAG_FILTER = 0x00000002;		// noise filter
	private final static int FLAG_DISABLE_G0 = 0x00000004;	// disable channel group 0
	private final static int FLAG_DISABLE_G1 = 0x00000008;	// disable channel group 1
	private final static int FLAG_DISABLE_G2 = 0x00000010;	// disable channel group 2
	private final static int FLAG_DISABLE_G3 = 0x00000020;	// disable channel group 3
	private final static int FLAG_EXTERNAL = 0x00000040;	// disable channel group 3
	private final static int FLAG_INVERTED = 0x00000080;	// disable channel group 3

	private final static int TRIGGER_DELAYMASK = 0x0000ffff;// mask for delay value
	private final static int TRIGGER_LEVELMASK = 0x00030000;// mask for level value
	private final static int TRIGGER_CHANNELMASK = 0x01f00000;// mask for level value
	private final static int TRIGGER_SERIAL = 0x04000000;	// trigger operates in serial mode
	private final static int TRIGGER_CAPTURE = 0x08000000;	// trigger will start capture when fired
	
	private final static int CLOCK = 100000000;	// device clock in Hz
	private final static int TRIGGER_STAGES = 4; // number of trigger stages
	
	/**
	 * Creates a device object.
	 *
	 */
	public Device() {
		triggerMask = new int[4];
		triggerValue = new int[4];
		triggerConfig = new int[4];
		for (int i = 0; i < TRIGGER_STAGES; i++) {
			triggerMask[i] = 0;
			triggerValue[i] = 0;
			triggerConfig[i] = 0;
		}
		triggerEnabled = false;
		filterEnabled = false;
		demux = false;
		setClockSource(CLOCK_INTERNAL);
		divider = 0;
		ratio = 0.5;
		size = 512;
		enabledGroups = new boolean[4];
		setEnabledChannels(-1); // enable all channels
		
		percentageDone = -1;
		stop();
		
		port = null;
	}

	/**
	 * Sets the number of samples to obtain when started.
	 * 
	 * @param size number of samples, must be between 4 and 256*1024
	 */
	public void setSize(int size) {
		this.size = size;
	}
	
	/**
	 * Sets the ratio for samples to read before and after started.
	 * @param ratio	value between 0 and 1; 0 means all before start, 1 all after
	 */
	public void setRatio(double ratio) {
		this.ratio = ratio;
	}

	/**
	 * Set the sampling rate.
	 * All rates must be a divisor of 200.000.000.
	 * Other rates will be adjusted to a matching divisor.
	 * 
	 * @param rate		sampling rate in Hz
	 */
	public void setRate(int rate) {
		if (rate > CLOCK) {
			demux = true;
			divider = (2 * CLOCK / rate) - 1;
		} else {
			demux = false;
			divider = (CLOCK / rate) - 1;
		}
	}
	
	/**
	 * Configures the given trigger stage in parallel mode. Currenty the trigger has four stages (0-3).
	 * <p>
	 * In mask and value each bit of the integer parameters represents one channel.
	 * The LSB represents channel 0, the MSB channel 31.
	 * <p>
	 * When a trigger fires, the trigger level will rise by one.
	 * Initially the trigger level is 0.
	 * 
	 * @param stage trigger stage to write mask und value to
	 * @param mask bit map defining which channels to watch
	 * @param value bit map defining what value to wait for on watched channels
	 * @param level trigger level at which the trigger will be armed (0 = immediatly) 
	 * @param delay delay in samples to wait in between match and fire
	 * @param startCapture if <code>true</code> that capture when trigger fires, otherwise only triggel level will increase
	 */
	public void setParallelTrigger(int stage, int mask, int value, int level, int delay, boolean startCapture) {
		if (!demux) { // TODO: demux modification should be done on the fly in run() and not with stored properties
			triggerMask[stage] = mask;
			triggerValue[stage] = value;
		} else {
			triggerMask[stage] = mask & 0xffff;
			triggerValue[stage] = value & 0xffff;
			triggerMask[stage] |= triggerMask[stage] << 16;
			triggerValue[stage] |= triggerValue[stage] << 16;
		}
		triggerConfig[stage] = 0;
		triggerConfig[stage] |= delay & TRIGGER_DELAYMASK;
		triggerConfig[stage] |= (level << 16) & TRIGGER_LEVELMASK;
		if (startCapture)
			triggerConfig[stage] |= TRIGGER_CAPTURE;
	}

	/**
	 * Configures the given trigger stage in serial mode. Currenty the trigger has four stages (0-3).
	 * <p>
	 * In mask and value each bit of the integer parameters represents one sample.
	 * The LSB represents the oldest sample not yet shifted out, the MSB the most recent.
	 * (The trigger compares to a 32bit shift register that is shifted by one for each sample.)
	 * <p>
	 * When a trigger fires, the trigger level will rise by one.
	 * Initially the trigger level is 0.
	 * 
	 * @param stage trigger stage to write mask und value to
	 * @param channel channel to attach trigger to
	 * @param mask bit map defining which channels to watch
	 * @param value bit map defining what value to wait for on watched channels
	 * @param level trigger level at which the trigger will be armed (0 = immediatly) 
	 * @param delay delay in samples to wait in between match and fire
	 * @param startCapture if <code>true</code> that capture when trigger fires, otherwise only triggel level will increase
	 */
	public void setSerialTrigger(int stage, int channel, int mask, int value, int level, int delay, boolean startCapture) {
		if (!demux) { // TODO: demux modification should be done on the fly in run() and not with stored properties
			triggerMask[stage] = mask;
			triggerValue[stage] = value;
		} else {
			triggerMask[stage] = mask & 0xffff;
			triggerValue[stage] = value & 0xffff;
			triggerMask[stage] |= triggerMask[stage] << 16;
			triggerValue[stage] |= triggerValue[stage] << 16;
		}
		triggerConfig[stage] = 0;
		triggerConfig[stage] |= delay & TRIGGER_DELAYMASK;
		triggerConfig[stage] |= (level << 16) & TRIGGER_LEVELMASK;
		triggerConfig[stage] |= (channel << 20) & TRIGGER_CHANNELMASK;
		triggerConfig[stage] |= TRIGGER_SERIAL;
		if (startCapture)
			triggerConfig[stage] |= TRIGGER_CAPTURE;
	}

	/**
	 * Sets wheter or not to enable the trigger.
	 * @param enable <code>true</code> enables the trigger, <code>false</code> disables it.
	 */
	public void setTriggerEnabled(boolean enable) {
		triggerEnabled = enable;
	}

	/**
	 * Sets wheter or not to enable the noise filter.
	 * @param enable <code>true</code> enables the noise filter, <code>false</code> disables it.
	 */
	public void setFilterEnabled(boolean enable) {
		filterEnabled = enable;
	}
	
	/**
	 * Set enabled channels.
	 * @param mask bit map defining enabled channels
	 */
	public void setEnabledChannels(int mask) {
		enabledChannels = mask;
		// determine enabled groups
		for (int i = 0; i < 4; i++)
			enabledGroups[i] = ((enabledChannels  >> (8 * i)) & 0xff) > 0;
	}

	/**
	 * Sets the clock source to use.
	 * @param source can be any CLOCK_ property of this class
	 */
	public void setClockSource(int source) {
		clockSource = source;
	}

	/**
	 * Get the maximum sampling rate available.
	 * @return maximum sampling rate
	 */
	public int getMaximumRate() {
		return (2 * CLOCK);
	}

	/**
	 * Returns the current trigger mask.
	 * @param stage trigger stage to read mask from
	 * @return current trigger mask
	 */
	public int getTriggerMask(int stage) {
		return (triggerMask[stage]);
	}

	/**
	 * Returns the current trigger value.
	 * @param stage trigger stage to read value from
	 * @return current trigger value
	 */
	public int getTriggerValue(int stage) {
		return (triggerValue[stage]);
	}

	/**
	 * Returns the current clock source.
	 * @return the clock source currently used as defined by the CLOCK_ properties
	 */
	public int getClockSource() {
		return (clockSource);
	}
	
	/**
	 * Returns the currently enabled channels.
	 * @return bitmask with enabled channels represented as 1
	 */
	public int getEnabledChannels() {
		return (enabledChannels);
	}

	/**
	 * Returns wether or not the trigger is enabled.
	 * @return <code>true</code> when trigger is enabled, <code>false</code> otherwise
	 */
	public boolean isTriggerEnabled() {
		return (triggerEnabled);
	}

	/**
	 * Returns wether or not the noise filter is enabled.
	 * @return <code>true</code> when noise filter is enabled, <code>false</code> otherwise
	 */
	public boolean isFilterEnabled() {
		return (filterEnabled);
	}

	/**
	 * Returns wether or not the noise filter can be used in the current configuration.
	 * @return <code>true</code> when noise filter is available, <code>false</code> otherwise
	 */
	public boolean isFilterAvailable() {
		return (!demux && clockSource == CLOCK_INTERNAL);
	}

	/**
	 * Returns the number of available trigger stages.
	 * @return number of available trigger stages
	 */
	public int getTriggerStageCount() {
		return (TRIGGER_STAGES);
	}

	/**
	 * Returns the number of available channels in current configuration.
	 * @return number of available channels
	 */
	public int getAvailableChannelCount() {
		if (demux && clockSource == CLOCK_INTERNAL)
			return (16);
		else
			return (32);
	}

	/**
	 * Returns wether or not the device is currently running.
	 * It is running, when another thread is inside the run() method reading data from the serial port.
	 * @return <code>true</code> when running, <code>false</code> otherwise
	 */
	public boolean isRunning() {
		return (running);
	}

	/**
	 * Returns the percentage of the expected data that has already been read.
	 * The return value is only valid when <code>isRunning()</code> returns <code>true</code>. 
	 * @return percentage already read (0-100)
	 */
	public int getPercentage() {
		return (percentageDone);
	}

	/**
	 * Gets a string array containing the names all available serial ports.
	 * @return array containing serial port names
	 */
	static public String[] getPorts() {
		Enumeration portIdentifiers = CommPortIdentifier.getPortIdentifiers();
		LinkedList portList = new LinkedList();
		CommPortIdentifier portId = null;

		while (portIdentifiers.hasMoreElements()) {
			portId = (CommPortIdentifier) portIdentifiers.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				portList.addLast(portId.getName());
				System.out.println(portId.getName());
			}
		}
			
		return ((String[])portList.toArray(new String[1]));
	}

	/**
	 * Attaches the given serial port to the device object.
	 * The method will try to open the port.
	 * <p>
	 * A return value of <code>true</code> does not guarantee that a
	 * logic analyzer is actually attached to the port.
	 * <p>
	 * If the device is already attached to a port this port will be
	 * detached automatically. It is therefore not necessary to manually
	 * call <code>detach()</code> before reattaching.
	 *
	 * @param portName		the name of the port to open
	 * @param portRate		transfer rate to use (bps)
	 * @return				<code>true</code> when the port has been assigned successfully;
	 * 						<code>false</code> otherwise.
	 */
	public boolean attach(String portName, int portRate) {
		Enumeration portList = CommPortIdentifier.getPortIdentifiers();
		CommPortIdentifier portId = null;
		boolean found = false;

		System.out.println("Attaching to: " + portName + " (" + portRate + "bps)");		

		try {
			detach();
	
			while (!found && portList.hasMoreElements()) {
				portId = (CommPortIdentifier) portList.nextElement();
	
				if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
					if (portId.getName().equals(portName)) {
						found = true;
					}
				}
			}
			
			if (found) {
				port = (SerialPort) portId.open("Logic Analyzer Client", 1000);
				
				port.setSerialPortParams(
					portRate,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE
				);
				port.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN);
				port.disableReceiveFraming();
				port.enableReceiveTimeout(100);

				outputStream = port.getOutputStream();
				inputStream = port.getInputStream();
			}
		} catch(Exception E) {
			E.printStackTrace(System.out);
			return (false);
		}		
		return (found);
	}
	
	/**
	 * Detaches the currently attached port, if one exists.
	 * This will close the serial port.
	 *
	 */
	public void detach() {
		if (port != null) {
			try {
				// try to make sure device is reset (see run() for loop explanation)
				if (outputStream != null) {
					for (int i = 0; i < 5; i++)
						sendCommand(RESET);
					outputStream.close();
				}
				if (inputStream != null)
					inputStream.close();
			} catch (IOException e) { /* don't care */ }
			port.close();
		}
	}
	
	/**
	 * Sends a long command to the given stream.
	 * 
	 * @param opcode	one byte operation code
	 * @param data		four byte data portion
	 * @throws IOException if writing to stream fails
	 */
	private void sendCommand(int opcode, int data) throws IOException {
		byte[] raw = new byte[5];
		int mask = 0xff;
		int shift = 0;
		
		raw[0] = (byte)opcode;
		for (int i = 1; i < 5; i++) {
			raw[i] = (byte)((data & mask) >> shift);
			mask = mask << 8;
			shift += 8;
		}

		String debugCmd = "";
		for (int j = 0; j < 5; j++) {
			for (int i = 7; i >= 0; i--) {
				if ((raw[j] & (1 << i)) != 0)
					debugCmd += "1";
				else
					debugCmd += "0";
			}
			debugCmd += " ";
		}
		System.out.println(debugCmd);
		
		outputStream.write(raw);
	}
	
	/**
	 * Sends a short command to the given stream.
	 * 
	 * This method is intended to be used for short commands, but can also be called
	 * with long command opcodes if the data portion is to be set to 0.
	 * 
	 * @param opcode	one byte operation code
	 * @throws IOException if writing to stream fails
	 */
	private void sendCommand(int opcode) throws IOException {
		byte raw = (byte)opcode;
		
//		String debugCmd = "";
//		for (int i = 7; i >= 0; i--) {
//			if ((raw & (1 << i)) != 0)
//				debugCmd += "1";
//			else
//				debugCmd += "0";
//		}
//		System.out.println(debugCmd);

		outputStream.write(raw);
	}

	/**
	 * Reads <code>channels</code> / 8 bytes from stream and compiles them into a single integer.
	 * 
	 * @param channels number of channels to read (must be multiple of 8)
	 * @return	integer containing four bytes read
	 * @throws IOException if stream reading fails
	 */
	private int readSample(int channels) throws IOException, InterruptedException {
		int v, value = 0;

		for (int i = 0; i < channels / 8; i++) {
			if (enabledGroups[i]) {
				v = inputStream.read();
			} else {
				v = 0;
			}
			if (v < 0 || Thread.interrupted())
				throw new InterruptedException("Data readout interrupted.");
			value |= v << (8 * i);
		}

		return (value);
	}

	/**
	 * Reads a integer (32bits) from stream and compiles them into a single integer.
	 * 
	 * @param channels number of channels to read (must be multiple of 8)
	 * @return	integer containing four bytes read
	 * @throws IOException if stream reading fails
	 */
	private int readInteger() throws IOException, InterruptedException {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int v = inputStream.read();
			if (v < 0 || Thread.interrupted())
				throw new InterruptedException("Data readout interrupted.");
			value |= v << (8 * i);
		}

		return (value);
	}

	/**
	 * Sends the configuration to the device, starts it, reads the captured data
	 * and returns a CapturedData object containing the data read as well as device configuration information.
	 * @return captured data
	 * @throws IOException when writing to or reading from device fails
	 * @throws InterruptedException if a read time out occurs after trigger match or stop() was called before trigger match
	 */
	public CapturedData run() throws IOException, InterruptedException {
		
		running = true;

		// send reset 5 times because in worst case first 4 are interpreted as data of long command
		for (int i = 0; i < 5; i++)
			sendCommand(RESET);
		
		// check if device is ready
		sendCommand(ID);
		int id = 0;
		try {
			id = readInteger();
		} catch (Exception e) { /* don't care */ }
		System.out.println("Device ID: 0x" + Integer.toHexString(id));
		if (id == 0x534c4130) { // SLA0
			throw new IOException("Device is obsolete. Please upgrade Firmware.");
		} else if (id != 0x534c4131) { // SLA1
			throw new IOException("Device not found.");
		}
		
		// configure device
		int stopCounter = (int)(size * ratio);
		int readCounter = size;
		int effectiveStopCounter;
		if (triggerEnabled) {
			for (int i = 0; i < TRIGGER_STAGES; i++) {
				sendCommand(SETTRIGMASK + 4 * i, triggerMask[i]);
				sendCommand(SETTRIGVAL + 4 * i, triggerValue[i]);
				sendCommand(SETTRIGCFG + 4 * i, triggerConfig[i]);
			}
			effectiveStopCounter = stopCounter;
		} else {
			sendCommand(SETTRIGMASK, 0);
			sendCommand(SETTRIGVAL, 0);
			sendCommand(SETTRIGCFG, TRIGGER_CAPTURE);
			effectiveStopCounter = readCounter;
		}
		sendCommand(SETDIVIDER, divider);

		int flags = 0;
		if (clockSource == CLOCK_EXTERNAL_RISING || clockSource == CLOCK_EXTERNAL_FALLING) {
			flags |= FLAG_EXTERNAL;
			if (clockSource == CLOCK_EXTERNAL_FALLING)
				flags |= FLAG_INVERTED;
		}
		if (demux && clockSource == CLOCK_INTERNAL) {
			flags |= FLAG_DEMUX;
			for (int i = 0; i < 2; i++)
				if (!enabledGroups[i]) {
					flags |= FLAG_DISABLE_G0 << i;
					flags |= FLAG_DISABLE_G2 << i;
				}
			sendCommand(SETSIZE, (((effectiveStopCounter - 8) & 0x7fff8) << 13) | (((readCounter & 0x7fff8) >> 3) - 1));
		} else {
			if (filterEnabled && isFilterAvailable())
				flags |= FLAG_FILTER;
			for (int i = 0; i < 4; i++)
				if (!enabledGroups[i])
					flags |= FLAG_DISABLE_G0 << i;
			sendCommand(SETSIZE, (((effectiveStopCounter - 4) & 0x3fffc) << 14) | (((readCounter & 0x3fffc) >> 2) - 1));
		}
		System.out.println("Flags: " + Integer.toString(flags, 2));
		sendCommand(SETFLAGS, flags);
		sendCommand(RUN);

		// check if data needs to be multiplexed
		int channels;
		int samples;
		if (demux && clockSource == CLOCK_INTERNAL) {
			channels = 16;
			samples = (readCounter & 0xffff8);
		} else {
			channels = 32;
			samples = (readCounter & 0xffffc);
		}

		int[] buffer = new int[samples];

		// wait for first byte forever (trigger could cause long delay)
		for (boolean wait = true; wait == true;) {
			try {
				buffer[samples - 1] =  readSample(channels);
				wait = false;
			} catch (InterruptedException e) {
				if (!running) {
					percentageDone = -1;
					throw e;
				}
			}
		}
		
		// read all other samples
		try {
			for (int i = samples - 2; i >= 0 && true; i--) {
				buffer[i] = readSample(channels);
				percentageDone = 100 - (100 * i) / buffer.length;
			}
		} finally {
			percentageDone = -1;
		}
		
		// collect additional information for CapturedData
		int pos = CapturedData.NOT_AVAILABLE;
		if (triggerEnabled)
			pos = readCounter - stopCounter - 3 - (4 / (divider + 1)) - (demux ? 5 : 0);
		int rate = CapturedData.NOT_AVAILABLE;
		if (clockSource == CLOCK_INTERNAL)
			rate = demux ? 2*CLOCK / (divider + 1) : CLOCK / (divider + 1);

		return (new CapturedData(buffer, pos, rate, channels, enabledChannels));
	}
	
	/**
	 * Informs the thread in run() that it is supposed to stop reading data and return.
	 *
	 */
	public void stop() {
		running = false;
	}
	
	private SerialPort port;
	private InputStream inputStream;
	private OutputStream outputStream;
	
	private boolean running;
	private int percentageDone;
	
	private int clockSource;
	private boolean demux;
	private boolean filterEnabled;
	private boolean triggerEnabled;
	private int triggerMask[];
	private int triggerValue[];
	private int triggerConfig[];
	private int enabledChannels;
	private boolean enabledGroups[];
	
	private int divider;
	private int size;
	private double ratio;
}
