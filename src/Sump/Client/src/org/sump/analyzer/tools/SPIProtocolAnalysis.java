/*
 *  Copyright (C) 2006 Frank Kunz
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
  
import java.awt.Container;
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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import org.sump.analyzer.CapturedData;
import org.sump.analyzer.Configurable;
import org.sump.util.Properties;

public class SPIProtocolAnalysis extends Base implements Tool, Configurable {

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
	 * Class for SPI dataset
	 * @author Frank Kunz
	 *
	 * A SPI dataset consists of a timestamp, MISO and MOSI values, or it can have
	 * an SPI event. This class is used to store the decoded SPI data in a Vector.
	 */
	private class SPIProtocolAnalysisDataSet {
		public SPIProtocolAnalysisDataSet (int tm, int mi, int mo) {
			this.time = tm;
			this.miso = mi;
			this.mosi = mo;
			this.event = null;
		}
		
		public SPIProtocolAnalysisDataSet (int tm, String ev) {
			this.time = tm;
			this.miso = 0;
			this.mosi = 0;
			this.event = new String(ev);
		}

		public boolean isEvent() {
			return (event != null);
		}
		public int time;
		public int miso;
		public int mosi;
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
	private class SPIProtocolAnalysisDialog extends JDialog implements ActionListener {
		public SPIProtocolAnalysisDialog(Frame frame, String name) {
			super(frame, name, true);
			Container pane = getContentPane();
			pane.setLayout(new GridBagLayout());
			getRootPane().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			decodedData = new Vector();
			startOfDecode = 0;
			
			/*
			 * add protocol settings elements
			 */
			JPanel panSettings = new JPanel();
			panSettings.setLayout(new GridLayout(7,2,5,5));
			panSettings.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Settings"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			
			String channels[] = new String[32];
			for (int i = 0; i < 32; i++)
				channels[i] = new String("Channel " + i);

			panSettings.add(new JLabel("SCK"));
			sck = new JComboBox(channels);
			panSettings.add(sck);
			
			panSettings.add(new JLabel("MISO"));
			miso = new JComboBox(channels);
			panSettings.add(miso);
			
			panSettings.add(new JLabel("MOSI"));
			mosi = new JComboBox(channels);
			panSettings.add(mosi);

			panSettings.add(new JLabel("/CS"));
			cs = new JComboBox(channels);
			panSettings.add(cs);

			panSettings.add(new JLabel("Mode"));
			modearray = new String[4];
			for (int i = 0; i < modearray.length; i++)
				modearray[i] = new String("" + i);
			mode = new JComboBox(modearray);
			panSettings.add(mode);
			
			panSettings.add(new JLabel("Bits"));
			bitarray = new String[13];
			for (int i = 0; i < bitarray.length; i++)
				bitarray[i] = new String("" + (i+4));
			bits = new JComboBox(bitarray);
			bits.setSelectedItem("8");
			panSettings.add(bits);

			panSettings.add(new JLabel("Order"));
			orderarray = new String[2];
			orderarray[0] = new String("MSB first");
			orderarray[1] = new String("LSB first");
			order = new JComboBox(orderarray);
			panSettings.add(order);
			pane.add(panSettings, createConstraints(0, 0, 1, 1, 0, 0));
			
			/*
			 * add an empty output view
			 */
			JPanel panTable = new JPanel();
			panTable.setLayout(new GridLayout(1, 1, 5, 5));
			panTable.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Results"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			String colData[][] = new String[1][4];
			colData[0][0] = new String();
			colData[0][1] = new String();
			colData[0][2] = new String();
			colData[0][3] = new String();
			outTable = new JTable(colData, colNames);
			panTable.add(new JScrollPane(outTable));
			add(panTable, createConstraints(1, 0, 3, 3, 1.0, 1.0));
			
			/*
			 * add buttons
			 */
			JButton convert = new JButton("Analyze");
			convert.addActionListener(this);
			add(convert, createConstraints(0, 3, 1, 1, 0.5, 0));
			JButton export = new JButton("Export");
			export.addActionListener(this);
			add(export, createConstraints(1, 3, 1, 1, 0.5, 0));
			JButton cancel = new JButton("Close");
			cancel.addActionListener(this);
			add(cancel, createConstraints(2, 3, 1, 1, 0.5, 0));
			
			fileChooser = new JFileChooser();
			fileChooser.addChoosableFileFilter((FileFilter) new CSVFilter());

			pack();
			setResizable(false);
		}

		/**
		 * shows the dialog and sets the data to use
		 * @param data data to use for analysis
		 */
		public void showDialog(CapturedData data) {
			analysisData = data;
			if (analysisData.hasTimingData()) {
				float step = 1 / analysisData.rate;
				
				unitFactor = 1;
				unitName = "s";
				if (step <= 0.000001) { unitFactor = 1000000000; unitName = "ns"; } 
				else if (step <= 0.001) { unitFactor = 1000000; unitName = "µs"; } 
				else if (step <= 1) { unitFactor = 1000; unitName = "ms"; } 
			} else {
				unitFactor = 1;
				unitName = "";
			}

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
					storeToFile(file);
				}
			}
		}

		/**
		 * This is the SPI protocol decoder core
		 *
		 * The decoder scans for a decode start event like CS high to
		 * low edge or the trigger of the captured data. After this the
		 * decoder starts to decode the data by the selected mode, number
		 * of bits and bit order. The decoded data are put to a JTable
		 * object directly.
		 */
		private void decode() {
			// process the captured data and write to output
			int a,b,c;
			int bitCount, mosivalue, misovalue, maxbits;
			
			// clear old data
			decodedData.clear();
			
			/*
			 * Buid bitmasks based on the SCK, MISO, MOSI and CS
			 * pins.
			 */
			int csmask = (1 << cs.getSelectedIndex());
			int sckmask = (1 << sck.getSelectedIndex());
			int misomask = (1 << miso.getSelectedIndex());
			int mosimask = (1 << mosi.getSelectedIndex());
			
			System.out.println("csmask   = 0x" + Integer.toHexString(csmask));
			System.out.println("sckmask  = 0x" + Integer.toHexString(sckmask));
			System.out.println("misomask = 0x" + Integer.toHexString(misomask));
			System.out.println("mosimask = 0x" + Integer.toHexString(mosimask));
			
			
			/*
			 * For analyze scan the CS line for a falling edge. If
			 * no edge could be found, the position of the trigger
			 * is used for start of analysis. If no trigger and no
			 * edge is found the analysis fails.
			 */
			a = analysisData.values[0] & csmask;
			c = 0;
			b = 0;
			for (int i = 0; i < analysisData.values.length; i++) {
				if (a > (analysisData.values[i] & csmask)) {
					// cs to low found here
					b = i;
					c = 1;
					System.out.println("CS found at " + i);
					break;
				}
				a = analysisData.values[i] & csmask;
			}
			if (c == 0)
			{
				// no CS edge found, look for trigger
				if (analysisData.hasTriggerData())
					b = analysisData.triggerPosition;
			}
			// now the trigger is in b, add trigger event to table
			decodedData.addElement(new SPIProtocolAnalysisDataSet(b, "CSLOW"));
			startOfDecode = b;
			
			/*
			 * Use the mode parameter to determine which eges are
			 * to detect. Mode 0 and mode 3 are sampling on the
			 * rising clk edge, mode 2 and 4 are sampling on the
			 * falling edge.
			 * a is used for start of value, c is register for 
			 * detect line changes.
			 */
			if ((mode.getSelectedItem().equals("0")) || (mode.getSelectedItem().equals("2"))) {
				// scanning for rising clk edges
				c = analysisData.values[b] & sckmask;
				a = analysisData.values[b] & csmask;
				bitCount = Integer.parseInt((String)bits.getSelectedItem()) - 1;
				maxbits = bitCount;
				misovalue = 0;
				mosivalue = 0;
				for (int i = b; i < analysisData.values.length; i++) {
					if(c < (analysisData.values[i] & sckmask)) {
						// sample here
						if (order.getSelectedItem().equals("MSB first")) {
							if ((analysisData.values[i] & misomask) == misomask)
								misovalue |= (1 << bitCount);
							if ((analysisData.values[i] & mosimask) == mosimask)
								mosivalue |= (1 << bitCount);
						} else {
							if ((analysisData.values[i] & misomask) == misomask)
								misovalue |= (1 << (maxbits - bitCount));
							if ((analysisData.values[i] & mosimask) == mosimask)
								mosivalue |= (1 << (maxbits - bitCount));
						}
						
						if (bitCount > 0) {
							bitCount--;
						} else {
							decodedData.addElement(new SPIProtocolAnalysisDataSet(i,mosivalue,misovalue));

							System.out.println("MISO = 0x" + Integer.toHexString(misovalue));
							System.out.println("MOSI = 0x" + Integer.toHexString(mosivalue));
							bitCount = Integer.parseInt((String)bits.getSelectedItem()) - 1;
							misovalue = 0;
							mosivalue = 0;

							/*
							 * CS edge detection is only done when a complete value is decoded
							 */
							if(a > (analysisData.values[i] & csmask)) {
								// falling edge
								decodedData.addElement(new SPIProtocolAnalysisDataSet(i,"CSLOW"));
							} else if (a < (analysisData.values[i] & csmask)) {
								// rising edge
								decodedData.addElement(new SPIProtocolAnalysisDataSet(i,"CSHIGH"));
							}
							a = analysisData.values[i] & csmask;
						}
					}
					c = analysisData.values[i] & sckmask;
				}
			} else {
				// scanning for falling clk edges
				c = analysisData.values[b] & sckmask;
				a = analysisData.values[b] & csmask;
				bitCount = Integer.parseInt((String)bits.getSelectedItem()) - 1;
				maxbits = bitCount;
				misovalue = 0;
				mosivalue = 0;
				for (int i = b; i < analysisData.values.length; i++) {
					if(c > (analysisData.values[i] & sckmask)) {
						// sample here
						if (order.getSelectedItem().equals("MSB first")) {
							if ((analysisData.values[i] & misomask) == misomask)
								misovalue |= (1 << bitCount);
							if ((analysisData.values[i] & mosimask) == mosimask)
								mosivalue |= (1 << bitCount);
						} else {
							if ((analysisData.values[i] & misomask) == misomask)
								misovalue |= (1 << (maxbits - bitCount));
							if ((analysisData.values[i] & mosimask) == mosimask)
								mosivalue |= (1 << (maxbits - bitCount));
						}

						if (bitCount > 0) {
							bitCount--;
						} else {
							decodedData.addElement(new SPIProtocolAnalysisDataSet(i,mosivalue,misovalue));

							System.out.println("MISO = 0x" + Integer.toHexString(misovalue));
							System.out.println("MOSI = 0x" + Integer.toHexString(mosivalue));
							bitCount = Integer.parseInt((String)bits.getSelectedItem()) - 1;
							misovalue = 0;
							mosivalue = 0;

							/*
							 * CS edge detection is only done when a complete value is decoded
							 */
							if(a > (analysisData.values[i] & csmask)) {
								// falling edge
								decodedData.addElement(new SPIProtocolAnalysisDataSet(i,"CSLOW"));
							} else if (a < (analysisData.values[i] & csmask)) {
								// rising edge
								decodedData.addElement(new SPIProtocolAnalysisDataSet(i,"CSHIGH"));
							}
							a = analysisData.values[i] & csmask;
						}
					}
					c = analysisData.values[i] & sckmask;
				}
			}
			outTable.setModel(toTableData());
		}
		
		/**
		 * exports the table data to a CSV file
		 * @param file File object
		 */
		private void storeToFile(File file) {
			System.out.println("writing decoded data to " + file.getPath());
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(file));
				
				for (int i = 0; i < outTable.getRowCount(); i++) {
					bw.write("\"" + 
							(String)outTable.getValueAt(i, 0) + 
							"\",\"" + 
							(String)outTable.getValueAt(i, 1) +
							"\",\"" + 
							(String)outTable.getValueAt(i, 2) +
							"\",\"" + 
							(String)outTable.getValueAt(i, 3) + 
							"\"");
					bw.newLine();
				}
				bw.close();
			} catch (Exception E) {
				E.printStackTrace(System.out);
			}
		}
		
		/**
		 * Convert sample count to time string.
		 * @param count sample count (or index)
		 * @return string containing time information
		 */
		private String indexToTime(int count) {
			return ((((count - startOfDecode) * unitFactor) / analysisData.rate) + unitName);
		}

		/**
		 * converts the analyzed data to table data
		 * @return the prepared table data
		 */
		private DefaultTableModel toTableData() {
			String data[][] = new String[decodedData.size()][4];
			SPIProtocolAnalysisDataSet ds;
			for (int i = 0; i < decodedData.size(); i++) {
				ds = (SPIProtocolAnalysisDataSet)decodedData.get(i);
				if (ds.isEvent()) {
					// index
					data[i][0] = new String("" + (i+1));
					// time
					data[i][1] = new String(indexToTime(ds.time));
					// MOSI
					data[i][2] = new String(ds.event);
					// MISO
					data[i][3] = new String(ds.event);
				} else {
					// index
					data[i][0] = new String("" + (i+1));
					// time
					data[i][1] = new String(indexToTime(ds.time));
					// MOSI
					data[i][2] = new String("0x" + Integer.toHexString(ds.mosi) + "," + ds.mosi);
					// MISO
					data[i][3] = new String("0x" + Integer.toHexString(ds.miso) + "," + ds.miso);
				}
			}
			DefaultTableModel mod = new DefaultTableModel(data, colNames);
			return(mod);
		}
		
		public void readProperties(Properties properties) {
			selectByIndex(sck, properties.getProperty("tools.SPIProtocolAnalysis.sck"));
			selectByIndex(miso, properties.getProperty("tools.SPIProtocolAnalysis.miso"));
			selectByIndex(mosi, properties.getProperty("tools.SPIProtocolAnalysis.mosi"));
			selectByIndex(cs, properties.getProperty("tools.SPIProtocolAnalysis.cs"));			
			selectByValue(mode, modearray, properties.getProperty("tools.SPIProtocolAnalysis.mode"));
			selectByValue(bits, bitarray, properties.getProperty("tools.SPIProtocolAnalysis.bits"));
			selectByValue(order, orderarray, properties.getProperty("tools.SPIProtocolAnalysis.order"));
		}

		public void writeProperties(Properties properties) {
			properties.setProperty("tools.SPIProtocolAnalysis.sck", Integer.toString(sck.getSelectedIndex()));
			properties.setProperty("tools.SPIProtocolAnalysis.miso", Integer.toString(miso.getSelectedIndex()));
			properties.setProperty("tools.SPIProtocolAnalysis.mosi", Integer.toString(mosi.getSelectedIndex()));
			properties.setProperty("tools.SPIProtocolAnalysis.cs", Integer.toString(cs.getSelectedIndex()));
			properties.setProperty("tools.SPIProtocolAnalysis.mode", (String)mode.getSelectedItem());
			properties.setProperty("tools.SPIProtocolAnalysis.bits", (String)bits.getSelectedItem());
			properties.setProperty("tools.SPIProtocolAnalysis.order", (String)order.getSelectedItem());
		}
		
		private String[] modearray;
		private String[] bitarray;
		private String[] orderarray;

		private JComboBox sck;
		private JComboBox miso;
		private JComboBox mosi;
		private JComboBox cs;
		private JComboBox mode;
		private JComboBox bits;
		private CapturedData analysisData;
		private JTable outTable;
		private JComboBox order;
		private long unitFactor;
		private String unitName;
		private String colNames[] = { "Index", "Time", "MOSI", "MISO" };
		private Vector decodedData;
		private JFileChooser fileChooser;
		private int startOfDecode;
		
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

	public SPIProtocolAnalysis () {
	}
	
	public void init(Frame frame) {
		spad = new SPIProtocolAnalysisDialog(frame, getName());
	}
	
	/**
	 * Returns the tools visible name.
	 * @return the tools visible name
	 */
	public String getName() {
		return ("SPI Protocol Analysis...");
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

	
	private SPIProtocolAnalysisDialog spad;
}
