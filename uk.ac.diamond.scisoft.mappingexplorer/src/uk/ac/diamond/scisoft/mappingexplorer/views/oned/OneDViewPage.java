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
package uk.ac.diamond.scisoft.mappingexplorer.views.oned;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPart2;
import org.eclipse.ui.part.PageBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.editors.HDF5TreeEditor;
import uk.ac.diamond.scisoft.analysis.rcp.hdf5.HDF5Selection;
import uk.ac.diamond.scisoft.analysis.rcp.inspector.DatasetSelection;
import uk.ac.diamond.scisoft.mappingexplorer.views.BaseViewPageComposite;
import uk.ac.diamond.scisoft.mappingexplorer.views.BaseViewPageComposite.ViewPageCompositeListener;
import uk.ac.diamond.scisoft.mappingexplorer.views.HDF5MappingViewData;
import uk.ac.diamond.scisoft.mappingexplorer.views.IContributingPart;
import uk.ac.diamond.scisoft.mappingexplorer.views.IMappingView2dData;
import uk.ac.diamond.scisoft.mappingexplorer.views.IMappingView3dData;
import uk.ac.diamond.scisoft.mappingexplorer.views.IMappingViewData;
import uk.ac.diamond.scisoft.mappingexplorer.views.IMappingViewDataContainingPage;
import uk.ac.diamond.scisoft.mappingexplorer.views.MappingPageBookViewPage;
import uk.ac.diamond.scisoft.mappingexplorer.views.twod.BlankPageComposite;

/**
 * @author rsr31645
 */
public class OneDViewPage extends MappingPageBookViewPage implements IMappingViewDataContainingPage {

	private static final Logger logger = LoggerFactory.getLogger(OneDViewPage.class);

	private Composite rootComposite;
	private PageBook pgBook;
	private BlankPageComposite blankPageComposite;
	private OneD3DViewPageComposite oneDShowing3DPage;
	private BaseViewPageComposite activePage;

	private String nodeName;

	private IMappingViewData data;

	public OneDViewPage(IWorkbenchPart partCreatedFor, String secondaryViewId) {
		super(partCreatedFor, secondaryViewId);
	}

	@Override
	protected void doSelectionForEditorSelectionChange(IWorkbenchPart part, ISelection selection) {
		if (this.part.equals(part) && selection instanceof DatasetSelection) {
			HDF5Selection hdf5Selection = (HDF5Selection) selection;
			if (nodeName == null || (nodeName != null && !hdf5Selection.getNode().equals(nodeName))) {
				nodeName = hdf5Selection.getNode();
				IMappingViewData mappingViewData = HDF5MappingViewData.getMappingViewData((DatasetSelection) selection);

				setMappingViewData(mappingViewData);
			}
		}
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		rootComposite = new Composite(parent, SWT.None);
		rootComposite.setLayout(new FillLayout());

		pgBook = new PageBook(rootComposite, SWT.None);

		blankPageComposite = new BlankPageComposite(pgBook, SWT.None);

		oneDShowing3DPage = new OneD3DViewPageComposite(this, pgBook, SWT.None);

		activePage = blankPageComposite;

		pgBook.showPage(activePage);

	}

	@Override
	public Control getControl() {
		return rootComposite;
	}

	@Override
	public void setFocus() {
		// Do nothing
		logger.warn("Focus gained in one d:{}", nodeName);
	}

	@Override
	public ISelection getSelection() {
		return null;
	}

	public void setMappingViewData(IMappingViewData data) {
		if (this.data != data) {
			this.data = data;
			if (data instanceof IMappingView3dData) {
				IMappingView3dData dataIn3D = (IMappingView3dData) data;
				if (!activePage.equals(oneDShowing3DPage)) {
					activePage.removeCompositeSelectionListener(pageSelectionListener);

					oneDShowing3DPage.addCompositeSelectionListener(pageSelectionListener);
					oneDShowing3DPage.setMappingViewData(dataIn3D);
					try {
						oneDShowing3DPage.initialPlot();
					} catch (Exception e) {
						logger.error("Initial plotting error in 3D view page {}", e);
					}
				} else {
					oneDShowing3DPage.setMappingViewData(dataIn3D);
					try {
						oneDShowing3DPage.initialPlot();
					} catch (Exception e) {
						logger.error("Initial plotting error in 3D view page {}", e);
					}
				}
				activePage = oneDShowing3DPage;
			} else {
				activePage.removeCompositeSelectionListener(pageSelectionListener);
				activePage = blankPageComposite;
			}
			pgBook.showPage(activePage);
		}
	}

	private ViewPageCompositeListener pageSelectionListener = new ViewPageCompositeListener() {

		@Override
		public void selectionChanged(ISelection selection) {
			fireNotifySelectionChanged(selection);
		}

	};

	@Override
	public void initialPlot() throws Exception {
		activePage.initialPlot();
	}

	@Override
	public void setSelection(ISelection selection) {
		if (activePage != null) {
			activePage.selectionChanged(null, selection);
		}
	}

	@Override
	public IMappingView2dData getMappingViewData() {
		return null;
	}

	@Override
	public String getOriginIdentifer() {
		if (!activePage.equals(blankPageComposite)) {
			if (this.part instanceof HDF5TreeEditor) {
				return nodeName;
			} else if (this.part instanceof IWorkbenchPart2) {
				IWorkbenchPart2 wp2 = (IWorkbenchPart2) this.part;
				return wp2.getPartName();
			}
		}
		return null;
	}

	protected void doSelectionChangedOnTwoDView(IWorkbenchPart part, ISelection selection) {
		if (part == null || selection == null) {
			// this will be the effect of using a INullSelectionListener
			activePage.selectionChanged(null, null);
		} else {
			String secondaryId = ((IViewPart) part).getViewSite().getSecondaryId();
			if (getSecondaryViewId() != null && getSecondaryViewId().equals(secondaryId)) {
				activePage.selectionChanged(part, selection);
			} else {
				// this needs to be filtered to know whether the selection
				// changed is coming from a part that is referring to the same
				// editor
				Object objContributingPart = part.getAdapter(IContributingPart.class);
				if (objContributingPart != null && objContributingPart.equals(this.part)) {
					activePage.selectionChanged(part, selection);
				}
			}
		}
	}

	@Override
	public void dispose() {
		activePage.removeCompositeSelectionListener(pageSelectionListener);
		super.dispose();
	}

}
