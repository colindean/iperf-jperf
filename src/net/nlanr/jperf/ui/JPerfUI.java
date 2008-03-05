/*
 * - 02/2008: Class updated by Nicolas Richasse
 * 
 * Changelog:
 * 	- code refactoring
 * 	- UI improved with the SwingX API and the Forms library
 * 	- graphs display improved
 * 	- the iperf command-line associated to the current parameters is displayed and updated in real-time
 * 	- 'Restore defaults' button added
 *  - the output window automatically scrolls as the output goes beyond the end of it
 *  - some bugs fixed when parsing the iperf output
 *	
 * To do: 
 * 	- ...
 *  
 * Old Notes:
 *	- If I have time, I'll try to throw together a help file.  I doubt I'll
 *	be able to pull it off though since I only have 2 weeks left.  If you
 *  want to do one, I recommend putting in (1) description (duh), (2) the
 *  command-line option equivalent, (3) what other options are necessary
 *  for this option to be used (ex: UDP bandwidth requires client and UDP),
 *  (4) default values.  Anyway, I'll try to at least start it (DC)
 **/

package net.nlanr.jperf.ui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import net.nlanr.jperf.core.IperfSizeUnit;
import net.nlanr.jperf.core.IperfSpeedUnit;
import net.nlanr.jperf.core.IperfThread;
import net.nlanr.jperf.core.Measurement;
import net.nlanr.jperf.core.OutputFormat;
import net.nlanr.jperf.core.TosOption;
import net.nlanr.jperf.ui.FormLayoutBuilder.Alignment;
import net.nlanr.jperf.ui.chart.IPerfChartPanel;
import net.nlanr.jperf.ui.chart.SeriesColorGenerator;

import java.io.*;

