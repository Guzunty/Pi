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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;

import org.sump.util.Properties;

/**
 * This component displays a diagram which is obtained from a {@link CapturedData} object.
 * The settings for the diagram are obtained from the embedded {@link DiagramSettings} and {@link DiagramLabels} objects.
 * Look there for an overview of ways to display data.
 * <p>
 * Component size changes with the size of the diagram.
 * Therefore it should only be used from within a JScrollPane.
 *
 * @version 0.7
 * @author Michael "Mr. Sump" Poppitz
 *
 */
public class Diagram extends JComponent implements MouseMotionListener, Configurable {

	/**
	 * Create a new empty diagram to be placed in a container.
	 *
	 */
	public Diagram() {
		super();
		
		this.size = new Dimension(25, 1);
		
		this.signal = new Color(0,0,196);
		this.trigger = new Color(196,255,196);
		this.grid = new Color(196,196,196);
		this.text = new Color(0,0,0);
		this.time = new Color(0,0,0);
		this.groupBackground = new Color(242,242,242);
		this.background = new Color(255,255,255);
		this.label = new Color(255,196,196);
		
		this.offsetX = 25;
		this.offsetY = 18;
		
		zoomDefault();
		setBackground(background);

		this.addMouseMotionListener(this);

		this.settings = new DiagramSettings();
		this.capturedData = null;
		
		this.labels = new DiagramLabels();
		// read label file to array
	}
	
	/**
	 * Resizes the diagram as required by available data and scaling factor.
	 *
	 */
	private void resize() {
		if (capturedData == null)
			return;

		int height = 20;
		for (int group = 0; group < capturedData.channels / 8 && group < 4; group++)
			if (((capturedData.enabledChannels >> (8 * group)) & 0xff) != 0) {
				if ((settings.groupSettings[group] & DiagramSettings.DISPLAY_CHANNELS) > 0)
					height += 20 * 8;
				if ((settings.groupSettings[group] & DiagramSettings.DISPLAY_SCOPE) > 0)
					height += 133;
				if ((settings.groupSettings[group] & DiagramSettings.DISPLAY_BYTE) > 0)
					height += 20;
			}
		int width = (int)(25 + scale * capturedData.values.length);
		
		Rectangle rect = getBounds();
		rect.setSize(width, height);
		setBounds(rect);
		size.width = width;
		size.height = height;
		update(this.getGraphics());
	}
	
	/**
	 * Sets the captured data object to use for drawing the diagram.
	 * 
	 * @param capturedData		captured data to base diagram on
	 */
	public void setCapturedData(CapturedData capturedData) {
		this.capturedData = capturedData;

		if (capturedData.hasTimingData()) {
			double step = (100 / scale) / capturedData.rate;
			
			unitFactor = 1;
			unitName = "s";
			if (step <= 0.000001) { unitFactor = 1000000000; unitName = "ns"; } 
			else if (step <= 0.001) { unitFactor = 1000000; unitName = "µs"; } 
			else if (step <= 1) { unitFactor = 1000; unitName = "ms"; } 
		} else {
			unitFactor = 1;
			unitName = "";
		}
		
		resize();
	}

	/**
	 * Returns the captured data object currently displayed in the diagram.
	 * 
	 * @return diagram's current captured data
	 */
	public CapturedData getCapturedData() {
		return (capturedData);
	}

	/**
	 * Returns wheter or not the diagram has any data.
	 * 
	 * @return <code>true</code> if captured data exists, <code>false</code> otherwise
	 */
	public boolean hasCapturedData() {
		return (capturedData != null);
	}
	
	/**
	 * Zooms in by factor 2 and resizes the component accordingly.
	 *
	 */
	public void zoomIn() {
		if (scale < 10) {
			scale = scale * 2;
			resize();
		}
	}
	
	/**
	 * Zooms out by factor 2 and resizes the component accordingly.
	 *
	 */
	public void zoomOut() {
		scale = scale / 2;
		resize();
	}
	
	/**
	 * Reverts back to the standard zoom level.
	 *
	 */
	public void zoomDefault() {
		scale = 10;
		resize();
	}

	/**
	 * Display the diagram settings dialog.
	 * Will block until the dialog is closed again.
	 *
	 */
	public void showSettingsDialog(Frame frame) {
		if (settings.showDialog(frame) == DiagramSettings.OK)
			resize();
	}

	/**
	 * Display the diagram labels dialog.
	 * Will block until the dialog is closed again.
	 *
	 */
	public void showLabelsDialog(Frame frame) {
		if (labels.showDialog(frame) == DiagramLabels.OK)
			resize();
	}

