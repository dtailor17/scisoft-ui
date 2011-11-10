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

package uk.ac.diamond.scisoft.analysis.rcp.preference;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * An extension to MessageDialog that adds optional text boxes in the {@link #createCustomArea(Composite)}
 */
public class ChangeInteractiveConsoleDialog extends MessageDialog {

	/**
	 * @see MessageDialog#MessageDialog(Shell, String, Image, String, int, String[], int)
	 */
	public ChangeInteractiveConsoleDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage,
			String dialogMessage, int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels,
				defaultIndex);
	}

	/**
	 * Exposed for unit tests only
	 */
	/*package*/ static class BoxWithTitle {
		String title, contents;

		public BoxWithTitle(String title, String contents) {
			this.title = title;
			this.contents = contents;
		}
		
		@Override
		public String toString() {
			return title + "\n" + contents;
		}
	}
	
	/**
	 * Exposed for unit tests only
	 */
	/*package*/ List<BoxWithTitle> getBoxes() {
		return boxes;
	}

	private List<BoxWithTitle> boxes = new LinkedList<BoxWithTitle>();

	/**
	 * Call as many times as desired to add a read-only text with the given title.
	 * 
	 * @param title
	 *            Title of the box
	 * @param contents
	 *            Contents, read-only, can be <code>null</code> to only have a label
	 */
	public void addTextBox(String title, String contents) {
		boxes.add(new BoxWithTitle(title, contents));
	}

	@Override
	protected Control createCustomArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(composite);
		
		
		for (BoxWithTitle box : boxes) {
			Label label = new Label(composite, SWT.NONE);
			label.setText(box.title);
			if (box.contents != null) {
				Text text = new Text(composite, SWT.READ_ONLY | SWT.MULTI | SWT.BORDER);
				text.setText(box.contents);
			}
		}

		return composite;
	}
}
