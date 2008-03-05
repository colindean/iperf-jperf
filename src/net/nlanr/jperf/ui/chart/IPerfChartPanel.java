/**
 * - 02/2008: Class created by Nicolas Richasse
 * 
 * Changelog:
 * 	- class created
 * 
 * To do:
 * 	- ...
 */

package net.nlanr.jperf.ui.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.nlanr.jperf.core.Measurement;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class IPerfChartPanel extends AbstractChartPanel
	implements Runnable
{
	private class SeriesData
	{
		public String		seriesId;
		public String		bandwidthLegend, jitterLegend;
		public JLabel		seriesLabel;
		public XYSeries	bandwidthSeries;
		public XYSeries	jitterSeries;
		public Color		seriesColor;
		public String		printfBandwidthValueExpression, printfJitterValueExpression;

		public SeriesData(String seriesId, String bandwidthLegend, String jitterLegend, Color seriesColor, String printfBandwidthValueExpression, String printfJitterValueExpression)
		{
			this.seriesId = seriesId;
			this.bandwidthLegend = bandwidthLegend;
			this.jitterLegend = jitterLegend;
			this.bandwidthSeries = new XYSeries(bandwidthLegend);
			this.jitterSeries = new XYSeries(bandwidthLegend);
			this.seriesLabel = new JLabel(bandwidthLegend);
			this.seriesColor = seriesColor;
			this.printfBandwidthValueExpression = printfBandwidthValueExpression;
			this.printfJitterValueExpression = printfJitterValueExpression;
			seriesLabel.setForeground(seriesColor);
		}
	}

	private int													proportion;
	private CombinedDomainXYPlot				graphSet;
	private XYItemRenderer							bandwidthRenderer, jitterRenderer;
	private XYSeriesCollection					bandwidthCollection, jitterCollection;
	private JPanel											panelTextStats	= new JPanel(new GridLayout(0, 4));
	private JLabel											labelDate				= new JLabel(" ");
	private double											delayInSeconds;
	private SimpleDateFormat						sdf							= new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
	private HashMap<String, SeriesData>	seriesData			= new HashMap<String, SeriesData>();
	private Thread											timeThread			= null;
	private Color												backgroundColor, foregroundColor, gridColor;
	private String											bandwidthUnit, jitterUnit;
	private boolean isServerMode = false;
	private double timeWindow, reportInterval;
	
	public IPerfChartPanel(String title, String bandwidthUnit, String jitterUnit, String timeAxisLabel, String bandwidthValueAxisLabel, String jitterValueAxisLabel, double delayInSeconds, double timeWindow, double reportInterval, Color backgroundColor, Color foregroundColor,
			Color gridColor)
	{
		this.delayInSeconds = delayInSeconds;
		this.proportion = 1;
		this.backgroundColor = backgroundColor;
		this.foregroundColor = foregroundColor;
		this.gridColor = gridColor;

		reconfigure(false, title, bandwidthUnit, jitterUnit, timeAxisLabel, bandwidthValueAxisLabel, jitterValueAxisLabel, timeWindow, reportInterval);
	}

	public void start()
	{
		if (timeThread == null)
		{
			this.timeThread = new Thread(this);
			timeThread.start();
		}
	}

	private boolean seriesExists(String seriesId)
	{
		if (seriesData.keySet().contains(seriesId))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public void maybeAddNewSeries(String seriesId, String seriesLegend, String jitterLegend, Color seriesColor)
	{
		maybeAddNewSeries(seriesId, seriesLegend, jitterLegend, seriesColor, "%4.2f", "%4.2f");
	}

	public synchronized void maybeAddNewSeries(String seriesId, String seriesLegend, String jitterLegend, Color seriesColor, String printfBandwidthValueExpression, String printfJitterValueExpression)
	{
		if (!seriesExists(seriesId))
		{
			SeriesData data = new SeriesData(seriesId, seriesLegend, jitterLegend, seriesColor, printfBandwidthValueExpression, printfJitterValueExpression);
			seriesData.put(seriesId, data);
			
			bandwidthCollection.addSeries(data.bandwidthSeries);
			bandwidthRenderer.setSeriesPaint(bandwidthCollection.getSeriesCount()-1, data.seriesColor);
			
			if (isServerMode)
			{
				jitterCollection.addSeries(data.jitterSeries);
				jitterRenderer.setSeriesPaint(jitterCollection.getSeriesCount()-1, data.seriesColor);
			}
			
			panelTextStats.add(data.seriesLabel);
		}
	}

	public void reconfigure(boolean isServerMode, String title, String bandwidthUnit, String jitterUnit, String timeAxisLabel, String bandwidthValueAxisLabel, String jitterValueAxisLabel, double timeWindow, double reportInterval)
	{
		this.isServerMode = isServerMode;
		this.bandwidthUnit = bandwidthUnit;
		this.jitterUnit = jitterUnit;
		this.timeWindow = timeWindow;
		this.reportInterval = reportInterval;
		
		// reset the content pane
		this.removeAll();
		panelTextStats.removeAll();
		SeriesColorGenerator.reset();

		seriesData.clear();

		// creation of the chart group
		graphSet = new CombinedDomainXYPlot(new NumberAxis(timeAxisLabel));
		// space between charts
		graphSet.setGap(10.0);
		// creation of the jFreeChart
		jFreeChart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, graphSet, false);
		jFreeChart.setBackgroundPaint(backgroundColor);
		if (title != null)
		{
			jFreeChart.setTitle(title);
			jFreeChart.getTitle().setPaint(foregroundColor);
		}
		// creation of the chart panel
		chartPanel = new ChartPanel(jFreeChart);
		chartPanel.setBackground(backgroundColor);
		// creation of the series set
		bandwidthCollection = new XYSeriesCollection();
		// creation of the renderer
		bandwidthRenderer = new XYLineAndShapeRenderer();
		// set the UI presentation
		NumberAxis rangeAxis = new NumberAxis(bandwidthValueAxisLabel);
		rangeAxis.setLabelPaint(foregroundColor);

		// creation of the bandwidth plot
		XYPlot bandwidthPlot = new XYPlot(bandwidthCollection, null, rangeAxis, bandwidthRenderer);
		bandwidthPlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		bandwidthPlot.setDomainCrosshairVisible(false);
		bandwidthPlot.setRangeCrosshairVisible(false);
		bandwidthPlot.setBackgroundPaint(backgroundColor);
		bandwidthPlot.setDomainGridlinePaint(gridColor);
		bandwidthPlot.setRangeGridlinePaint(gridColor);

		// add the plot
		graphSet.add(bandwidthPlot, proportion);

		// set up the domain axis
		ValueAxis axis = bandwidthPlot.getDomainAxis();
		if (timeAxisLabel != null)
		{
			axis.setTickLabelPaint(foregroundColor);
			axis.setLabel(timeAxisLabel);
			axis.setLabelPaint(foregroundColor);
		}
		else
		{
			axis.setVisible(false);
		}
		axis.setAutoRange(true);
		axis.setFixedAutoRange((int)Math.min(timeWindow, 30));

		// set up the range axis
		axis = bandwidthPlot.getRangeAxis();
		axis.setTickLabelPaint(foregroundColor);

		if (isServerMode)
		{
			rangeAxis = new NumberAxis(jitterValueAxisLabel);
			rangeAxis.setLabelPaint(foregroundColor);

			// creation of the jitter plot
			jitterRenderer = new XYLineAndShapeRenderer();
			jitterCollection = new XYSeriesCollection();
			XYPlot jitterPlot = new XYPlot(jitterCollection, null, rangeAxis, jitterRenderer);
			jitterPlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
			jitterPlot.setDomainCrosshairVisible(false);
			jitterPlot.setRangeCrosshairVisible(false);
			jitterPlot.setBackgroundPaint(backgroundColor);
			jitterPlot.setDomainGridlinePaint(gridColor);
			jitterPlot.setRangeGridlinePaint(gridColor);

			// add the plot
			graphSet.add(jitterPlot, proportion);

			// set up the range axis
			axis = jitterPlot.getRangeAxis();
			axis.setTickLabelPaint(foregroundColor);
		}

		// set up the hour&date label presentation
		labelDate.setHorizontalAlignment(JLabel.RIGHT);
		labelDate.setForeground(foregroundColor);

		panelTextStats.setBackground(backgroundColor);

		this.add(labelDate, BorderLayout.NORTH);
		this.add(chartPanel, BorderLayout.CENTER);
		this.add(panelTextStats, BorderLayout.SOUTH);

		this.setBackground(backgroundColor);
	}

	public void addSeriesBandwidthMeasurement(String seriesId, Measurement measurement)
	{
		SeriesData data = seriesData.get(seriesId);
		
		if (measurement.getEndTime()-measurement.getStartTime() > reportInterval)
		{
			// this is the sum-up of the test
			data.seriesLabel.setText(String.format("<html><b>%s</b> [" + data.printfBandwidthValueExpression + "%s] </html>", data.bandwidthLegend, measurement.getValue(), bandwidthUnit+"/s"));
			return;
		}
		
		if (isServerMode)
		{
			try
			{
				if (data.bandwidthSeries.getDataItem((int)measurement.getEndTime()) != null)
				{
					// clear the series
					data.bandwidthSeries.clear();
				}
			}
			catch(Exception e)
			{
				// nothing
			}
		}
		data.bandwidthSeries.add(measurement.getEndTime(), measurement.getValue());
		data.seriesLabel.setText(String.format("<html><b>%s</b> " + data.printfBandwidthValueExpression + "%s </html>", data.bandwidthLegend, measurement.getValue(), bandwidthUnit+"/s"));
	}
	
	public void addSeriesBandwidthAndJitterMeasurement(String seriesId, Measurement bandwidth, Measurement jitter)
	{
		SeriesData data = seriesData.get(seriesId);
		
		if (bandwidth.getEndTime()-bandwidth.getStartTime() > reportInterval)
		{
			// this is the sum-up of the test
			data.seriesLabel.setText(String.format("<html><b>%s</b> [" + data.printfBandwidthValueExpression + "%s]<br><b>%s</b> ["+data.printfJitterValueExpression+"%s]</html>", data.bandwidthLegend, bandwidth.getValue(), bandwidthUnit+"/s", data.jitterLegend, jitter.getValue(), jitterUnit));
			return;
		}
		
		if (isServerMode)
		{
			try
			{
				if (data.bandwidthSeries.getDataItem((int)bandwidth.getEndTime()) != null)
				{
					// clear the series
					data.bandwidthSeries.clear();
				}
			}
			catch(Exception e)
			{
				// nothing
			}
			try
			{
				if (data.jitterSeries.getDataItem((int)jitter.getEndTime()) != null)
				{
					// clear the series
					data.jitterSeries.clear();
				}
			}
			catch(Exception e)
			{
				// nothing
			}
		}
		data.bandwidthSeries.add(bandwidth.getEndTime(), bandwidth.getValue());
		data.jitterSeries.add(jitter.getEndTime(), jitter.getValue());
		data.seriesLabel.setText(String.format("<html><b>%s</b> " + data.printfBandwidthValueExpression + "%s<br><b>%s</b> "+data.printfJitterValueExpression+"%s</html>", data.bandwidthLegend, bandwidth.getValue(), bandwidthUnit+"/s", data.jitterLegend, jitter.getValue(), jitterUnit));
	}

	public void run()
	{
		while (true)
		{
			try
			{
				Date d = new Date();
				labelDate.setText(sdf.format(d));
				Thread.sleep((int) (delayInSeconds * 1000));
			}
			catch (Exception ex)
			{
			}
		}
	}
}
