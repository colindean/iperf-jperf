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

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FormLayoutBuilder
{
	public enum Alignment
	{
		left, right, fill, center;
	}
	
	private ArrayList<ArrayList<Component>> formLines = new ArrayList<ArrayList<Component>>();
	private ArrayList<FormLayoutColumn> columnsSpec = new ArrayList<FormLayoutColumn>();
	private ArrayList<Component> currentLineCells;
	private HashMap<Component, Integer> componentsColSpan = new HashMap<Component, Integer>(); 
	
	public FormLayoutBuilder(int nbColumns)
	{
		this(nbColumns, new FormLayoutColumn());
	}
	
	public FormLayoutBuilder(int nbColumns, FormLayoutColumn columnLayoutModel)
	{
		for (int i=0 ; i<nbColumns ; i++)
		{
			columnsSpec.add(columnLayoutModel);
		}
	}
	
	public FormLayoutBuilder(FormLayoutColumn column, FormLayoutColumn ... columns)
	{
		columnsSpec.add(column);
		for (FormLayoutColumn col : columns)
		{
			columnsSpec.add(col);
		}
	}
	
	public void addEmptyCell()
	{
		JPanel empty = new JPanel();
		empty.setOpaque(false);
		addCell(empty, 1);
	}
	
	public void addCell(Component component)
	{
		addCell(component, 1);
	}
	
	public void addCompositeCell(Component ... components)
	{
		FormLayoutBuilder b = new FormLayoutBuilder(components.length);
		for (Component c : components)
		{
			b.addCell(c);
		}
		JPanel composite = b.getPanel();
		composite.setOpaque(false);
		addCell(composite);
	}
	
	public void addVerticalCompositeCell(Component ... components)
	{
		FormLayoutBuilder b = new FormLayoutBuilder(components.length);
		for (Component c : components)
		{
			b.addCell(c);
			b.newLine();
		}
		JPanel vComposite = b.getPanel();
		vComposite.setOpaque(false);
		addCell(vComposite);
	}
	
	public void addCell(Component component, int colSpan)
	{
		if (currentLineCells == null)
		{
			newLine();
		}
		currentLineCells.add(component);
		componentsColSpan.put(component, new Integer(colSpan));
	}
	
	public void newLine()
	{
		if (currentLineCells != null && currentLineCells.size() != 0)
		{
			formLines.add(currentLineCells);
			currentLineCells = null;
		}
		
		currentLineCells = new ArrayList<Component>();
	}
	
	public JPanel getPanel()
	{
		if (currentLineCells != null && currentLineCells.size() != 0)
		{
			formLines.add(currentLineCells);
			currentLineCells = null;
		}
		
		String layoutRowString = "";
		for (int i=1 ; i<=formLines.size() ; i++)
		{
			layoutRowString += "pref";
			if (i != formLines.size())
			{
				layoutRowString += ",";
				// add a space between lines
				layoutRowString += "2dlu,";
			}
		}
		String layoutColumnsString = "";
		int i=0;
		for (FormLayoutColumn column : columnsSpec)
		{
			i++;
			
			// add the alignment attribute
			layoutColumnsString += column.alignment.name();
			
			// add the width attribute
			if (column.width <= 0)
			{
				layoutColumnsString += ":pref";
			}
			else
			{
				layoutColumnsString += ":"+column.width+"dlu";
			}
			
			if (column.grow)
			{
				layoutColumnsString += ":grow";
			}
			
			if (i < columnsSpec.size())
			{
				// add space between columns
				layoutColumnsString += ",4dlu";
			}
			else
			{
				layoutColumnsString += ",0dlu";
			}
			
			if (i < columnsSpec.size())
			{
				layoutColumnsString += ",";
			}
		}
		FormLayout layout = new FormLayout(layoutColumnsString, layoutRowString);
		JPanel panel = new JPanel(layout);
		CellConstraints cc = new CellConstraints();
		
		int yPos = 1;
		for (ArrayList<Component> line :  formLines)
		{
			int xPos = 1;
			for (Component cmp : line)
			{
				int colspan = componentsColSpan.get(cmp);
				panel.add(cmp, cc.xyw(xPos, yPos, colspan));
				
				xPos+=colspan;
				xPos++;
			}
			
			// there is always a blank line
			yPos+=2;
		}
		
		return panel;
	}
}
