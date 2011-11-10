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

package uk.ac.diamond.scisoft.analysis.rcp.plotting.roi;

import uk.ac.diamond.scisoft.analysis.coords.SectorCoords;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

/**
 * Wrapper class for a SectorROI that adds handles
 */
public class SectorROIHandler extends ROIHandles {
	/**
	 * Number of handle areas
	 */
	private final static int NHANDLE = 10;

	/**
	 * Handler for SectorROI
	 * 
	 * @param roi
	 */
	public SectorROIHandler(SectorROI roi) {
		super();
		for (int h = 0; h < NHANDLE; h++) {
			add(-1);
		}
		this.roi = roi;
	}

	/**
	 * @return Returns the roi.
	 */
	@Override
	public SectorROI getROI() {
		return (SectorROI) roi;
	}

	@Override
	public int[] getHandlePoint(int handle, int size) {
		return null;
	}

	/**
	 * @param handle
	 * @param size
	 * @param dphi 
	 * @return handle point in polar coords
	 */
	public double[] getSectorPoint(int handle, int size, double dphi) {
		SectorROI oroi = (SectorROI) roi;
		double[] pt = new double[2];

		switch (handle) {
		case 0:
			pt[0] = oroi.getRadius(0);
			pt[1] = oroi.getAngle(0);
			break;
		case 1:
			pt[0] = oroi.getRadius(0);
			pt[1] = 0.5 * (oroi.getAngle(0) + oroi.getAngle(1) - dphi);
			break;
		case 2:
			pt[0] = oroi.getRadius(0);
			pt[1] = oroi.getAngle(1) - dphi;
			break;
		case 3:
			pt[0] = 0.5 * (oroi.getRadius(0) + oroi.getRadius(1) - size);
			pt[1] = oroi.getAngle(0);
			break;
		case 4:
			pt[0] = 0.5 * (oroi.getRadius(0) + oroi.getRadius(1) - size);
			pt[1] = 0.5 * (oroi.getAngle(0) + oroi.getAngle(1) - dphi);
			break;
		case 5:
			pt[0] = 0.5 * (oroi.getRadius(0) + oroi.getRadius(1) - size);
			pt[1] = oroi.getAngle(1) - dphi;
			break;
		case 6:
			pt[0] = oroi.getRadius(1) - size;
			pt[1] = oroi.getAngle(0);
			break;
		case 7:
			pt[0] = oroi.getRadius(1) - size;
			pt[1] = 0.5 * (oroi.getAngle(0) + oroi.getAngle(1) - dphi);
			break;
		case 8:
			pt[0] = oroi.getRadius(1) - size;
			pt[1] = oroi.getAngle(1) - dphi;
			break;
		}
		return pt;
	}

	@SuppressWarnings("null")
	@Override
	public int[] getAnchorPoint(int handle, int size) {
		SectorROI oroi = (SectorROI) roi;
		int[] pt = new int[2];
		double[] cpt = roi.getPoint();
		SectorCoords sc = null;

		switch (handle) {
		case 0:
			sc = new SectorCoords(oroi.getRadius(0), oroi.getAngle(0), false, false);
			break;
		case 1:
			sc = new SectorCoords(oroi.getRadius(0), 0.5 * (oroi.getAngle(0) + oroi.getAngle(1)), false, false);
			break;
		case 2:
			sc = new SectorCoords(oroi.getRadius(0), oroi.getAngle(1), false, false);
			break;
		case 3:
			sc = new SectorCoords(0.5 * (oroi.getRadius(0) + oroi.getRadius(1)), oroi.getAngle(0), false, false);
			break;
		case 4:
			sc = new SectorCoords(0.5 * (oroi.getRadius(0) + oroi.getRadius(1)),
					0.5 * (oroi.getAngle(0) + oroi.getAngle(1)), false, false);
			break;
		case 5:
			sc = new SectorCoords(0.5 * (oroi.getRadius(0) + oroi.getRadius(1)), oroi.getAngle(1), false, false);
			break;
		case 6:
			sc = new SectorCoords(oroi.getRadius(1), oroi.getAngle(0), false, false);
			break;
		case 7:
			sc = new SectorCoords(oroi.getRadius(1), 0.5 * (oroi.getAngle(0) + oroi.getAngle(1)), false, false);
			break;
		case 8:
			sc = new SectorCoords(oroi.getRadius(1), oroi.getAngle(1), false, false);
			break;
		}
		pt[0] = (int) (cpt[0] + sc.getCartesian()[0]);
		pt[1] = (int) (cpt[1] + sc.getCartesian()[1]);
		return pt;
	}

	/**
	 * @param handle
	 * @param spt
	 *            start point
	 * @param ept
	 *            end point
	 * @return resized ROI
	 */
	public SectorROI resize(int handle, double[] spt, double[] ept) {
		SectorROI sroi = null;

		if (handle == 4)
			return sroi;
		sroi = (SectorROI) roi.copy();

		switch (handle) {
		case -1: // new definition
			double t;
			if (spt[0] > ept[0]) {
				t = spt[0];
				spt[0] = ept[0];
				ept[0] = t;
			}
			if (spt[1] > ept[1]) {
				t = spt[1];
				spt[1] = ept[1];
				ept[1] = t;
			}
			sroi.setRadii(spt[0], ept[0]);
			sroi.setAngles(spt[1], ept[1]);
			break;
		case 0:
			sroi.addRadius(0, ept[0] - spt[0]);
			sroi.addAngle(0, ept[1] - spt[1]);
			break;
		case 1:
			sroi.addRadius(0, ept[0] - spt[0]);
			break;
		case 2:
			sroi.addRadius(0, ept[0] - spt[0]);
			sroi.addAngle(1, ept[1] - spt[1]);
			break;
		case 3:
			sroi.addAngle(0, ept[1] - spt[1]);
			break;
		case 5:
			sroi.addAngle(1, ept[1] - spt[1]);
			break;
		case 6:
			sroi.addRadius(1, ept[0] - spt[0]);
			sroi.addAngle(0, ept[1] - spt[1]);
			break;
		case 7:
			sroi.addRadius(1, ept[0] - spt[0]);
			break;
		case 8:
			sroi.addRadius(1, ept[0] - spt[0]);
			sroi.addAngle(1, ept[1] - spt[1]);
			break;
		}
		return sroi;
	}

	/**
	 * Constrained ROI move
	 * @param handle
	 * @param spt
	 * @param ept
	 * @return moved ROI
	 */
	public SectorROI crmove(int handle, double[] spt, double[] ept) {
		SectorROI sroi = null;

		if (handle == 4)
			return sroi;
		sroi = (SectorROI) roi.copy();

		switch (handle) {
		case 0: case 2: case 6: case 8:
			sroi.addRadii(ept[0] - spt[0]);
			sroi.addAngles(ept[1] - spt[1]);
			break;
		case 1: case 7:
			sroi.addRadii(ept[0] - spt[0]);
			break;
		case 3: case 5:
			sroi.addAngles(ept[1] - spt[1]);
			break;
		}
		return sroi;
	}
}
