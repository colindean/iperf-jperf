/*
 * - 04/2009: Class created by Nicolas Richasse
 */

package net.nlanr.jperf.core;

public enum IperfPacketSizeUnit
{
	BITS("b", "Bits"), BYTES("B", "Bytes"), KBITS("k", "KBits"), KBYTES("K", "KBytes");
	
	private String shortcut;
	private String description;
	
	IperfPacketSizeUnit(String shortcut, String description)
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
