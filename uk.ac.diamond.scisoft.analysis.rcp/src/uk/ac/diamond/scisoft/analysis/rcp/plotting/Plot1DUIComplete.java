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

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataSetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.AxisMode;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot.ISidePlotView;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.PlotActionComplexEvent;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.PlotActionEvent;
import uk.ac.diamond.scisoft.analysis.rcp.views.SidePlotView;


/**
 * A very general UI for 1D Plots using SWT / Eclipse RCP
 * 
 * With complete action set in the toolbar.
 */
public class Plot1DUIComplete extends Plot1DUIAdapter {

	/**
	 * Status item ID
	 */
	public final static String STATUSITEMID = "uk.ac.dimaond.scisoft.analysis.rcp.plotting.Plot1DUI";
	private final static String STATUSSTRING = "Pos: ";
	
	
	private StatusLineContributionItem statusLine;	
	private AxisValues xAxis;
	private IWorkbenchPage page;
	private String plotViewID;
	private PlotWindow plotWindow;
	private List<Action> switchToTabs;
	private IGuiInfoManager manager;
	
	/**
	 * Constructor of a Plot1DUI 
	 * @param window Plot window
	 * @param bars ActionBars from the parent view
	 * @param parent parent composite 
	 * @param page workbench page
	 * @param viewName name of the view associated to this UI
	 */
	
	public Plot1DUIComplete(final PlotWindow window, 
							final IGuiInfoManager manager,
							IActionBars bars, 
					        Composite parent, IWorkbenchPage page,
					        String viewName) {
		super(window.getMainPlotter(), parent, viewName);
		this.page = page;
		plotWindow = window;
		plotViewID = viewName;
		this.xAxis = new AxisValues();
		this.manager = manager;
		
		initSidePlotView();
		buildToolActions(bars.getToolBarManager());
		buildMenuActions(bars.getMenuManager());
		buildStatusLineItems(bars.getStatusLineManager());
		
	}

	@Override
	public ISidePlotView initSidePlotView() {
		try {
			SidePlotView spv = getSidePlotView();
			spv.setPlotView(plotWindow.getMainPlotter(),manager); 
			spv.setSwitchActions(switchToTabs);
			
//			SidePlotUtils.bringToTop(page, spv);
			return spv;
		} catch (IllegalStateException ex) {
			logger.debug("Cannot initiate side plot view", ex);
		}
		return null;
	}

	@Override
	public SidePlotView getSidePlotView() {
		return SidePlotUtils.getSidePlotView(page, plotViewID);
	}

	@Override
	public void deactivate(boolean leaveSidePlotOpen) {
		super.deactivate(leaveSidePlotOpen);
		try {
			getSidePlotView().deactivate(leaveSidePlotOpen);
		} catch (IllegalStateException ex) {
		} catch (NullPointerException ne) {
		}
	}

	/**
	 * 
	 * @param manager
	 */
	@Override
	public void buildStatusLineItems(IStatusLineManager manager)
	{
		statusLine = new StatusLineContributionItem(STATUSITEMID);
		statusLine.setText(STATUSSTRING);
		manager.add(statusLine);
	}
	
	/**
	 * 
	 */
	@Override
	public void buildMenuActions(IMenuManager manager)
	{
		MenuManager xAxis = new MenuManager("X-Axis");
		MenuManager yAxis = new MenuManager("Y-Axis");
		manager.add(xAxis);
		manager.add(yAxis);

		xAxis.add(xLabelTypeRound);
		xAxis.add(xLabelTypeFloat);
		xAxis.add(xLabelTypeExponent);
		xAxis.add(xLabelTypeSI);
		yAxis.add(yLabelTypeRound);
		yAxis.add(yLabelTypeFloat);
		yAxis.add(yLabelTypeExponent);
		yAxis.add(yLabelTypeSI);
		manager.add(yAxisScaleLinear);
		manager.add(yAxisScaleLog);		
		
	}
	
	/**
	 * @param manager 
	 * 
	 */
	@Override
	public void buildToolActions(IToolBarManager manager)
	{
		try {
			switchToTabs = getSidePlotView().createSwitchActions(this);
			for (Action action: switchToTabs) {
				manager.add(action);
			}
		} catch (IllegalStateException ex) {}
			
		manager.add(new Separator(getClass().getName()+"Data"));
		manager.add(displayPlotPos);
		manager.add(rightClickOnGraphAction);
		manager.add(new Separator(getClass().getName()+"History"));
		manager.add(addToHistory);
		manager.add(removeFromHistory);
		manager.add(new Separator(getClass().getName()+"Zoom"));
		manager.add(activateRegionZoom);
		manager.add(activateAreaZoom);
		manager.add(zoomAction);
		manager.add(resetZoomAction);
		manager.add(new Separator(getClass().getName()+"Appearance"));
		manager.add(changeColour);
		manager.add(activateXgrid);
		manager.add(activateYgrid);
		manager.add(new Separator(getClass().getName()+"Print"));
		manager.add(saveGraph);
		manager.add(copyGraph);
		manager.add(printGraph);
		
		// Needed when toolbar is attached to an editor
		// or else the bar looks empty.
		manager.update(true);

	}
	
