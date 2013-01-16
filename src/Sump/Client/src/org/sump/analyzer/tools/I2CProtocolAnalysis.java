/*
 *  Copyright (C) 2007 Frank Kunz
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
package org.sump.analyzer.tools;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

import org.sump.analyzer.CapturedData;
import org.sump.analyzer.Configurable;
import org.sump.util.Properties;

/**
 * @author frank
 *
 */
public class I2CProtocolAnalysis extends Base implements Tool, Configurable {

	/**
	 * create constraints for GridBagLayout
	 * @param x x grid position
	 * @param y y grid position
	 * @param w grid width
	 * @param h grid height
	 * @param wx weighting for extra horizontal space
	 * @param wy weighting for extra vertical space
	 * @return constraints object
	 */
	private static GridBagConstraints createConstraints(int x, int y, int w, int h, double wx, double wy) {
	 	GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.gridx = x; gbc.gridy = y;
		gbc.gridwidth = w; gbc.gridheight = h;
		gbc.weightx = wx; gbc.weighty = wy;
	    return (gbc);
	}

	/**
	 * Class for I2C dataset
	 * @author Frank Kunz
	 *
	 * An I2C dataset consists of a timestamp, a value, or it can have
	 * an I2C event. This class is used to store the decoded I2C data in a Vector.
	 */
	private class I2CProtocolAnalysisDataSet {
		public I2CProtocolAnalysisDataSet (int tm, int val) {
			this.time = tm;
			this.value = val;
			this.event = null;
		}
		
		public I2CProtocolAnalysisDataSet (int tm, String ev) {
			this.time = tm;
			this.value = 0;
			this.event = new String(ev);
		}

		public boolean isEvent() {
			return (event != null);
		}
		
		public int time;
		public int value;
		public String event;
	}
	
	/**
	 * The Dialog Class
	 * @author Frank Kunz
	 *
	 * The dialog class draws the basic dialog with a grid layout. The dialog
	 * consists of three main parts. A settings panel, a table panel
	 * and three buttons.
	 */
	private class I2CProtocolAnalysisDialog extends JDialog implements ActionListener {
		public I2CProtocolAnalysisDialog(Frame frame, String name) {
			super(frame, name, true);
			setLayout(new GridBagLayout());
			getRootPane().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			decodedData = new Vector();
			startOfDecode = 0;
			
			/*
			 * add protocol settings elements
			 */
			JPanel panSettings = new JPanel();
			panSettings.setLayout(new GridLayout(6,2,5,5));
			panSettings.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Settings"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			
			String channels[] = new String[32];
			for (int i = 0; i < 32; i++)
				channels[i] = new String("Channel " + i);

			panSettings.add(new JLabel("Line A"));
			lineA = new JComboBox(channels);
			panSettings.add(lineA);
			
			panSettings.add(new JLabel("Line B"));
			lineB = new JComboBox(channels);
			panSettings.add(lineB);

			detectSTART = new JCheckBox("Show START", true);
			panSettings.add(detectSTART);
			panSettings.add(new JLabel(""));

			detectSTOP = new JCheckBox("Show STOP", true);
			panSettings.add(detectSTOP);
			panSettings.add(new JLabel(""));

			detectACK = new JCheckBox("Show ACK", true);
			panSettings.add(detectACK);
			panSettings.add(new JLabel(""));

			detectNACK = new JCheckBox("Show NACK", true);
			panSettings.add(detectNACK);
			panSettings.add(new JLabel(""));

			add(panSettings, createConstraints(0, 0, 1, 1, 0, 0));
			
			/*
			 * add bus configuration panel
			 */
			JPanel panBusConfig = new JPanel();
			panBusConfig.setLayout(new GridLayout(2,2,5,5));
			panBusConfig.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Bus Configuration"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			
			panBusConfig.add(new JLabel("SCL :"));
			busSetSCL = new JLabel("<autodetect>");
			panBusConfig.add(busSetSCL);
			panBusConfig.add(new JLabel("SDA :"));
			busSetSDA = new JLabel("<autodetect>");
			panBusConfig.add(busSetSDA);
			
			add(panBusConfig, createConstraints(0, 1, 1, 1, 0, 0));

			/*
			 * add an empty output view
			 */
			JPanel panTable = new JPanel();
			panTable.setLayout(new GridLayout(1, 1, 5, 5));
			panTable.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Results"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));

			//panTable.setSize(800, 600);
			outText = new JEditorPane("text/html", toHtmlPage(true));
			outText.setMargin(new Insets(5,5,5,5));
			panTable.add(new JScrollPane(outText));
			
			add(panTable, createConstraints(1, 0, 3, 3, 1.0, 1.0));
			
			/*
			 * add buttons
			 */
			JPanel panButton = new JPanel();
			//panButton.setLayout(new GridLayout(3,1,5,5));
			JButton convert = new JButton("Analyze");
			convert.addActionListener(this);
			panButton.add(convert);
			JButton export = new JButton("Export");
			export.addActionListener(this);
			panButton.add(export);
			JButton cancel = new JButton("Close");
			cancel.addActionListener(this);
			panButton.add(cancel);
			add(panButton, createConstraints(3, 3, 1, 1, 0, 0));
			
			
			fileChooser = new JFileChooser();
			fileChooser.addChoosableFileFilter((FileFilter) new CSVFilter());
			fileChooser.addChoosableFileFilter((FileFilter) new HTMLFilter());

			//pack();
			setSize(900, 500);
			setResizable(false);
		}

