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

package uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.primitives;

import java.awt.Color;

import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.VectorOverlayStyles;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.ShaderUtility;

/**
 *
 */
public class LinePrimitive extends OverlayPrimitive {

	private double coords[][] = new double[2][3];
	private double dataPoints[][] = new double[2][2];
	private IndexedLineSetFactory lineFactory = null;
	private int[][] edges = new int[1][2];
	private DefaultLineShader dls = null;
	private double lineThickness = 1.0;

	/**
	 * Constructor for a new line primitive
	 * @param comp
	 */
	public LinePrimitive(SceneGraphComponent comp) {
		this(comp,false);
	}

	/**
	 * Constructor for a new line primitive
	 * @param comp
	 * @param isFixed is the size fixed (invariant to zoom) true or false
	 */
	public LinePrimitive(SceneGraphComponent comp, boolean isFixed) {
		super(comp,isFixed);
		ap = new Appearance();
		comp.setAppearance(ap);
		ap.setAttribute(CommonAttributes.POINT_SHADER+ "." + CommonAttributes.SPHERES_DRAW,false);
		ap.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute("useGLSL", false);
		DefaultGeometryShader dgs = 
			ShaderUtility.createDefaultGeometryShader(ap, true);
		 dls = 
			(DefaultLineShader)dgs.createLineShader("default");
		dls.setDiffuseColor(java.awt.Color.WHITE);
		dgs.setShowFaces(false);
		dgs.setShowLines(true);
		dgs.setShowPoints(false);
		lineFactory = new IndexedLineSetFactory();
		edges[0][0] = 0;
		edges[0][1] = 1;
	}

	/**
	 * Set the line points in world space
	 * @param x
	 * @param y
	 * @param x1
	 * @param y1
	 */
	public void setLinePoints(double x, double y, double x1, double y1) {
		needToUpdateGeom = true;
		coords[0][0] = x;
		coords[0][1] = y;
		coords[0][2] = 0.0005;
		coords[1][0] = x1;
		coords[1][1] = y1;
		coords[1][2] = 0.0005;
	}
	
	/**
	 * Set the line coordinates in data coordinate space
	 * @param x
	 * @param y
	 * @param x1
	 * @param y1
	 */
	public void setDataPoints(double x, double y, double x1, double y1) {
		dataPoints[0][0] = x;
		dataPoints[0][1] = y;
		dataPoints[1][0] = x1;
		dataPoints[1][1] = y1;
	}
	
	/**
	 * Get the line coordinates in data coordinate space
	 * @return the line coordinates in data coordinate space
	 */
	public double[][] getDataPoints() {
		return dataPoints;
	}
	
	/**
	 * Get the line coordinates
	 * @return the line primitive coordinates
	 */
	public double[][] getLinePoints() {
		double [][] returnValues = {{coords[0][0], coords[0][1]},
									{coords[1][0], coords[1][1]}};
		return returnValues;
	}
	
	private IndexedLineSet createLineGeometry()
	{
		lineFactory.setVertexCount(2);
		lineFactory.setEdgeCount(1);
		lineFactory.setVertexCoordinates(coords);
		lineFactory.setEdgeIndices(edges);
		lineFactory.update();
		return lineFactory.getIndexedLineSet();
	}
	
	
	@Override
	public void updateNode() {
		if (needToUpdateTrans) {
			MatrixBuilder.euclidean(transformMatrix).assignTo(comp);
		}
		if (needToUpdateGeom)
		{
			comp.setGeometry(createLineGeometry());
		}
		if (needToUpdateApp)
		{
			dls.setDiffuseColor(colour);
			dls.setLineWidth(lineThickness);
		}
		needToUpdateApp = false;
		needToUpdateGeom = false;
		needToUpdateTrans = false;
		transformMatrix = null;
	}

	@Override
	public void setStyle(VectorOverlayStyles style) {
		// Nothing to do
	}

	@Override
	public void setOutlineColour(Color outlineColour) {
		// Nothing to do
	}

	@Override
	public void setLineThickness(double thickness) {
		needToUpdateApp = true;
		lineThickness = thickness;
		
	}

	@Override
	public void setOutlineTransparency(double value) {
		setTransparency(value);
	}

	@Override
	public void setTransparency(double value) {
		if (value > 0.0f) {
			ap.setAttribute(CommonAttributes.LINE_SHADER+"." + CommonAttributes.TRANSPARENCY_ENABLED, true);
			ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		} else {
			ap.setAttribute(CommonAttributes.LINE_SHADER+"." + CommonAttributes.TRANSPARENCY_ENABLED, false);
			ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);
		}
		ap.setAttribute(CommonAttributes.LINE_SHADER+"." + CommonAttributes.TRANSPARENCY,value);
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"." + CommonAttributes.TRANSPARENCY,value);
		ap.setAttribute(CommonAttributes.ADDITIVE_BLENDING_ENABLED,true);
	}

}
