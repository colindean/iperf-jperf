/**
 * - 02/2008: Class created by Nicolas Richasse
 * 
 * Changelog:
 * 	- class created
 * 
 * To do:
 * 	- ...
 */

package net.nlanr.jperf.core;

import java.util.Vector;

public class JperfStreamResult
{
	private int									ID;
	private Vector<Measurement>	Bandwidth;
	private Vector<Measurement>	Jitter;

	public JperfStreamResult(int i)
	{
		ID = i;
		Bandwidth = new Vector<Measurement>();
		Jitter = new Vector<Measurement>();
	}

	public void addBW(Measurement M)
	{
		Bandwidth.add(M);
	}

	public void addJitter(Measurement M)
	{
		Jitter.add(M);
	}

	public int getID()
	{
		return ID;
	}

	public void print()
	{
		for (int i = 0; i < Bandwidth.size(); ++i)
		{
			((Measurement) Bandwidth.get(i)).print();
		}
	}

	public Vector<Measurement> getBW()
	{
		return Bandwidth;
	}

	public Vector<Measurement> getJitter()
	{
		return Jitter;
	}

	public boolean equals(Object o)
	{
		return o instanceof JperfStreamResult && ((JperfStreamResult) o).ID == ID;
	}
}
