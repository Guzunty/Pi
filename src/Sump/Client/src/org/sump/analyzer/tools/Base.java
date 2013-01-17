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

import java.awt.Frame;

import javax.swing.JComboBox;

import org.sump.analyzer.CapturedData;

/**
 * Abstract base class that may be used for tools. 
 * <p>
 * This class is provided for convenience to ease implementing new tools
 * and reduce the redundancy for commonly used methods.	
 * <p>
 * For details about the methods required for tools, see {@link Tool} interface.
 * 
 * @version 0.7
 * @author Michael "Mr. Sump" Poppitz
 */
public abstract class Base extends Object implements Tool {
	public abstract String getName();

	/**
	 * Does nothing. If initialization is needed it must be overwritten.
	 * @param frame ignored
	 */
	public void init(Frame frame) {
	}
	
	public abstract CapturedData process(CapturedData data);

	/**
	 * Calls <code>process(CapturedData data)</code>.
	 */
	public CapturedData process(CapturedData data, int group, int channel, int position) {
		return (process(data));
	}

	/**
	 * Selects the item of a combo box whose index corresponds to a string array index matching the given value.
	 * 
	 * @param box combo box where the entry should be selected
	 * @param entries list of strings corresponding to combo box entries
	 * @param value value to be searched for in string array and highlighted in combo box
	 */
	public void selectByValue(JComboBox box, String[] entries, String value) {
		if (value != null)
			for (int i = 0; i < entries.length; i++)
				if (value.equals(entries[i]))
					box.setSelectedIndex(i);
	}

	/**
	 * Selects the first item of the combo box that matches the given string value.
	 * 
	 * @param box combo box where the entry should be selected
	 * @param value value to be searched for in string array and highlighted in combo box
	 */
	public void selectByValue(JComboBox box, String value) {
		if (value != null)
			for (int i = 0; i < box.getItemCount(); i++)
				if (value.equals((String)box.getItemAt(i)))
					box.setSelectedIndex(i);
	}

	/**
	 * Selects the item of a combo box at the given index.
	 * 
	 * @param box combo box where the entry should be selected
	 * @param index string containing integer to be used as index for selected item in combo box
	 */
	public void selectByIndex(JComboBox box, String index) {
		try {
			box.setSelectedIndex(Integer.parseInt(index));
		} catch (Exception e) { /* don't care */ }
	}
}
