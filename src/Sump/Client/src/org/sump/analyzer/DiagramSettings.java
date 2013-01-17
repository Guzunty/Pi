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

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.sump.util.Properties;

/**
 * Stores diagram display settings and provides a dialog for changing them.
 * 
 * @version 0.7
 * @author Michael "Mr. Sump" Poppitz
 */
public class DiagramSettings extends JComponent implements ActionListener, Configurable {
	/** the user cancelled the dialog - all changes were discarded */
	public final static int CANCEL = 0;
	/** the user clicked ok - all changes were written to the settings */
	public final static int OK = 1;
	/** display a group in 8 channel logic level view (used in <code>groupSettings</code>)*/
	public final static int DISPLAY_CHANNELS = 1;
	/** display a group in a 8bit resolution scope view (used in <code>groupSettings</code>)*/
	public final static int DISPLAY_SCOPE = 2;
	/** display a group in a 8bit hex value view (used in <code>groupSettings</code>)*/
	public final static int DISPLAY_BYTE = 4;

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
	 * Constructs diagram settings component.
	 */
	public DiagramSettings() {
		super();
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel modePane = new JPanel();
		modePane.setLayout(new GridBagLayout());
		modePane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Group Display Settings"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)
		));
		
		groupSettingBoxes = new JCheckBox[4][3];
		for (int i = 0; i < 4; i++) {
			modePane.add(new JLabel("Group " + i + ": "), createConstraints(0, i, 1, 1, 0, 0));
			groupSettingBoxes[i][0] = new JCheckBox();
			modePane.add(groupSettingBoxes[i][0], createConstraints(1, i, 1, 1, 0, 0));
			modePane.add(new JLabel("Channels"), createConstraints(2, i, 1, 1, 0, 0));
			groupSettingBoxes[i][1] = new JCheckBox();
			modePane.add(groupSettingBoxes[i][1], createConstraints(3, i, 1, 1, 0, 0));
			modePane.add(new JLabel("Scope"), createConstraints(4, i, 1, 1, 0, 0));
			groupSettingBoxes[i][2] = new JCheckBox();
			modePane.add(groupSettingBoxes[i][2], createConstraints(5, i, 1, 1, 0, 0));
			modePane.add(new JLabel("Byte Value"), createConstraints(6, i, 1, 1, 0, 0));
		}
		
		add(modePane, createConstraints(0, 0, 2, 1, 0, 0));
		
		JButton ok = new JButton("Ok");
		ok.addActionListener(this);
		add(ok, createConstraints(0, 1, 1, 1, 0.5, 0));
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		add(cancel, createConstraints(1, 1, 1, 1, 0.5, 0));

		groupSettings = new int[4];
		for (int i = 0; i < groupSettings.length; i++)
			groupSettings[i] = DISPLAY_CHANNELS | DISPLAY_BYTE;		
	}
	
	/**
	 * Internal method that initializes a dialog and add this component to it.
	 * @param frame owner of the dialog
	 */
	private void initDialog(Frame frame) {
		// check if dialog exists with different owner and dispose if so
		if (dialog != null && dialog.getOwner() != frame) {
			dialog.dispose();
			dialog = null;
		}
		// if no valid dialog exists, create one
		if (dialog == null) {
			dialog = new JDialog(frame, "Diagram Settings", true);
			dialog.getContentPane().add(this);
			dialog.pack();
			dialog.setResizable(false);
		}
	}	
	
	private void updateFields() {
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 3; j++)
				groupSettingBoxes[i][j].setSelected((groupSettings[i] & (1 << j)) > 0);
	}
	
	/**
	 * Handles all action events for this component.
	 */ 
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Ok")) {
			for (int i = 0; i < 4; i++) {
				groupSettings[i] = 0;
				for (int j = 0; j < 3; j++)
					if (groupSettingBoxes[i][j].isSelected())
						groupSettings[i] |= 1 << j;
			}
			result = OK;
		}	
		dialog.setVisible(false);
	}

	/**
	 * Display the settings dialog.
	 * If the user clicks ok, all changes are reflected in the properties of this object.
	 * Otherwise changes are discarded.
	 * @param frame parent frame (needed for creating a modal dialog)
	 * @return <code>OK</code> when user accepted changes, <code>CANCEL</code> otherwise
	 */
	public int showDialog(Frame frame) {
		initDialog(frame);
		updateFields();
		result = CANCEL;
		dialog.setVisible(true);
		return (result);
	}
	
	public void readProperties(Properties properties) {
		String value;
		
		for (int i = 0; i < 4; i++) {
			value = properties.getProperty("DiagramSettings.group" + i);
			if (value != null) {
				groupSettings[i] = 0;
				if (value.indexOf("channels") >= 0)
					groupSettings[i] |= DISPLAY_CHANNELS;
				if (value.indexOf("scope") >= 0)
					groupSettings[i] |= DISPLAY_SCOPE;
				if (value.indexOf("byte") >= 0)
					groupSettings[i] |= DISPLAY_BYTE;
			}
		}
		updateFields();
	}
	
	public void writeProperties(Properties properties) {
		for (int i = 0; i < 4; i++) {
			StringBuffer value = new StringBuffer();
			if ((groupSettings[i] & DISPLAY_CHANNELS) != 0)
				value.append("channels ");
			if ((groupSettings[i] & DISPLAY_SCOPE) != 0)
				value.append("scope ");
			if ((groupSettings[i] & DISPLAY_BYTE) != 0)
				value.append("byte ");
			properties.setProperty("DiagramSettings.group" + i, value.toString());
		}
	}

	/**
	 * Display settings for each group.
	 * Can be any combinations (ored) of the defined MODE_* values.
	 */
	public int[] groupSettings;

	private JDialog dialog;
	private JCheckBox[][] groupSettingBoxes;
	private int result;
}
