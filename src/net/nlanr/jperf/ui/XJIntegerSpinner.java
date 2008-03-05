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
import java.awt.Dimension;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class XJIntegerSpinner extends JSpinner
{
	private SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel();
	
	public XJIntegerSpinner(int min, int max, int step, int initialValue)
	{
		spinnerNumberModel.setValue(new Integer(initialValue));
		spinnerNumberModel.setStepSize(new Integer(step));
		spinnerNumberModel.setMaximum(new Integer(max));
		spinnerNumberModel.setMinimum(new Integer(min));
		
		this.setModel(spinnerNumberModel);
		this.setPreferredSize(new Dimension(120, 20));
		setColors(Color.white, Color.black);
	}
	
	public XJIntegerSpinner(int min, int max, int initialValue)
	{
		this(min, max, 1, initialValue);
	}
	
	public void setColors(Color background, Color foreground)
	{
		getEditor().getComponents()[0].setBackground(background);
		getEditor().getComponents()[0].setForeground(foreground);
	}
	
	public Integer getValue()
	{
		return (Integer)super.getValue();
	}
}