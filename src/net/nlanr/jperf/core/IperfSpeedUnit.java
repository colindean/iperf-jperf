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
	KILOBYTES_PERSEC("K", "KBytes/sec"), MEGABYTES_PERSEC("M", "MBytes/sec");
	
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
