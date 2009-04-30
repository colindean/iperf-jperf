package net.nlanr.jperf.core;

public enum IperfUnit
{
	ADAPTIVE_BITS("a", "Adaptive Bits"), ADAPTIVE_BYTES("A", "Adaptive Bytes"), BITS("b", "Bits"), BYTES("B", "Bytes"), KBITS("k", "KBits"), KBYTES("K", "KBytes"), MBITS("m", "MBits"), MBYTES("M", "MBytes"), GBITS("g", "GBits"), GBYTES("G", "GBytes");
	
	private String shortcut;
	private String description;
	
	IperfUnit(String shortcut, String description)
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
	
	public static IperfUnit[] getAllowedOutputFormatUnits()
	{
		return new IperfUnit[] {ADAPTIVE_BITS, ADAPTIVE_BYTES, BITS, BYTES, KBITS, KBYTES, MBITS, MBYTES, GBITS, GBYTES};
	}
	
	public static IperfUnit[] getAllowedTCPWindowSizeUnits()
	{
		return new IperfUnit[] {BITS, BYTES, KBITS, KBYTES, MBITS, MBYTES};
	}
	
	public static IperfUnit[] getAllowedTCPMaxSegmentSizeUnits()
	{
		return new IperfUnit[] {BITS, BYTES, KBITS, KBYTES};
	}
	
	public static IperfUnit[] getAllowedUDPPacketSizeUnits()
	{
		return new IperfUnit[] {BITS, BYTES, KBITS, KBYTES, MBITS, MBYTES};
	}
	
	public static IperfUnit[] getAllowedBufferSizeUnits()
	{
		return new IperfUnit[] {BITS, BYTES, KBITS, KBYTES, MBITS, MBYTES};
	}
}
