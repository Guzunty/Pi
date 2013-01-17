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

import org.sump.analyzer.CapturedData;

/**
 * Interface for pluggable tools.
 * <p>
 * All tools implementing this interface that are added to the tools class list
 * will be automatically added to the tools menu in the client.
 * 
 * @version 0.7
 * @author Michael "Mr. Sump" Poppitz
 */
public interface Tool {

	/**
	 * Is called to get the name for the menu entry.
	 * The name must be unique among all tools. Should end in "..." if it opens a dialog window.
	 * @return name for this tool
	 */
	public String getName();

	/**
	 * Performs tool initialization.
	 * This method should also prepare the dialog, if one is needed
	 * @param frame main window's frame (needed for modal dialogs)
	 */
	public void init(Frame frame);
	
	/**
	 * This method is invoked when the tool is selected from the Tools menu.
	 * It should request any missing information using a dialog and perform the tool's actual task.
	 * @param data currently displayed captured data
	 * @return new <code>CapturedData</code> if provided data has been altered or <code>null</code> otherwise
	 */
	public CapturedData process(CapturedData data);

	/**
	 * This method is invoked when the tool is selected from the context menu after right clicking someplace in the diagram.
	 * It should request any missing information using a dialog and perform the tool's actual task.
	 * @param data currently displayed captured data
	 * @param group channel group at the mouse position where the menu was openend
	 * @param channel number of channel at the mouse position where the menu was openend
	 * @param position number of sample at the mouse position where the menu was openend
	 * @return new <code>CapturedData</code> if provided data has been altered or <code>null</code> otherwise
	 */
	public CapturedData process(CapturedData data, int group, int channel, int position);
}
