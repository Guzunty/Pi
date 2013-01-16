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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * CapturedData encapsulates the data obtained by the analyzer during a single run.
 * It also provides a method for saving the data to a file.
 * <p>
 * Data files will start with a header containing meta data marked by lines starting with ";".
 * The actual readout values will follow after the header. A value is a
 * single logic level measurement of all channels at a particular time.
 * This means a value is 32bits long. The value is encoded in hex and
 * each value is followed by a new line.
 * <p>
 * In the java code each value is represented by an integer.
 * 
 * @version 0.7
 * @author Michael "Mr. Sump" Poppitz
 *
 */
public class CapturedData extends Object {
	/** indicates that rate or trigger position are not available */
	public final static int NOT_AVAILABLE = -1;

	/**
	 * Constructs CapturedData based on the given data.
	 * 
	 * @param values 32bit values as read from device
	 * @param triggerPosition position of trigger as index of values array
	 * @param rate sampling rate (may be set to <code>NOT_AVAILABLE</code>)
	 * @param channels number of used channels
	 * @param enabledChannels bit mask identifying used channels
	 */
	public CapturedData(int[] values, int triggerPosition, int rate, int channels, int enabledChannels) {
		this.values = values;
		this.triggerPosition = triggerPosition;
		this.rate = rate;
		this.channels = channels;
		this.enabledChannels = enabledChannels;
	}

	/**
	 * Constructs CapturedData based on the data read from the given file.
	 * 
	 * @param file			file to read captured data from
	 * @throws IOException when reading from file failes
	 */
	public CapturedData(File file) throws IOException {
		int size = 0, r = -1, t = -1, channels = 32, enabledChannels = -1;
		String line;
		BufferedReader br = new BufferedReader(new FileReader(file));
		do {
			line = br.readLine();
			if (line == null)
				throw new IOException("File appears to be corrupted.");
			else if (line.startsWith(";Size: "))
				size = Integer.parseInt(line.substring(7));
			else if (line.startsWith(";Rate: "))
				r = Integer.parseInt(line.substring(7));
			else if (line.startsWith(";Channels: "))
				channels = Integer.parseInt(line.substring(11));
			else if (line.startsWith(";TriggerPosition: "))
				t = Integer.parseInt(line.substring(18));
			else if (line.startsWith(";EnabledChannels: "))
				enabledChannels = Integer.parseInt(line.substring(18));
		} while (line.startsWith(";"));

		if (size <= 0 || size > 1024 * 256)
			throw new IOException("Invalid size encountered.");
			
		values = new int[size];
		try {
			for (int i = 0; i < values.length && line != null; i++) {
				// TODO: modify to work with all channel counts up to 32
				if (channels > 16) {
					values[i] =
						Integer.parseInt(line.substring(0, 4), 16) << 16
						| Integer.parseInt(line.substring(4, 8), 16);
				} else {
					values[i] = Integer.parseInt(line.substring(0, 4), 16);					
				}
				line = br.readLine();
			}
		} catch (NumberFormatException E) {
			throw new IOException("Invalid data encountered.");
		}

		this.triggerPosition = t;
		this.rate = r;
		this.channels = channels;
		this.enabledChannels = enabledChannels;

		br.close();
	}
	
	/**
	 * Writes device data to given file.
	 * 
	 * @param file			file to write to
	 * @throws IOException when writing to file failes
	 */
	public void writeToFile(File file) throws IOException  {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			
			bw.write(";Size: " + values.length);
			bw.newLine();
			bw.write(";Rate: " + rate);
			bw.newLine();
			bw.write(";Channels: " + channels);
			bw.newLine();
			bw.write(";EnabledChannels: " + enabledChannels);
			bw.newLine();
			if (triggerPosition >= 0) {
				bw.write(";TriggerPosition: " + triggerPosition);
				bw.newLine();
			}
			
			for (int i = 0; i < values.length; i++) {
				String hexVal = Integer.toHexString(values[i]);
				bw.write("00000000".substring(hexVal.length()) + hexVal);
				bw.newLine();
			}
			bw.close();
		} catch (Exception E) {
			E.printStackTrace(System.out);
		}
	}

	/**
	 * Returns wether or not the object contains timing data
	 * @return <code>true</code> when timing data is available
	 */
	public boolean hasTimingData() {
		return (rate != NOT_AVAILABLE);
	}

	/**
	 * Returns wether or not the object contains trigger data
	 * @return <code>true</code> when trigger data is available
	 */
	public boolean hasTriggerData() {
		return (triggerPosition != NOT_AVAILABLE);
	}

	/** captured values */
	public final int[] values;
	/** position of trigger as index of values */
	public final int triggerPosition;
	/** sampling rate in Hz */
	public final int rate;
	/** number of channels (1-32) */
	public final int channels;
	/** bit map of enabled channels */
	public final int enabledChannels;
}
