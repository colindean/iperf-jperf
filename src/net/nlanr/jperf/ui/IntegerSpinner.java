/**
 * - 02/2008: Class created by Nicolas Richasse
 * 
 * Changelog:
 * 	- class created
 * 
 * To do:
 * 	- ...
 */

package net.nlanr.jperf.ui;

import java.awt.Dimension;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class IntegerSpinner extends JSpinner
{
	private SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel();
	
	public IntegerSpinner(int min, int max, int step, int initialValue)
	{
		spinnerNumberModel.setValue(new Integer(initialValue));
		spinnerNumberModel.setStepSize(new Integer(step));
		spinnerNumberModel.setMaximum(new Integer(max));
		spinnerNumberModel.setMinimum(new Integer(min));
		
		this.setModel(spinnerNumberModel);
		this.setPreferredSize(new Dimension(120, 20));
	}
	
	public IntegerSpinner(int min, int max, int initialValue)
	{
		this(min, max, 1, initialValue);
	}
	
	public Integer getValue()
	{
		return ((Number)super.getValue()).intValue();
	}
}