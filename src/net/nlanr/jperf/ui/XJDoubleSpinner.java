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

import java.awt.Color;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class XJDoubleSpinner extends JSpinner
{
	private SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel();
	
	public XJDoubleSpinner(double min, double max, double step, double initialValue)
	{
		spinnerNumberModel.setValue(new Double(initialValue));
		spinnerNumberModel.setStepSize(new Double(step));
		spinnerNumberModel.setMaximum(new Double(max));
		spinnerNumberModel.setMinimum(new Double(min));
		
		this.setModel(spinnerNumberModel);
		
		setColors(Color.white, Color.black);
	}
	
	public XJDoubleSpinner(double min, double max, double initialValue)
	{
		this(min, max, 1.0, initialValue);
	}
	
	public void setColors(Color background, Color foreground)
	{
		getEditor().getComponents()[0].setBackground(background);
		getEditor().getComponents()[0].setForeground(foreground);
	}
	
	public Double getValue()
	{
		Object val = super.getValue();
		if (val instanceof Double)
		{
			return (Double)val;
		}
		else
		{
			return new Double((Integer)super.getValue());
		}
	}
}
