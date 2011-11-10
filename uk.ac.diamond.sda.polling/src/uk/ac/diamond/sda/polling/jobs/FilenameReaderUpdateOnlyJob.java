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

package uk.ac.diamond.sda.polling.jobs;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import uk.ac.diamond.sda.polling.Activator;

public abstract class FilenameReaderUpdateOnlyJob extends FilenameReaderJob {

	ArrayList<String>  oldFilenames = new ArrayList<String>();
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			File file = new File(getJobParameters().get(FILE_NAME));
			FileInputStream fin = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fin);
			BufferedReader br = new BufferedReader(new InputStreamReader(bis));
			
			ArrayList<String> filenames = new ArrayList<String>();
			
			String filename = br.readLine();
			if(filename == null) {
				throw new IOException("No File Specified in drop location");
			}
			
			// otherwise try to load in all the image filenames
			while(filename != null) {
				filenames.add(filename);
				filename = br.readLine();
			}
			
			if(newFilesPresent(oldFilenames, filenames)) {
				processFile(filenames);
				oldFilenames.clear();
				for (String string : filenames) {
					oldFilenames.add(string);
				}
			}
		
		} catch (Exception e) {
			setStatus(e.getLocalizedMessage());
			return new Status(IStatus.INFO, Activator.PLUGIN_ID, e.getLocalizedMessage());
		}
		
		setStatus("OK");
		return Status.OK_STATUS;
	}

	private boolean newFilesPresent(ArrayList<String> oldFilenames, ArrayList<String> filenames) {
		if(oldFilenames.size() != filenames.size()) return true;
		for(int i = 0; i < oldFilenames.size(); i++) {
			if(filenames.get(i).compareTo(oldFilenames.get(i)) != 0) {
				return true;
			}
		}
		return false;
	}

}
