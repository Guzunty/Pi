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
package org.sump.analyzer;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.sump.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Stores the diagram labels and provides a dialog to change them.
 *
 * @version 0.7
 * @author Frank Kunz
 *
 */
public class DiagramLabels extends JComponent implements ActionListener, Configurable {
	/** the user cancelled the dialog - all changes were discarded */
	public final static int CANCEL = 0;
	/** the user clicked ok - all changes were written to the settings */
	public final static int OK = 1;

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
	 * Constructs diagram labels component.
	 */
	public DiagramLabels() {
		super();
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel modePane = new JPanel();
		modePane.setLayout(new GridBagLayout());
		modePane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Diagram Labels"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)
		));
		
		labelFields = new JTextField[32];
		diagramLabels = new String[32];
		for (int col = 0; col < 2; col++) {
			for (int row = 0; row < 16; row++) {
				int num = 16 * col + row;
				modePane.add(new JLabel("Channel " + num + ": "), createConstraints(2 * col, row, 1, 1, 0, 0));
				labelFields[num] = new JTextField(20);
				modePane.add(labelFields[num], createConstraints(2 * col + 1, row, 1, 1, 0, 0));
				diagramLabels[num] = new String();
			}
		}
		add(modePane, createConstraints(0, 0, 5, 1, 0, 0));
		
		JButton ok = new JButton("Ok");
		ok.addActionListener(this);
		add(ok, createConstraints(0, 1, 1, 1, 0.34, 0));
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		add(cancel, createConstraints(1, 1, 1, 1, 0.33, 0));
		JButton clear = new JButton("Clear");
		clear.addActionListener(this);
		add(clear, createConstraints(2, 1, 1, 1, 0.33, 0));
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
			dialog = new JDialog(frame, "Diagram Labels", true);
			dialog.getContentPane().add(this);
			dialog.pack();
			dialog.setResizable(false);
		}
	}	
	
	private void updateFields() {
		for (int i = 0; i < 32; i++)
			labelFields[i].setText(diagramLabels[i]);
	}
	
	/**
	 * Handles all action events for this component.
	 */ 
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Ok")) {
			for (int i = 0; i < 32; i++) {
				diagramLabels[i] = new String(labelFields[i].getText());
			}
			result = OK;
			dialog.setVisible(false);
		} else if (e.getActionCommand().equals("Clear")) {
			for (int i = 0; i < 32; i++)
				labelFields[i].setText("");
		} else {
			dialog.setVisible(false);
		}
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
	
	/**
	 * Reads user settings from given properties.
	 * Uses the property prefix "DiagramLabels".
	 * @param properties properties to read settings from
	 */
	public void readProperties(Properties properties) {
		for (int i = 0; i < 32; i++)
			if (properties.containsKey("DiagramLabels.channel" + i))
				diagramLabels[i] = properties.getProperty("DiagramLabels.channel" + i);
	}

	/**
	 * Writes user settings to given properties.
	 * Uses the property prefix "DiagramLabels".
	 * @param properties properties to write settings to
	 */
	public void writeProperties(Properties properties) {
		for (int i = 0; i < 32; i++)
			properties.setProperty("DiagramLabels.channel" + i, diagramLabels[i]);
	}

	public String[] diagramLabels;
	private JDialog dialog;
	private JTextField[] labelFields;
	private int result;
}
