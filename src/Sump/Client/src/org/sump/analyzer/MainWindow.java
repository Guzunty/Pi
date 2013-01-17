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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import org.sump.analyzer.tools.Tool;

/**
 * Main frame and starter for Logic Analyzer Client.
 * <p>
 * This class only provides a simple end-user frontend and no functionality to be used by other code.
 * 
 * @version 0.7
 * @author Michael "Mr. Sump" Poppitz
 */
public final class MainWindow extends WindowAdapter implements Runnable, ActionListener, WindowListener, StatusChangeListener {

	/**
	 * Creates a JMenu containing items as specified.
	 * If an item name is empty, a separator will be added in its place.
	 * 
	 * @param name Menu name
	 * @param entries array of menu item names.
	 * @return created menu
	 */
	private JMenu createMenu(String name, String[] entries) {
		JMenu menu = new JMenu(name);
		for (int i = 0; i < entries.length; i++) {
			if (!entries[i].equals("")) {
				JMenuItem item = new JMenuItem(entries[i]);
				item.addActionListener(this);
				menu.add(item);
			} else {
				menu.add(new JSeparator());
			}
		}
		return (menu);
	}
	
	/**
	 * Creates tool icons and adds them the the given tool bar.
	 * 
	 * @param tools tool bar to add icons to
	 * @param files array of icon file names
	 * @param descriptions array of icon descriptions
	 */
	private void createTools(JToolBar tools, String[] files, String[] descriptions) {
		for (int i = 0; i < files.length; i++) {
			URL u = MainWindow.class.getResource("icons/" + files[i]);
			JButton b = new JButton(new ImageIcon(u, descriptions[i]));
			b.setMargin(new Insets(0,0,0,0));
			b.addActionListener(this);
			tools.add(b);
		}
	}

	/**
	 * Enables or disables functions that can only operate when captured data has been added to the diagram.
	 * @param enable set <code>true</code> to enable these functions, <code>false</code> to disable them
	 */
	private void enableDataDependingFunctions(boolean enable) {
		diagramMenu.setEnabled(enable);
		toolMenu.setEnabled(enable);
	}
	
	/**
	 * Inner class defining a File Filter for SLA files.
	 * 
	 * @author Michael "Mr. Sump" Poppitz
	 *
	 */
	private class SLAFilter extends FileFilter {
		public boolean accept(File f) {
			return (f.isDirectory() || f.getName().toLowerCase().endsWith(".sla"));
		}
		public String getDescription() {
			return ("Sump's Logic Analyzer Files (*.sla)");
		}
	}

	/**
	 * Inner class defining a File Filter for SLP files.
	 * 
	 * @author Michael "Mr. Sump" Poppitz
	 *
	 */
	private class SLPFilter extends FileFilter {
		public boolean accept(File f) {
			return (f.isDirectory() || f.getName().toLowerCase().endsWith(".slp"));
		}
		public String getDescription() {
			return ("Sump's Logic Analyzer Project Files (*.slp)");
		}
	}
	
	/**
	 * Default constructor.
	 *
	 */
	public MainWindow() {
		super();
		project = new Project();
	}
	