		/**
		 * shows the dialog and sets the data to use
		 * @param data data to use for analysis
		 */
		public void showDialog(CapturedData data) {
			analysisData = data;
			setVisible(true);
		}
		
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("Analyze")) {
				decode();
			} else if (e.getActionCommand().equals("Close")) {
				setVisible(false);
			} else if (e.getActionCommand().equals("Export")) {
				if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					if(fileChooser.getFileFilter().getDescription().equals("Website (*.html)")) {
						storeToHtmlFile(file);
					} else {
						storeToCsvFile(file);
					}
				}
			}
		}

		/**
		 * This is the I2C protocol decoder core
		 *
		 * The decoder scans for a decode start event like CS high to
		 * low edge or the trigger of the captured data. After this the
		 * decoder starts to decode the data by the selected mode, number
		 * of bits and bit order. The decoded data are put to a JTable
		 * object directly.
		 */
		private void decode() {
			// process the captured data and write to output
			int a,b,c,d;
			int sdaValue;
			int sdaMask, sclMask;
						
			// clear old data
			decodedData.clear();
			sdaMask = 0;
			sclMask = 0;
			statBusErrorCount = 0;
			statDecodedBytes = 0;
			
			/*
			 * Buid bitmasks based on the SCK, MISO, MOSI and CS
			 * pins.
			 */
			int lineAmask = (1 << lineA.getSelectedIndex());
			int lineBmask = (1 << lineB.getSelectedIndex());
			
			System.out.println("lineAmask = 0x" + Integer.toHexString(lineAmask));
			System.out.println("lineBmask = 0x" + Integer.toHexString(lineBmask));
			
			
			/*
			 * first of all scan both lines until they are high (IDLE), then
			 * the first line that goes low is the SDA line (START condition).
			 */
			for(a = 0; a < analysisData.values.length; a++)
			{
				if((analysisData.values[a] & (lineAmask | lineBmask)) == (lineAmask | lineBmask))
				{
					// IDLE found here
					break;
				}
			}
			if(a == analysisData.values.length)
			{
				// no idle state could be found
				return;
			}
			// a is now the start of idle, now find the first start condition
			for(; a < analysisData.values.length; a++)
			{
				if(((analysisData.values[a] & (lineAmask | lineBmask)) != (lineAmask | lineBmask)) &&
						((analysisData.values[a] & (lineAmask | lineBmask)) != 0))
				{
					// one line is low
					if((analysisData.values[a] & lineAmask) == 0)
					{
						// lineA is low and lineB is high here: lineA = SDA, lineB = SCL
						sdaMask = lineAmask;
						sclMask = lineBmask;
						
						busSetSCL.setText((String)lineB.getSelectedItem());
						busSetSDA.setText((String)lineA.getSelectedItem());
					}
					else
					{
						// lineB is low and lineA is high here: lineA = SCL, lineB = SDA
						sdaMask = lineBmask;
						sclMask = lineAmask;

						busSetSCL.setText((String)lineA.getSelectedItem());
						busSetSDA.setText((String)lineB.getSelectedItem());
					}
					break;
				}
			}
			if(a == analysisData.values.length)
			{
				// no start condition could be found
				return;
			}
			
			/*
			 * now it is clear what is SCL (sclMask) and what is SDA (sdaMask).
			 * Variable a points to the start condition. 
			 */
			if(detectSTART.isSelected()) {
				decodedData.addElement(new I2CProtocolAnalysisDataSet(a, "START"));
			}
			startOfDecode = a;
			
			/*
			 * Now decode the bytes, SDA may only change when SCL is low. Otherwise
			 * it may be a repeated start condition or stop condition. If the start/stop
			 * condition is not at a byte boundary a bus error is detected. So we have to
			 * scan for SCL rises and for SDA changes during SCL is high.
			 * Each byte is followed by a 9th bit (ACK/NACK).
			 */
			b = analysisData.values[a] & sclMask;
			c = analysisData.values[a] & sdaMask;
			d = 8;
			sdaValue = 0;
			while(a < analysisData.values.length-1)
			{
				a++;
				
				// detect SCL rise
				if((analysisData.values[a] & sclMask) > b)
				{
					// SCL rises
					if((analysisData.values[a] & sdaMask) != c)
					{
						// SDA changes too, bus error
						decodedData.addElement(new I2CProtocolAnalysisDataSet(a, "BUS-ERROR"));
						statBusErrorCount++;
					}
					else
					{
						// read SDA
						if(d == 0)
						{
							// read the ACK/NACK state
							if((analysisData.values[a] & sdaMask) != 0)
							{
								// NACK
								if(detectNACK.isSelected()) {
									decodedData.addElement(new I2CProtocolAnalysisDataSet(a, "NACK"));
								}
							}
							else
							{
								// ACK
								if(detectACK.isSelected()) {
									decodedData.addElement(new I2CProtocolAnalysisDataSet(a, "ACK"));
								}
							}
							// next byte
							d = 8;
						}
						else
						{
							d--;
							if((analysisData.values[a] & sdaMask) != 0)
							{
								sdaValue |= (1 << d);
							}
							if(d == 0)
							{
								// store decoded byte
								decodedData.addElement(new I2CProtocolAnalysisDataSet(a,sdaValue));
								sdaValue = 0;
								statDecodedBytes++;
							}
						}
					}
				}
				
				// detect SDA change when SCL high
				if(((analysisData.values[a] & sclMask) == sclMask) && ((analysisData.values[a] & sdaMask) != c))
				{
					// SDA changes here
					if(d < 7)
					{
						// bus error, no complete byte detected
						decodedData.addElement(new I2CProtocolAnalysisDataSet(a, "BUS-ERROR"));
						statBusErrorCount++;
					}
					else
					{
						if((analysisData.values[a] & sdaMask) > c)
						{
							// SDA rises, this is a stop condition
							if(detectSTOP.isSelected()) {
								decodedData.addElement(new I2CProtocolAnalysisDataSet(a, "STOP"));
							}
						}
						else
						{
							// SDA falls, this is a start condition
							if(detectSTART.isSelected()) {
								decodedData.addElement(new I2CProtocolAnalysisDataSet(a, "START"));
							}
						}
						// new byte
						d = 8;
					}
				}
				
				b = analysisData.values[a] & sclMask;
				c = analysisData.values[a] & sdaMask;
			}

			outText.setText(toHtmlPage(false));
			outText.setEditable(false);
		}
		
		/**
		 * generate a HTML page
		 * @param empty if this is true an empty output is generated
		 * @return String with HTML data
		 */
		private String toHtmlPage(boolean empty) {
			
			// generate html page header
			String header =
				"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">" +
				"<html>" +
				"  <head>" +
				"    <title></title>" +
				"    <meta content=\"\">" +
				"    <style>" +
				"			th { text-align:left;font-style:italic;font-weight:bold;font-size:medium;font-family:sans-serif;background-color:#C0C0FF; }" +
				"		</style>" +
				"  </head>" +
				"	<body>" +
				"		<H2>I2C Analysis Results</H2>" +
				"		<hr>" +
				"			<div style=\"text-align:right;font-size:x-small;\">07.02.2007, iec_dat.sla</div>" +
				"		<br>";

			// generate the statistics table
			String stats = 
				"<table style=\"width:100%;\">";
			if(empty) {
				stats +=
					"<TR><TD style=\"width:30%;\">Decoded Bytes</TD><TD>-</TD></TR>" +
					"<TR><TD style=\"width:30%;\">Detected Bus Errors</TD><TD>-</TD></TR>";
			} else {
				stats +=
					"<TR><TD style=\"width:30%;\">Decoded Bytes</TD><TD>" + statDecodedBytes + "</TD></TR>" +
					"<TR><TD style=\"width:30%;\">Detected Bus Errors</TD><TD>" + statBusErrorCount + "</TD></TR>";
			}
			stats +=
				"</table>" +
				"<br>" +
				"<br>";
			
			// generate the data table
			String data =
				"<table style=\"font-family:monospace;width:100%;\">" +
				"<tr><th style=\"width:15%;\">Index</th><th style=\"width:15%;\">Time</th><th style=\"width:20%;\">Hex</th><th style=\"width:20%;\">Bin</th><th style=\"width:20%;\">Dec</th><th style=\"width:10%;\">ASCII</th></tr>";
			if(empty) {
			} else {
					I2CProtocolAnalysisDataSet ds;
					for (int i = 0; i < decodedData.size(); i++) {
						ds = (I2CProtocolAnalysisDataSet)decodedData.get(i);
						if(ds.isEvent()) {
							// this is an event
							if(ds.event.equals("START")) {
								// start condition
								data += 
									"<tr style=\"background-color:#E0E0E0;\"><td>" +
									i +
									"</td><td>" +
									indexToTime(ds.time) +
									"</td><td>START</td><td></td><td></td><td></td></tr>";
							} else if(ds.event.equals("STOP")) {
								// stop condition
								data += 
									"<tr style=\"background-color:#E0E0E0;\"><td>" +
									i +
									"</td><td>" +
									indexToTime(ds.time) +
									"</td><td>STOP</td><td></td><td></td><td></td></tr>";
							} else if(ds.event.equals("ACK")) {
								// acknowledge
								data += 
									"<tr style=\"background-color:#C0FFC0;\"><td>" +
									i +
									"</td><td>" +
									indexToTime(ds.time) +
									"</td><td>ACK</td><td></td><td></td><td></td></tr>";
							} else if(ds.event.equals("NACK")) {
								// no acknowledge
								data += 
									"<tr style=\"background-color:#FFC0C0;\"><td>" +
									i +
									"</td><td>" +
									indexToTime(ds.time) +
									"</td><td>NACK</td><td></td><td></td><td></td></tr>";
							} else if(ds.event.equals("BUS-ERROR")) {
								// bus error
								data += 
									"<tr style=\"background-color:#FF8000;\"><td>" +
									i +
									"</td><td>" +
									indexToTime(ds.time) +
									"</td><td>BUS-ERROR</td><td></td><td></td><td></td></tr>";
							} else {
								// unknown event
								data += 
									"<tr style=\"background-color:#FF8000;\"><td>" +
									i +
									"</td><td>" +
									indexToTime(ds.time) +
									"</td><td>UNKNOWN</td><td></td><td></td><td></td></tr>";
							}
						} else {
							data +=
								"<tr style=\"background-color:#FFFFFF;\"><td>" +
								i +
								"</td><td>" +
								indexToTime(ds.time) +
								"</td><td>" +
								"0x" + integerToHexString(ds.value, 2) +
								"</td><td>" +
								"0b" + integerToBinString(ds.value, 8) +
								"</td><td>" +
								ds.value +
								"</td><td>";
								if(ds.value >= 32)
									data += (char)ds.value;
								data += "</td></tr>";
						}
					}
			}
			data += "</table";

			// generate the footer table
			String footer =
			"	</body>" +
			"</html>";

			return(header + stats + data + footer);
		}
		
		/**
		 * exports the table data to a CSV file
		 * @param file File object
		 */
		private void storeToCsvFile(File file) {
			if(decodedData.size() > 0) {
				I2CProtocolAnalysisDataSet dSet;
				System.out.println("writing decoded data to " + file.getPath());
				try {
					BufferedWriter bw = new BufferedWriter(new FileWriter(file));

					for(int i = 0; i < decodedData.size(); i++) {
						dSet = (I2CProtocolAnalysisDataSet)decodedData.get(i);
						if(dSet.isEvent()) {
							bw.write("\"" + 
									i + 
									"\",\"" +
									indexToTime(dSet.time) +
									"\",\"" +
									dSet.event +
									"\"");
						} else {
							bw.write("\"" + 
									i + 
									"\",\"" +
									indexToTime(dSet.time) +
									"\",\"" +
									dSet.value +
									"\"");
						}
						bw.newLine();
					}
					bw.close();
				} catch (Exception E) {
					E.printStackTrace(System.out);
				}
			}
		}
		
		/**
		 * stores the data to a HTML file
		 * @param file file object
		 */
		private void storeToHtmlFile(File file) {
			if(decodedData.size() > 0) {
				System.out.println("writing decoded data to " + file.getPath());
				try {
					BufferedWriter bw = new BufferedWriter(new FileWriter(file));
					
					// write the complete displayed html page to file
					bw.write(outText.getText());
					
					bw.close();
				} catch (Exception E) {
					E.printStackTrace(System.out);
				}
			}
		}
		
		/**
		 * Convert sample count to time string.
		 * @param count sample count (or index)
		 * @return string containing time information
		 */
		private String indexToTime(int count) {
			count -= startOfDecode;
			if(count < 0) count = 0;
			if(analysisData.hasTimingData()) {
				float time = (float)(count * (1.0 / analysisData.rate));
				if(time < 1.0e-6) 			{return(Math.rint(time*1.0e9*100)/100 + "ns");}
				else if(time < 1.0e-3) 		{return(Math.rint(time*1.0e6*100)/100 + "µs");}
				else if(time < 1.0) 		{return(Math.rint(time*1.0e3*100)/100 + "ms");}
				else 						{return(Math.rint(time*100)/100 + "s");}
			} else {
				return("" + count);
			}
		}

		public void readProperties(Properties properties) {
			selectByIndex(lineA, properties.getProperty("tools.I2CProtocolAnalysis.lineA"));
			selectByIndex(lineB, properties.getProperty("tools.I2CProtocolAnalysis.lineB"));
		}

		public void writeProperties(Properties properties) {
			properties.setProperty("tools.I2CProtocolAnalysis.lineA", Integer.toString(lineA.getSelectedIndex()));
			properties.setProperty("tools.I2CProtocolAnalysis.lineB", Integer.toString(lineB.getSelectedIndex()));
		}
		
		/**
		 * converts an integer to a hex string with leading zeros
		 * @param val integer value for conversion
		 * @param fieldWidth number of charakters in field
		 * @return a nice string
		 */
		private String integerToHexString(int val, int fieldWidth) {
			// first build a mask to cut off the signed extension
			int mask = (int)Math.pow(16.0, (double)fieldWidth);
			mask--;
			String str = Integer.toHexString(val & mask);
			int numberOfLeadingZeros = fieldWidth - str.length();
			if(numberOfLeadingZeros < 0) numberOfLeadingZeros = 0;
			if(numberOfLeadingZeros > fieldWidth) numberOfLeadingZeros = fieldWidth;
			char zeros[] = new char[numberOfLeadingZeros];
			for(int i = 0; i < zeros.length; i++)
				zeros[i] = '0';
			String ldz = new String(zeros);
			return(new String(ldz + str));
		}

		/**
		 * converts an integer to a bin string with leading zeros
		 * @param val integer value for conversion
		 * @param fieldWidth number of charakters in field
		 * @return a nice string
		 */
		private String integerToBinString(int val, int fieldWidth) {
			// first build a mask to cut off the signed extension
			int mask = (int)Math.pow(16.0, (double)(fieldWidth/8));
			mask--;
			String str = Integer.toBinaryString(val & mask);
			int numberOfLeadingZeros = fieldWidth - str.length();
			if(numberOfLeadingZeros < 0) numberOfLeadingZeros = 0;
			if(numberOfLeadingZeros > fieldWidth) numberOfLeadingZeros = fieldWidth;
			char zeros[] = new char[numberOfLeadingZeros];
			for(int i = 0; i < zeros.length; i++)
				zeros[i] = '0';
			String ldz = new String(zeros);
			return(new String(ldz + str));
		}

		private JComboBox lineA;
		private JComboBox lineB;
		private CapturedData analysisData;
		private JEditorPane outText;
		private Vector decodedData;
		private JFileChooser fileChooser;
		private int startOfDecode;
		private JLabel busSetSCL;
		private JLabel busSetSDA;
		private JCheckBox detectSTART;
		private JCheckBox detectSTOP;
		private JCheckBox detectACK;
		private JCheckBox detectNACK;
		private int statDecodedBytes;
		private int statBusErrorCount;
		
		private static final long serialVersionUID = 1L;
	}
	
	/**
	 * Inner class defining a File Filter for CSV files. 
	 * 
	 */
	private class CSVFilter extends FileFilter {
		public boolean accept(File f) {
			return (f.isDirectory() || f.getName().toLowerCase().endsWith(".csv"));
		}
		public String getDescription() {
			return ("Character sepatated Values (*.csv)");
		}
	}

	/**
	 * Inner class defining a File Filter for HTML files. 
	 * 
	 */
	private class HTMLFilter extends FileFilter {
		public boolean accept(File f) {
			return (f.isDirectory() || f.getName().toLowerCase().endsWith(".html"));
		}
		public String getDescription() {
			return ("Website (*.html)");
		}
	}

	public I2CProtocolAnalysis () {
	}
	
	public void init(Frame frame) {
		spad = new I2CProtocolAnalysisDialog(frame, getName());
	}
	
	/**
	 * Returns the tools visible name.
	 * @return the tools visible name
	 */
	public String getName() {
		return ("I2C Protocol Analysis...");
	}

	/**
	 * Convert captured data from timing data to state data using the given channel as clock.
	 * @param data - captured data to work on
	 * @return always <code>null</code>
	 */
	public CapturedData process(CapturedData data) {
		spad.showDialog(data);
		return(null);
	}
	
	/**
	 * Reads dialog settings from given properties.
	 * @param properties Properties containing dialog settings
	 */
	public void readProperties(Properties properties) {
		spad.readProperties(properties);
	}

	/**
	 * Writes dialog settings to given properties.
	 * @param properties Properties where the settings are written to
	 */
	public void writeProperties(Properties properties) {
		spad.writeProperties(properties);
	}

	
	private I2CProtocolAnalysisDialog spad;
}
