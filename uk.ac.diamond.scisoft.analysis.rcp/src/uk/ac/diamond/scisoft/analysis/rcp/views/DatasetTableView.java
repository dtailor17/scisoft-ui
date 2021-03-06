/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.views;

import java.io.PrintStream;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataToClipboardCommand;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.export.command.ExportCommand;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.event.FreezeEvent;
import org.eclipse.nebula.widgets.nattable.freeze.event.UnfreezeEvent;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOffCommand;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOnCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.ClearAllSelectionsCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectAllCommand;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.explorers.AbstractExplorer;

/**
 * Display a 2D dataset
 */
public class DatasetTableView extends ViewPart {
	public static final String ID = "uk.ac.diamond.scisoft.analysis.rcp.views.DatasetTableView";
	private Composite pComp;
	private NatTable table = null;
	private DatasetGridLayerStack dStack = null;

	@Override
	public void createPartControl(Composite parent) {
		pComp = parent;
		parent.setLayout(new FillLayout());
		createToolbar(getViewSite().getActionBars());
	}

	@Override
	public void setFocus() {
	}

	/**
	 * Set dataset for table view
	 * 
	 * @param dataset
	 * @param rows
	 *            values for row header, can be null
	 * @param cols
	 *            values for column header, can be null
	 */
	public void setData(IDataset dataset, IDataset rows, IDataset cols) {
		if (dataset.getRank() != 2)
			return;

		if (table == null) {
			dStack = new DatasetGridLayerStack(dataset, rows, cols);
			SelectionLayer sLayer = dStack.getSelectionLayer();
			sLayer.registerCommandHandler(new ExportSelectionCommandHandler(dStack, sLayer));

			table = new NatTable(pComp, dStack, false);
			table.addConfiguration(new DefaultNatTableStyleConfiguration());
			table.configure();
			pComp.layout();
		} else {
			dStack.setData(dataset, rows, cols);
		}
	}

	private void createToolbar(IActionBars bars) {
		Action selAction = new Action() {
			@Override
			public void run() {
				table.doCommand(new SelectAllCommand());
				table.redraw();
			}
		};
		selAction.setToolTipText("Select all in table");
		selAction.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/table_add.png"));

		Action deselAction = new Action() {
			@Override
			public void run() {
				table.doCommand(new ClearAllSelectionsCommand());
				table.redraw();
			}
		};
		deselAction.setToolTipText("Clear selection");
		deselAction.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/table_delete.png"));

		Action clipboardAction = new Action() {
			@Override
			public void run() {
				table.doCommand(new CopyDataToClipboardCommand("\t", "\n", table.getConfigRegistry()));
			}
		};
		clipboardAction.setToolTipText("Copy selection to clipboard");
		clipboardAction.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/table_go.png"));

		Action excelAction = new Action() {
			@Override
			public void run() {
				try {
					table.doCommand(new TurnViewportOffCommand());
					table.doCommand(new ExportCommand(table.getConfigRegistry(), table.getShell()));
					table.doCommand(new TurnViewportOnCommand());
				} catch (Exception e) {
					Status status = new Status(IStatus.ERROR, AnalysisRCPActivator.PLUGIN_ID, e.getCause().getMessage(), e); 
					ErrorDialog.openError(table.getShell(), "Excel export error", "Error exporting Excel table", status);
				}
			}
		};
		excelAction.setToolTipText("Export all as Excel");
		excelAction.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/page_excel.png"));

		Action saveAction = new Action() {
			@Override
			public void run() {
				table.doCommand(new ExportSelectionCommand(table.getShell()));
			}
		};
		saveAction.setToolTipText("Export selection");
		saveAction.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/table_save.png"));

		IToolBarManager mgr = bars.getToolBarManager();
		mgr.add(selAction);
		mgr.add(deselAction);
		mgr.add(clipboardAction);
		mgr.add(excelAction);
		mgr.add(saveAction);
		bars.updateActionBars();
	}
}

class DatasetGridLayerStack extends DefaultGridLayer {

	private DatasetContentProvider bodyDataProvider;
	private DatasetColumnProvider columnHeaderDataProvider;
	private DatasetRowProvider rowHeaderDataProvider;
	private SelectionLayer selectionLayer;
	private DefaultBodyLayerStack bodyLayer;
	private FreezeLayer freezeLayer;
	private boolean rowsCustom = false;
	private boolean colsCustom = false;
	

