/*
 * - 02/2008: Class updated by Nicolas Richasse
 * - 03/2008: Class updated by Nicolas Richasse
 * 
 * Changelog:
 *-02/2008:
 * 	- code refactoring
 * 	- UI improved with the SwingX API and the Forms library
 * 	- graphs display improved
 * 	- the iperf command-line associated to the current parameters is displayed and updated in real-time
 * 	- 'Restore defaults' button added
 *  - the output window automatically scrolls as the output goes beyond the end of it
 *  - some bugs fixed when parsing the iperf output
 *	
 *-03/2008:
 *  - the server port changes did not update the iperf command label
 *  
 *-04/2009: 
 *	- new layout for Start/Stop/Defaults buttons
 *	- main menu improved
 *	- usage of SwingUtilities.invokeLater() method into event methods
 * 	- udp packet size unit limited to KBits and KBytes
 * 
 *-05/2009:
 *	- code improvements
 *	- UI improvements
 *	- import/export feature implemented
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
import org.jfree.ui.ExtensionFileFilter;

import net.nlanr.jperf.JPerf;
import net.nlanr.jperf.core.IPerfProperties;
import net.nlanr.jperf.core.IperfSpeedUnit;
import net.nlanr.jperf.core.IperfThread;
import net.nlanr.jperf.core.Measurement;
import net.nlanr.jperf.core.TosOption;
import net.nlanr.jperf.core.IperfUnit;
import net.nlanr.jperf.ui.FormLayoutBuilder.Alignment;
import net.nlanr.jperf.ui.chart.IPerfChartPanel;
import net.nlanr.jperf.ui.chart.SeriesColorGenerator;

import java.io.*;

import static net.nlanr.jperf.core.IPerfProperties.*;

public class JPerfUI extends JFrame
	implements ActionListener, KeyListener, ChangeListener, WindowListener
{
	private JPerfAboutPanel	aboutPanel;

	// Menu stuff
	private JMenuBar				menuBar;
	private JMenu						menuJPerf;
	private JMenuItem				menuJPerfOpen, menuJPerfSaveAs, menuJPerfRestoreDefaults, menuJPerfAbout, menuJPerfQuit;

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
	private IntegerSpinner	serverPort, listenPort;
	private IntegerSpinner	simultaneousConnectionsNumber, connectionsLimitNumber;
	private JButton						startIperf, stopIperf, restoreDefaults, saveConfiguration, loadConfiguration;

	// transport parameters
	private JRadioButton			tcpRadioButton, udpRadioButton;
	private JCheckBox					lb_tcpBufferLength;
	private DoubleSpinner	tcpBufferLength;
	private JComboBox					tcpBufferSizeUnit;
	private JCheckBox					lb_tcpWindowSize;
	private DoubleSpinner	tcpWindowSize;
	private JComboBox					tcpWindowSizeUnit;
	private JCheckBox					lb_mss;
	private DoubleSpinner	mss;
	private JComboBox					mssUnit;
	private JCheckBox					lb_udpBufferSize;
	private DoubleSpinner	udpBufferSize;
	private JComboBox					udpBufferSizeUnit;
	private JCheckBox					lb_udpPacketSize;
	private DoubleSpinner	udpPacketSize;
	private JComboBox					udpPacketSizeUnit;
	private JLabel						lb_udpBandwidth;
	private DoubleSpinner	udpBandwidth;
	private JComboBox					udpBandwidthUnit;
	private JCheckBox					tcpNoDelay;

	// ip parameters
	private JLabel						lb_bindHost, lb_TTL;
	private JTextField				bindhost;
	private IntegerSpinner	TTL;
	private JComboBox					tos;

	// other parameters
	private JCheckBox					printMSS;
	private JTextField				representativeFile;
	private IntegerSpinner	transmit;
	private JComboBox					formatList;
	private IntegerSpinner	interval;
	private ButtonGroup				iperfModeButtonGroup;
	private ButtonGroup				protocolButtonGroup;
	private JRadioButton			transmitBytesRadioButton, transmitSecondsRadioButton;
	private JCheckBox					ipv6;
	private IperfThread				iperf;
	private JCheckBox					alwaysClearOutput;
	private JButton						clearOutputButton, saveOutputButton;
	private JCheckBox					compatibilityMode;
	private IntegerSpinner	testPort;
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
	
	private IPerfProperties defaultConfiguration = new IPerfProperties(true);
	
	private JFileChooser saveFileChooser, loadFileChooser;
	
	public JPerfUI(String iperfCommand, String version)
	{
		super("JPerf "+JPerf.JPERF_VERSION+" - Network performance measurement graphical tool");
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
		applyConfiguration(defaultConfiguration);

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
	
	private void applyConfiguration(IPerfProperties p)
	{
		// quickstart panel
		boolean isServerModeSelected = p.getString(KEY_MODE, DEFAULT_MODE).toLowerCase().trim().equals("server");
		setServerModeSelected(isServerModeSelected);
		setClientModeSelected(!isServerModeSelected);
		if (isServerModeSelected)
		{
			serverModeRadioButton.doClick();
		}
		else
		{
			clientModeRadioButton.doClick();
		}
		serverAddress.setText(p.getString(KEY_SERVER_ADDRESS, DEFAULT_SERVER_ADDRESS));
		serverPort.setValue(p.getInteger(KEY_SERVER_PORT, DEFAULT_SERVER_PORT));
		lb_clientLimit.setSelected(p.getBoolean(KEY_CLIENT_LIMIT_ENABLED, DEFAULT_CLIENT_LIMIT_ENABLED));
		_lb_clientLimit_actionPerformed();
		clientLimit.setText(p.getString(KEY_CLIENT_LIMIT, DEFAULT_CLIENT_LIMIT));
		listenPort.setValue(p.getInteger(KEY_LISTEN_PORT, DEFAULT_LISTEN_PORT));
		simultaneousConnectionsNumber.setValue(p.getInteger(KEY_PARALLEL_STREAMS, DEFAULT_PARALLEL_STREAMS));
		connectionsLimitNumber.setValue(p.getInteger(KEY_NUM_CONNECTIONS, DEFAULT_NUM_CONNECTIONS));
		
		// application layer panel
		compatibilityMode.setSelected(p.getBoolean(KEY_COMPATIBILITY_MODE_ENABLED, DEFAULT_COMPATIBILITY_MODE_ENABLED));
		_compatibilityMode_actionPerformed();
		transmit.setValue(p.getInteger(KEY_TRANSMIT, DEFAULT_TRANSMIT));
		boolean isTransmitSecondsSelected = p.getString(KEY_TRANSMIT_UNIT, DEFAULT_TRANSMIT_UNIT).trim().toLowerCase().equals("seconds");
		transmitSecondsRadioButton.setSelected(isTransmitSecondsSelected);
		transmitBytesRadioButton.setSelected(!isTransmitSecondsSelected);
		if (isTransmitSecondsSelected)
		{
			_transmitSecondsRadioButton_actionPerformed();
		}
		else
		{
			_transmitBytesRadioButton_actionPerformed();
		}
		formatList.setSelectedItem(p.getUnit(KEY_OUTPUT_FORMAT, DEFAULT_OUTPUT_FORMAT));
		interval.setValue(p.getInteger(KEY_REPORT_INTERVAL, DEFAULT_REPORT_INTERVAL));
		dualMode.setSelected(p.getBoolean(KEY_TEST_MODE_DUAL_ENABLED, DEFAULT_TEST_MODE_DUAL_ENABLED));
		_dualMode_actionPerformed();
		tradeMode.setSelected(p.getBoolean(KEY_TEST_MODE_DUAL_ENABLED, DEFAULT_TEST_MODE_TRADE_ENABLED));
		_tradeMode_actionPerformed();
		testPort.setValue(p.getInteger(KEY_TEST_MODE_PORT, DEFAULT_TEST_MODE_PORT));
		printMSS.setSelected(p.getBoolean(KEY_PRINT_MSS_ENABLED, DEFAULT_PRINT_MSS_ENABLED));
		_printMSS_actionPerformed();
		
		// transport layer panel
		boolean isUDPModeSelected = p.getString(KEY_TRANSPORT_PROTOCOL, DEFAULT_TRANSPORT_PROTOCOL).equals("udp");
		setUDPOptionsEnabled(isUDPModeSelected);
		setTCPOptionsEnabled(!isUDPModeSelected);
		if (isUDPModeSelected)
		{
			udpRadioButton.doClick();
		}
		else
		{
			tcpRadioButton.doClick();
		}
		
		lb_tcpBufferLength.setSelected(p.getBoolean(KEY_TCP_BUFFER_LENGTH_ENABLED, DEFAULT_TCP_BUFFER_LENGTH_ENABLED));
		tcpBufferLength.setValue(p.getDouble(KEY_TCP_BUFFER_LENGTH, DEFAULT_TCP_BUFFER_LENGTH));
		tcpBufferSizeUnit.setSelectedItem(p.getUnit(KEY_TCP_BUFFER_LENGTH_UNIT, DEFAULT_TCP_BUFFER_LENGTH_UNIT));
		_lb_tcpBufferLength_actionPerformed();
		
		lb_tcpWindowSize.setSelected(p.getBoolean(KEY_TCP_WINDOW_SIZE_ENABLED, DEFAULT_TCP_WINDOW_SIZE_ENABLED));
		tcpWindowSize.setValue(p.getDouble(KEY_TCP_WINDOW_SIZE, DEFAULT_TCP_WINDOW_SIZE));
		tcpWindowSizeUnit.setSelectedItem(p.getUnit(KEY_TCP_WINDOW_SIZE_UNIT, DEFAULT_TCP_WINDOW_SIZE_UNIT));
		_lb_tcpWindowSize_actionPerformed();
		
		lb_mss.setSelected(p.getBoolean(KEY_TCP_MSS_ENABLED, DEFAULT_TCP_MSS_ENABLED));
		mss.setValue(p.getDouble(KEY_TCP_MSS, DEFAULT_TCP_MSS));
		mssUnit.setSelectedItem(p.getUnit(KEY_TCP_MSS_UNIT, DEFAULT_TCP_MSS_UNIT));
		_lb_mss_actionPerformed();
		
		tcpNoDelay.setSelected(p.getBoolean(KEY_TCP_NO_DELAY_ENABLED, DEFAULT_TCP_NO_DELAY_ENABLED));
		_tcpNoDelay_actionPerformed();
		
		udpBandwidth.setValue(p.getDouble(KEY_UDP_BANDWIDTH, DEFAULT_UDP_BANDWIDTH));
		udpBandwidthUnit.setSelectedItem(p.getSpeedUnit(KEY_UDP_BANDWIDTH_UNIT, DEFAULT_UDP_BANDWIDTH_UNIT));
		
		lb_udpBufferSize.setSelected(p.getBoolean(KEY_UDP_BUFFER_SIZE_ENABLED, DEFAULT_UDP_BUFFER_SIZE_ENABLED));
		udpBufferSize.setValue(p.getDouble(KEY_UDP_BUFFER_SIZE, DEFAULT_UDP_BUFFER_SIZE));
		udpBufferSizeUnit.setSelectedItem(p.getUnit(KEY_UDP_BUFFER_SIZE_UNIT, DEFAULT_UDP_BUFFER_SIZE_UNIT));
		_lb_udpBufferSize_actionPerformed();
		
		lb_udpPacketSize.setSelected(p.getBoolean(KEY_UDP_PACKET_SIZE_ENABLED, DEFAULT_UDP_PACKET_SIZE_ENABLED));
		udpPacketSize.setValue(p.getDouble(KEY_UDP_PACKET_SIZE, DEFAULT_UDP_PACKET_SIZE));
		udpPacketSizeUnit.setSelectedItem(p.getUnit(KEY_UDP_PACKET_SIZE_UNIT, DEFAULT_UDP_PACKET_SIZE_UNIT));
		_lb_udpPacketSize_actionPerformed();
		
		// IP layer panel
		TTL.setValue(p.getInteger(KEY_TTL, DEFAULT_TTL));
		tos.setSelectedItem(p.getTosOption(KEY_TOS, DEFAULT_TOS));
		bindhost.setText(p.getString(KEY_BIND_TO_HOST, DEFAULT_BIND_TO_HOST));
		
		ipv6.setSelected(!p.getBoolean(KEY_IPV6_ENABLED, DEFAULT_IPV6_ENABLED));
		ipv6.doClick();
	}
	
	
	private IPerfProperties getCurrentConfiguration()
	{
		IPerfProperties p = new IPerfProperties(true);
		
		// quickstart panel
		p.put(KEY_MODE, serverModeRadioButton.isSelected() ? "server" : "client");
		p.put(KEY_SERVER_ADDRESS, serverAddress.getText());
		p.put(KEY_SERVER_PORT, serverPort.getValue());
		p.put(KEY_CLIENT_LIMIT_ENABLED, lb_clientLimit.isSelected());
		p.put(KEY_CLIENT_LIMIT, clientLimit.getText());
		p.put(KEY_LISTEN_PORT, listenPort.getValue());
		p.put(KEY_PARALLEL_STREAMS, simultaneousConnectionsNumber.getValue());
		p.put(KEY_NUM_CONNECTIONS, connectionsLimitNumber.getValue());
		
		// application layer panel
		p.put(KEY_COMPATIBILITY_MODE_ENABLED, compatibilityMode.isSelected());
		p.put(KEY_TRANSMIT, transmit.getValue());
		p.put(KEY_TRANSMIT_UNIT, transmitSecondsRadioButton.isSelected() ? "seconds" : "bytes");
		p.put(KEY_OUTPUT_FORMAT, (IperfUnit)formatList.getSelectedItem());
		p.put(KEY_REPORT_INTERVAL, interval.getValue());
		p.put(KEY_TEST_MODE_DUAL_ENABLED, dualMode.isSelected());
		p.put(KEY_TEST_MODE_TRADE_ENABLED, tradeMode.isSelected());
		p.put(KEY_TEST_MODE_PORT, testPort.getValue());
		p.put(KEY_PRINT_MSS_ENABLED, printMSS.isSelected());
		
		// transport layer panel
		p.put(KEY_TRANSPORT_PROTOCOL, udpRadioButton.isSelected() ? "udp" : "tcp");
		
		p.put(KEY_TCP_BUFFER_LENGTH_ENABLED, lb_tcpBufferLength.isSelected());
		p.put(KEY_TCP_BUFFER_LENGTH, tcpBufferLength.getValue());
		p.put(KEY_TCP_BUFFER_LENGTH_UNIT, (IperfUnit)tcpBufferSizeUnit.getSelectedItem());
		
		p.put(KEY_TCP_WINDOW_SIZE_ENABLED, lb_tcpWindowSize.isSelected());
		p.put(KEY_TCP_WINDOW_SIZE, tcpWindowSize.getValue());
		p.put(KEY_TCP_WINDOW_SIZE_UNIT, (IperfUnit)tcpWindowSizeUnit.getSelectedItem());
		
		p.put(KEY_TCP_MSS_ENABLED, lb_mss.isSelected());
		p.put(KEY_TCP_MSS, mss.getValue());
		p.put(KEY_TCP_MSS_UNIT, (IperfUnit)mssUnit.getSelectedItem());
		
		p.put(KEY_TCP_NO_DELAY_ENABLED, tcpNoDelay.isSelected());
		
		p.put(KEY_UDP_BANDWIDTH, udpBandwidth.getValue());
		p.put(KEY_UDP_BANDWIDTH_UNIT, (IperfSpeedUnit)udpBandwidthUnit.getSelectedItem());
		
		p.put(KEY_UDP_BUFFER_SIZE_ENABLED, lb_udpBufferSize.isSelected());
		p.put(KEY_UDP_BUFFER_SIZE, udpBufferSize.getValue());
		p.put(KEY_UDP_BUFFER_SIZE_UNIT, (IperfUnit)udpBufferSizeUnit.getSelectedItem());
		
		p.put(KEY_UDP_PACKET_SIZE_ENABLED, lb_udpPacketSize.isSelected());
		p.put(KEY_UDP_PACKET_SIZE, udpPacketSize.getValue());
		p.put(KEY_UDP_PACKET_SIZE_UNIT, (IperfUnit)udpPacketSizeUnit.getSelectedItem());
		
		// IP layer panel
		p.put(KEY_TTL, TTL.getValue());
		p.put(KEY_TOS, (TosOption)tos.getSelectedItem());
		p.put(KEY_BIND_TO_HOST, bindhost.getText());
		p.put(KEY_IPV6_ENABLED, ipv6.isSelected());
		
		return p;
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
			serverPort = new IntegerSpinner(1, Integer.MAX_VALUE, 5001);
			serverPort.addChangeListener(this);
			applicationForm.addCell(lb_serverAddress);
			applicationForm.addCompositeCell(serverAddress, lb_serverPort, serverPort);
			applicationForm.newLine();
			applicationForm.addCell(new JLabel());
			applicationForm.addCell(new JLabel());
			lb_simultaneousConnectionsNumber = new JLabel("Parallel Streams");
			lb_simultaneousConnectionsNumber.setOpaque(false);
			lb_simultaneousConnectionsNumber.setToolTipText("The number of simultaneous connections to make to the server. Default is 1.   (command line: -P)");
			simultaneousConnectionsNumber = new IntegerSpinner(1, Integer.MAX_VALUE, 1);
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
			listenPort = new IntegerSpinner(1, Integer.MAX_VALUE, 5001);
			listenPort.addChangeListener(this);
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
			connectionsLimitNumber = new IntegerSpinner(0, Integer.MAX_VALUE, 0);
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
			transmit = new IntegerSpinner(0, Integer.MAX_VALUE, 10);
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
			formatList = new JComboBox(IperfUnit.getAllowedOutputFormatUnits());
			formatList.addActionListener(this);
			formatList.setSelectedItem(IperfUnit.KBITS);
			applicationForm.addCell(lb_outputFormat);
			applicationForm.addCell(formatList);

			applicationForm.newLine();

			// interval of reports
			lb_reportInterval = new JLabel("Report Interval");
			lb_reportInterval.setToolTipText("Sets the interval time (secs) between periodic bandwidth, jitter, and loss reports   (command line: -i)");
			interval = new IntegerSpinner(1, Integer.MAX_VALUE, 1);
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
			testPort = new IntegerSpinner(1, Integer.MAX_VALUE, 5001);
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
			FormLayoutBuilder tcpForm = new FormLayoutBuilder(3, new FormLayoutColumn(Alignment.fill));
			tcpForm.addCell(tcpRadioButton);
			tcpForm.newLine();
			// buffer length
			lb_tcpBufferLength = new JCheckBox("Buffer Length");
			lb_tcpBufferLength.setToolTipText("Read/Write buffer length. Use 'K' or 'M' for kilo/mega bytes. (i.e 8K)   (command line: -l)");
			lb_tcpBufferLength.addActionListener(this);
			tcpForm.addCell(lb_tcpBufferLength);
			tcpBufferLength = new DoubleSpinner(1, 9999, 8);
			tcpBufferLength.addChangeListener(this);
			tcpForm.addCell(tcpBufferLength);
			tcpBufferSizeUnit = new JComboBox(IperfUnit.getAllowedBufferSizeUnits());
			tcpBufferSizeUnit.addActionListener(this);
			tcpBufferSizeUnit.setSelectedItem(IperfUnit.KBYTES);
			tcpForm.addCell(tcpBufferSizeUnit);

			tcpForm.newLine();

			// set window size
			lb_tcpWindowSize = new JCheckBox("TCP Window Size");
			lb_tcpWindowSize.setToolTipText("Set TCP window size. Use 'K' or 'M' for kilo/mega bytes. (i.e 8K)   (command line: -w)");
			lb_tcpWindowSize.addActionListener(this);
			tcpForm.addCell(lb_tcpWindowSize);
			tcpWindowSize = new DoubleSpinner(1, 9999, 8);
			tcpWindowSize.addChangeListener(this);
			tcpForm.addCell(tcpWindowSize);
			tcpWindowSizeUnit = new JComboBox(IperfUnit.getAllowedTCPWindowSizeUnits());
			tcpWindowSizeUnit.addActionListener(this);
			tcpWindowSizeUnit.setSelectedItem(IperfUnit.KBYTES);
			tcpForm.addCell(tcpWindowSizeUnit);

			tcpForm.newLine();

			// attempt to set MSS
			lb_mss = new JCheckBox("Max Segment Size");
			lb_mss.setToolTipText("Attempt to set max segment size. Use 'K' or 'M' for kilo/mega bytes. (i.e 8K)   (command line: -M)");
			lb_mss.addActionListener(this);
			tcpForm.addCell(lb_mss);
			mss = new DoubleSpinner(1, 9999, 8);
			mss.addChangeListener(this);
			tcpForm.addCell(mss);
			mssUnit = new JComboBox(IperfUnit.getAllowedTCPMaxSegmentSizeUnits());
			mssUnit.addActionListener(this);
			mssUnit.setSelectedItem(IperfUnit.KBYTES);
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
			udpBandwidth = new DoubleSpinner(1, 9999, 1);
			udpBandwidth.addChangeListener(this);
			udpForm.addCell(udpBandwidth);
			udpBandwidthUnit = new JComboBox(IperfSpeedUnit.values());
			udpBandwidthUnit.addActionListener(this);
			udpBandwidthUnit.setSelectedItem(IperfSpeedUnit.MEGABITS_PERSEC);
			udpForm.addCell(udpBandwidthUnit);

			udpForm.newLine();

			// buffer size
			lb_udpBufferSize = new JCheckBox("UDP Buffer Size");
			lb_udpBufferSize.setToolTipText("Set UDP buffer size. Use 'K' or 'M' for kilo/mega bytes. (i.e 8K)   (command line: -w)");
			lb_udpBufferSize.addActionListener(this);
			udpForm.addCell(lb_udpBufferSize);
			udpBufferSize = new DoubleSpinner(1, 9999, 8);
			udpBufferSize.addChangeListener(this);
			udpForm.addCell(udpBufferSize);
			udpBufferSizeUnit = new JComboBox(IperfUnit.getAllowedBufferSizeUnits());
			udpBufferSizeUnit.addActionListener(this);
			udpBufferSizeUnit.setSelectedItem(IperfUnit.KBYTES);
			udpForm.addCell(udpBufferSizeUnit);

			udpForm.newLine();

			// packet size
			lb_udpPacketSize = new JCheckBox("UDP Packet Size");
			lb_udpPacketSize.setToolTipText("Set UDP datagram buffer size. Use 'K' or 'M' for kilo/mega bytes. (i.e 1470)   (command line: -l)");
			lb_udpPacketSize.addActionListener(this);
			udpForm.addCell(lb_udpPacketSize);
			udpPacketSize = new DoubleSpinner(1, 9999, 1500);
			udpPacketSize.addChangeListener(this);
			udpForm.addCell(udpPacketSize);
			udpPacketSizeUnit = new JComboBox(IperfUnit.getAllowedUDPPacketSizeUnits());
			udpPacketSizeUnit.addActionListener(this);
			udpPacketSizeUnit.setSelectedItem(IperfUnit.KBYTES);
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
			TTL = new IntegerSpinner(0, Integer.MAX_VALUE, 1);
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
		
		saveFileChooser = new JFileChooser();
		saveFileChooser.setFileFilter(new ExtensionFileFilter("JPerf files", ".jperf"));
		
		loadFileChooser = new JFileChooser();
		loadFileChooser.setFileFilter(new ExtensionFileFilter("JPerf files", ".jperf"));
		
		// set up our menu
		menuBar = new JMenuBar();
		menuJPerf = new JMenu("JPerf");
		menuJPerf.setMnemonic(KeyEvent.VK_H);
		
		menuJPerfOpen = new JMenuItem("Open configuration...");
		menuJPerfOpen.setActionCommand("LoadConfiguration");
		menuJPerfOpen.addActionListener(this);
		menuJPerf.add(menuJPerfOpen);
		
		menuJPerfSaveAs = new JMenuItem("Save configuration as...");
		menuJPerfSaveAs.setActionCommand("SaveConfiguration");
		menuJPerfSaveAs.addActionListener(this);
		menuJPerf.add(menuJPerfSaveAs);
		
		menuJPerfRestoreDefaults = new JMenuItem("Restore default configuration");
		menuJPerfRestoreDefaults.setActionCommand("Restore");
		menuJPerfRestoreDefaults.addActionListener(this);
		menuJPerf.add(menuJPerfRestoreDefaults);
		
		menuJPerf.addSeparator();
		
		menuJPerfAbout = new JMenuItem("About...");
		menuJPerfAbout.setActionCommand("About");
		menuJPerfAbout.addActionListener(this);
		menuJPerf.add(menuJPerfAbout);
		
		menuJPerf.addSeparator();
		
		menuJPerfQuit = new JMenuItem("Quit");
		menuJPerfQuit.setActionCommand("Quit");
		menuJPerfQuit.addActionListener(this);
		menuJPerf.add(menuJPerfQuit);
		
		menuBar.add(menuJPerf);
		this.setJMenuBar(menuBar);

		// set up the tool bar
		toolbar = new JToolBar(JToolBar.HORIZONTAL);
		toolbar.setFloatable(false);
		toolbar.add(getQuickStartPanel());

		// run button
		startIperf = new JButton(new ImageIcon(JPerfUI.class.getResource("start.png")));
		startIperf.setToolTipText("Run IPerf!");
		startIperf.setText("Run IPerf!");
		startIperf.setActionCommand("Run");
		startIperf.addActionListener(this);

		// add stop button
		stopIperf = new JButton(new ImageIcon(JPerfUI.class.getResource("logout.png")));
		stopIperf.setToolTipText("Stop Iperf");
		stopIperf.setText("Stop IPerf!");
		stopIperf.setActionCommand("Stop");
		stopIperf.addActionListener(this);
		stopIperf.setEnabled(false);
		
		// save configuration button
		saveConfiguration = new JButton(new ImageIcon(JPerfUI.class.getResource("filesaveas.png")));
		saveConfiguration.setToolTipText("Save configuration");
		saveConfiguration.setActionCommand("SaveConfiguration");
		saveConfiguration.addActionListener(this);
		
		// load configuration button
		loadConfiguration = new JButton(new ImageIcon(JPerfUI.class.getResource("fileopen.png")));
		loadConfiguration.setToolTipText("Load configuration");
		loadConfiguration.setActionCommand("LoadConfiguration");
		loadConfiguration.addActionListener(this);
		
		// restore defaults button
		restoreDefaults = new JButton(new ImageIcon(JPerfUI.class.getResource("reload.png")));
		restoreDefaults.setToolTipText("Restore default settings");
		restoreDefaults.setActionCommand("Restore");
		restoreDefaults.addActionListener(this);
		
		JPanel slrContainer = new JPanel(new GridLayout(1, 3));
		slrContainer.add(saveConfiguration);
		slrContainer.add(loadConfiguration);
		slrContainer.add(restoreDefaults);
		
		JPanel buttonContainer = new JPanel(new GridLayout(3, 1));
		buttonContainer.add(startIperf);
		buttonContainer.add(stopIperf);
		buttonContainer.add(slrContainer);
		
		toolbar.add(buttonContainer);
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
			alwaysClearOutput = new JCheckBox("Clear Output on each Iperf Run");
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
	
	private void _compatibilityMode_actionPerformed()
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
	
	private void _lb_clientLimit_actionPerformed()
	{
		// nothing
	}
	
	private void _transmitBytesRadioButton_actionPerformed()
	{
		// nothing
	}
	
	private void _transmitSecondsRadioButton_actionPerformed()
	{
		// nothing
	}
	
	private void _dualMode_actionPerformed()
	{
		// nothing
	}
	
	private void _tradeMode_actionPerformed()
	{
		// nothing
	}
	
	private void _printMSS_actionPerformed()
	{
		// nothing
	}
	
	private void _lb_tcpBufferLength_actionPerformed()
	{
		tcpBufferLength.setEnabled(lb_tcpBufferLength.isSelected());
		tcpBufferSizeUnit.setEnabled(lb_tcpBufferLength.isSelected());
	}
	
	private void _lb_tcpWindowSize_actionPerformed()
	{
		tcpWindowSize.setEnabled(lb_tcpWindowSize.isSelected());
		tcpWindowSizeUnit.setEnabled(lb_tcpWindowSize.isSelected());
	}
	
	private void _lb_mss_actionPerformed()
	{
		mss.setEnabled(lb_mss.isSelected());
		mssUnit.setEnabled(lb_mss.isSelected());
	}
	
	private void _lb_udpBufferSize_actionPerformed()
	{
		udpBufferSize.setEnabled(lb_udpBufferSize.isSelected());
		udpBufferSizeUnit.setEnabled(lb_udpBufferSize.isSelected());
	}
	
	private void _lb_udpPacketSize_actionPerformed()
	{
		udpPacketSize.setEnabled(lb_udpPacketSize.isSelected());
		udpPacketSizeUnit.setEnabled(lb_udpPacketSize.isSelected());
	}
	
	private void _tcpNoDelay_actionPerformed()
	{
		// nothing
	}
	
	public void actionPerformed(final ActionEvent e)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
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
					_compatibilityMode_actionPerformed();
				}
				else if (source == lb_clientLimit)
				{
					_lb_clientLimit_actionPerformed();
				}
				else if (source == transmitBytesRadioButton)
				{
					_transmitBytesRadioButton_actionPerformed();
				}
				else if (source == transmitSecondsRadioButton)
				{
					_transmitSecondsRadioButton_actionPerformed();
				}
				else if (source == dualMode)
				{
					_dualMode_actionPerformed();
				}
				else if (source == tradeMode)
				{
					_tradeMode_actionPerformed();
				}
				else if (source == printMSS)
				{
					_printMSS_actionPerformed();
				}
				else if (source == lb_tcpBufferLength)
				{
					_lb_tcpBufferLength_actionPerformed();
				}
				else if (source == lb_tcpWindowSize)
				{
					_lb_tcpWindowSize_actionPerformed();
				}
				else if (source == lb_mss)
				{
					_lb_mss_actionPerformed();
				}
				else if (source == lb_udpBufferSize)
				{
					_lb_udpBufferSize_actionPerformed();
				}
				else if (source == lb_udpPacketSize)
				{
					_lb_udpPacketSize_actionPerformed();
				}
				else if (source == tcpNoDelay)
				{
					_tcpNoDelay_actionPerformed();
				}
				else
				{
					String command = e.getActionCommand();
		
					if (command == "Restore")
					{
						applyConfiguration(defaultConfiguration);
					}
					else if (command == "SaveConfiguration")
					{
						int res = saveFileChooser.showSaveDialog(JPerfUI.this);
						if (res == JFileChooser.APPROVE_OPTION)
						{
							File selectedFile = saveFileChooser.getSelectedFile();
							try
							{
								IPerfProperties p = getCurrentConfiguration();
								if (!selectedFile.getName().endsWith(".jperf"))
								{
									selectedFile = new File(selectedFile.getAbsolutePath()+".jperf");
								}
								p.saveAs(selectedFile);
							}
							catch (Exception e1)
							{
								e1.printStackTrace();
								JOptionPane.showMessageDialog(JPerfUI.this, "<html>Impossible to save this configuration.<br>(cause='"+e1.getMessage()+"')</html>", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
					else if (command == "LoadConfiguration")
					{
						int res = loadFileChooser.showOpenDialog(JPerfUI.this);
						if (res == JFileChooser.APPROVE_OPTION)
						{
							File selectedFile = loadFileChooser.getSelectedFile();
							try
							{
								applyConfiguration(new IPerfProperties(selectedFile));
							}
							catch (Exception e1)
							{
								JOptionPane.showMessageDialog(JPerfUI.this, "<html>Impossible to load this configuration file.<br>(cause='"+e1.getMessage()+"')</html>", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
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
							JOptionPane.showMessageDialog(JPerfUI.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
		
						// can not have adaptive bits for graph
						IperfUnit of = (IperfUnit) formatList.getSelectedItem();
						if (of == IperfUnit.ADAPTIVE_BITS || of == IperfUnit.ADAPTIVE_BYTES)
						{
							JOptionPane.showMessageDialog(JPerfUI.this, "The bandwidth graph will not be created because an adaptive format is selected", "Information", JOptionPane.INFORMATION_MESSAGE);
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
							iperf = new IperfThread(serverModeRadioButton.isSelected(), options, JPerfUI.this);
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
						JOptionPane.showMessageDialog(JPerfUI.this, aboutPanel);
					}
					else if (command == "Quit")
					{
						System.exit(0);
					}
					else if (command == "Browse")
					{
						JFileChooser fc = new JFileChooser();
						int returnVal = fc.showDialog(JPerfUI.this, "Select representative file");
						if (returnVal == JFileChooser.APPROVE_OPTION)
						{
							File file = fc.getSelectedFile();
							representativeFile.setText(file.getAbsolutePath());
						}
					}
					else if (command == "Save")
					{
						JFileChooser fc = new JFileChooser();
						int returnVal = fc.showDialog(JPerfUI.this, "Save");
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
								JOptionPane.showMessageDialog(JPerfUI.this, "Error while saving output: " + ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
				updateIperfCommandLabel();
			}
		});
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
			options += " -w " + tcpWindowSize.getValue() + ((IperfUnit) tcpWindowSizeUnit.getSelectedItem()).getShortcut();
		}
		else if (lb_udpBufferSize.isSelected() && udpBufferSize.isEnabled())
		{
			options += " -w " + udpBufferSize.getValue() + ((IperfUnit) udpBufferSizeUnit.getSelectedItem()).getShortcut();
		}
		if (bindhost.getText().length() > 0)
		{
			options += " -B " + bindhost.getText();
		}
		if (lb_mss.isSelected() && mss.isEnabled())
		{
			options += " -M " + mss.getValue() + ((IperfUnit) mssUnit.getSelectedItem()).getShortcut();
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
			options += " -l " + tcpBufferLength.getValue() + ((IperfUnit) tcpBufferSizeUnit.getSelectedItem()).getShortcut();
		}
		else if (lb_udpPacketSize.isSelected() && udpPacketSize.isEnabled())
		{
			options += " -l " + udpPacketSize.getValue() + ((IperfUnit) udpPacketSizeUnit.getSelectedItem()).getShortcut();
		}
		if (compatibilityMode.isSelected() && compatibilityMode.isEnabled())
		{
			options += " -C";
		}

		// do format
		options += " -f " + ((IperfUnit) formatList.getSelectedItem()).getShortcut();

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
