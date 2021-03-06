/*
 * Copyright (c) 2012, 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.analysis.plotclient.connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
//import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisOperation;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DatasetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;

/**
 * Class to create a 1D plotting
 * 
 */
class Plotting1DUI extends PlottingGUIUpdate {
	private static final Logger logger = LoggerFactory.getLogger(Plotting1DUI.class);
	private static final int LEGEND_LIMIT = 5; // maximum number of lines for legend otherwise it is not shown

	/**
	 * Colour idx
	 */
	private int idx;
	/**
	 * trace colour
	 */
//	private Color plotColor;

	/**
	 * Constructor of a plotting 1D 
	 * @param plottingSystem plotting system
	 */
	public Plotting1DUI(IPlottingSystem plottingSystem) {
		super(plottingSystem);
		idx = 0;
	}

	private static boolean isStringOK(String s) {
		return s != null && s.trim().length() > 0;
	}

	@Override
	public void processPlotUpdate(final DataBean dbPlot, boolean isUpdate) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				List<DatasetWithAxisInformation> plotData = dbPlot.getData();
				if (plotData == null)
					return;

				GuiBean gb = dbPlot.getGuiParameters();
				String title = gb == null ? null : (String) gb.get(GuiParameters.TITLE);
				boolean hasTitle = title != null;

				String plotOperation = gb == null ? null : (String) gb.get(GuiParameters.PLOTOPERATION);
				Collection<ITrace> oldTraces = plottingSystem.getTraces();
				int traces = oldTraces.size();
				boolean useOldTraces = false;
				final int plots = plotData.size();
				if (GuiParameters.PLOTOP_NONE.equals(plotOperation) || GuiParameters.PLOTOP_UPDATE.equals(plotOperation)) {
					if (plots > LEGEND_LIMIT) {
						plottingSystem.setShowLegend(false);
					}

					// check if same lines are being plotted
					if (plots <= traces) {
						int nt = 0;
						for (ITrace t : oldTraces) {
							if (t instanceof ILineTrace) {
								String oyn = t.getName();
								Dataset ox = (Dataset)((ILineTrace) t).getXData();
								String oxn = ox == null ? null : ox.getName();
								if (oyn != null && oxn != null) {
									for (DatasetWithAxisInformation d : plotData) {
										String nyn = d.getData().getName();
										String nxn = dbPlot.getAxis(d.getAxisMap().getAxisID()[0]).getName();
										if (oyn.equals(nyn)) {
											if (oxn.equals(nxn)) {
												nt++;
												break;
											}
										}
									}
								}
							}
						}
						useOldTraces = nt == plots;
					}

					if (!useOldTraces) {
						for (ITrace t : oldTraces) {
							if (t instanceof ILineTrace) {
								plottingSystem.removeTrace(t);
							}
						}
						traces = 0;
					}
				}

				if (useOldTraces) {
					List<ITrace> unused = new ArrayList<ITrace>();
					for (ITrace t : oldTraces) {
						if (t instanceof ILineTrace) {
							boolean used = false;
							String oyn = t.getName();
							Dataset x = (Dataset)((ILineTrace) t).getXData();
							String oxn = x == null ? null : x.getName();
							for (DatasetWithAxisInformation d : plotData) {
								Dataset ny = d.getData();
								String nyn = ny.getName();
								Dataset nx = dbPlot.getAxis(d.getAxisMap().getAxisID()[0]);
								String nxn = nx.getName();
								if (oyn != null && oyn.equals(nyn)) {
									if (oxn != null && oxn.equals(nxn)) {
										((ILineTrace) t).setData(nx, ny);
										((ILineTrace) t).repaint();
										used = true;
										break;
									}
								}
							}
							if (!used)
								unused.add(t);
						}
					}
					for (ITrace t : unused) {
						plottingSystem.removeTrace(t);
					}
					// if rescale axis option is checked in the x/y plot menu
					if (plottingSystem.isRescale())
						plottingSystem.autoscaleAxes();
					logger.debug("Plot 1D updated");
				} else {
					// increment the colour
//					int index = traces;
					if (idx > 15) idx = 0; // There are 15 default colours.
//					plotColor = ColorUtility.getSwtColour(index > ColorUtility.getSize() ? idx++ : index);

					List<IAxis> axes = plottingSystem.getAxes();
					Map<String, Dataset> axisData = dbPlot.getAxisData();
					int i = 0; // number of plots
					boolean against = true;
					IAxis firstAxis = null;
					ArrayList<IDataset> yl = new ArrayList<IDataset>();
					for (DatasetWithAxisInformation d : plotData) {
						String[] names = d.getAxisMap().getAxisNames();
						String id = d.getAxisMap().getAxisID()[0];
						String an;
						an = names[0];
						if (!isStringOK(an)) {
							an = AxisMapBean.XAXIS;
						}
						Dataset nx = axisData.get(id);
						String n = nx.getName();
						IAxis ax = findAxis(axes, an);
						if (ax == null || ax.isYAxis()) {
							if (isStringOK(n)) {
								an = n; // override axis name with dataset's name
								ax = findAxis(axes, an); // in case of overwrite by plotting system
							}
							if (ax == null || ax.isYAxis()) {
								// help!
								System.err.println("Haven't found x axis " + an);
								ax = plottingSystem.createAxis(an, false, AxisOperation.BOTTOM);
								axes.add(ax);
							}
						}
						plottingSystem.setSelectedXAxis(ax);
						if (!hasTitle) {
							if (firstAxis == null) {
								firstAxis = ax;
							} else if (ax != firstAxis) {
								against = false;
							}
						}

						an = names[1];
						if (!isStringOK(an)) {
							an = AxisMapBean.YAXIS;
						}
						IAxis ay = findAxis(axes, an);
						if (ay == null || !ay.isYAxis()) {
							// help!
							System.err.println("Haven't found y axis " + an);
							ay = plottingSystem.createAxis(an, true, AxisOperation.LEFT);
							axes.add(ay);
						}
						plottingSystem.setSelectedYAxis(ay);

						Dataset ny = d.getData();
						yl.add(ny);
						String nyn = ny.getName();
						// set a name to the data if none
						if (!isStringOK(nyn)) {
							nyn = "Plot " + i;
							ny.setName(nyn);
						}
						if (!hasTitle) {
							if (i == 0) {
								title = nyn;
							} else if (i < 3) {
								title += ", " + nyn;
							} else if (i == 3) {
								title += "...";
							}
						}
						i++;
						Collection<ITrace> newTraces = plottingSystem.createPlot1D(nx, yl, null, null);
						for (ITrace iTrace : newTraces) {
							final ILineTrace lineTrace = (ILineTrace) iTrace;
							lineTrace.setTraceType(TraceType.SOLID_LINE);
							// if (plotColor != null)
							// lineTrace.setTraceColor(plotColor);
						}
						yl.clear();
					}

					if (!hasTitle && isStringOK(title)) {
						title = "Plot of " + title + (against && firstAxis != null ? " against "  + firstAxis.getTitle() : "");
					}
					plottingSystem.setTitle(title);

					logger.debug("Plot 1D created");
				}
			}
		});
	}

	private static IAxis findAxis(List<IAxis> axes, String n) {
		if (n == null) {
			return null;
		}
		for (IAxis a : axes) {
			String t = a.getTitle();
			if (n.equals(t)) {
				return a;
			}
		}

		return null;
	}

}
