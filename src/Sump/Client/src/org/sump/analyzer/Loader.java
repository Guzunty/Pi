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

import javax.swing.SwingUtilities;

/**
 * Loader for the Logic Analyzer Client.
 * <p>
 * Processes command arguments and starts the UI. After the UI is closed it terminates the VM.
 * <p>
 * See description for {@link Loader#main(String[])} for details on supported arguments.
 * 
 * @version 0.7
 * @author Michael "Mr. Sump" Poppitz
 *
 */
public class Loader {

	/**
	 * Constructs a new loader.
	 */
	public Loader() {
	}

	/**
	 * Starts up the logic analyzer client.
	 * Project ("*.slp") and data ("*.sla") files can be supplied as arguments.
	 * The files will then be loaded automatically. If a file cannot be read, the client will exit.
	 * @param args arguments
	 */
	public static void main(String[] args) {
		MainWindow w = new MainWindow();

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			// handle options (there aren't any yet)
			if (arg.startsWith("-")) {
				System.out.println();
				System.out.println("Sumps Logic Analyzer Client");
				System.out.println("Copyright (C) 2006 Michael Poppitz");
				System.out.println("This software is released under the GNU GPL.");
				System.out.println();
				System.out.println("Usage: run [<project file>] [<data file>]");
				System.out.println();
				System.out.println("	<project file> is a saved project with file extension \".slp\"");
				System.out.println("	<data file> is saved data with file extension \".sla\"");
				System.out.println();
				System.exit(0);

			// handle file arguments
			} else {
				try {
					File f = new File(arg);
					if (!f.isFile()) {
						System.out.println("Error: File does not exist: " + arg);
						System.exit(-1);
					}
					if (arg.toLowerCase().endsWith(".slp")) {
						w.loadProject(f);
					} else if (arg.toLowerCase().endsWith(".sla")) {
						w.loadData(f);
					} else {
						System.out.println("Error: Unknown file type in argument: " + arg);
						System.exit(-1);
					}
				} catch (Exception e) {
					System.out.println("Error: Exception occured while reading file: " + e.getMessage());
					System.exit(-1);
				}
			}
		}
		
		try {
			SwingUtilities.invokeAndWait(w);
		} catch (Exception e) {
			System.out.println("Error while invoking application: " + e.getMessage() + "\n");
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
