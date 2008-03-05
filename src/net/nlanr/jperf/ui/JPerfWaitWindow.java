/**
 * - 02/2008: Class created by Nicolas Richasse
 * 
 * Changelog:
 * 	- class created
 * 
 * To do:
 * 	- ...
 */


package net.nlanr.jperf.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.jdesktop.swingx.JXBusyLabel;

public class JPerfWaitWindow extends JDialog
{
	private JFrame parent;
	
	public JPerfWaitWindow(JFrame parent)
	{
		super(parent, "Stopping iperf...", false);
		this.parent = parent;
		init();
	}
	
	private void init()
	{
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		JLabel messageLabel = new JLabel("Please wait while iPerf is stopping...");
		messageLabel.setHorizontalAlignment(JLabel.CENTER);
		JXBusyLabel busyLabel = new JXBusyLabel();
		busyLabel.setOpaque(false);
		busyLabel.setBusy(true);
		busyLabel.setHorizontalAlignment(JLabel.CENTER);
		this.setLayout(new BorderLayout());
		this.add(messageLabel, BorderLayout.NORTH);
		this.add(busyLabel, BorderLayout.CENTER);
		this.setSize(new Dimension(300, 80));
		
		this.setLocationRelativeTo(parent);
	}
}
