/*-
 * Copyright © 2011 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Shows the default colours used by {@link PlotColorUtility}.
 */
public class PlotColorUtilityColours {

	public static void main(String[] args) {
		
		final Color[] colours = PlotColorUtility.GRAPH_DEFAULT_COLORS;
		
		final int width = 300;
		final int spacing = 40;
		final int height = spacing * (colours.length + 1);
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// white background
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		
		// show each colour
		for (int i=0; i<colours.length; i++) {
			final Color c = colours[i];
			g.setColor(c);
			final int y = spacing * (i+1);
			
			// line
			g.drawLine(0, y, width, y);
			
			// triangle
			Polygon p = new Polygon();
			p.addPoint(width/2, y);
			p.addPoint(width, y-10);
			p.addPoint(width, y+10);
			g.fillPolygon(p);
			
			// RGB values
			final String text = String.format("%d: (%d, %d, %d)", i, c.getRed(), c.getGreen(), c.getBlue());
			g.drawString(text, 20, y-4);
		}
		
		JLabel label = new JLabel(new ImageIcon(image));
		JFrame frame = new JFrame(PlotColorUtility.class.getSimpleName() + " colours");
		frame.add(label);
		frame.pack();
		frame.setLocation(300, 300);
		frame.setVisible(true);
	}

}