	/**
	 * Gets the dimensions of the full diagram.
	 * Used to inform the container (preferrably a JScrollPane) about the size.
	 */
	public Dimension getPreferredSize() {
		return (size);
	}
	
	/**
	 * Gets the dimensions of the full diagram.
	 * Used to inform the container (preferrably a JScrollPane) about the size.
	 */
	public Dimension getMinimumSize() {
		return (size);
	}

	private void drawEdge(Graphics g, int x, int y, boolean falling, boolean rising) {
		if (scale <= 1) {
			g.drawLine(x, y, x, y + 14);
		} else {
			int edgeX = x;
			if (scale >= 5)
				edgeX += (int)(scale * 0.4);
	
			if (rising) {
				g.drawLine(x, y + 14, edgeX, y);
				g.drawLine(edgeX, y, x + (int)scale, y);
			}	
			if (falling) {
				g.drawLine(x, y, edgeX, y + 14);
				g.drawLine(edgeX, y + 14, x + (int)scale, y + 14);
			}	
		}
	}
	/**
	 * Draws a channel.
	 * @param g graphics context to draw on
	 * @param x x offset
	 * @param y y offset
	 * @param data array containing the sampled data
	 * @param n number of channel to display
	 * @param from index of first sample to display
	 * @param to index of last sample to display
	 */
	private void drawChannel(Graphics g, int x, int y, int[] data, int n, int from, int to) {
		for (int current = from; current < to;) {
			int currentX = (int)(x + current * scale);
			int currentV = (data[current] >> n) & 0x01;
			int nextV = currentV;
			int next = current;
	
			// scan for the next change
			do {
				nextV = (data[++next] >> n) & 0x01;
			} while ((next < to) && (nextV == currentV));
			int currentEndX = currentX + (int)(scale * (next - current - 1));
			
			// draw straight line up to the point of change and a edge if not at end
			if (currentV == nextV) {
				g.drawLine(currentX, y + 14 * (1 - currentV), currentEndX + (int)scale, y + 14 * (1 - currentV));
			} else {
				g.drawLine(currentX, y + 14 * (1 - currentV), currentEndX, y + 14 * (1 - currentV));
				if (currentV > nextV)
					drawEdge(g, currentEndX, y, true, false);
				else if (currentV < nextV)
					drawEdge(g, currentEndX, y, false, true);
			}
			current = next;
		}
	}
	
	private void drawGridLine(Graphics g, Rectangle clipArea, int y) {
		g.setColor(grid);
		g.drawLine(clipArea.x, y, clipArea.x + clipArea.width, y);
	}
	
	/**
	 * Draws a byte bar.
	 * @param g graphics context to draw on
	 * @param x x offset
	 * @param y y offset
	 * @param data array containing the sampled data
	 * @param n number of group to display (0-3 for 32 channels)
	 * @param from index of first sample to display
	 * @param to index of last sample to display
	 */
	private int drawGroupByte(Graphics g, int x, int y, int[] data, Rectangle clipArea, int n, int from, int to) {
		// draw background
		g.setColor(groupBackground);
		g.fillRect(clipArea.x, y, clipArea.width, 19);
		g.setColor(text);
		g.drawString("B" + n, 5, y + 14);
		// draw bottom grid line
		drawGridLine(g, clipArea, y + 19);
		
		g.setColor(signal);
		
		int yOfs = y + 2;
		int h = 14;

		for (int current = from; current < to;) {
			int currentX = (int)(x + current * scale);
			int currentXSpace = (int)(x + (current - 1) * scale);
			int currentV = (data[current] >> (8 * n)) & 0xff;
			int nextV = currentV;
			int next = current;

			
			// scan for the next change
			do {
				nextV = (data[++next] >> (8 * n)) & 0xff;
			} while ((next < to) && (nextV == currentV));
			int currentEndX = currentX + (int)(scale * (next - current - 1));
			
			// draw straight lines up to the point of change and a edge if not at end
			if (currentV == nextV) {
				g.drawLine(currentX, yOfs + h, currentEndX + (int)scale, yOfs + h);
				g.drawLine(currentX, yOfs, currentEndX + (int)scale, yOfs);
			} else {
				g.drawLine(currentX, yOfs + h, currentEndX, yOfs + h);
				g.drawLine(currentX, yOfs, currentEndX, yOfs);
				drawEdge(g, currentEndX, yOfs, true, true);
			}
			
			// if steady long enough, add hex value
			if (currentEndX - currentXSpace > 15) {
				if (currentV >= 0x10)
					g.drawString(Integer.toString(currentV, 16), (currentXSpace + currentEndX) / 2 - 2, y + 14);
				else
					g.drawString("0" + Integer.toString(currentV, 16), (currentXSpace + currentEndX) / 2 - 2, y + 14);

			}
			
			current = next;
		}
		return (20);
	}
	