	/**
	 * Creates the GUI.
	 *
	 */
	void createGUI() {

		frame = new JFrame("Logic Analyzer Client");
		frame.setIconImage((new ImageIcon("org/sump/analyzer/icons/la.png")).getImage());
		Container contentPane = frame.getContentPane();
		contentPane.setLayout(new BorderLayout());

		JMenuBar mb = new JMenuBar();
		
		// file menu
		String[] fileEntries = {"Open...", "Save as...", "", "Exit"};
		JMenu fileMenu = createMenu("File", fileEntries);
		mb.add(fileMenu);

		// project menu
		String[] projectEntries = {"Open Project...", "Save Project as...", };
		JMenu projectMenu = createMenu("Project", projectEntries);
		mb.add(projectMenu);

		// device menu
		String[] deviceEntries = {"Capture...", "Repeat Capture"};
		JMenu deviceMenu = createMenu("Device", deviceEntries);
		mb.add(deviceMenu);
		
		// diagram menu
		String[] diagramEntries = {"Zoom In", "Zoom Out", "Default Zoom", "", "Diagram Settings...", "Labels..."};
		diagramMenu = createMenu("Diagram", diagramEntries);
		mb.add(diagramMenu);

		// tools menu
		String[] toolClasses = { 	// TODO: should be read from properties
				"org.sump.analyzer.tools.StateAnalysis",
				"org.sump.analyzer.tools.SPIProtocolAnalysis",
				"org.sump.analyzer.tools.I2CProtocolAnalysis"
		};
		List loadedTools = new LinkedList();
		for (int i = 0; i < toolClasses.length; i++) {
			try {
				Class tool = Class.forName(toolClasses[i]);
				Object o = tool.newInstance();
				if (o instanceof Tool)
					loadedTools.add(o);
				if (o instanceof Configurable)
					project.addConfigurable((Configurable)o);
			} catch (Exception e) { e.printStackTrace(); }
		}

		tools = new Tool[loadedTools.size()];
		Iterator test = loadedTools.iterator();
		for (int i = 0; test.hasNext(); i++)
			tools[i] = (Tool)test.next();

		String[] toolEntries = new String[tools.length];
		for (int i = 0; i < tools.length; i++) {
			tools[i].init(frame);
			toolEntries[i] = tools[i].getName();
		}

		toolMenu = createMenu("Tools", toolEntries);
		mb.add(toolMenu);

		// help menu
		String[] helpEntries = {"About"};
		JMenu helpMenu = createMenu("Help", helpEntries);
		mb.add(helpMenu);

		frame.setJMenuBar(mb);
		
		JToolBar tools = new JToolBar();
		tools.setRollover(true);
		tools.setFloatable(false);
		
		String[] fileToolsF = {"fileopen.png", "filesaveas.png"}; // , "fileclose.png"};
		String[] fileToolsD = {"Open...", "Save as..."}; // , "Close"};
		createTools(tools, fileToolsF, fileToolsD);
		tools.addSeparator();

		String[] deviceToolsF = {"launch.png", "reload.png"};
		String[] deviceToolsD = {"Capture...", "Repeat Capture"};
		createTools(tools, deviceToolsF, deviceToolsD);
		tools.addSeparator();

		String[] diagramToolsF = {"viewmag+.png", "viewmag-.png", "viewmag1.png"};
		String[] diagramToolsD = {"Zoom In", "Zoom Out", "Default Zoom"};
		createTools(tools, diagramToolsF, diagramToolsD);
		
		contentPane.add(tools, BorderLayout.NORTH);
		
		status = new JLabel(" ");
		contentPane.add(status, BorderLayout.SOUTH);
		
		diagram = new Diagram();
		project.addConfigurable(diagram);
		diagram.addStatusChangeListener(this);
		contentPane.add(new JScrollPane(diagram), BorderLayout.CENTER);

		enableDataDependingFunctions(false);

		frame.setSize(1000, 835);
		frame.addWindowListener(this);
		frame.setVisible(true);

		fileChooser = new JFileChooser();
		fileChooser.addChoosableFileFilter((FileFilter) new SLAFilter());

		projectChooser = new JFileChooser();
		projectChooser.addChoosableFileFilter((FileFilter) new SLPFilter());
		
		controller = new DeviceController();
		project.addConfigurable(controller);

	}
	
