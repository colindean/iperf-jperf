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

public enum OutputFormat
{
	ADAPTIVE_BITS("a", "Adaptive Bits"), ADAPTIVE_BYTES("A", "Adaptive Bytes"), BITS("b", "Bits"), BYTES("B", "Bytes"), GBITS("g", "GBits"), GBYTES("G", "GBytes"), KBITS("k", "KBits"), KBYTES("K", "KBytes"), MBITS("m", "MBits"), MBYTES("M", "MBytes");
	
	private String shortcut;
	private String description;
	OutputFormat(String shortcut, String description)
	{
		this.shortcut = shortcut;
		this.description = description;
	}
	
	public String getShortcut()
	{
		return shortcut;
	}
	
	public String toString()
	{
		return description;
	}
}