public class JPerfUI extends JFrame
	implements ActionListener, KeyListener, ChangeListener, WindowListener
{
	private JPerfAboutPanel	aboutPanel;

	// Menu stuff
	private JMenuBar				menuBar;
	private JMenu						menu;
	private JMenuItem				menuItem;

	// Panels
	private JSplitPane			centerPanel;
	private JTabbedPane			tabbedPane;
	private JScrollPane			outputScrollPane;
	private JTextArea				output;

	// quickstart parameters
	private JTextField			iperfCommandLabel;
	private JRadioButton		serverModeRadioButton, clientModeRadioButton;
	private JCheckBox				lb_clientLimit;
	private JLabel					lb_serverAddress, lb_serverPort, lb_listenPort, lb_simultaneousConnectionsNumber, lb_connectionsLimitNumber;
	private JTextField			serverAddress;
	private JTextField			clientLimit;
	private XJIntegerSpinner	serverPort, listenPort;
	private XJIntegerSpinner	simultaneousConnectionsNumber, connectionsLimitNumber;
	private JButton						startIperf, stopIperf, restoreDefaults;

	// transport parameters
	private JRadioButton			tcpRadioButton, udpRadioButton;
	private JCheckBox					lb_tcpBufferLength;
	private XJIntegerSpinner	tcpBufferLength;
	private JComboBox					tcpBufferSizeUnit;
	private JCheckBox					lb_tcpWindowSize;
	private XJIntegerSpinner	tcpWindowSize;
	private JComboBox					tcpWindowSizeUnit;
	private JCheckBox					lb_mss;
	private XJIntegerSpinner	mss;
	private JComboBox					mssUnit;
	private JCheckBox					lb_udpBufferSize;
	private XJIntegerSpinner	udpBufferSize;
	private JComboBox					udpBufferSizeUnit;
	private JCheckBox					lb_udpPacketSize;
	private XJIntegerSpinner	udpPacketSize;
	private JComboBox					udpPacketSizeUnit;
	private JLabel						lb_udpBandwidth;
	private XJIntegerSpinner	udpBandwidth;
	private JComboBox					udpBandwidthUnit;
	private JCheckBox					tcpNoDelay;

	// ip parameters
	private JLabel						lb_bindHost, lb_TTL;
	private JTextField				bindhost;
	private XJIntegerSpinner	TTL;
	private JComboBox					tos;

	// other parameters
	private JCheckBox					printMSS;
	private JTextField				representativeFile;
	private XJIntegerSpinner	transmit;
	private JComboBox					formatList;
	private XJIntegerSpinner	interval;
	private ButtonGroup				iperfModeButtonGroup;
	private ButtonGroup				protocolButtonGroup;
	private JRadioButton			transmitBytesRadioButton, transmitSecondsRadioButton;
	private JCheckBox					ipv6;
	private IperfThread				iperf;
	private JCheckBox					alwaysClearOutput;
	private JButton						clearOutputButton, saveOutputButton;
	private JCheckBox					compatibilityMode;
	private XJIntegerSpinner	testPort;
	private JCheckBox					dualMode, tradeMode;
	private JButton						browse;
	private float							iperfVersion;

	private JToolBar					toolbar;

	// labels only for disabling stuff
	private JLabel						lb_transmit, lb_tos, lb_representativeFile, lb_testingMode, lb_testPort, lb_outputFormat, lb_reportInterval;

	// to run iperf
	private String						options;
	private String						version;

	private String						iperfCommand;

	public JPerfUI(String iperfCommand, String version)
	{
		super("JPerf 2.0 - Network performance measurement graphical tool");
		/*
		 * When the jperf gui is first started up, the version is obtained. It is
		 * passed when creating an JperfGUI we use the version number to disable or
		 * enable some features
		 */
		this.iperfCommand = iperfCommand;
		this.version = version;

		// set current version
		String[] version_split = version.split(" ");
		String vers = version_split[2].replace('.', '-');
		String[] version_num = vers.split("-");
		iperfVersion = (new Float(version_num[1])).floatValue();
		iperfVersion /= 10.0;
		iperfVersion += (new Float(version_num[0])).floatValue();

		// set up main panels
		init();

		// apply default values
		applyDefaultValues();

		// start the stats panel
		chartPanel.start();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.setSize(970, 700);
		this.setVisible(true);

		// set focus on the server address field
		serverAddress.grabFocus();
	}

	private JPanel	quickStartPanel		= null;
	private JPanel	applicationPanel	= null;
	private JPanel	transportPanel		= null;
	private JPanel	tcpPanel					= null;
	private JPanel	udpPanel					= null;
	private JPanel	ipPanel						= null;

	private void applyDefaultValues()
	{
		setServerModeSelected(false);
		setClientModeSelected(true);
		clientModeRadioButton.doClick();
		setUDPOptionsEnabled(false);
		setTCPOptionsEnabled(true);
		tcpRadioButton.doClick();

		serverPort.setValue(5001);
		testPort.setValue(5001);
		tcpBufferLength.setValue(2);
		tcpBufferSizeUnit.setSelectedItem(IperfSizeUnit.MBYTES);
		tcpWindowSize.setValue(56);
		tcpWindowSizeUnit.setSelectedItem(IperfSizeUnit.KBYTES);
		mss.setValue(1);
		mssUnit.setSelectedItem(IperfSizeUnit.KBYTES);

		udpBufferSize.setValue(41);
		udpBufferSizeUnit.setSelectedItem(IperfSizeUnit.KBYTES);
		udpPacketSize.setValue(32);
		udpPacketSizeUnit.setSelectedItem(IperfSizeUnit.KBYTES);

		// desactivate compatibility mode
		compatibilityMode.setSelected(true);
		compatibilityMode.doClick();

		// desactivate IPv6
		ipv6.setSelected(true);
		ipv6.doClick();

		// deselect dualmode and trademode
		dualMode.setSelected(false);
		tradeMode.setSelected(false);
	}

	private void setClientModeSelected(boolean clientModeSelected)
	{
		serverAddress.setEnabled(clientModeSelected);
		lb_serverAddress.setEnabled(clientModeSelected);

		lb_serverPort.setEnabled(clientModeSelected);
		serverPort.setEnabled(clientModeSelected);

		lb_simultaneousConnectionsNumber.setEnabled(clientModeSelected);
		simultaneousConnectionsNumber.setEnabled(clientModeSelected);

		lb_representativeFile.setEnabled(iperfVersion >= 1.2 && clientModeSelected);
		representativeFile.setEnabled(iperfVersion >= 1.2 && clientModeSelected);
		browse.setEnabled(iperfVersion >= 1.2 && clientModeSelected);

		lb_testingMode.setEnabled(!compatibilityMode.isSelected() && iperfVersion >= 1.7 && clientModeSelected);
		dualMode.setEnabled(!compatibilityMode.isSelected() && iperfVersion >= 1.7 && clientModeSelected);
		tradeMode.setEnabled(!compatibilityMode.isSelected() && iperfVersion >= 1.7 && clientModeSelected);
		lb_testPort.setEnabled(!compatibilityMode.isSelected() && iperfVersion >= 1.7 && clientModeSelected);
		testPort.setEnabled(!compatibilityMode.isSelected() && iperfVersion >= 1.7 && clientModeSelected);
	}

	private void setServerModeSelected(boolean serverModeSelected)
	{
		// set default values for compatibility
		lb_clientLimit.setEnabled(false);
		clientLimit.setEnabled(false);
		lb_connectionsLimitNumber.setEnabled(false);
		connectionsLimitNumber.setEnabled(false);

		// application layer
		lb_transmit.setEnabled(!serverModeSelected);
		transmit.setEnabled(!serverModeSelected);
		transmitSecondsRadioButton.setEnabled(!serverModeSelected);
		transmitBytesRadioButton.setEnabled(!serverModeSelected);
		lb_testingMode.setEnabled(!serverModeSelected);
		dualMode.setEnabled(!serverModeSelected);
		lb_testPort.setEnabled(!serverModeSelected);
		tradeMode.setEnabled(!serverModeSelected);
		testPort.setEnabled(!serverModeSelected);
		lb_representativeFile.setEnabled(!serverModeSelected);
		representativeFile.setEnabled(!serverModeSelected);
		browse.setEnabled(!serverModeSelected);

		// transport layer
		lb_udpBandwidth.setEnabled(!serverModeSelected && udpRadioButton.isSelected());
		udpBandwidth.setEnabled(!serverModeSelected && udpRadioButton.isSelected());
		udpBandwidthUnit.setEnabled(!serverModeSelected && udpRadioButton.isSelected());

		// IP layer
		lb_tos.setEnabled(!serverModeSelected);
		tos.setEnabled(!serverModeSelected);
		lb_TTL.setEnabled(!serverModeSelected);
		TTL.setEnabled(!serverModeSelected);

		if (iperfVersion >= 1.7)
		{
			lb_clientLimit.setEnabled(serverModeSelected);
			clientLimit.setEnabled(serverModeSelected);

			lb_connectionsLimitNumber.setEnabled(serverModeSelected);
			connectionsLimitNumber.setEnabled(serverModeSelected);
		}

		lb_listenPort.setEnabled(serverModeSelected);
		listenPort.setEnabled(serverModeSelected);
	}

	private JPanel getQuickStartPanel()
	{
		if (quickStartPanel == null)
		{
			FormLayoutBuilder applicationForm = new FormLayoutBuilder(5, new FormLayoutColumn(Alignment.left, true));

			iperfCommandLabel = new JTextField("");
			iperfCommandLabel.setEditable(false);
			applicationForm.addCell(new JLabel("Iperf command:"));
			applicationForm.addCell(iperfCommandLabel, 5);
			applicationForm.newLine();

			// radio buttons for selecting client or server
			applicationForm.addCell(new JLabel("Choose iPerf Mode:"));
			clientModeRadioButton = new JRadioButton("Client");
			clientModeRadioButton.setOpaque(false);
			clientModeRadioButton.setSelected(true);
			clientModeRadioButton.setActionCommand("client");
			clientModeRadioButton.addActionListener(this);
			clientModeRadioButton.setToolTipText("Run Iperf as a client   (command line: -c)");
			applicationForm.addCell(clientModeRadioButton);
			lb_serverAddress = new JLabel("Server address");
			lb_serverAddress.setOpaque(false);
			lb_serverAddress.setToolTipText("Specify what server the Iperf client should connect to   (command line: -c)");
			serverAddress = new JTextField(15);
			serverAddress.addKeyListener(this);
			lb_serverPort = new JLabel("Port");
			lb_serverPort.setOpaque(false);
			lb_serverPort.setToolTipText("Specify port   (command line: -p)");
			serverPort = new XJIntegerSpinner(1, Integer.MAX_VALUE, 5001);
			serverPort.addChangeListener(this);
			applicationForm.addCell(lb_serverAddress);
			applicationForm.addCompositeCell(serverAddress, lb_serverPort, serverPort);
			applicationForm.newLine();
			applicationForm.addCell(new JLabel());
			applicationForm.addCell(new JLabel());
			lb_simultaneousConnectionsNumber = new JLabel("Parallel Streams");
			lb_simultaneousConnectionsNumber.setOpaque(false);
			lb_simultaneousConnectionsNumber.setToolTipText("The number of simultaneous connections to make to the server. Default is 1.   (command line: -P)");
			simultaneousConnectionsNumber = new XJIntegerSpinner(1, Integer.MAX_VALUE, 1);
			simultaneousConnectionsNumber.addChangeListener(this);

			applicationForm.addCell(lb_simultaneousConnectionsNumber);
			applicationForm.addCell(simultaneousConnectionsNumber);

			applicationForm.newLine();

			applicationForm.addEmptyCell();
			serverModeRadioButton = new JRadioButton("Server");
			serverModeRadioButton.setOpaque(false);
			serverModeRadioButton.setActionCommand("server");
			serverModeRadioButton.addActionListener(this);
			serverModeRadioButton.setToolTipText("Run Iperf as a server   (command line: -s)");
			applicationForm.addCell(serverModeRadioButton);
			lb_listenPort = new JLabel("Listen Port");
			lb_listenPort.setOpaque(false);
			lb_listenPort.setToolTipText("Specify listen port   (command line: -p)");
			listenPort = new XJIntegerSpinner(1, Integer.MAX_VALUE, 5001);
			applicationForm.addCell(lb_listenPort);
			lb_clientLimit = new JCheckBox("Client Limit");
			lb_clientLimit.addActionListener(this);
			lb_clientLimit.setOpaque(false);
			lb_clientLimit.setToolTipText("Specify a host for Iperf server to only accept connections from");
			clientLimit = new JTextField(15);
			clientLimit.addKeyListener(this);
			applicationForm.addCompositeCell(listenPort, lb_clientLimit, clientLimit);
			applicationForm.newLine();
			applicationForm.addEmptyCell();
			applicationForm.addEmptyCell();
			lb_connectionsLimitNumber = new JLabel("Num Connections");
			lb_connectionsLimitNumber.setOpaque(false);
			lb_connectionsLimitNumber.setToolTipText("The number of connections to handle by the server before closing. Default is 0 (handle forever)   (command line: -P)");
			applicationForm.addCell(lb_connectionsLimitNumber);
			connectionsLimitNumber = new XJIntegerSpinner(0, Integer.MAX_VALUE, 0);
			connectionsLimitNumber.addChangeListener(this);
			applicationForm.addCell(connectionsLimitNumber);

			applicationForm.newLine();

			iperfModeButtonGroup = new ButtonGroup();
			iperfModeButtonGroup.add(clientModeRadioButton);
			iperfModeButtonGroup.add(serverModeRadioButton);

			quickStartPanel = applicationForm.getPanel();
			quickStartPanel.setOpaque(false);
		}

		return quickStartPanel;
	}

	private JPanel getApplicationPanel()
	{
		if (applicationPanel == null)
		{
			FormLayoutBuilder applicationForm = new FormLayoutBuilder(3);

			// Compatibility mode?
			compatibilityMode = new JCheckBox("Enable Compatibility Mode");
			applicationForm.addCell(compatibilityMode, 3);
			compatibilityMode.setToolTipText("Compatibility mode allows for use with older version of iperf   (command line: -C)");
			compatibilityMode.setSelected(false);
			if (iperfVersion < 1.7)
			{
				compatibilityMode.setEnabled(false);
			}
			compatibilityMode.addActionListener(this);

			applicationForm.newLine();

			// num buffers to transmit
			lb_transmit = new JLabel("Transmit");
			lb_transmit.setToolTipText("Time to transmit, or number of buffers to transmit. Default is 10secs   (command line: -t, -n)");
			transmit = new XJIntegerSpinner(0, Integer.MAX_VALUE, 10);
			transmit.addChangeListener(this);
			applicationForm.addCell(lb_transmit);
			applicationForm.addCell(transmit);
			applicationForm.newLine();

			// are we sending a specific number of bytes or for a certain amount of
			// time
			transmitBytesRadioButton = new JRadioButton("Bytes");
			transmitBytesRadioButton.addActionListener(this);
			transmitSecondsRadioButton = new JRadioButton("Seconds");
			transmitSecondsRadioButton.addActionListener(this);
			applicationForm.addEmptyCell();
			applicationForm.addCompositeCell(transmitBytesRadioButton, transmitSecondsRadioButton);
			ButtonGroup btrans = new ButtonGroup();
			transmitSecondsRadioButton.setSelected(true);
			btrans.add(transmitSecondsRadioButton);
			btrans.add(transmitBytesRadioButton);

			applicationForm.newLine();

			// output format
			lb_outputFormat = new JLabel("Output Format");
			lb_outputFormat.setToolTipText("Format to print bandwidth numbers in. Adaptive formats choose between kilo- and mega-   (command line: -f)");
			formatList = new JComboBox(OutputFormat.values());
			formatList.addActionListener(this);
			formatList.setSelectedItem(OutputFormat.KBITS);
			applicationForm.addCell(lb_outputFormat);
			applicationForm.addCell(formatList);

			applicationForm.newLine();

			// interval of reports
			lb_reportInterval = new JLabel("Report Interval");
			lb_reportInterval.setToolTipText("Sets the interval time (secs) between periodic bandwidth, jitter, and loss reports   (command line: -i)");
			interval = new XJIntegerSpinner(1, Integer.MAX_VALUE, 1);
			interval.addChangeListener(this);
			applicationForm.addCell(lb_reportInterval);
			applicationForm.addCompositeCell(interval, new JLabel("seconds"));

			applicationForm.newLine();

			// testing mode
			lb_testingMode = new JLabel("Testing Mode");
			applicationForm.addCell(lb_testingMode);
			dualMode = new JCheckBox("Dual");
			dualMode.addActionListener(this);
			dualMode.setToolTipText("Cause the server to connect back to the client immediately and run tests simultaneously   (command line: -d)");
			tradeMode = new JCheckBox("Trade");
			tradeMode.addActionListener(this);
			tradeMode.setToolTipText("Cause the server to connect back to the client following termination of the client   (command line: -r)");
			applicationForm.addCompositeCell(dualMode, tradeMode);
			applicationForm.newLine();
			lb_testPort = new JLabel("test port");
			lb_testPort.setToolTipText("This specifies the port that the server will connect back to the client on   (command line: -L)");
			testPort = new XJIntegerSpinner(1, Integer.MAX_VALUE, 5001);
			testPort.addChangeListener(this);
			applicationForm.addEmptyCell();
			applicationForm.addCompositeCell(lb_testPort, testPort);

			if (iperfVersion < 1.7)
			{
				lb_testingMode.setEnabled(false);
				dualMode.setEnabled(false);
				tradeMode.setEnabled(false);
				lb_testPort.setEnabled(false);
				testPort.setEnabled(false);
			}

			applicationForm.newLine();

			// file to transmit
			lb_representativeFile = new JLabel("Representative File");
			lb_representativeFile.setToolTipText("Use a representative stream to measure bandwidth   (command line: -F)");
			applicationForm.addCell(lb_representativeFile);
			representativeFile = new JTextField(14);
			representativeFile.addKeyListener(this);

			// add browse button
			browse = new JButton("...");
			browse.setPreferredSize(new Dimension(35, 20));
			browse.setActionCommand("Browse");
			browse.addActionListener(this);
			applicationForm.addCompositeCell(representativeFile, browse);

			if (iperfVersion < 1.2)
			{
				representativeFile.setEnabled(false);
				lb_representativeFile.setEnabled(false);
				browse.setEnabled(false);
			}

			applicationForm.newLine();

			// should we print MSS?
			printMSS = new JCheckBox("Print MSS");
			printMSS.addActionListener(this);
			printMSS.setToolTipText("Print out TCP maximum segment size   (command line: -m)");
			printMSS.setSelected(false);
			applicationForm.addCell(printMSS);

			applicationForm.newLine();

			applicationPanel = applicationForm.getPanel();
		}

		return applicationPanel;
	}

	private JPanel getTransportPanel()
	{
		if (transportPanel == null)
		{
			FormLayoutBuilder transportForm = new FormLayoutBuilder(1, new FormLayoutColumn(Alignment.fill));

			// radio buttons for TCP/UDP
			transportForm.addCell(new JLabel("Choose the protocol to use"));
			transportForm.newLine();

			tcpRadioButton = new JRadioButton("TCP");
			tcpRadioButton.setSelected(true);
			tcpRadioButton.setActionCommand("TCP");
			tcpRadioButton.addActionListener(this);
			tcpRadioButton.setToolTipText("Use TCP Protocol   (command line: default)");
			transportForm.addCell(getTCPPanel());

			transportForm.newLine();

			udpRadioButton = new JRadioButton("UDP");
			udpRadioButton.setActionCommand("UDP");
			udpRadioButton.addActionListener(this);
			udpRadioButton.setToolTipText("Use UDP Protocol   (command line: -u)");
			transportForm.addCell(getUDPPanel());

			transportForm.newLine();

			protocolButtonGroup = new ButtonGroup();
			protocolButtonGroup.add(tcpRadioButton);
			protocolButtonGroup.add(udpRadioButton);

			transportPanel = transportForm.getPanel();
		}

		return transportPanel;
	}

	private void setTCPOptionsEnabled(boolean enabled)
	{
		lb_tcpBufferLength.setEnabled(enabled);
		tcpBufferLength.setEnabled(enabled && lb_tcpBufferLength.isSelected());
		tcpBufferSizeUnit.setEnabled(enabled && lb_tcpBufferLength.isSelected());

		lb_tcpWindowSize.setEnabled(enabled);
		tcpWindowSize.setEnabled(enabled && lb_tcpWindowSize.isSelected());
		tcpWindowSizeUnit.setEnabled(enabled && lb_tcpWindowSize.isSelected());

		lb_mss.setEnabled(enabled);
		mss.setEnabled(enabled && lb_mss.isSelected());
		mssUnit.setEnabled(enabled && lb_mss.isSelected());

		tcpNoDelay.setEnabled(enabled);

		// other options
		printMSS.setEnabled(enabled);
		lb_TTL.setEnabled(!enabled);
		TTL.setEnabled(!enabled);
	}

	private JPanel getTCPPanel()
	{
		if (tcpPanel == null)
		{
			FormLayoutBuilder tcpForm = new FormLayoutBuilder(3);
			tcpForm.addCell(tcpRadioButton);
			tcpForm.newLine();
			// buffer length
			lb_tcpBufferLength = new JCheckBox("Buffer Length");
			lb_tcpBufferLength.setToolTipText("Read/Write buffer length. Use 'K' or 'M' for kilo/mega bytes. (i.e 8K)   (command line: -l)");
			lb_tcpBufferLength.addActionListener(this);
			tcpForm.addCell(lb_tcpBufferLength);
			tcpBufferLength = new XJIntegerSpinner(1, 9999, 8);
			tcpBufferLength.addChangeListener(this);
			tcpForm.addCell(tcpBufferLength);
			tcpBufferSizeUnit = new JComboBox(IperfSizeUnit.values());
			tcpBufferSizeUnit.addActionListener(this);
			tcpBufferSizeUnit.setSelectedItem(IperfSizeUnit.KBYTES);
			tcpForm.addCell(tcpBufferSizeUnit);

			tcpForm.newLine();

			// set window size
			lb_tcpWindowSize = new JCheckBox("TCP Window Size");
			lb_tcpWindowSize.setToolTipText("Set TCP window size. Use 'K' or 'M' for kilo/mega bytes. (i.e 8K)   (command line: -w)");
			lb_tcpWindowSize.addActionListener(this);
			tcpForm.addCell(lb_tcpWindowSize);
			tcpWindowSize = new XJIntegerSpinner(1, 9999, 8);
			tcpWindowSize.addChangeListener(this);
			tcpForm.addCell(tcpWindowSize);
			tcpWindowSizeUnit = new JComboBox(IperfSizeUnit.values());
			tcpWindowSizeUnit.addActionListener(this);
			tcpWindowSizeUnit.setSelectedItem(IperfSizeUnit.KBYTES);
			tcpForm.addCell(tcpWindowSizeUnit);

			tcpForm.newLine();

			// attempt to set MSS
			lb_mss = new JCheckBox("Max Segment Size");
			lb_mss.setToolTipText("Attempt to set max segment size. Use 'K' or 'M' for kilo/mega bytes. (i.e 8K)   (command line: -M)");
			lb_mss.addActionListener(this);
			tcpForm.addCell(lb_mss);
			mss = new XJIntegerSpinner(1, 9999, 8);
			mss.addChangeListener(this);
			tcpForm.addCell(mss);
			mssUnit = new JComboBox(IperfSizeUnit.values());
			mssUnit.addActionListener(this);
			mssUnit.setSelectedItem(IperfSizeUnit.KBYTES);
			tcpForm.addCell(mssUnit);

			tcpForm.newLine();

			// no delay?
			tcpNoDelay = new JCheckBox("TCP No Delay");
			tcpNoDelay.addActionListener(this);
			tcpNoDelay.setToolTipText("Disable Nagle's algorithm   (command line: -N)");
			tcpNoDelay.setSelected(false);
			tcpForm.addCell(tcpNoDelay);

			tcpForm.newLine();

			tcpPanel = tcpForm.getPanel();
			tcpPanel.setBorder(new TitledBorder(""));
		}

		return tcpPanel;
	}

	private void setUDPOptionsEnabled(boolean enabled)
	{
		lb_udpBufferSize.setEnabled(enabled);
		udpBufferSize.setEnabled(enabled && lb_udpBufferSize.isSelected());
		udpBufferSizeUnit.setEnabled(enabled && lb_udpBufferSize.isSelected());

		lb_udpPacketSize.setEnabled(enabled);
		udpPacketSize.setEnabled(enabled && lb_udpPacketSize.isSelected());
		udpPacketSizeUnit.setEnabled(enabled && lb_udpPacketSize.isSelected());

		lb_udpBandwidth.setEnabled(!serverModeRadioButton.isSelected() && enabled);
		udpBandwidth.setEnabled(!serverModeRadioButton.isSelected() && enabled);
		udpBandwidthUnit.setEnabled(!serverModeRadioButton.isSelected() && enabled);

		// other options
		lb_TTL.setEnabled(!serverModeRadioButton.isSelected() && enabled);
		TTL.setEnabled(!serverModeRadioButton.isSelected() && enabled);
	}

	private JPanel getUDPPanel()
	{
		if (udpPanel == null)
		{
			FormLayoutBuilder udpForm = new FormLayoutBuilder(3, new FormLayoutColumn(Alignment.fill));

			udpForm.addCell(udpRadioButton);
			udpForm.newLine();

			// bandwidth
			lb_udpBandwidth = new JLabel("UDP Bandwidth");
			lb_udpBandwidth.setToolTipText("Set bandwidth to send in bits/sec. Use 'K' or 'M' for kilo/mega bits. (i.e 8K)   (command line: -b)");
			udpForm.addCell(lb_udpBandwidth);
			udpBandwidth = new XJIntegerSpinner(1, 9999, 1);
			udpBandwidth.addChangeListener(this);
			udpForm.addCell(udpBandwidth);
			udpBandwidthUnit = new JComboBox(IperfSpeedUnit.values());
			udpBandwidthUnit.addActionListener(this);
			udpBandwidthUnit.setSelectedItem(IperfSpeedUnit.MEGABYTES_PERSEC);
			udpForm.addCell(udpBandwidthUnit);

			udpForm.newLine();

			// buffer size
			lb_udpBufferSize = new JCheckBox("UDP Buffer Size");
			lb_udpBufferSize.setToolTipText("Set UDP buffer size. Use 'K' or 'M' for kilo/mega bytes. (i.e 8K)   (command line: -w)");
			lb_udpBufferSize.addActionListener(this);
			udpForm.addCell(lb_udpBufferSize);
			udpBufferSize = new XJIntegerSpinner(1, 9999, 8);
			udpBufferSize.addChangeListener(this);
			udpForm.addCell(udpBufferSize);
			udpBufferSizeUnit = new JComboBox(IperfSizeUnit.values());
			udpBufferSizeUnit.addActionListener(this);
			udpBufferSizeUnit.setSelectedItem(IperfSizeUnit.KBYTES);
			udpForm.addCell(udpBufferSizeUnit);

			udpForm.newLine();

			// packet size
			lb_udpPacketSize = new JCheckBox("UDP Packet Size");
			lb_udpPacketSize.setToolTipText("Set UDP datagram buffer size. Use 'K' or 'M' for kilo/mega bytes. (i.e 1470)   (command line: -l)");
			lb_udpPacketSize.addActionListener(this);
			udpForm.addCell(lb_udpPacketSize);
			udpPacketSize = new XJIntegerSpinner(1, 9999, 1470);
			udpPacketSize.addChangeListener(this);
			udpForm.addCell(udpPacketSize);
			udpPacketSizeUnit = new JComboBox(IperfSizeUnit.values());
			udpPacketSizeUnit.addActionListener(this);
			udpPacketSizeUnit.setSelectedItem(IperfSizeUnit.KBYTES);
			udpForm.addCell(udpPacketSizeUnit);

			udpForm.newLine();

			udpPanel = udpForm.getPanel();
			udpPanel.setBorder(new TitledBorder(""));
		}

		return udpPanel;
	}

	private JPanel getIPPanel()
	{
		if (ipPanel == null)
		{
			FormLayoutBuilder ipForm = new FormLayoutBuilder(2);

			// TTL
			lb_TTL = new JLabel("TTL");
			lb_TTL.setToolTipText("Set time to live (number of hops). Default is 1.   (command line: -T)");
			ipForm.addCell(lb_TTL);
			TTL = new XJIntegerSpinner(0, Integer.MAX_VALUE, 1);
			TTL.addChangeListener(this);
			ipForm.addCell(TTL);

			ipForm.newLine();

			// TOS
			lb_tos = new JLabel("Type of Service");
			lb_tos.setToolTipText("The type-of-service for outgoing packets. (Many routers ignore the TOS field)   (command line: -S)");
			ipForm.addCell(lb_tos);
			tos = new JComboBox(TosOption.values());
			tos.addActionListener(this);
			tos.setSelectedItem(TosOption.NONE);
			ipForm.addCell(tos);

			ipForm.newLine();

			// bind to a specific host
			lb_bindHost = new JLabel("Bind to Host");
			lb_bindHost.setToolTipText("Bind to host, one of this machine's addresses. For multihomed hosts.   (command line: -B)");
			ipForm.addCell(lb_bindHost);
			bindhost = new JTextField(18);
			bindhost.addKeyListener(this);
			ipForm.addCell(bindhost);

			ipForm.newLine();

			// bind to IPv6 address
			ipv6 = new JCheckBox("IPv6");
			ipForm.addCell(ipv6);
			ipv6.setToolTipText("Bind to an IPv6 address   (command line: -V)");
			ipv6.setSelected(false);
			if (iperfVersion < 1.6)
			{
				ipv6.setEnabled(false);
			}
			ipv6.addActionListener(this);

			ipForm.newLine();

			ipPanel = ipForm.getPanel();
		}

		return ipPanel;
	}

	/*
	 * This creates the top connection panel that lets the user select
	 * client/server mode tcp or udp, and the server/port. Of course it has the
	 * usual run/stop buttons.
	 */
	public void init()
	{
		addWindowListener(this);
		
		setLayout(new BorderLayout());

		// set up our menu
		menuBar = new JMenuBar();
		menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		menuItem = new JMenuItem("About...");
		menuItem.setActionCommand("About");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuBar.add(menu);
		this.setJMenuBar(menuBar);

		// set up the tool bar
		toolbar = new JToolBar(JToolBar.HORIZONTAL);
		toolbar.setFloatable(false);
		toolbar.add(getQuickStartPanel());

		// run button
		startIperf = new JButton(new ImageIcon(JPerfUI.class.getResource("start.png")));
		startIperf.setToolTipText("Run Iperf!");
		startIperf.setActionCommand("Run");
		startIperf.addActionListener(this);

		// add stop button
		stopIperf = new JButton(new ImageIcon(JPerfUI.class.getResource("stop.png")));
		stopIperf.setToolTipText("Stop Iperf");
		stopIperf.setActionCommand("Stop");
		stopIperf.addActionListener(this);
		stopIperf.setEnabled(false);

		// restore defaults button
		restoreDefaults = new JButton(new ImageIcon(JPerfUI.class.getResource("restore-default-settings.png")));
		restoreDefaults.setToolTipText("Restore default settings");
		restoreDefaults.setActionCommand("Restore");
		restoreDefaults.addActionListener(this);
		
		toolbar.add(startIperf);
		toolbar.add(stopIperf);
		toolbar.add(restoreDefaults);

		add(toolbar, BorderLayout.PAGE_START);

		JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();
		JXTaskPane applicationTP = new JXTaskPane();
		applicationTP.setAnimated(true);
		applicationTP.setTitle("Application layer options");
		applicationTP.add(getApplicationPanel());

		JXTaskPane transportTP = new JXTaskPane();
		transportTP.setAnimated(true);
		transportTP.setTitle("Transport layer options");
		transportTP.add(getTransportPanel());

		JXTaskPane ipTP = new JXTaskPane();
		ipTP.setAnimated(true);
		ipTP.setTitle("IP layer options");
		ipTP.add(getIPPanel());

		taskPaneContainer.add(applicationTP);
		taskPaneContainer.add(transportTP);
		taskPaneContainer.add(ipTP);

		taskPaneContainer.setScrollableTracksViewportHeight(true);
		taskPaneContainer.setScrollableTracksViewportWidth(true);

		JScrollPane scroll = new JScrollPane(taskPaneContainer);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(scroll, BorderLayout.WEST);

		centerPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		centerPanel.setLeftComponent(getGraphPanel());
		centerPanel.setRightComponent(getOutputPanel());
		
		centerPanel.setDividerLocation(300);
		centerPanel.setResizeWeight(1);
		centerPanel.setOneTouchExpandable(true);

		add(centerPanel, BorderLayout.CENTER);

		createAbout();
	}

	private JPanel	outputPanel	= null;

	public void logMessage(String message)
	{
		output.append(message + "\n");
		output.setCaretPosition(output.getDocument().getLength());
	}

	public void addNewStreamBandwidthMeasurement(int streamID, Measurement measurement)
	{
		chartPanel.maybeAddNewSeries("" + streamID, "#"+streamID+": ", "Jitter: ", SeriesColorGenerator.nextColor());
		chartPanel.addSeriesBandwidthMeasurement("" + streamID, measurement);
	}

	public void addNewStreamBandwidthAndJitterMeasurement(int streamID, Measurement bandwidth, Measurement jitter)
	{
		chartPanel.maybeAddNewSeries("" + streamID, "#"+streamID+": ",  "Jitter: ", SeriesColorGenerator.nextColor());
		chartPanel.addSeriesBandwidthAndJitterMeasurement("" + streamID, bandwidth, jitter);
	}

	public void setStartedStatus()
	{
		startIperf.setEnabled(false);
		stopIperf.setEnabled(true);
	}

	public void setStoppedStatus()
	{
		stopIperf.setEnabled(false);
		startIperf.setEnabled(true);
	}

	private JPanel getGraphPanel()
	{
		if (chartPanel == null)
		{
			chartPanel = new IPerfChartPanel("Bandwidth", "KBits", "ms", "Time", "Bandwidth", "Jitter", 1.0, 20.0, 1.0, Color.black, Color.white, Color.gray);
		}

		return chartPanel;
	}

	private IPerfChartPanel	chartPanel	= null;

	private JPanel getOutputPanel()
	{
		if (outputPanel == null)
		{
			outputPanel = new JPanel(new BorderLayout());

			tabbedPane = new JTabbedPane();
			output = new JTextArea();
			output.setWrapStyleWord(false);
			outputScrollPane = new JScrollPane(output);
			tabbedPane.addTab("Output", null, outputScrollPane, "Iperf Output");
			outputPanel.add(tabbedPane, BorderLayout.CENTER);

			// add output control buttons
			JPanel outputButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			clearOutputButton = new JButton("Clear now");
			clearOutputButton.setActionCommand("Clear");
			clearOutputButton.addActionListener(this);
			clearOutputButton.setToolTipText("Clears output from Iperf run");
			saveOutputButton = new JButton("Save");
			saveOutputButton.setActionCommand("Save");
			saveOutputButton.addActionListener(this);
			saveOutputButton.setToolTipText("Save output to a file");
			alwaysClearOutput = new JCheckBox("Clear Output for new Iperf Run");
			alwaysClearOutput.setToolTipText("Always clear Iperf output between runs.");
			alwaysClearOutput.setSelected(false);
			outputButtonsPanel.add(saveOutputButton);
			outputButtonsPanel.add(clearOutputButton);
			outputButtonsPanel.add(alwaysClearOutput);

			outputPanel.add(outputButtonsPanel, BorderLayout.SOUTH);
		}

		return outputPanel;
	}

	public void updateIperfCommandLabel()
	{
		try
		{
			validateFormOptions();
			iperfCommandLabel.setForeground(Color.black);
			iperfCommandLabel.setText(options);
		}
		catch (Exception ex)
		{
			iperfCommandLabel.setForeground(Color.red);
			iperfCommandLabel.setText(ex.getMessage());
		}
	}

	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		if (source == ipv6)
		{
			if (ipv6.isSelected())
			{
				if (udpRadioButton.isSelected())
				{
					udpBufferSize.setValue(1450);
				}
			}
			else if (udpRadioButton.isSelected())
			{
				udpBufferSize.setValue(1470);
			}
		}
		else if (source == compatibilityMode)
		{
			if (compatibilityMode.isSelected())
			{
				lb_testingMode.setEnabled(false);
				dualMode.setEnabled(false);
				tradeMode.setEnabled(false);
				lb_testPort.setEnabled(false);
				testPort.setEnabled(false);
			}
			else
			{
				if (!serverModeRadioButton.isSelected())
				{
					lb_testingMode.setEnabled(true);
					dualMode.setEnabled(true);
					tradeMode.setEnabled(true);
					lb_testPort.setEnabled(true);
					testPort.setEnabled(true);
				}
			}
		}
		else if (source == lb_tcpBufferLength)
		{
			tcpBufferLength.setEnabled(lb_tcpBufferLength.isSelected());
			tcpBufferSizeUnit.setEnabled(lb_tcpBufferLength.isSelected());
		}
		else if (source == lb_tcpWindowSize)
		{
			tcpWindowSize.setEnabled(lb_tcpWindowSize.isSelected());
			tcpWindowSizeUnit.setEnabled(lb_tcpWindowSize.isSelected());
		}
		else if (source == lb_mss)
		{
			mss.setEnabled(lb_mss.isSelected());
			mssUnit.setEnabled(lb_mss.isSelected());
		}
		else if (source == lb_udpBufferSize)
		{
			udpBufferSize.setEnabled(lb_udpBufferSize.isSelected());
			udpBufferSizeUnit.setEnabled(lb_udpBufferSize.isSelected());
		}
		else if (source == lb_udpPacketSize)
		{
			udpPacketSize.setEnabled(lb_udpPacketSize.isSelected());
			udpPacketSizeUnit.setEnabled(lb_udpPacketSize.isSelected());
		}
		else
		{
			String command = e.getActionCommand();

			if (command == "Restore")
			{
				applyDefaultValues();
			}
			else if (command == "TCP")
			{
				setUDPOptionsEnabled(false);
				setTCPOptionsEnabled(true);
			}
			else if (command == "UDP")
			{
				setTCPOptionsEnabled(false);
				setUDPOptionsEnabled(true);
			}
			else if (command == "Run")
			{
				boolean optionsReady = false;
				try
				{
					optionsReady = validateFormOptions();
				}
				catch (Exception ex)
				{
					JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				// can not have adaptive bits for graph
				OutputFormat of = (OutputFormat) formatList.getSelectedItem();
				if (of == OutputFormat.ADAPTIVE_BITS || of == OutputFormat.ADAPTIVE_BYTES)
				{
					JOptionPane.showMessageDialog(this, "The bandwidth graph will not be created because an adaptive format is selected", "Information", JOptionPane.INFORMATION_MESSAGE);
				}

				if (optionsReady)
				{
					// "Bandwidth", "Kbps", "Kbps", "Time", "Bitrate in Kbps", 1.0, 20.0,
					// Color.black, Color.white, Color.gray
					chartPanel.reconfigure(serverModeRadioButton.isSelected(), serverModeRadioButton.isSelected() ? "Bandwidth & Jitter" : "Bandwidth", formatList.getSelectedItem().toString(), "ms", transmitSecondsRadioButton
							.isSelected() ? "Time (sec)" : "Bytes transmitted", formatList.getSelectedItem().toString() + " (BW)", "ms (Jitter)", transmit.getValue(), interval.getValue());

					if (alwaysClearOutput.isSelected())
					{
						output.setText("");
					}
					iperf = new IperfThread(serverModeRadioButton.isSelected(), options, this);
					iperf.start();
				}
			}
			else if (command == "Stop")
			{
				iperf.quit();
			}
			else if (command == "Clear")
			{
				iperf.quit();
				output.setText("");
			}
			else if (command == "server")
			{
				setClientModeSelected(false);
				setServerModeSelected(true);
			}
			else if (command == "client")
			{
				setServerModeSelected(false);
				setClientModeSelected(true);
			}
			else if (command == "About")
			{
				JOptionPane.showMessageDialog(this, aboutPanel);
			}
			else if (command == "Browse")
			{
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showDialog(this, "Select representative file");
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					File file = fc.getSelectedFile();
					representativeFile.setText(file.getAbsolutePath());
				}
			}
			else if (command == "Save")
			{
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showDialog(this, "Save");
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					File file = fc.getSelectedFile();

					// write output of textarea to file
					String text = new String(output.getText());

					try
					{
						FileWriter fw = new FileWriter(file);
						fw.write(text);
						fw.close();
					}
					catch (IOException ioe)
					{
						JOptionPane.showMessageDialog(this, "Error while saving output: " + ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		updateIperfCommandLabel();
	}

	private boolean validateFormOptions()
		throws Exception
	{
		// form options string, but return if we are missing stuff
		// clear it first
		options = iperfCommand;

		// determine if its a client or server
		if (serverModeRadioButton.isSelected())
		{
			options += " -s";
			if (clientLimit.getText().length() > 0 && lb_clientLimit.isSelected())
			{
				options += " -c " + clientLimit.getText();
			}
		}
		else
		{
			if (serverAddress.getText().length() > 0)
			{
				options += " -c " + serverAddress.getText();
			}
			else
			{
				// set focus on server address
				serverAddress.grabFocus();
				throw new Exception("Please enter the host to connect to");
			}
		}

		// these options are the same for server and client
		if (udpRadioButton.isSelected())
		{
			options += " -u";
		}
		if (simultaneousConnectionsNumber.isEnabled())
		{
			options += " -P " + simultaneousConnectionsNumber.getValue();
		}
		else if (connectionsLimitNumber.isEnabled())
		{
			options += " -P " + connectionsLimitNumber.getValue();
		}

		options += " -i " + interval.getValue();

		if (printMSS.isSelected() && printMSS.isEnabled())
		{
			options += " -m";
		}
		if (serverPort.isEnabled())
		{
			options += " -p " + serverPort.getValue();
		}
		else if (listenPort.isEnabled())
		{
			options += " -p " + listenPort.getValue();
		}
		if (lb_tcpWindowSize.isSelected() && tcpWindowSize.isEnabled())
		{
			options += " -w " + tcpWindowSize.getValue() + ((IperfSizeUnit) tcpWindowSizeUnit.getSelectedItem()).getShortcut();
		}
		else if (lb_udpBufferSize.isSelected() && udpBufferSize.isEnabled())
		{
			options += " -w " + udpBufferSize.getValue() + ((IperfSizeUnit) udpBufferSizeUnit.getSelectedItem()).getShortcut();
		}
		if (bindhost.getText().length() > 0)
		{
			options += " -B " + bindhost.getText();
		}
		if (lb_mss.isSelected() && mss.isEnabled())
		{
			options += " -M " + mss.getValue() + ((IperfSizeUnit) mssUnit.getSelectedItem()).getShortcut();
		}
		if (tcpNoDelay.isSelected() && tcpNoDelay.isEnabled())
		{
			options += " -N";
		}
		if (ipv6.isSelected() && ipv6.isEnabled())
		{
			options += " -V";
		}
		if (lb_tcpBufferLength.isSelected() && tcpBufferLength.isEnabled())
		{
			options += " -l " + tcpBufferLength.getValue() + ((IperfSizeUnit) tcpBufferSizeUnit.getSelectedItem()).getShortcut();
		}
		else if (lb_udpPacketSize.isSelected() && udpPacketSize.isEnabled())
		{
			options += " -l " + udpPacketSize.getValue() + ((IperfSizeUnit) udpPacketSizeUnit.getSelectedItem()).getShortcut();
		}
		if (compatibilityMode.isSelected() && compatibilityMode.isEnabled())
		{
			options += " -C";
		}

		// do format
		options += " -f " + ((OutputFormat) formatList.getSelectedItem()).getShortcut();

		if (udpBandwidthUnit.isEnabled() && udpBandwidth.isEnabled())
		{
			options += " -b " + udpBandwidth.getValue() + ((IperfSpeedUnit) udpBandwidthUnit.getSelectedItem()).getUnit();
		}

		if (transmit.isEnabled())
		{
			if (transmitBytesRadioButton.isSelected())
			{
				options += " -n " + transmit.getValue();
			}
			else if (transmitSecondsRadioButton.isSelected())
			{
				options += " -t " + transmit.getValue();
			}
		}

		if (dualMode.isSelected() && dualMode.isEnabled())
		{
			options += " -d";
		}
		if (tradeMode.isSelected() && tradeMode.isEnabled())
		{
			options += " -r";
		}
		if (((dualMode.isSelected() && dualMode.isEnabled()) || (tradeMode.isSelected() && tradeMode.isEnabled())) && testPort.isEnabled())
		{
			options += " -L " + testPort.getValue();
		}

		if (TTL.isEnabled())
		{
			options += " -T " + TTL.getValue();
		}
		if ((TosOption) tos.getSelectedItem() != TosOption.NONE)
		{
			options += " -S " + ((TosOption) tos.getSelectedItem()).getCode();
		}
		// check if file exists
		if (representativeFile.getText().length() > 0 && representativeFile.isEnabled())
		{
			File f = new File(representativeFile.getText());
			if (!f.exists())
			{
				// set focus on representative file field
				representativeFile.grabFocus();
				throw new Exception("The file you selected does not exist");
			}
			options += " -F " + representativeFile.getText();
		}
		return true;
	}

	private void createAbout()
	{
		aboutPanel = new JPerfAboutPanel(version);
		aboutPanel.setPreferredSize(new Dimension(450, 350));
	}

	// KeyListener methods
	public void keyPressed(KeyEvent e)
	{
	}

	public void keyReleased(KeyEvent e)
	{
		updateIperfCommandLabel();
	}

	public void keyTyped(KeyEvent e)
	{
	}

	// ChangeListener methods
	public void stateChanged(ChangeEvent e)
	{
		updateIperfCommandLabel();
	}

	public void windowActivated(WindowEvent arg0) {}

	public void windowClosed(WindowEvent arg0) {}

	public void windowClosing(WindowEvent arg0) 
	{
		stopIperf.doClick();
	}

	public void windowDeactivated(WindowEvent arg0) {}

	public void windowDeiconified(WindowEvent arg0) {}

	public void windowIconified(WindowEvent arg0) {}

	public void windowOpened(WindowEvent arg0) {}
}
