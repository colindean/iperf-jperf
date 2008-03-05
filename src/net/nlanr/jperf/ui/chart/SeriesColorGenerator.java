/**
 * - 02/2008: Class created by Nicolas Richasse
 * 
 * Changelog:
 * 	- class created
 * 
 * To do:
 * 	- add more colors
 */

package net.nlanr.jperf.ui.chart;

import java.awt.Color;

public class SeriesColorGenerator 
{
	private static Color[] allColors = {Color.green, Color.red, Color.yellow, Color.blue, Color.gray, Color.magenta, Color.white, Color.orange};
	private static int currentIndex = 0;
	
	public static Color nextColor()
	{
		Color res = allColors[currentIndex];
		currentIndex = (currentIndex+1)%allColors.length;
		return res;
	}
	
	public static void reset()
	{
		currentIndex = 0;
	}
}
