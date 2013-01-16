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
package org.sump.analyzer.tools;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.sump.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.sump.analyzer.CapturedData;
import org.sump.analyzer.Configurable;

/**
 * Tool to convert captured data for state analysis using a user selected channel as clock.
 * Wether sampling should be performed on rising or falling edge can be selected too.
 * 
 * @version 0.7
 * @author Michael "Mr. Sump" Poppitz
 */
public class StateAnalysis extends Base implements Tool, Configurable {

	private class StateAnalysisDialog extends JDialog implements ActionListener {
		public final static int CANCEL = 0;
		public final static int OK = 1;
		public final static int RISING = 0;
		public final static int FALLING = 1;

		public StateAnalysisDialog(Frame frame, String name) {
			super(frame, name, true);
			Container pane = getContentPane();
			pane.setLayout(new GridLayout(3, 2, 5, 5));
			getRootPane().setBorder(BorderFactory.createLineBorder(getBackground(), 5));

			channels = new String[32];
			for (int i = 0; i < channels.length; i++)
				channels[i] = Integer.toString(i);
			channelSelect = new JComboBox(channels);
			pane.add(new JLabel("Clock Channel:"));
			pane.add(channelSelect);

			String[] tmp = {"Rising", "Falling"};
			edges = tmp;
			edgeSelect = new JComboBox(edges);
			pane.add(new JLabel("Clock Edge:"));
			pane.add(edgeSelect);
			
			JButton convert = new JButton("Convert");
			convert.addActionListener(this);
			pane.add(convert);
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(this);
			pane.add(cancel);
			pack();
			setResizable(false);
			result = CANCEL;
		}

		public int showDialog() {
			show();
			return (result);
		}
		
		public void actionPerformed(ActionEvent e) {
			channel = Integer.parseInt((String)channelSelect.getSelectedItem());

			if (((String)edgeSelect.getSelectedItem()).equals("Rising"))
				edge = RISING;
			else
				edge = FALLING;
			
			if(e.getActionCommand().equals("Convert"))
				result = OK;
			else
				result = CANCEL;
			
			hide();
		}

		public void readProperties(Properties properties) {
			selectByValue(edgeSelect, edges, properties.getProperty("tools.StateAnalysis.edge"));
			selectByValue(channelSelect, channels, properties.getProperty("tools.StateAnalysis.channel"));
		}

		public void writeProperties(Properties properties) {
			properties.setProperty("tools.StateAnalysis.channel", (String)channelSelect.getSelectedItem());
			properties.setProperty("tools.StateAnalysis.edge", (String)edgeSelect.getSelectedItem());
		}

		public int channel;
		public int edge;
		
		private JComboBox edgeSelect;
		private JComboBox channelSelect;
		private String[] edges;
		private String[] channels;
		private int result;
	}
	
	public StateAnalysis () {
	}
	
	public void init(Frame frame) {
		sad = new StateAnalysisDialog(frame, getName());
	}
	
	/**
	 * Returns the tools visible name.
	 * @return the tools visible name
	 */
	public String getName() {
		return ("State Analysis...");
	}

	/**
	 * Convert captured data from timing data to state data using the given channel as clock.
	 * @param data - captured data to work on
	 */
	public CapturedData process(CapturedData data) {
		// if no data exists or init has has not been called, return null
		if (data == null || sad == null)
			return (null);

		// if function has been cancelled by the user, return null
		if (sad.showDialog() == StateAnalysisDialog.CANCEL)
			return (null);
		
		// obtain user choices
		int number = sad.channel;
		int level = (sad.edge == StateAnalysisDialog.RISING ? 0 : 1); // this seems overly complicated right now, but RISING might change
		
		// obtain data from captured data
		int[] values = data.values;
		int triggerPosition = data.triggerPosition;

		// calculate new sample array size
		int last = values[0] & 1 << number;
		int size = 0;
		for (int i = 0; i < values.length; i++) {
			int current = values[i] & 1 << number;
			if (last == level && current != level)
				size++;
			last = current;
		}

		// convert captured data
		last = values[0] & 1 << number;
		int pos = 0;
		int newTrigger = -1;
		int[] newValues = new int[size];
		for (int i = 0; i < values.length; i++) {
			int current = values[i] & 1 << number;
			if (last == level && current != level)
				newValues[pos++] = values[i - 1];
			if (triggerPosition == i)
				newTrigger = pos;
			last = current;
		}

		// return new data
		return (new CapturedData(newValues, newTrigger, CapturedData.NOT_AVAILABLE, data.channels, data.enabledChannels));
	}
	
	/**
	 * Reads dialog settings from given properties.
	 * @param properties Properties containing dialog settings
	 */
	public void readProperties(Properties properties) {
		sad.readProperties(properties);
	}

	/**
	 * Writes dialog settings to given properties.
	 * @param properties Properties where the settings are written to
	 */
	public void writeProperties(Properties properties) {
		sad.writeProperties(properties);
	}
	
	private StateAnalysisDialog sad;
}
