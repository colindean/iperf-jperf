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

public enum IperfSpeedUnit
{
	KILOBITS_PERSEC("K", "Kbit/sec"), MEGABITS_PERSEC("M", "Mbit/sec");
	
	private String unit;
	private String description;
	IperfSpeedUnit(String unit, String description)
	{
		this.unit = unit;
		this.description = description;
	}
	
	public String getUnit()
	{
		return unit;
	}
	
	public String toString()
	{
		return description;
	}
}
