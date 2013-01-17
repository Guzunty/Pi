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
package org.sump.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.TreeSet;

/**
 * Essentially the same as <code>java.util.Properties</code> but will store the properties in alphabetical order.
 * Ordering is implemented using a simple hack. It may stop working in future versions of Java.
 *
 * @version 0.7
 * @author Michael "Mr. Sump" Poppitz
 *
 */
public class Properties extends java.util.Properties {
	/**
	 * Get keys in alphabetical order.
	 * This method is used by <code>store()</code>.
	 */
	public synchronized Enumeration keys() {
		return Collections.enumeration(new TreeSet(keySet()));
	}
}
