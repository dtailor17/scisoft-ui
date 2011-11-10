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

package uk.ac.diamond.scisoft.analysis.rcp.plotting.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.AbstractPlotView;

/**
 *
 */
public class PlotActivateXGridAction extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String viewID = event.getParameter("uk.ac.diamond.scisoft.analysis.command.sourceView");
		if (viewID != null)
		{
			final AbstractPlotView apv = (AbstractPlotView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(viewID);
			DataSetPlotter plotter = apv.getPlotter();
			if (plotter != null) {
				Command command = event.getCommand();
				boolean value = HandlerUtil.toggleCommandState(command);
				plotter.setTickGridLines(!value, plotter.getYGridActive(),plotter.getZGridActive());
				plotter.refresh(false);			
			}
			return Boolean.TRUE;
			
		}
		return Boolean.FALSE;
	}

}
