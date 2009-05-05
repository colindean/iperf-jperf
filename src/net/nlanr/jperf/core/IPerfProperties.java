package net.nlanr.jperf.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class IPerfProperties
{
	public static final String KEY_MODE = "mode";
	public static final String DEFAULT_MODE = "client";
	
	public static final String KEY_SERVER_ADDRESS = "server-address";
	public static final String DEFAULT_SERVER_ADDRESS = "";
	
	public static final String KEY_SERVER_PORT = "server-port";
	public static final int DEFAULT_SERVER_PORT = 5001;
	
	public static final String KEY_PARALLEL_STREAMS = "serverside-parallel-streams";
	public static final int DEFAULT_PARALLEL_STREAMS = 1;
	
	public static final String KEY_LISTEN_PORT = "listen-port";
	public static final int DEFAULT_LISTEN_PORT = 5001;
	
	public static final String KEY_CLIENT_LIMIT = "client-limit";
	public static final String DEFAULT_CLIENT_LIMIT = "";
	
	public static final String KEY_CLIENT_LIMIT_ENABLED = "client-limit-enabled";
	public static final boolean DEFAULT_CLIENT_LIMIT_ENABLED = false;
	
	public static final String KEY_NUM_CONNECTIONS = "clientside-parallel-streams";
	public static final int DEFAULT_NUM_CONNECTIONS = 0;
	
	public static final String KEY_TTL = "ttl";
	public static final int DEFAULT_TTL = 1;
	
	public static final String KEY_TOS = "tos";
	public static final TosOption DEFAULT_TOS = TosOption.NONE;
	
	public static final String KEY_BIND_TO_HOST = "bind-to-host";
	public static final String DEFAULT_BIND_TO_HOST = "";
	
	public static final String KEY_IPV6_ENABLED = "ipv6-enabled";
	public static final boolean DEFAULT_IPV6_ENABLED = false;
	
	public static final String KEY_TRANSPORT_PROTOCOL = "transport-protocol";
	public static final String DEFAULT_TRANSPORT_PROTOCOL = "tcp";
	
	public static final String KEY_TCP_BUFFER_LENGTH = "tcp-buffer-length";
	public static final double DEFAULT_TCP_BUFFER_LENGTH = 2;
	
	public static final String KEY_TCP_BUFFER_LENGTH_UNIT = "tcp-buffer-length-unit";
	public static final IperfUnit DEFAULT_TCP_BUFFER_LENGTH_UNIT = IperfUnit.MBYTES;
	
	public static final String KEY_TCP_BUFFER_LENGTH_ENABLED = "tcp-buffer-length-enabled";
	public static final boolean DEFAULT_TCP_BUFFER_LENGTH_ENABLED = false;
	
	public static final String KEY_TCP_WINDOW_SIZE = "tcp-window-size";
	public static final double DEFAULT_TCP_WINDOW_SIZE = 56;
	
	public static final String KEY_TCP_WINDOW_SIZE_UNIT = "tcp-window-size-unit";
	public static final IperfUnit DEFAULT_TCP_WINDOW_SIZE_UNIT = IperfUnit.KBYTES;
	
	public static final String KEY_TCP_WINDOW_SIZE_ENABLED = "tcp-window-size-enabled";
	public static final boolean DEFAULT_TCP_WINDOW_SIZE_ENABLED = false;
	
	public static final String KEY_TCP_MSS = "tcp-mss";
	public static final double DEFAULT_TCP_MSS = 1;
	
	public static final String KEY_TCP_MSS_UNIT = "tcp-mss-unit";
	public static final IperfUnit DEFAULT_TCP_MSS_UNIT = IperfUnit.KBYTES;
	
	public static final String KEY_TCP_MSS_ENABLED = "tcp-mss-enabled";
	public static final boolean DEFAULT_TCP_MSS_ENABLED = false;
	
	public static final String KEY_TCP_NO_DELAY_ENABLED = "tcp-no-delay-enabled";
	public static final boolean DEFAULT_TCP_NO_DELAY_ENABLED = false; 
	
	public static final String KEY_UDP_BANDWIDTH = "udp-bandwidth";
	public static final double DEFAULT_UDP_BANDWIDTH = 1;
	
	public static final String KEY_UDP_BANDWIDTH_UNIT = "udp-bandwidth-unit";
	public static final IperfSpeedUnit DEFAULT_UDP_BANDWIDTH_UNIT = IperfSpeedUnit.MEGABYTES_PERSEC;
	
	public static final String KEY_UDP_BUFFER_SIZE = "udp-buffer-size";
	public static final double DEFAULT_UDP_BUFFER_SIZE = 41;
	
	public static final String KEY_UDP_BUFFER_SIZE_UNIT = "udp-buffer-size-unit";
	public static final IperfUnit DEFAULT_UDP_BUFFER_SIZE_UNIT = IperfUnit.KBYTES;
	
	public static final String KEY_UDP_BUFFER_SIZE_ENABLED = "udp-buffer-size-enabled";
	public static final boolean DEFAULT_UDP_BUFFER_SIZE_ENABLED = false;
	
	public static final String KEY_UDP_PACKET_SIZE = "udp-packet-size";
	public static final double DEFAULT_UDP_PACKET_SIZE = 1500;
	
	public static final String KEY_UDP_PACKET_SIZE_UNIT = "udp-packet-size-unit";
	public static final IperfUnit DEFAULT_UDP_PACKET_SIZE_UNIT = IperfUnit.BYTES;
	
	public static final String KEY_UDP_PACKET_SIZE_ENABLED = "udp-packet-size-enabled";
	public static final boolean DEFAULT_UDP_PACKET_SIZE_ENABLED = false;
	
	public static final String KEY_COMPATIBILITY_MODE_ENABLED = "compatibility-mode-enabled";
	public static final boolean DEFAULT_COMPATIBILITY_MODE_ENABLED = false;
	
	public static final String KEY_TRANSMIT = "transmit-value";
	public static final int DEFAULT_TRANSMIT = 10;
	
	public static final String KEY_TRANSMIT_UNIT = "transmit-unit";
	public static final String DEFAULT_TRANSMIT_UNIT = "seconds";
	
	public static final String KEY_OUTPUT_FORMAT = "output-format";
	public static final IperfUnit DEFAULT_OUTPUT_FORMAT = IperfUnit.KBITS;
	
	public static final String KEY_REPORT_INTERVAL = "report-interval";
	public static final int DEFAULT_REPORT_INTERVAL = 1;
	
	public static final String KEY_TEST_MODE_DUAL_ENABLED = "test-mode-dual-enabled";
	public static final boolean DEFAULT_TEST_MODE_DUAL_ENABLED = false;
	
	public static final String KEY_TEST_MODE_TRADE_ENABLED = "test-mode-trade-enabled";
	public static final boolean DEFAULT_TEST_MODE_TRADE_ENABLED = false;
	
	public static final String KEY_TEST_MODE_PORT = "test-mode-port";
	public static final int DEFAULT_TEST_MODE_PORT = 5001;
	
	public static final String KEY_PRINT_MSS_ENABLED = "print-mss-enabled";
	public static final boolean DEFAULT_PRINT_MSS_ENABLED = false;
	
	private Properties properties = new Properties();
	
	public IPerfProperties(boolean putDefaultValues)
	{
		if (putDefaultValues)
		{
			put(KEY_MODE, DEFAULT_MODE);
			put(KEY_SERVER_ADDRESS, DEFAULT_SERVER_ADDRESS);
			put(KEY_SERVER_PORT, DEFAULT_SERVER_PORT);
			put(KEY_PARALLEL_STREAMS, DEFAULT_PARALLEL_STREAMS);
			put(KEY_LISTEN_PORT, DEFAULT_LISTEN_PORT);
			put(KEY_CLIENT_LIMIT, DEFAULT_CLIENT_LIMIT);
			put(KEY_CLIENT_LIMIT_ENABLED, DEFAULT_CLIENT_LIMIT_ENABLED);
			put(KEY_NUM_CONNECTIONS, DEFAULT_NUM_CONNECTIONS);
			put(KEY_TTL, DEFAULT_TTL);
			put(KEY_TOS, DEFAULT_TOS);
			put(KEY_BIND_TO_HOST, DEFAULT_BIND_TO_HOST);
			put(KEY_IPV6_ENABLED, DEFAULT_IPV6_ENABLED);
			put(KEY_TRANSPORT_PROTOCOL, DEFAULT_TRANSPORT_PROTOCOL);
			put(KEY_TCP_BUFFER_LENGTH, DEFAULT_TCP_BUFFER_LENGTH);
			put(KEY_TCP_BUFFER_LENGTH_UNIT,	DEFAULT_TCP_BUFFER_LENGTH_UNIT);
			put(KEY_TCP_BUFFER_LENGTH_ENABLED, DEFAULT_TCP_BUFFER_LENGTH_ENABLED);
			put(KEY_TCP_WINDOW_SIZE, DEFAULT_TCP_WINDOW_SIZE);
			put(KEY_TCP_WINDOW_SIZE_UNIT, DEFAULT_TCP_WINDOW_SIZE_UNIT);
			put(KEY_TCP_WINDOW_SIZE_ENABLED, DEFAULT_TCP_WINDOW_SIZE_ENABLED);
			put(KEY_TCP_MSS, DEFAULT_TCP_MSS);
			put(KEY_TCP_MSS_UNIT, DEFAULT_TCP_MSS_UNIT);
			put(KEY_TCP_MSS_ENABLED, DEFAULT_TCP_MSS_ENABLED);
			put(KEY_TCP_NO_DELAY_ENABLED, DEFAULT_TCP_NO_DELAY_ENABLED);  
			put(KEY_UDP_BANDWIDTH, DEFAULT_UDP_BANDWIDTH);
			put(KEY_UDP_BANDWIDTH_UNIT, DEFAULT_UDP_BANDWIDTH_UNIT); 
			put(KEY_UDP_BUFFER_SIZE, DEFAULT_UDP_BUFFER_SIZE);
			put(KEY_UDP_BUFFER_SIZE_UNIT, DEFAULT_UDP_BUFFER_SIZE_UNIT);
			put(KEY_UDP_BUFFER_SIZE_ENABLED, DEFAULT_UDP_BUFFER_SIZE_ENABLED);
			put(KEY_UDP_PACKET_SIZE, DEFAULT_UDP_PACKET_SIZE);
			put(KEY_UDP_PACKET_SIZE_UNIT, DEFAULT_UDP_PACKET_SIZE_UNIT);
			put(KEY_UDP_PACKET_SIZE_ENABLED, DEFAULT_UDP_PACKET_SIZE_ENABLED);
			put(KEY_COMPATIBILITY_MODE_ENABLED, DEFAULT_COMPATIBILITY_MODE_ENABLED);
			put(KEY_TRANSMIT, DEFAULT_TRANSMIT);
			put(KEY_TRANSMIT_UNIT, DEFAULT_TRANSMIT_UNIT);
			put(KEY_OUTPUT_FORMAT, DEFAULT_OUTPUT_FORMAT);
			put(KEY_REPORT_INTERVAL, DEFAULT_REPORT_INTERVAL);
			put(KEY_TEST_MODE_DUAL_ENABLED, DEFAULT_TEST_MODE_DUAL_ENABLED);
			put(KEY_TEST_MODE_TRADE_ENABLED, DEFAULT_TEST_MODE_TRADE_ENABLED);
			put(KEY_TEST_MODE_PORT, DEFAULT_TEST_MODE_PORT);
			put(KEY_PRINT_MSS_ENABLED, DEFAULT_PRINT_MSS_ENABLED);
		}
	}
	
	public IPerfProperties(File propertiesFile) throws Exception
	{
		if (!propertiesFile.exists())
		{
			throw new Exception("The file '"+propertiesFile.getAbsolutePath()+"' does not exist");
		}
		
		properties.load(new FileInputStream(propertiesFile));
	}
	
	public void saveAs(File destinationFile) throws Exception
	{
		destinationFile.delete();
		properties.store(new FileOutputStream(destinationFile), "");
	}
	
	public void put(String key, int value)
	{
		properties.put(key, ""+value);
	}
	
	public int getInteger(String key, int defaultValue)
	{
		try
		{
			String value = (String)properties.get(key);
			return Integer.parseInt(value);
		}
		catch(Exception e)
		{
			return defaultValue;
		}
	}
	
	public void put(String key, double value)
	{
		properties.put(key, ""+value);
	}
	
	public double getDouble(String key, double defaultValue)
	{
		try
		{
			String value = (String)properties.get(key);
			return Double.parseDouble(value);
		}
		catch(Exception e)
		{
			return defaultValue;
		}
	}
	
	public void put(String key, boolean value)
	{
		properties.put(key, ""+value);
	}
	
	public boolean getBoolean(String key, boolean defaultValue)
	{
		try
		{
			String value = (String)properties.get(key);
			return Boolean.parseBoolean(value);
		}
		catch(Exception e)
		{
			return defaultValue;
		}
	}
	
	public void put(String key, IperfUnit value)
	{
		properties.put(key, value.name());
	}
	
	public IperfUnit getUnit(String key, IperfUnit defaultValue)
	{
		try
		{
			String value = (String)properties.get(key);
			return IperfUnit.valueOf(value.toUpperCase());
		}
		catch(Exception e)
		{
			return defaultValue;
		}
	}
	
	public void put(String key, IperfSpeedUnit value)
	{
		properties.put(key, value.name());
	}
	
	public IperfSpeedUnit getSpeedUnit(String key, IperfSpeedUnit defaultValue)
	{
		try
		{
			String value = (String)properties.get(key);
			return IperfSpeedUnit.valueOf(value.toUpperCase());
		}
		catch(Exception e)
		{
			return defaultValue;
		}
	}
	
	public void put(String key, TosOption value)
	{
		properties.put(key, value.name());
	}
	
	public TosOption getTosOption(String key, TosOption defaultValue)
	{
		try
		{
			String value = (String)properties.get(key);
			return TosOption.valueOf(value.toUpperCase());
		}
		catch(Exception e)
		{
			return defaultValue;
		}
	}
	
	public void put(String key, String value)
	{
		properties.put(key, value);
	}
	
	public String getString(String key, String defaultValue)
	{
		String value = (String)properties.get(key);
		if (value == null || value.trim().equals(""))
		{
			return defaultValue;
		}
		else
		{
			return value;
		}
	}
}