	public DatasetGridLayerStack(IDataset dataset, IDataset rows, IDataset cols) {
		super(true);
		if (rows != null && rows.getName().startsWith(AbstractExplorer.DIM_PREFIX))
			rows = null;
		rowsCustom = rows != null;
		if (cols != null && cols.getName().startsWith(AbstractExplorer.DIM_PREFIX))
			cols = null;
		colsCustom = cols != null;
		bodyDataProvider = new DatasetContentProvider(dataset, rows, cols);

		rowHeaderDataProvider = new DatasetRowProvider(dataset.getShape()[0], cols);
		columnHeaderDataProvider = new DatasetColumnProvider(dataset.getShape()[1], rows);
		IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider,
				rowHeaderDataProvider);

		init(bodyDataProvider, columnHeaderDataProvider, rowHeaderDataProvider, cornerDataProvider);
		configureFreeze(rowsCustom, colsCustom);
	}

	public void setData(IDataset dataset, IDataset rows, IDataset cols) {
		if (rows != null && rows.getName().startsWith(AbstractExplorer.DIM_PREFIX))
			rows = null;
		rowsCustom = rows != null;
		if (cols != null && cols.getName().startsWith(AbstractExplorer.DIM_PREFIX))
			cols = null;
		colsCustom = cols != null;

		bodyDataProvider.setData(dataset, rows, cols);
		int[] shape = dataset.getShape();
		rowHeaderDataProvider.setData(shape[0], cols);
		if (shape[1] == 1) { // special case of 1D datasets
			String n = dataset.getName();
			if (n != null && n.length() > 0) {
				String[] headers;
				if (rows != null) {
					String header = rows.getName();
					headers = new String[2];
					headers[0] = header == null || header.length() == 0 ? "x" : header;
					headers[1] = n;
				} else {
					headers = new String[] { n };
				}
				columnHeaderDataProvider.setData(1, rows != null ? 1 : 0, headers);
			} else
				columnHeaderDataProvider.setData(1, rows);
		} else {
			columnHeaderDataProvider.setData(shape[1], rows);
		}

		refresh();
		configureFreeze(rowsCustom, colsCustom);
	}

	@Override
	protected void init(IDataProvider bodyDataProvider, IDataProvider columnHeaderDataProvider,
			IDataProvider rowHeaderDataProvider, IDataProvider cornerDataProvider) {
		init(new DataLayer(bodyDataProvider), new DefaultColumnHeaderDataLayer(columnHeaderDataProvider),
				new DefaultRowHeaderDataLayer(rowHeaderDataProvider), new DataLayer(cornerDataProvider));
	}

	@Override
	protected void init(IUniqueIndexLayer bodyDataLayer, IUniqueIndexLayer columnHeaderDataLayer,
			IUniqueIndexLayer rowHeaderDataLayer, IUniqueIndexLayer cornerDataLayer) {
		// Body
		this.bodyDataLayer = bodyDataLayer;
		bodyLayer = new DefaultBodyLayerStack(bodyDataLayer);

		selectionLayer = bodyLayer.getSelectionLayer();
		freezeLayer = new FreezeLayer(selectionLayer);

		final CompositeFreezeLayer compositeFreezeLayer = new CompositeFreezeLayer(freezeLayer, bodyLayer
				.getViewportLayer(), selectionLayer);

		// Column header
		this.columnHeaderDataLayer = columnHeaderDataLayer;
		ILayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, compositeFreezeLayer, selectionLayer);

		// Row header
		this.rowHeaderDataLayer = rowHeaderDataLayer;
		ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, compositeFreezeLayer, selectionLayer);

		// Corner
		this.cornerDataLayer = cornerDataLayer;
		ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);

		setBodyLayer(compositeFreezeLayer);
		setColumnHeaderLayer(columnHeaderLayer);
		setRowHeaderLayer(rowHeaderLayer);
		setCornerLayer(cornerLayer);
	}

	private void refresh() {
		bodyDataLayer.fireLayerEvent(new StructuralRefreshEvent(bodyDataLayer));
	}

	private void configureFreeze(boolean r, boolean c) {
		ViewportLayer v = bodyLayer.getViewportLayer();
		if (r) {
			if (c) {
				freezeLayer.setTopLeftPosition(0, 0);
				freezeLayer.setBottomRightPosition(0, 0);
				v.setMinimumOrigin(1, 1);
				v.fireLayerEvent(new FreezeEvent(v));
			} else {
				freezeLayer.setTopLeftPosition(0, -1);
				freezeLayer.setBottomRightPosition(0, -1);
				v.setMinimumOrigin(1, 0);
				v.fireLayerEvent(new FreezeEvent(v));
			}
		} else {
			if (c) {
				freezeLayer.setTopLeftPosition(-1, 0);
				freezeLayer.setBottomRightPosition(-1, 0);
				v.setMinimumOrigin(0, 1);
				v.fireLayerEvent(new FreezeEvent(v));
			} else {
				freezeLayer.setTopLeftPosition(-1, -1);
				freezeLayer.setBottomRightPosition(-1, -1);
				v.resetOrigin(0, 0);
				v.fireLayerEvent(new UnfreezeEvent(v));
			}
		}
		refresh();
	}

	public SelectionLayer getSelectionLayer() {
		return selectionLayer;
	}

	@Override
	public DefaultBodyLayerStack getBodyLayer() {
		return bodyLayer;
	}

	public boolean isRowHeadersCustom() {
		return rowsCustom;
	}

	public boolean isColHeadersCustom() {
		return colsCustom;
	}

	public IDataProvider getDataProvider() {
		return bodyDataProvider;
	}
}

