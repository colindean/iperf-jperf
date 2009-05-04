/**
 * - 02/2008: Class created by Nicolas Richasse
 * - 03/2008: Class updated by Nicolas Richasse
 * 
 * Changelog:
 *-02/2008:
 * 	- class improved
 * 	- a dialog box is displayed when no executable is found instead of writing a console message
 * 	- on windows platforms, if iperf is not found into the system path, JPerf tries to search for it into the .\bin directory
 * 
 *-03/2008:
 * 	- the frame is now centered on screen at startup
 * 
 *-04/2009:
 * 	- URL and version updated
 * 
 *-05/2009:
 *	- System Look'n feel used under windows
 */

package net.nlanr.jperf;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.nlanr.jperf.ui.JPerfUI;

public class JPerf
{
	public static final String JPERF_VERSION = "2.0.2";
	public static final String IPERF_URL = "http://iperf.sourceforge.net";
	
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
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
								"<html>Iperf is probably not in your path!<br>Please download it here '<b><font color='blue'><u>"+IPERF_URL+"</u></font></b>'<br>and put the executable into your <b>PATH</b> environment variable.</html>",
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
				
				// set the locale to EN_US
				Locale.setDefault(Locale.ENGLISH);
				
				// if the OS is Windows, then we set the sytem Look'n Feel
				Properties sysprops = System.getProperties();
				String osName = ((String)sysprops.get("os.name")).toLowerCase();
				if (osName.matches(".*win.*") || osName.matches(".*dos.*") || osName.matches(".*microsoft.*"))
				{
					try 
					{
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					}
					catch (Exception e)
					{
						// nothing
					}
				}
				
				// we start the user interface
				JPerfUI frame = new JPerfUI(iperfCommand, version);
				centerFrameOnScreen(frame);
				frame.setVisible(true);
			}
		});
	}
	
	private static GraphicsDevice screenDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	
	public static void centerFrameOnScreen(JFrame frame)
	{
		Rectangle bounds = frame.getBounds();
		frame.setLocation(
				screenDevice.getDisplayMode().getWidth()/2-bounds.width/2,
				screenDevice.getDisplayMode().getHeight()/2-bounds.height/2);
	}
}