	private int drawGroupAnalyzer(Graphics g, int xofs, int yofs, int data[], Rectangle clipArea, int n, int from, int to, String labels[]) {
		// draw channel separators
		for (int bit = 0; bit < 8; bit++) {
			g.setColor(grid);
			g.drawLine(clipArea.x, 20 * bit + yofs + 19, clipArea.x + clipArea.width, 20 * bit + yofs + 19);
			g.setColor(text);
			g.drawString("" + (bit + n * 8), 5, 20 * bit + yofs + 14);
			g.setColor(label);
			if(labels[bit + n * 8] != null)
			{
				if(clipArea.x < xofs)
					g.drawString(labels[bit + n * 8], xofs, 20 * bit + yofs + 14);
				else
					g.drawString(labels[bit + n * 8], clipArea.x, 20 * bit + yofs + 14);
			}
		}
		
		// draw actual data
		g.setColor(signal);
		for (int bit = 0; bit < 8; bit++)
			drawChannel(g, xofs, yofs + 20 * bit + 2, data, 8 * n + bit, from, to);

		return (20 * 8);
	}

	private int drawGroupScope(Graphics g, int x, int y, int data[], Rectangle clipArea, int n, int from, int to) {
		// draw label
		g.setColor(text);
		g.drawString("S" + n, 5, y + 70);
		
		// draw actual data
		g.setColor(signal);
		int last = -1;
		for (int pos = from; pos < to; pos++) {
			int val = (255 - ((data[pos] >> (n * 8)) & 0xff)) / 2;
			if (last >= 0) {
				g.drawLine(x + (int)((pos - 1) * scale), y + 2 + last, x + (int)(pos * scale), y + 2 + val);
			}
			last = val;
		}
		
		// draw bottom grid line
		drawGridLine(g, clipArea, y + 132);

		return (133);
	}

	/**
	 * Paints the diagram to the extend necessary.
	 */
	public void paintComponent(Graphics g) {
		if (capturedData == null)
			return;
		
		int[] data = capturedData.values;
		boolean hasTiming = capturedData.hasTimingData();
		boolean hasTrigger = capturedData.hasTriggerData();
		int channels = capturedData.channels;
		int enabled = capturedData.enabledChannels;
		int triggerPosition = capturedData.triggerPosition;
		if (!hasTrigger)
			triggerPosition = 0;
		int rate = capturedData.rate;
		if (!hasTiming)	// value of rate is only valid if timing data exists
			rate = 1;
		
		int xofs = offsetX;
		int yofs = offsetY + 2;

		// obtain portion of graphics that needs to be drawn
		Rectangle clipArea = g.getClipBounds();

		// find index of first row that needs drawing
		int firstRow = xToIndex(clipArea.x);
		if (firstRow < 0)
			firstRow = 0;
			
		// find index of last row that needs drawing
		int lastRow = xToIndex(clipArea.x + clipArea.width) + 1;
		if (lastRow >= data.length)
 			lastRow = data.length - 1;

		// paint portion of background that needs drawing
		g.setColor(background);
		g.fillRect(clipArea.x, clipArea.y, clipArea.width, clipArea.height);

		// draw trigger if existing and visible
		if (hasTrigger && triggerPosition >= firstRow && triggerPosition <= lastRow) {
			g.setColor(trigger);
			g.fillRect(xofs + (int)(triggerPosition * scale) - 1, 0, (int)(scale) + 2, yofs + 36 * 20);		
		}
		
		// draw time line
		int rowInc = (int)(10 / scale);
		int timeLineShift = (triggerPosition % rowInc);
		g.setColor(time);
		for (int row = ( firstRow / rowInc) * rowInc + timeLineShift; row < lastRow; row += rowInc) {
			int pos = (int)(xofs + scale * row);
			if (((row - triggerPosition) / rowInc) % 10 == 0) {
				g.drawLine(pos, 1, pos, 15);
				if (hasTiming)
					g.drawString((Math.round(10 * ((row - triggerPosition) * unitFactor) / (float)rate) / 10F) + unitName, pos + 5, 10);
				else
					g.drawString(Long.toString(row - triggerPosition), pos + 5, 10);
			} else {
				g.drawLine(pos, 12, pos, 15);
			}
		}

		// draw groups
		int bofs = yofs;
		drawGridLine(g, clipArea, bofs++);
		for (int block = 0; block < channels / 8; block++)
			if (((enabled >> (8 * block)) & 0xff) != 0) {
				if (block < 4 && (settings.groupSettings[block] & DiagramSettings.DISPLAY_CHANNELS) > 0)
					bofs += drawGroupAnalyzer(g, xofs, bofs, data, clipArea, block, firstRow, lastRow, labels.diagramLabels);
				if (block < 4 && (settings.groupSettings[block] & DiagramSettings.DISPLAY_SCOPE) > 0)
					bofs += drawGroupScope(g, xofs, bofs, data, clipArea, block, firstRow, lastRow);
				if (block < 4 && (settings.groupSettings[block] & DiagramSettings.DISPLAY_BYTE) > 0)
					bofs += drawGroupByte(g, xofs, bofs, data, clipArea, block, firstRow, lastRow);
			}
	}
	
