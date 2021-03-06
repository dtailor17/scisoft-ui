/*
 * Copyright (c) 2012, 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.analysis.rcp;

import org.dawb.common.ui.perspective.AbstractPerspectiveLaunch;

public class JythonPerspectiveLaunch extends AbstractPerspectiveLaunch {

	@Override
	public String getID() {
		return JythonPerspective.ID;
	}
}
