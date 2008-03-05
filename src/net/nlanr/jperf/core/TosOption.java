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

public enum TosOption
{
	NONE("", "None"), LOW_COST("0x02", "Low Cost"), LOW_RELIABILITY("0x10", "Low Reliability"), THROUGHPUT("0x04", "Throughput");
	
	private String code;
	private String description;
	
	TosOption(String code, String description)
	{
		this.code = code;
		this.description = description;
	}
	
	public String getCode()
	{
		return code;
	}
	
	public String toString()
	{
		return description;
	}
}
