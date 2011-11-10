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

package uk.ac.diamond.scisoft.analysis.rcp.histogram.mapfunctions;

/**
 * Cosine map function return a sin value from the input using some given frequency modulation and
 * optionally returning the absolute value
 */
public class CosMapFunction extends AbstractMapFunction {

	private String functionName;
	private double freqMod;
	private boolean useAbsolute;
	
	/**
	 * CosMapFunction constructor
	 * @param functionName function name
	 * @param frequency frequency modulator
	 * @param useAbs use absolute value
	 */
	public CosMapFunction(String functionName, 
						  double frequency, boolean useAbs)
	{
		this.functionName = functionName;
		this.freqMod = frequency;
		this.useAbsolute = useAbs;
	}
	
	@Override
	public String getMapFunctionName() {
		return functionName;
	}

	@Override
	public double mapFunction(double input) {
		double returnValue = Math.cos(input * freqMod);
		if (useAbsolute)
			returnValue = Math.abs(returnValue);
		return returnValue;	
	}
}
