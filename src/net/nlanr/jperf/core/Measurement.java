/**
 * - 02/2008: Class created by Nicolas Richasse
 * 
 * Changelog:
 * 	- class created
 * 
 * To do:
 * 	- ...
 */

package net.nlanr.jperf.core;

public class Measurement
{
	private double	start;
	private double	end;
	private double	value;
	private String	units;

	public Measurement(double start, double end, double value, String units)
	{
		this.start = start;
		this.end = end;
		this.value = value;
		this.units = units;
	}

	public double getStartTime()
	{
		return start;
	}

	public double getEndTime()
	{
		return end;
	}

	public double getValue()
	{
		return value;
	}

	public String getUnits()
	{
		return units;
	}

	public void print()
	{
		System.out.println("Start Time: " + start);
		System.out.println("End Time: " + end);
		System.out.println("Value: " + value);
		System.out.println("Units: " + units);
	}
}
