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
package uk.ac.diamond.scisoft.mappingexplorer.views;

import org.eclipse.jface.viewers.ISelection;

/**
 * @author rsr31645
 */
public class AxisSelection implements ISelection {
	private String dimLabel;
	private int dimension;

	public AxisSelection(String dimLabel, int dimension) {
		this.dimLabel = dimLabel;
		this.dimension = dimension;
	}

	public String getLabel() {
		return dimLabel;
	}

	public int getDimension() {
		return dimension;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

}
