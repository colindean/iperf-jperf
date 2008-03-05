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

import net.nlanr.jperf.ui.FormLayoutBuilder.Alignment;

public class FormLayoutColumn
{
	public Alignment alignment;
	public int width;
	public boolean grow;
	
	public FormLayoutColumn()
	{
		this(Alignment.left);
	}
	
	public FormLayoutColumn(Alignment alignment)
	{
		this(alignment, -1);
	}
	
	public FormLayoutColumn(Alignment alignment, boolean grow)
	{
		this(alignment, -1, grow);
	}
	
	public FormLayoutColumn(Alignment alignment, int width)
	{
		this(alignment, width, false);
	}
	
	public FormLayoutColumn(Alignment alignment, int width, boolean grow)
	{
		this.alignment = alignment;
		this.width = width;
		this.grow = grow;
	}
}