	/**
	 * Convert x position to sample index.
	 * @param x horizontal position in pixels
	 * @return sample index
	 */
	private int xToIndex(int x) {
		int index = (int)((x - offsetX) / scale);
		if (index < 0)
			index = 0;
		if (index >= capturedData.values.length)
			index = capturedData.values.length - 1;
		return (index);
	}
	
	/**
	 * Convert sample count to time string.
	 * @param count sample count (or index)
	 * @return string containing time information
	 */
	private String indexToTime(int count) {
		return (((count * unitFactor) / capturedData.rate) + unitName);
	}

	/**
	 * Update status information.
	 * Notifies {@link StatusChangeListener}.
	 * @param dragging <code>true</code> indicates that dragging information should be added 
	 */
	private void updateStatus(boolean dragging) {
		if (capturedData == null || statusChangeListener == null)
			return;

		StringBuffer sb = new StringBuffer(" ");
		
		int row = (mouseY - offsetY) / 20;
		if (row <= capturedData.channels + (capturedData.channels / 9)) {
			if (row % 9 == 8)
				sb.append("Byte " + (row / 9));
			else
				sb.append("Channel " + (row - (row / 9)));
			sb.append(" | ");
		}

		if (dragging && xToIndex(mouseDragX) != xToIndex(mouseX)) {
			int index = xToIndex(mouseDragX);
			
			if (!capturedData. hasTimingData()) {
				sb.append("Sample " + (index - capturedData.triggerPosition));
				sb.append(" (Distance " + (index - xToIndex(mouseX)) + ")");
			} else {
				float frequency = Math.abs(capturedData.rate / (index - xToIndex(mouseX)));
				String unit;
				int div;
				if (frequency >= 1000000) { unit = "MHz"; div = 1000000; }
				else if (frequency >= 1000) { unit = "kHz"; div = 1000; }
				else { unit = "Hz"; div = 1; } 
				sb.append("Time " + indexToTime(index - capturedData.triggerPosition));
				sb.append(" (Duration " + indexToTime(index - xToIndex(mouseX)) + ", ");
				sb.append("Frequency " + (frequency / (float)div) + unit + ")");
			}
		} else {
			if (!capturedData. hasTimingData())
				sb.append("Sample " + (xToIndex(mouseX) - capturedData.triggerPosition));
			else
				sb.append("Time " + indexToTime(xToIndex(mouseX) - capturedData.triggerPosition));
		}
		statusChangeListener.statusChanged(sb.toString());
	}
	
	/**
	 * Handles mouse dragged events and produces status change "events" accordingly.
	 */
	public void mouseDragged(MouseEvent event) {
		mouseDragX = event.getX();
		updateStatus(true);
	}

	/**
	 * Handles mouse moved events and produces status change "events" accordingly.
	 */
	public void mouseMoved(MouseEvent event) {
		mouseX = event.getX();
		mouseY = event.getY();
		updateStatus(false);
	}

	/**
	 * Adds a status change listener for this diagram,
	 * Simple implementation that will only call the last added listener on status change.
	 */
	public void addStatusChangeListener(StatusChangeListener listener) {
		statusChangeListener = listener;
	}

	public void readProperties(Properties properties) {
		settings.readProperties(properties);
		labels.readProperties(properties);
		resize();
	}

	public void writeProperties(Properties properties) {
		settings.writeProperties(properties);
		labels.writeProperties(properties);
	}
	
	private CapturedData capturedData;
	private DiagramSettings settings;
	private DiagramLabels labels;
	private long unitFactor;
	private String unitName;
	
	private int offsetX;
	private int offsetY;
	private int mouseX;
	private int mouseY;
	private int mouseDragX;
	private StatusChangeListener statusChangeListener;
	
	private double scale;
	
	private Color signal;
	private Color trigger;
	private Color grid;
	private Color text;
	private Color time;
	private Color groupBackground;
	private Color background;
	private Color label;
	
	private Dimension size;

	private static final long serialVersionUID = 1L;
}
