/*-
 * Copyright 2014 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.trace.ILineTrace;
import org.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataSetWithAxisInformation;
/**
 *
 */
public class PlottingScatter2DUI extends AbstractPlottingUI {

	public final static String STATUSITEMID = "uk.ac.diamond.scisoft.analysis.rcp.plotting.PlottingScatter2DUI";
	private IPlottingSystem plottingSystem;
	private Logger logger = LoggerFactory.getLogger(PlottingScatter2DUI.class);

	public PlottingScatter2DUI(IPlottingSystem plotter) {
		this.plottingSystem = plotter;
	}

	@Override
	public void processPlotUpdate(final DataBean dbPlot, final boolean isUpdate) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				Collection<DataSetWithAxisInformation> plotData = dbPlot.getData();
				if (plotData != null) {
					Iterator<DataSetWithAxisInformation> iter = plotData.iterator();
					final List<AbstractDataset> yDatasets = Collections.synchronizedList(new LinkedList<AbstractDataset>());

					int counter = 0;

					while (iter.hasNext()) {
						DataSetWithAxisInformation dataSetAxis = iter.next();
						AbstractDataset data = dataSetAxis.getData();
						yDatasets.add(data);

						AbstractDataset xAxisValues = dbPlot.getAxis(AxisMapBean.XAXIS + Integer.toString(counter));
						AbstractDataset yAxisValues = dbPlot.getAxis(AxisMapBean.YAXIS + Integer.toString(counter));

						plottingSystem.getSelectedYAxis().setTitle(yAxisValues.getName());
						plottingSystem.getSelectedXAxis().setTitle(xAxisValues.getName());
						// if we add points (an update) we do not clear the plot
						if (!isUpdate)
							plottingSystem.clear();
						ILineTrace scatterPlotPoints = plottingSystem.createLineTrace(yAxisValues.getName());
						scatterPlotPoints.setTraceType(TraceType.POINT);
						scatterPlotPoints.setTraceColor(ColorConstants.blue);
						scatterPlotPoints.setPointStyle(PointStyle.FILLED_CIRCLE);
						scatterPlotPoints.setPointSize(6);
						scatterPlotPoints.setName(xAxisValues.getName());
						scatterPlotPoints.setData(xAxisValues, yAxisValues);
						plottingSystem.addTrace(scatterPlotPoints);
						plottingSystem.setTitle("Plot of " + yAxisValues.getName() + " against "+ xAxisValues.getName());
						plottingSystem.autoscaleAxes();
						if (!isUpdate)
							logger.debug("Scatter plot created");
						else
							logger.debug("Scatter plot updated");
						counter++;
					}
				}
			}
		});
	}
}
