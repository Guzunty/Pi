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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.Timer;

import org.sump.util.Properties;

// TODO: when the dialog is closed using the window decoration's close function, close() is not called

/**
 * GUI Component that allows the user to control the device and start captures.
 * <p>
 * Its modelled after JFileChooser and should allow for non-dialog implementations
 * making it somewhat reusable.
 * 
 * @version 0.7
 * @author Michael "Mr. Sump" Poppitz
 *
 */
public class DeviceController extends JComponent implements ActionListener, Runnable, Configurable {
	/** dialog showing and waiting for user action */
	public final static int IDLE = 0;
	/** capture currently running */
	public final static int RUNNING = 1;
	/** capture / dialog aborted by user */
	public final static int ABORTED = 2;
	/** capture finished */
	public final static int DONE = 3;
	
	/**
	 * Creates an array of check boxes, adds it to the device controller and returns it.
	 * @param label label to use on device controller component
	 * @return array of created check boxes
	 */
	private JCheckBox[] createChannelList(JPanel pane, GridBagConstraints constraints) {
		JCheckBox[] boxes = new JCheckBox[32];

		Container container = new Container();
		container.setLayout(new GridLayout(1, 32));
	
		for (int col = 31; col >= 0; col--) {
			JCheckBox box = new JCheckBox();
			box.setEnabled(false);
			container.add(box);
			if ((col % 8) == 0 && col > 0)
				container.add(new JLabel());
			boxes[col] = box;
		}
	
		pane.add(container, constraints);
		return (boxes);
	}
	
	private static GridBagConstraints createConstraints(int x, int y, int w, int h, double wx, double wy) {
	 	GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
	        gbc.insets = new Insets(2, 2, 2, 2);
	 	gbc.gridx = x; gbc.gridy = y;
		gbc.gridwidth = w; gbc.gridheight = h;
	        gbc.weightx = wx; gbc.weighty = wy;
	        return (gbc);
	}
	
