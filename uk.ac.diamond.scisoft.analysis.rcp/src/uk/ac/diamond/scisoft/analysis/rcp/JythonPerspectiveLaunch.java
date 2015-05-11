/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp;

import org.eclipse.dawnsci.analysis.api.EventTracker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;


public class JythonPerspectiveLaunch implements IWorkbenchWindowActionDelegate{

	@Override
	public void run(IAction action) {
		try {
			PlatformUI.getWorkbench().showPerspective(JythonPerspective.ID, PlatformUI.getWorkbench().getActiveWorkbenchWindow());

			// track perspective launch with perspective name
			IPerspectiveRegistry reg = PlatformUI.getWorkbench().getPerspectiveRegistry();
			IPerspectiveDescriptor per = reg.findPerspectiveWithId(JythonPerspective.ID);
			String perspectiveName = "NA";
			if (per!= null)
				perspectiveName = per.getLabel();
			EventTracker tracker = EventTrackerServiceLoader.getService();
			if (tracker != null)
				tracker.track(perspectiveName);
		} catch (WorkbenchException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		
	}

	@Override
	public void dispose() {
		
	}

	@Override
	public void init(IWorkbenchWindow window) {
		
	}

}