	@Override
	public void plotActionPerformed(final PlotActionEvent event) {
		if (event instanceof PlotActionComplexEvent)
		{
			parent.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run()
				{
					PlotDataTableDialog dataDialog = 
						new PlotDataTableDialog(parent.getShell(),(PlotActionComplexEvent)event);
					dataDialog.open();								
				}
			});
		} else
		{
			parent.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run()
				{
					String pos;
					if (event.getPosition().length > 2) {
						pos = String.format("%s %g (%g):%g", STATUSSTRING, event.getPosition()[0], event.getPosition()[2], event.getPosition()[1]);
					} else {
						pos = String.format("%s %g:%g", STATUSSTRING, event.getPosition()[0], event.getPosition()[1]);
					}
					statusLine.setText(pos);	
				}
			});
		}
		super.plotActionPerformed(event);
	}

	@Override
	public void processPlotUpdate(DataBean dbPlot, boolean isUpdate) {
		Collection<DataSetWithAxisInformation> plotData = dbPlot.getData();
		
		if (plotData != null) {
			Iterator<DataSetWithAxisInformation> iter = plotData.iterator();
			final List<AbstractDataset> datasets = Collections.synchronizedList(new LinkedList<AbstractDataset>());

			// check for x-axis data
			xAxis.clear();
			AbstractDataset xAxisValues = dbPlot.getAxis(AxisMapBean.XAXIS);
			AbstractDataset xAxisValues2 = dbPlot.getAxis(AxisMapBean.XAXIS2);
			AxisValues xAxes2 = null;
			if (xAxisValues != null) {
				String xName = xAxisValues.getName();
				if (xName != null && xName.length() > 0)
						plotter.setXAxisLabel(xName);
				else
					plotter.setXAxisLabel("X-Axis");
				xAxis.setValues(xAxisValues);
			} else
				plotter.setXAxisLabel("X-Axis");
			
			
			Plot1DGraphTable colourTable = plotter.getColourTable();
			colourTable.clearLegend();
			if (xAxisValues != null)
			{
				plotter.setAxisModes(AxisMode.CUSTOM, AxisMode.LINEAR, AxisMode.LINEAR);
				plotter.setXAxisValues(xAxis, plotData.size());
			} else {
				plotter.setAxisModes(AxisMode.LINEAR, AxisMode.LINEAR, AxisMode.LINEAR);
			}

			if (xAxisValues2 != null) {
				String secondXAxisName = "X-Axis 2";
				if (xAxisValues2.getName() != null &&
					xAxisValues2.getName().length() > 0)
					secondXAxisName = xAxisValues2.getName();
				xAxes2 = new AxisValues(xAxisValues2);
				plotter.setSecondaryXAxisValues(xAxes2,secondXAxisName);
			} else
				plotter.setSecondaryXAxisValues(null,"");
			
			while (iter.hasNext()) {
				DataSetWithAxisInformation dataSetAxis = iter.next();
				AbstractDataset data = dataSetAxis.getData();
				
				Plot1DAppearance newApp = 
					new Plot1DAppearance(PlotColorUtility.getDefaultColour(colourTable.getLegendSize()),
							             PlotColorUtility.getDefaultStyle(colourTable.getLegendSize()),
							             PlotColorUtility.getDefaultLineWidth(colourTable.getLegendSize()),
							             data.getName());
				colourTable.addEntryOnLegend(newApp);
				datasets.add(data);
			}
			
			// calculate an nice label for the Y-Axis
			ArrayList<String> AxisNames = new ArrayList<String>();
			
			for(int i = 0; i < datasets.size(); i++) {
				String name = datasets.get(i).getName();
				if(name != null) {
					AxisNames.add(name);
				}
			}
			String yLabel = "Y-Axis";
			if (AxisNames.size() > 0) {
				yLabel = AxisNames.get(0);
				for (int i = 1; i < AxisNames.size(); i++) {
					yLabel = yLabel + ", " + AxisNames.get(i);
				}
			}
			
			plotter.setYAxisLabel(yLabel);			
			
			int numHistory = plotter.getNumHistory();
			for (int i = 0; i < numHistory; i++) {
				Plot1DAppearance newApp =
					new Plot1DAppearance(PlotColorUtility.getDefaultColour(colourTable.getLegendSize()),
										 PlotColorUtility.getDefaultStyle(colourTable.getLegendSize()),
										 PlotColorUtility.getDefaultLineWidth(colourTable.getLegendSize()),
										 Plot1DUIAdapter.HISTORYSTRING + " " + (i + 1));
				colourTable.addEntryOnLegend(newApp);
			}
			plotter.setPlotUpdateOperation(isUpdate);
			try {
				plotter.replaceAllPlots(datasets);
			} catch (PlotException e) {
				e.printStackTrace();
			}
			
			parent.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					plotter.refresh(true);
					plotter.updateAllAppearance();
					getSidePlotView().processPlotUpdate();
					plotWindow.notifyUpdateFinished();
				}
			});
		}
	}
}