	/**
	 * Constructs device controller component.
	 *
	 */
	public DeviceController() {
		super();
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		device = new Device();

		// connection pane
		JPanel connectionPane = new JPanel();
		connectionPane.setLayout(new GridLayout(2, 2, 5, 5));
		connectionPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Connection Settings"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)
		));		String[] ports = Device.getPorts();
		portSelect = new JComboBox(ports);
		connectionPane.add(new JLabel("Analyzer Port:"));
		connectionPane.add(portSelect);

		String[] portRates = {
			"115200bps (LL)",
			"57600bps (LH)",
			"38400bps (HL)",
			"19200bps (HH)"
		};
		portRateSelect = new JComboBox(portRates);
		connectionPane.add(new JLabel("Port Speed (SW1,SW0):"));
		connectionPane.add(portRateSelect);

		add(connectionPane, createConstraints(0, 0, 1, 1, 1.0, 0.5));
		
		// settings pane
		JPanel settingsPane = new JPanel();
		settingsPane.setLayout(new GridLayout(5, 2, 5, 5));
		settingsPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Analyzer Settings"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)
		));
		
		String[] sources = {
			"Internal",
			"External / Rising",
			"External / Falling"
		};
		sourceSelect = new JComboBox(sources);
		sourceSelect.addActionListener(this);
		settingsPane.add(new JLabel("Sampling Clock:"));
		settingsPane.add(sourceSelect);
		
		String[] speeds = {
			"200MHz",
			"100MHz", "50MHz", "20MHz", "10MHz", "5MHz", "2MHz", "1MHz",
			"500kHz", "200kHz", "100kHz", "50kHz", "20kHz", "10kHz",
			"1kHz", "500Hz", "200Hz", "100Hz", "50Hz", "20Hz", "10Hz"
		};
		speedSelect = new JComboBox(speeds);
		speedSelect.setSelectedIndex(1);
		speedSelect.addActionListener(this);
		settingsPane.add(new JLabel("Sampling Rate:"));
		settingsPane.add(speedSelect);

		Container groups = new Container();
		groups.setLayout(new GridLayout(1, 4));
		channelGroup = new JCheckBox[4];
		for (int i = 0; i < channelGroup.length; i++) {
			channelGroup[i] = new JCheckBox(Integer.toString(i));
			channelGroup[i].setSelected(true);
			groups.add(channelGroup[i]);
		}
		settingsPane.add(new JLabel("Channel Groups:"));
		settingsPane.add(groups);
		
		String[] sizes = {
			"256K", "128K", "64K", "32K", "16K", "8K", "4K",
			"2K", "1K", "512", "256", "128", "64"
		};
		sizeSelect = new JComboBox(sizes);
		sizeSelect.setSelectedIndex(7);
		settingsPane.add(new JLabel("Recording Size:"));
		settingsPane.add(sizeSelect);

		add(settingsPane, createConstraints(0, 1, 1, 1, 1.0, 0.5));

		filterEnable = new JCheckBox("Enable");
		filterEnable.setSelected(true);
		filterEnable.setEnabled(false);
		settingsPane.add(new JLabel("Noise Filter: "));
		settingsPane.add(filterEnable);

		// trigger pane
		JPanel triggerPane = new JPanel();
		triggerPane.setLayout(new GridBagLayout());
		triggerPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Trigger Settings"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)
		));
		triggerEnable = new JCheckBox("Enable");
		triggerEnable.addActionListener(this);
		triggerPane.add(new JLabel("Trigger: "), createConstraints(0, 0, 1, 1, 0.0, 1.0));
		triggerPane.add(triggerEnable, createConstraints(1, 0, 1, 1, 0.0, 1.0));
		triggerPane.add(new JLabel(), createConstraints(2, 0, 1, 1, 10.0, 1.0));
		
		String[] ratios = {"0/100", "25/75", "50/50", "75/25", "100/0"};
		ratioSelect = new JComboBox(ratios);
		ratioSelect.setSelectedIndex(2);
		triggerPane.add(new JLabel("Before/After Ratio: "), createConstraints(0, 1, 1, 1, 0.5, 1.0));
		triggerPane.add(ratioSelect, createConstraints(1, 1, 1, 1, 0.5, 1.0));

		String[] types = {"Simple", "Complex"};
		triggerTypeSelect = new JComboBox(types);
		triggerTypeSelect.addActionListener(this);
		triggerPane.add(new JLabel("Type: "), createConstraints(0, 2, 1, 1, 0.5, 1.0));
		triggerPane.add(triggerTypeSelect, createConstraints(1, 2, 1, 1, 0.5, 1.0));

		triggerPane.add(new JLabel(" "), createConstraints(0, 3, 1, 1, 1.0, 1.0));

		triggerStageTabs = new JTabbedPane();
		triggerStages = device.getTriggerStageCount();
		triggerMask = new JCheckBox[4][];
		triggerValue = new JCheckBox[4][];
		triggerLevel = new JComboBox[4];
		triggerDelay = new JTextField[4];
		triggerMode = new JComboBox[4];
		triggerChannel = new JComboBox[4];
		triggerStart = new JCheckBox[4];
		for (int i = 0; i < triggerStages; i++) {
			JPanel stagePane = new JPanel();
			stagePane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			stagePane.setLayout(new GridBagLayout());

			String[] levels = {"Immediatly", "On Level 1", "On Level 2", "On Level 3"};
			triggerLevel[i] = new JComboBox(levels);
			if (i > 0) triggerLevel[i].setSelectedIndex(3);
			stagePane.add(new JLabel("Arm:"), createConstraints(0, 0, 1, 1, 1.0, 1.0));
			stagePane.add(triggerLevel[i], createConstraints(1, 0, 1, 1, 0.5, 1.0));
			String[] modes = {"Parallel", "Serial"};
			triggerMode[i] = new JComboBox(modes);
			stagePane.add(new JLabel("Mode:", JLabel.RIGHT), createConstraints(2, 0, 1, 1, 0.5, 1.0));
			stagePane.add(triggerMode[i], createConstraints(3, 0, 1, 1, 0.5, 1.0));
			triggerMode[i].addActionListener(this);
			String[] channels = {
				"0",  "1",  "2",  "3",  "4",  "5",  "6",  "7",  "8",  "9",  "10", "11", "12", "13", "14", "15",
				"16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"
			};
			triggerChannel[i] = new JComboBox(channels);
			stagePane.add(new JLabel("Channel:", JLabel.RIGHT), createConstraints(4, 0, 1, 1, 0.5, 1.0));
			stagePane.add(triggerChannel[i], createConstraints(5, 0, 1, 1, 0.5, 1.0));
			
			stagePane.add(new JLabel("31"), createConstraints(1, 1, 1, 1, 1.0, 1.0));
			stagePane.add(new JLabel("0", JLabel.RIGHT), createConstraints(5, 1, 1, 1, 1.0, 1.0));
			stagePane.add(new JLabel("Mask:"), createConstraints(0, 2, 1, 1, 1.0, 1.0));
			triggerMask[i] = createChannelList(stagePane, createConstraints(1, 2, 5, 1, 1.0, 1.0));
			stagePane.add(new JLabel("Value:"), createConstraints(0, 3, 1, 1, 1.0, 1.0));
			triggerValue[i] = createChannelList(stagePane, createConstraints(1, 3, 5, 1, 1.0, 1.0));

			stagePane.add(new JLabel("Action:"), createConstraints(0, 4, 1, 1, 1.0, 1.0));
			triggerStart[i] = new JCheckBox("Start Capture    (otherwise trigger level will rise by one)");
			stagePane.add(triggerStart[i], createConstraints(1, 4, 3, 1, 1.0, 1.0));
			stagePane.add(new JLabel("Delay:", JLabel.RIGHT), createConstraints(4, 4, 1, 1, 0.5, 1.0));
			triggerDelay[i] = new JTextField("0");
			stagePane.add(triggerDelay[i], createConstraints(5, 4, 1, 1, 0.5, 1.0));
			triggerStageTabs.add("Stage " + i, stagePane);
		}
		triggerPane.add(triggerStageTabs, createConstraints(0, 4, 3, 1, 1.0, 1.0));
		add(triggerPane, createConstraints(1, 0, 2, 2, 1.0, 0.5));
		
		// progress pane
		JPanel progressPane = new JPanel();
		progressPane.setLayout(new BorderLayout());
		progressPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Progress"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)
		));
		progress = new JProgressBar(0, 100);
		progressPane.add(progress, BorderLayout.CENTER);
		add(progressPane, createConstraints(0, 2, 3, 1, 1.0, 0));

		add(new JLabel(), createConstraints(0, 3, 1, 1, 0.5, 0));
		
		captureButton = new JButton("Capture");
		captureButton.addActionListener(this);
		add(captureButton, createConstraints(1, 3, 1, 1, 0.5, 0));

		JButton cancel = new JButton("Close");
		cancel.addActionListener(this);
		add(cancel, createConstraints(2, 3, 1, 1, 0.5, 0));
		
		capturedData = null;
		timer = null;
		worker = null;
		status = IDLE;
	}

	/**
	 * Internal method that initializes a dialog and add this component to it.
	 * @param frame owner of the dialog
	 */
	private void initDialog(JFrame frame) {
		// check if dialog exists with different owner and dispose if so
		if (dialog != null && dialog.getOwner() != frame) {
			dialog.dispose();
			dialog = null;
		}
		// if no valid dialog exists, create one
		if (dialog == null) {
			dialog = new JDialog(frame, "Capture", true);
			dialog.getContentPane().add(this);
			dialog.setResizable(false);
			dialog.setSize(this.getPreferredSize());
			// dialog.pack();
		}
		// reset progress bar
		progress.setValue(0);

		// sync dialog status with device
		updateFields();
	}

	/**
	 * Return the device data of the last successful run.
	 * 
	 * @return device data
	 */
	public CapturedData getDeviceData() {
		return (capturedData);
	}

	/**
	 * Extracts integers from strings regardless of trailing trash.
	 * 
	 * @param s string to be parsed
	 * @return integer value, 0 if parsing fails
	 */
	private int smartParseInt(String s) {
		int val = 0;
	
		try {
			for (int i = 1; i <= s.length(); i++)
				val = Integer.parseInt(s.substring(0, i));
		} catch (NumberFormatException E) {}
		
		return (val);
	}

	/**
	 * Sets the enabled state of all available trigger check boxes and the ratio select.
	 * @param enable <code>true</code> to enable trigger configuration fields, <code>false</code> to disable them
	 */
	private void setTriggerEnabled(boolean enable) {
		int channels = device.getAvailableChannelCount();
		boolean complex = "Complex".equals((String)triggerTypeSelect.getSelectedItem());
		if (!complex)
			triggerStageTabs.setSelectedIndex(0);
		triggerTypeSelect.setEnabled(enable);
		ratioSelect.setEnabled(enable);
		
		for (int stage = 0; stage < triggerStages; stage++) {
			for (int i = 0; i < channels; i++) {
				triggerMask[stage][i].setEnabled(enable);
				triggerValue[stage][i].setEnabled(enable);
			}
			for (int i = channels; i < 32; i++) {
				triggerMask[stage][i].setEnabled(false);
				triggerValue[stage][i].setEnabled(false);
			}
			triggerStageTabs.setEnabledAt(stage, enable && (stage == 0 || complex));
			triggerLevel[stage].setEnabled(enable && complex);
			triggerDelay[stage].setEnabled(enable);
			triggerMode[stage].setEnabled(enable);
			if (enable && triggerMode[stage].getSelectedIndex() == 1) {
				triggerChannel[stage].setEnabled(true);
			} else {
				triggerChannel[stage].setEnabled(false);
			}
			triggerStart[stage].setEnabled(enable && complex);
		}
	}
	
	/**
	 * Sets the enabled state of all configuration components of the dialog.
	 * @param enable <code>true</code> to enable components, <code>false</code> to disable them
	 */
	private void setDialogEnabled(boolean enable) {
		triggerEnable.setEnabled(enable);
		captureButton.setEnabled(enable);
		portSelect.setEnabled(enable);
		portRateSelect.setEnabled(enable);
		speedSelect.setEnabled(enable);
		sizeSelect.setEnabled(enable);
		updateFields(enable);
	}

	/** activates / deactivates dialog options according to device status */
	public void updateFields() {
		updateFields(true);
	}

	/** activates / deactivates dialog options according to device status */
	private void updateFields(boolean enable) {
		triggerEnable.setSelected(device.isTriggerEnabled());
		setTriggerEnabled(device.isTriggerEnabled());
		filterEnable.setEnabled(device.isFilterAvailable() && enable);
		for (int i = 0; i < channelGroup.length; i++)
			channelGroup[i].setEnabled(enable && (i < device.getAvailableChannelCount() / 8));
		speedSelect.setEnabled(device.getClockSource() == Device.CLOCK_INTERNAL);
	}
	
	/** writes the dialog settings to the device */
	private void updateDevice() {
		String value;
		
		// set clock source
		value = (String)sourceSelect.getSelectedItem();
		if (value.equals("Internal")) {
			device.setClockSource(Device.CLOCK_INTERNAL);
		} else {
			if (value.equals("External / Rising"))
				device.setClockSource(Device.CLOCK_EXTERNAL_RISING);
			else 
				device.setClockSource(Device.CLOCK_EXTERNAL_FALLING);
		}

		// set sample rate
		value = (String)speedSelect.getSelectedItem();
		int f = smartParseInt(value);
		if (value.indexOf("M") > 0)
			f *= 1000000;
		else if (value.indexOf("k") > 0)
			f *= 1000;
		device.setRate(f);
		
		// set sample count
		value = (String)sizeSelect.getSelectedItem();
		int s = smartParseInt(value);
		if (value.indexOf("K") > 0)
			s *= 1024;
		device.setSize(s);
		
		// set before / after ratio
		value = (String)ratioSelect.getSelectedItem();
		double r = 0.5;
		if (value.equals("100/0")) r = 0;
		else if (value.equals("25/75")) r = 0.75;
		else if (value.equals("50/50")) r = 0.5;
		else if (value.equals("75/25")) r = 0.25;
		else if (value.equals("0/100")) r = 1;
		device.setRatio(r);
		
		// set filter
		device.setFilterEnabled(filterEnable.isSelected());

		// set trigger
		boolean triggerEnabled = triggerEnable.isSelected();
		device.setTriggerEnabled(triggerEnabled);
		if (triggerEnabled) {
			boolean complex = "Complex".equals((String)triggerTypeSelect.getSelectedItem());
			for (int stage = 0; stage < triggerStages; stage++) {
				int m = 0;
				int v = 0;
				for (int i = 0; i < 32; i++) {
					if (triggerMask[stage][i].isSelected())
						m |= 1 << i;
					if (triggerValue[stage][i].isSelected())
						v |= 1 << i;				
				}
				int level = triggerLevel[stage].getSelectedIndex();
				int delay = smartParseInt(triggerDelay[stage].getText());
				int channel = triggerChannel[stage].getSelectedIndex();
				boolean startCapture = triggerStart[stage].isSelected();
				if (complex) {
					if (triggerMode[stage].getSelectedIndex() == 0) {
						device.setParallelTrigger(stage, m, v, level, delay, startCapture);
					} else {
						device.setSerialTrigger(stage, channel, m, v, level, delay, startCapture);				
					}
				} else {
					if (stage == 0) {
						if (triggerMode[stage].getSelectedIndex() == 0) {
							device.setParallelTrigger(stage, m, v, 0, delay, true);
						} else {
							device.setSerialTrigger(stage, channel, m, v, 0, delay, true);				
						}
					} else {
						// make sure stages > 0 will not interfere
						device.setParallelTrigger(stage, 0, 0, 3, 0, false);
					}
				}
			}
		}
		
		// set enabled channel groups
		int enabledChannels = 0;
		for (int i = 0; i < channelGroup.length; i++)
			if (channelGroup[i].isSelected())
				enabledChannels |= 0xff << (8 * i);
		device.setEnabledChannels(enabledChannels);
	}

	/**
	 * Starts capturing from device. Should not be called externally.
	 */
	public void run()  {
		// TODO: need to check if attach was successful
		device.attach(
			(String)portSelect.getSelectedItem(),
			smartParseInt((String)portRateSelect.getSelectedItem())
		);
	
		status = RUNNING;
		
		try {
			System.out.println("Run started");
			errorMessage = "";
			capturedData = device.run();
			System.out.println("Run completed");
			status = DONE;
		} catch (Exception ex) {
			// TODO: could make sense to also return half read captures if array length is corrected
			capturedData = null;
			status = ABORTED;
			System.out.println("Run aborted");
			if (!(ex instanceof InterruptedException)) {
				errorMessage = ex.getMessage();
				ex.printStackTrace(System.out);
			}
		}
		device.detach();
	}
	
	/**
	 * Properly closes the dialog.
	 * This method makes sure timer and worker thread are stopped before the dialog is closed.
	 *
	 */
	private void close() {
		if (timer != null) {
			timer.stop();
			timer = null;
		}
		if (worker != null) {
			device.stop(); // lets hope no one gets here before device.run() is called
			worker.interrupt();
			worker = null;
		}
		dialog.hide();
	}
	
	/**
	 * Starts the capture thread.
	 */
	private void startCapture() {
		try {
			setDialogEnabled(false);
			timer = new Timer(100, this);
			worker = new Thread(this);
			timer.start();
			worker.start();
		} catch(Exception E) {
			E.printStackTrace(System.out);
		}
	}
	
	/**
	 * Handles all action events for this component.
	 */ 
	public void actionPerformed(ActionEvent event) {
		Object o = event.getSource();
		String l = event.getActionCommand();
		
		// ignore all events when dialog is not displayed
		if (dialog == null || !dialog.isVisible())
			return;
		
		if (o == timer) {
			if (status == DONE) {
				close();
			} else if (status == ABORTED) {
				timer.stop();
				JOptionPane.showMessageDialog(this,
					"Error while trying to communicate with device:\n\n"
					+ "\"" + errorMessage + "\"\n\n"
					+ "Make sure the device is:\n"
					+ " - connected to the specified port\n"
					+ " - turned on and properly programmed\n"
					+ " - set to the selected transfer rate\n",
					"Communication Error",
					JOptionPane.ERROR_MESSAGE
				);
				setDialogEnabled(true);
			} else {
				if(device.isRunning())
					progress.setValue(device.getPercentage());
			}
		} else {
		
			if (o == triggerEnable) {
				updateDevice();
				updateFields();

			} else if (o == sourceSelect) {
				updateDevice();
				updateFields();

			} else if (o == speedSelect) {
				updateDevice();
				updateFields();

			} else if (l.equals("Capture")) {
				updateDevice();
				startCapture();

			} else if (l.equals("Close")) {
				close();

			} else {
				updateFields();
			}
		}
	}
	
	private void selectByValue(JComboBox box, String value) {
		if (value != null)
			for (int i = 0; i < box.getItemCount(); i++)
				if (value.equals((String)box.getItemAt(i)))
					box.setSelectedIndex(i);
	}

	public void readProperties(Properties properties) {
		selectByValue(portSelect, properties.getProperty(NAME + ".port"));
		selectByValue(portRateSelect, properties.getProperty(NAME + ".portRate"));
		selectByValue(sourceSelect, properties.getProperty(NAME + ".source"));
		selectByValue(speedSelect, properties.getProperty(NAME + ".speed"));
		selectByValue(sizeSelect, properties.getProperty(NAME + ".size"));
		selectByValue(ratioSelect, properties.getProperty(NAME + ".ratio"));
		filterEnable.setSelected("true".equals(properties.getProperty(NAME + ".filter")));
		triggerEnable.setSelected("true".equals(properties.getProperty(NAME + ".trigger")));
		selectByValue(triggerTypeSelect, properties.getProperty(NAME + ".triggerType"));

		for (int stage = 0; stage < triggerStages; stage++) {
			selectByValue(triggerLevel[stage], properties.getProperty(NAME + ".triggerStage" + stage + "Level"));
			triggerDelay[stage].setText(properties.getProperty(NAME + ".triggerStage" + stage + "Delay"));
			selectByValue(triggerMode[stage], properties.getProperty(NAME + ".triggerStage" + stage + "Mode"));
			selectByValue(triggerChannel[stage], properties.getProperty(NAME + ".triggerStage" + stage + "Channel"));
			
			String mask = properties.getProperty(NAME + ".triggerStage" + stage + "Mask");
			if (mask != null)
				for (int i = 0; i < 32 && i < mask.length(); i++)
					triggerMask[stage][i].setSelected(mask.charAt(i) == '1');
	
			String value = properties.getProperty(NAME + ".triggerStage" + stage + "Value");
			if (value != null)
				for (int i = 0; i < 32 && i < value.length(); i++)
					triggerValue[stage][i].setSelected(value.charAt(i) == '1');

			triggerStart[stage].setSelected("true".equals(properties.getProperty(NAME + ".triggerStage" + stage + "StartCapture")));
		}
		
		String group = properties.getProperty(NAME + ".channelGroup");
		if (group != null)
			for (int i = 0; i < 4 && i < group.length(); i++)
				channelGroup[i].setSelected(group.charAt(i) == '1');

		updateDevice();
		updateFields();
}
	
	public void writeProperties(Properties properties) {
		properties.setProperty(NAME + ".port", (String)portSelect.getSelectedItem());
		properties.setProperty(NAME + ".portRate", (String)portRateSelect.getSelectedItem());
		properties.setProperty(NAME + ".source", (String)sourceSelect.getSelectedItem());
		properties.setProperty(NAME + ".speed", (String)speedSelect.getSelectedItem());
		properties.setProperty(NAME + ".size", (String)sizeSelect.getSelectedItem());
		properties.setProperty(NAME + ".ratio", (String)ratioSelect.getSelectedItem());
		properties.setProperty(NAME + ".filter", filterEnable.isSelected()?"true":"false");
		properties.setProperty(NAME + ".trigger", triggerEnable.isSelected()?"true":"false");
		properties.setProperty(NAME + ".triggerType", (String)triggerTypeSelect.getSelectedItem());

		for (int stage = 0; stage < triggerStages; stage++) {
			properties.setProperty(NAME + ".triggerStage" + stage + "Level", (String)triggerLevel[stage].getSelectedItem());
			properties.setProperty(NAME + ".triggerStage" + stage + "Delay", triggerDelay[stage].getText());
			properties.setProperty(NAME + ".triggerStage" + stage + "Mode", (String)triggerMode[stage].getSelectedItem());
			properties.setProperty(NAME + ".triggerStage" + stage + "Channel", (String)triggerChannel[stage].getSelectedItem());
			
			StringBuffer mask = new StringBuffer();
			for (int i = 0; i < 32; i++)
				mask.append(triggerMask[stage][i].isSelected()?"1":"0");
			properties.setProperty(NAME + ".triggerStage" + stage + "Mask", mask.toString());
	
			StringBuffer value = new StringBuffer();
			for (int i = 0; i < 32; i++)
				value.append(triggerValue[stage][i].isSelected()?"1":"0");
			properties.setProperty(NAME + ".triggerStage" + stage + "Value", value.toString());

			properties.setProperty(NAME + ".triggerStage" + stage + "StartCapture", triggerStart[stage].isSelected()?"true":"false");
		}
		
		StringBuffer group = new StringBuffer();
		for (int i = 0; i < 4; i++)
			group.append(channelGroup[i].isSelected()?"1":"0");
		properties.setProperty(NAME + ".channelGroup", group.toString());
	}

	
	/**
	 * Displays the device controller dialog with enabled configuration portion and waits for user input.
	 * 
	 * @param frame parent frame of this dialog
	 * @return status, which is either <code>ABORTED</code> or <code>DONE</code>
	 * @throws Exception
	 */
	public int showCaptureDialog(JFrame frame) throws Exception {
		status = IDLE;
		initDialog(frame);
		setDialogEnabled(true);
		dialog.show();
		return status;
	}

	/**
	 * Displays the device controller dialog with disabled configuration, starting capture immediately.
	 * 
	 * @param frame parent frame of this dialog
	 * @return status, which is either <code>ABORTED</code> or <code>DONE</code>
	 * @throws Exception
	 */
	public int showCaptureProgress(JFrame frame) throws Exception {
		status = IDLE;
		initDialog(frame);
		startCapture();
		dialog.show();
		return status;
	}

	private Thread worker;
	private Timer timer;
	
	private JComboBox portSelect;
	private JComboBox portRateSelect;
	private JComboBox sourceSelect;
	private JComboBox speedSelect;
	private JComboBox sizeSelect;
	private JComboBox ratioSelect;
	private JCheckBox filterEnable;
	private JCheckBox triggerEnable;
	private JComboBox triggerTypeSelect;
	private JTabbedPane triggerStageTabs;
	private JComboBox[] triggerLevel;
	private JTextField[] triggerDelay;
	private JComboBox[] triggerMode;
	private JComboBox[] triggerChannel;
	private JCheckBox[] triggerStart;
	private JCheckBox[][] triggerMask;
	private JCheckBox[][] triggerValue;
	private JCheckBox[] channelGroup;
	private JProgressBar progress;
	private JButton captureButton;
	
	private JDialog dialog;
	private Device device;
	private CapturedData capturedData;
	
	private int triggerStages;
	
	private int status;
	private String errorMessage;
	
	private static final long serialVersionUID = 1L;
	private static final String NAME = "DeviceController";
}