	/**
	 * Handles all user interaction.
	 */
	public void actionPerformed(ActionEvent event) {
		String label = event.getActionCommand();
		// if no action command, check if button and if so, use icon description as action
		if (label.equals("")) {
			if (event.getSource() instanceof JButton)
				label = ((ImageIcon)((JButton)event.getSource()).getIcon()).getDescription();
		}
		System.out.println(label);
		try {
			
			if (label.equals("Open...")) {
				if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					if (file.isFile())
						loadData(file);
				}
			
			} else if (label.equals("Save as...")) {
				if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					System.out.println("Saving: " + file.getName() + ".");
					diagram.getCapturedData().writeToFile(file);
				}

			} else if (label.equals("Open Project...")) {
				if (projectChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File file = projectChooser.getSelectedFile();
					if (file.isFile())
						loadProject(file);
				}
				
			} else if (label.equals("Save Project as...")) {
				if (projectChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File file = projectChooser.getSelectedFile();
					System.out.println("Saving Project: " + file.getName() + ".");
					project.store(file);
				}
			
			} else if (label.equals("Capture...")) {
				if (controller.showCaptureDialog(frame) == DeviceController.DONE) {
					diagram.setCapturedData(controller.getDeviceData());
				}

			} else if (label.equals("Repeat Capture")) {
				if (controller.showCaptureProgress(frame) == DeviceController.DONE) {
					diagram.setCapturedData(controller.getDeviceData());
				}

			} else if (label.equals("Exit")) {
				exit();
			
			} else if (label.equals("Zoom In")) {
				diagram.zoomIn();
			
			} else if (label.equals("Zoom Out")) {
				diagram.zoomOut();

			} else if (label.equals("Default Zoom")) {
				diagram.zoomDefault();

			} else if (label.equals("Diagram Settings...")) {
				diagram.showSettingsDialog(frame);
				
			} else if (label.equals("Labels...")) {
				diagram.showLabelsDialog(frame);

			} else if (label.equals("About")) {
				JOptionPane.showMessageDialog(null,
						"Sump's Logic Analyzer Client\n"
						+ "\n"
						+ "Copyright 2006 Michael Poppitz\n"
						+ "This software is released under the GNU GPL.\n"
						+ "\n"
						+ "For more information see:\n"
						+ "http://www.sump.org/projects/analyzer/",
					"About", JOptionPane.INFORMATION_MESSAGE
				);
			} else {
				// check if a tool has been selected and if so, process captured data by tool
				for (int i = 0; i < tools.length; i++)
					if (label.equals(tools[i].getName())) {
						CapturedData newData = tools[i].process(diagram.getCapturedData());
						if (newData != null)
							diagram.setCapturedData(newData);
					}
			}
			enableDataDependingFunctions(diagram.hasCapturedData());
				
		} catch(Exception E) {
			E.printStackTrace(System.out);
		}
	}

	/** 
	 * Handles status change requests.
	 */
	public void statusChanged(String s) {
		status.setText(s);
	}
	
	/**
	 * Handles window close requests.
	 */
	public void windowClosing(WindowEvent event) {
		exit();
	}

	/**
	 * Load the given file as data.
	 * @param file file to be loaded as data
	 * @throws IOException when an IO error occurs
	 */
	public void loadData(File file) throws IOException {
		System.out.println("Opening: " + file.getName());
		diagram.setCapturedData(new CapturedData(file));
	}
	
	/**
	 * Load the given file as project.
	 * @param file file to be loaded as projects
	 * @throws IOException when an IO error occurs
	 */
	public void loadProject(File file) throws IOException {
		System.out.println("Opening Project: " + file.getName());
		project.load(file);
	}
	
	/**
	 * Starts GUI creation and displays it.
	 * Must be called be Swing event dispatcher thread.
	 */
	public void run() {
		createGUI();
	}
	
	/**
	 * Tells the main thread to exit. This will stop the VM.
	 */
	public void exit() {
		System.exit(0);
	}
		
	private JMenu toolMenu;
	private JMenu diagramMenu;
	
	private JFileChooser fileChooser;
	private JFileChooser projectChooser;
	private DeviceController controller;
	private Diagram diagram;
	private Project project;
	private JLabel status;
	private Tool[] tools;
	
	private JFrame frame;
}