class DatasetColumnProvider implements IDataProvider {
	int ncol;
	int off;
	private String[] headers;

	/**
	 * @param columns
	 * @param data
	 */
	public DatasetColumnProvider(int columns, IDataset data) {
		setData(columns, data);
	}

	/**
	 * @param columns
	 * @param offset
	 * @param headers
	 */
	public DatasetColumnProvider(int columns, int offset, String[] headers) {
		setData(columns, offset, headers);
	}

	/**
	 * @param columns
	 * @param data
	 */
	public void setData(int columns, IDataset data) {
		ncol = columns;
		off = data != null ? 1 : 0;
		String header = data != null ? data.getName() : null;
		if (header == null || header.length() == 0)
			headers = new String[] {};
		else
			headers = new String[] { header };
	}

	/**
	 * @param columns
	 * @param offset
	 * @param headers
	 */
	public void setData(int columns, int offset, String[] headers) {
		ncol = columns;
		off = offset;
		this.headers = headers;
	}

	@Override
	public int getColumnCount() {
		return ncol + off;
	}

	@Override
	public Object getDataValue(int col, int row) {
		int i = col - off;
		if (col < headers.length)
			return headers[col];
		return i;
	}

	@Override
	public int getRowCount() {
		return 1;
	}

	@Override
	public void setDataValue(int col, int row, Object arg2) {
	}
}

class DatasetRowProvider implements IDataProvider {
	int nrow;
	int off;
	private String header;

	/**
	 * @param rows
	 * @param data
	 */
	public DatasetRowProvider(int rows, IDataset data) {
		setData(rows, data);
	}

	/**
	 * @param rows
	 * @param data
	 */
	public void setData(int rows, IDataset data) {
		nrow = rows;
		off = data != null ? 1 : 0;
		header = data != null ? data.getName() : null;
		if (header == null || header.length() == 0)
			header = "y";
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Object getDataValue(int col, int row) {
		return row >= off ? row - off : header;
	}

	@Override
	public int getRowCount() {
		return nrow + off;
	}

	@Override
	public void setDataValue(int col, int row, Object arg) {
	}
}

class DatasetContentProvider implements IDataProvider {
	IDataset data;
	IDataset drow;
	IDataset dcol;
	int[] shape;
	private int roffset; // one if need to offset row items
	private int coffset;

	/**
	 * @param dataset
	 * @param row
	 * @param column
	 */
	public DatasetContentProvider(IDataset dataset, IDataset row, IDataset column) {
		setData(dataset, row, column);
	}

	/**
	 * @param dataset
	 * @param row
	 * @param column
	 */
	public void setData(IDataset dataset, IDataset row, IDataset column) {
		data = dataset;
		drow = row;
		dcol = column;
		shape = dataset.getShape();
		coffset = drow != null ? 1 : 0;
		if (coffset != 0) {
			shape[1]++;
		}
		roffset = dcol != null ? 1 : 0;
		if (roffset != 0) {
			shape[0]++;
		}
	}

	@Override
	public int getColumnCount() {
		return shape[1];
	}

	@Override
	public Object getDataValue(int col, int row) {
		if (row != 0 && col != 0) {
			return data.getObject(row - roffset, col - coffset);
		}
		if (row == 0) {
			if (col == 0) {
				if (roffset != 0) {
					if (coffset != 0) {
						return "";
					}
					return dcol.getObject(0);
				}
				if (coffset != 0)
					return drow.getObject(0);
				return data.getObject(0, 0);
			}
			final int c = col - coffset;
			if (roffset != 0) {
				return dcol.getObject(c);
			}
			return data.getObject(0, c);
		}
		if (col == 0) {
			final int r = row - roffset;
			if (coffset != 0) {
				return drow.getObject(r);
			}
			return data.getObject(r, 0);
		}

		throw new IllegalStateException("Should not have got here!");
	}

