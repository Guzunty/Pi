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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.sump.util.Properties;

/**
 * Project maintains a global properties list for all registered objects implementing {@link Configurable}.
 * It also provides methods for loading and storing these properties from and to project configuration files.
 * This allows to keep multiple sets of user settings across multiple instance lifecycles.
 * 
 * @version 0.7
 * @author Michael "Mr. Sump" Poppitz
 */
public class Project extends Object {
	/**
	 * Constructs a new project with an empty set of properties and configurable objects.
	 */
	public Project() {
		this.properties = new Properties();
		this.configurableObjectList = new LinkedList();
	}
	
	/**
	 * Adds a configurable object to the project.
	 * The given objects properties will be read and written whenever load and store operations take place.
	 * @param configurable configurable object
	 */
	public void addConfigurable(Configurable configurable) {
		this.configurableObjectList.add(configurable);
	}
	
	/**
	 * Gets all currently defined properties for this project.
	 * @return project properties
	 */
	public Properties getProperties() {
		Iterator i = configurableObjectList.iterator();
		while(i.hasNext())
			((Configurable)i.next()).writeProperties(properties);
		return (properties);
	}
	
	/**
	 * Loads properties from the given file and notifies all registered configurable objects.
	 * @param file file to read properties from
	 * @throws IOException when IO operation failes
	 */
	public void load(File file) throws IOException {
		InputStream stream = new FileInputStream(file);
		properties.load(stream);
		Iterator i = configurableObjectList.iterator();
		while(i.hasNext())
			((Configurable)i.next()).readProperties(properties);
	}

	/**
	 * Stores properties fetched from all registered configurable objects in the given file.
	 * @param file file to store properties in
	 * @throws IOException when IO operation failes
	 */
	public void store(File file) throws IOException {
		// creating new properties object will remove alien properties read from broken / old project files
		properties = new Properties();
		Iterator i = configurableObjectList.iterator();
		while(i.hasNext())
			((Configurable)i.next()).writeProperties(properties);
		OutputStream stream = new FileOutputStream(file);
		properties.store(stream, "Sumps Logic Analyzer Project File");
	}
	
	private Properties properties;
	private List configurableObjectList;
}
