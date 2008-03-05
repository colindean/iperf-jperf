/**
 * - 02/2008: Class created by Nicolas Richasse
 * 
 * Changelog:
 * 	- class improved
 * 	- a dialog box is displayed when no executable is found instead of writing a console message
 * 	- on windows platforms, if iperf is not found into the system path, JPerf tries to search for it into the .\bin directory
 * 
 * To do:
 * 	- ...
 */

package net.nlanr.jperf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.swing.JOptionPane;

import net.nlanr.jperf.ui.JPerfUI;

public class JPerf
{
	public static void main(String[] args)
	{
		String iperfCommand = "iperf";
		String version = "";
		Process process;
		
		// get version of Iperf
		try
		{
			process = Runtime.getRuntime().exec(iperfCommand+" -v");
		}
		catch (Exception ioe)
		{
			Properties sysprops = System.getProperties();
			String osName = ((String)sysprops.get("os.name")).toLowerCase();
			
			if (new File("bin/iperf.exe").exists() && (osName.matches(".*win.*") || osName.matches(".*microsoft.*")))
			{
				iperfCommand = "bin/iperf.exe";
				try
				{
					process = Runtime.getRuntime().exec(iperfCommand+" -v");
				}
				catch(Exception ex)
				{
					JOptionPane.showMessageDialog(
							null, 
							"<html>"+
							"Impossible to start the iperf executable located here : <br>"+
							new File(iperfCommand).getAbsolutePath()+
							"</html>",
							"Error",
							JOptionPane.ERROR_MESSAGE);
					System.exit(1);
					return;
				}
			}
			else
			{
				JOptionPane.showMessageDialog(
						null, 
						"<html>Iperf is probably not in your path!<br>Please download it here '<b><font color='blue'><u>http://dast.nlanr.net/Projects/Iperf/</u></font></b>'<br>and put the executable into your <b>PATH</b> environment variable.</html>",
						"Iperf not found",
						JOptionPane.ERROR_MESSAGE);
				System.exit(1);
				return;
			}
		}
		
		// try to read the Iperf version on the standard output
		BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
		try
		{
			String line;
			line = input.readLine();

			while (line != null)
			{
				version = line;
				line = input.readLine();
			}
		}
		catch (IOException e)
		{
			// nothing
		}
		
		if (version == null || version.trim().equals(""))
		{
			// try to read the Iperf version on the error output
			input = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			try
			{
				String line;
				line = input.readLine();

				while (line != null)
				{
					version = line;
					line = input.readLine();
				}
			}
			catch (IOException e)
			{
				// nothing
			}
		}
		
		if (version == null || version.trim().equals(""))
		{
			version = "iperf version 1.0.0";
			System.err.println("Impossible to get iperf version. Using '"+version+"' as default.");
		}
		
		new JPerfUI(iperfCommand, version).setVisible(true);
	}
}
