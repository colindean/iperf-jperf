/**
 * - 02/2008: Class created by Nicolas Richasse
 * - 03/2008: Class updated by Nicolas Richasse
 * 
 * Changelog:
 *-02/2008:
 *  - class created
 * 
 *-03/2008:
 *  - acknowledgements panel improved
 *  
 *-04/2009:
 * 	- URL and version updated
 */

package net.nlanr.jperf.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import net.nlanr.jperf.JPerf;

public class JPerfAboutPanel extends JPanel 
{
	private String version;
	
	public JPerfAboutPanel(String version)
	{
		this.version = version;
		init();
	}
	
	private void init()
	{
		this.setLayout(new BorderLayout());
		
		JPanel top = new JPanel();
		top.setLayout(new FlowLayout(FlowLayout.LEFT));
		JTabbedPane bottom = new JTabbedPane();
		
		this.add(top, BorderLayout.NORTH);
		this.add(bottom, BorderLayout.CENTER);
				
		// print out name, print out version, print out copyright (short statement), and web link

		// Iperf graphic
		ImageIcon icon = new ImageIcon(JPerfAboutPanel.class.getResource("Iperf-words.jpg"));
		JLabel pic = new JLabel(icon);
		top.add(pic);

		JPanel info = new JPanel();
		info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

		// Make this bold!
		JLabel name = new JLabel("<html><b>JPERF "+JPerf.JPERF_VERSION+"</b></html>");
		name.setHorizontalAlignment(JLabel.CENTER);
		name.setAlignmentX(Component.CENTER_ALIGNMENT);
		info.add(name);

		JLabel lb_version = new JLabel(version);
		lb_version.setAlignmentX(Component.CENTER_ALIGNMENT);
		info.add(lb_version);

		JLabel author = new JLabel("<html><b>NLANR Distributed Applications Support Team</b></html>");
		author.setHorizontalAlignment(JLabel.CENTER);
		author.setAlignmentX(Component.CENTER_ALIGNMENT);
		info.add(author);

		JLabel webpage = new JLabel("<html><font color='blue'><b><u>"+JPerf.IPERF_URL+"</u></b></font></html>");
		webpage.setHorizontalAlignment(JLabel.CENTER);
		webpage.setAlignmentX(Component.CENTER_ALIGNMENT);
		info.add(webpage);
		top.add(info);

		// set up tabbed pane
		// add developers information
		JPanel devPanel = new JPanel();
		devPanel.setLayout(new BoxLayout(devPanel, BoxLayout.Y_AXIS));
		
		JLabel dev;
		
		dev = new JLabel("  ");
		dev.setAlignmentX(Component.CENTER_ALIGNMENT);
		devPanel.add(dev);
		
		dev = new JLabel("JPerf 2.x.x developers:");
		dev.setAlignmentX(Component.CENTER_ALIGNMENT);
		devPanel.add(dev);
		dev = new JLabel("Nicolas Richasse");
		dev.setAlignmentX(Component.CENTER_ALIGNMENT);
		devPanel.add(dev);
		
		dev = new JLabel("  ");
		dev.setAlignmentX(Component.CENTER_ALIGNMENT);
		devPanel.add(dev);
		dev = new JLabel("  ");
		dev.setAlignmentX(Component.CENTER_ALIGNMENT);
		devPanel.add(dev);
		
		dev = new JLabel("JPerf 1.x.x developers:");
		dev.setAlignmentX(Component.CENTER_ALIGNMENT);
		devPanel.add(dev);
		dev = new JLabel("Mark Gates");
		dev.setAlignmentX(Component.CENTER_ALIGNMENT);
		devPanel.add(dev);
		dev = new JLabel("Ajay Tirumala");
		dev.setAlignmentX(Component.CENTER_ALIGNMENT);
		devPanel.add(dev);
		dev = new JLabel("Jim Ferguson");
		dev.setAlignmentX(Component.CENTER_ALIGNMENT);
		devPanel.add(dev);
		dev = new JLabel("Jon Dugan");
		dev.setAlignmentX(Component.CENTER_ALIGNMENT);
		devPanel.add(dev);
		dev = new JLabel("Feng Qin");
		dev.setAlignmentX(Component.CENTER_ALIGNMENT);
		devPanel.add(dev);
		dev = new JLabel("Kevin Gibbs");
		dev.setAlignmentX(Component.CENTER_ALIGNMENT);
		devPanel.add(dev);
		dev = new JLabel("Tanya Brethour");
		dev.setAlignmentX(Component.CENTER_ALIGNMENT);
		devPanel.add(dev);
		dev = new JLabel("National Laboratory for Applied Network Research (NLANR)");
		dev.setAlignmentX(Component.CENTER_ALIGNMENT);
		devPanel.add(dev);
		dev = new JLabel("National Center for Supercomputing Applications (NCSA)");
		dev.setAlignmentX(Component.CENTER_ALIGNMENT);
		devPanel.add(dev);
		dev = new JLabel("University of Illinois at Urbana-Champaign (UIUC)");
		dev.setAlignmentX(Component.CENTER_ALIGNMENT);
		devPanel.add(dev);
		dev = new JLabel("http://www.ncsa.uiuc.edu");
		dev.setAlignmentX(Component.CENTER_ALIGNMENT);
		devPanel.add(dev);
		dev = new JLabel("API from http://swinglabs.org/");
		dev.setAlignmentX(Component.CENTER_ALIGNMENT);
		devPanel.add(dev);
		dev = new JLabel("API from http://www.jgoodies.com/");
		dev.setAlignmentX(Component.CENTER_ALIGNMENT);
		devPanel.add(dev);
		dev = new JLabel("API from http://www.jfree.org/jfreechart/");
		dev.setAlignmentX(Component.CENTER_ALIGNMENT);
		devPanel.add(dev);
		
		bottom.addTab("Developers", null, new JScrollPane(devPanel));

		JPanel ackPanel = new JPanel(new BorderLayout());
		JTextArea ack = new JTextArea(
				"Thanks to Mark Gates (NLANR), Alex Warshavsky (NLANR) and Justin Pietsch (University of Washington) who were responsible for the 1.1.x releases of Iperf.\n" +
				"For this release, we would like to thank Bill Cerveny (Internet2), Micheal Lambert (PSC), Dale Finkelson (UNL) and Matthew Zekauskas (Internet2) for help in getting access to IPv6 networks / machines.\n" +
				"Special thanks to Matthew Zekauskas (Internet2) for helping out in the FreeBSD implementation.\n" +
				"Also, thanks to Kraemer Oliver (Sony) for providing an independent implementation of IPv6 version of Iperf, which provided a useful comparison for testing our features.");
		ack.setBackground(new Color(220, 239, 206));
		ack.setEditable(false);
		ack.setLineWrap(true);
		ack.setWrapStyleWord(true);
		ackPanel.add(ack, BorderLayout.CENTER);
		bottom.addTab("Acknowledgements", null, new JScrollPane(ackPanel));

		JTextArea license = new JTextArea();
		JScrollPane licensePanel = new JScrollPane(license);
		bottom.addTab("License", null, licensePanel);
		license.setEditable(false);
		try
		{
			InputStream inIS = this.getClass().getResourceAsStream("/license.txt");
			BufferedReader in = new BufferedReader(new InputStreamReader(inIS));
			String line = in.readLine();
			while (line != null)
			{
				license.append(line);
				license.append("\n");
				line = in.readLine();
			}
		}
		catch (FileNotFoundException f)
		{
			license.append("Error: " + f.getMessage());
		}
		catch (IOException e)
		{
			license.append("Error: " + e.getMessage());
		}
	}
}
