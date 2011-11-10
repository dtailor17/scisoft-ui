/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay;

import org.eclipse.swt.SWT;

import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.OverlayType;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.PrimitiveType;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.objects.ArrowObject;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.objects.BoxObject;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.objects.CircleObject;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.objects.PointListObject;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.IImagePositionEvent;

/**
 * A demonstration overlay that demonstrates the functionality of a Overlay2DConsumer
 */
@SuppressWarnings("unused")
public class DemoOverlay implements Overlay2DConsumer {

	private Overlay2DProvider2 provider;
	private int sx,sy,ex,ey;
	private boolean drawing = false;
	private ArrowObject line;
	private BoxObject box;
	private CircleObject circle;
	private PointListObject pointList;
	private OverlayImage image;
	int primID = -1;
	int primID2 = -1;
	/**
	 * Constructor of the TestOverlay
	 */
	public DemoOverlay()
	{
		provider = null;
		sx = 0;
		sy = 0;
		ex = 0;
		ey = 0;
	}
	
	@Override
	public void registerProvider(OverlayProvider provider) {
		this.provider = (Overlay2DProvider2)provider;
		line = (ArrowObject) this.provider.registerObject(PrimitiveType.ARROW);
		box = (BoxObject)this.provider.registerObject(PrimitiveType.BOX);
		circle = (CircleObject)this.provider.registerObject(PrimitiveType.CIRCLE);
		pointList = (PointListObject)this.provider.registerObject(PrimitiveType.POINTLIST);
		image = this.provider.registerOverlayImage(128, 128);
		if (line != null)
			line.setLinePoints(30,30,60,60);
		if (box != null)
			box.setBoxPoints(80, 80, 120, 110);
		if (circle != null) {
			circle.setCirclePoint(128, 128);
			circle.setRadius(30);
		}
		if (pointList != null) {
			double[] xs = new double[100];
			double[] ys = new double[100];
			for (int i = 0; i < xs.length; i++) {
				xs[i] = 256 * Math.random();
				ys[i] = 256 * Math.random();
			}
			pointList.setPointPositions(xs, ys);
		}
		drawOverlay();
	}

	private void drawOverlay() {
		provider.begin(OverlayType.VECTOR2D);
		line.setColour(java.awt.Color.blue);
		line.draw();
		box.setColour(java.awt.Color.red);
		box.draw();
		circle.setColour(java.awt.Color.DARK_GRAY);
		circle.draw();
		pointList.setColour(java.awt.Color.MAGENTA);
		pointList.draw();
		provider.end(OverlayType.VECTOR2D);
		provider.begin(OverlayType.IMAGE);
		image.clear((short)255, (short)0, (short)0,(short) 128);
		image.putPixel(64, 64, (short)255, (short)255, (short)255, (short)255);
		provider.end(OverlayType.IMAGE);
		
	}
	
	@Override
	public void imageDragged(IImagePositionEvent event) {
		ex = event.getImagePosition()[0];
		ey = event.getImagePosition()[1];
		provider.setPlotAreaCursor(SWT.CURSOR_CROSS);
	}


	@Override
	public void imageFinished(IImagePositionEvent event) {
		provider.restoreDefaultPlotAreaCursor();
	}


	@Override
	public void imageStart(IImagePositionEvent event) {
		sx = event.getImagePosition()[0];
		sy = event.getImagePosition()[1];
		System.out.println(event.getPrimitiveID());
		provider.setPlotAreaCursor(SWT.CURSOR_HELP);
	}

	@Override
	public void unregisterProvider() {
		provider = null;
	}

	@Override
	public void hideOverlays() {
		if (provider != null)
			provider.setPrimitiveVisible(primID, false);
	}

	@Override
	public void showOverlays() {
		if (provider != null)
			provider.setPrimitiveVisible(primID, true);
	}

	@Override
	public void removePrimitives() {
		primID = -1;
	}


}