	@Override
	public int getRowCount() {
		return shape[0];
	}

	@Override
	public void setDataValue(int col, int row, Object arg) {
	}
}

class ExportSelectionCommand extends AbstractContextFreeCommand {
	private Shell shell;

	public ExportSelectionCommand(Shell shell) {
		this.shell = shell;
	}

	public Shell getShell() {
		return shell;
	}
}

class ExportSelectionCommandHandler extends AbstractLayerCommandHandler<ExportSelectionCommand> {
	private SelectionLayer selLayer;
	private DatasetGridLayerStack dataLayer;
	private IDataProvider dataProvider;

	public ExportSelectionCommandHandler(DatasetGridLayerStack gridLayer, SelectionLayer selectionLayer) {
		selLayer = selectionLayer;
		dataLayer = gridLayer;
		dataProvider = gridLayer.getDataProvider();
	}

	@Override
	protected boolean doCommand(ExportSelectionCommand command) {
		FileDialog dialog = new FileDialog(command.getShell(), SWT.SAVE);
		dialog.setOverwrite(true);
		dialog.setFileName("table_export.txt");
		dialog.setFilterNames(new String[] { "Ascii text - tab-separated (.txt)",
		"Ascii text - comma-separated (.csv)" });
		dialog.setFilterExtensions(new String[] { "*.txt", "*.csv" });

		String fileName = dialog.open();
		if (fileName == null) {
			return true;
		}

		final String separator = fileName.endsWith(".csv") ? ", " : "\t";
		try {
			final PrintStream stream = new PrintStream(fileName);

			command.getShell().getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					save(stream, separator, "\n");
				}
			});
		} catch (Exception e) {
		 	Status status = new Status(IStatus.ERROR, AnalysisRCPActivator.PLUGIN_ID, e.getMessage(), e); 
		 	ErrorDialog.openError(command.getShell(), "Data export error", "Error exporting data table", status);
		 	return false;
		}

		return true;
	}

	public void save(PrintStream stream, String separator, String rowDelimiter) {
		final StringBuilder textData = new StringBuilder();
		assembleHeaders(textData, separator, selLayer.getSelectedColumnPositions());
		textData.append(rowDelimiter);

		final Set<Range> selectedRows = selLayer.getSelectedRowPositions();
		for (Range range : selectedRows) {
			for (int rowPosition = range.start; rowPosition < range.end; rowPosition++) {
				if (assembleBody(textData, separator, rowPosition))
					textData.append(rowDelimiter);
			}
		}

		stream.append(textData);
		stream.close();
	}

	@Override
	public Class<ExportSelectionCommand> getCommandClass() {
		return ExportSelectionCommand.class;
	}

	protected void assembleHeaders(StringBuilder text, String delimiter, int... selectedColumnPositions) {
		if (dataLayer.isColHeadersCustom()) {
			text.append(dataLayer.getColumnHeaderDataLayer().getDataValueByPosition(0, 0));
			for (int col : selectedColumnPositions) {
				if (col == 0 && dataLayer.isRowHeadersCustom())
					continue;
				text.append(delimiter);
				text.append(dataProvider.getDataValue(col, 0));
			}
		} else {
			for (int col : selectedColumnPositions) {
				if (col == 0 && dataLayer.isRowHeadersCustom())
					continue;
				text.append(delimiter);
				text.append(dataLayer.getColumnHeaderDataLayer().getDataValueByPosition(col, 0));
			}
		}
	}

	protected boolean assembleBody(StringBuilder text, String delimiter, int currentRowPosition) {
		final int[] selectedColumns = selLayer.getSelectedColumnPositions();

		if (currentRowPosition == 0 && dataLayer.isColHeadersCustom())
			return false;

		if (dataLayer.isRowHeadersCustom()) {
			text.append(dataProvider.getDataValue(0, currentRowPosition));
		} else {
			text.append(dataLayer.getRowHeaderDataLayer().getDataValueByPosition(0, currentRowPosition));
		}
		for (int col : selectedColumns) {
			if (selLayer.isCellPositionSelected(col, currentRowPosition)) {
				if (col == 0 && dataLayer.isRowHeadersCustom())
					continue;
				text.append(delimiter);
				text.append(selLayer.getDataValueByPosition(col, currentRowPosition));
			}
		}
		return true;
	}
}
