package com.kaijin.AdvPowerMan;

public class MovingAverage
{
	protected int window[] = null;
	protected int position = 0;
	protected int total = 0;

	int perSecondSum = 0;
	byte tickCount = 0;

	public MovingAverage(int size)
	{
		window = new int[size];
		position = 0;
		total = 0;
		for (int i : window)
		{
			i = 0;
		}
	}

	public void tick(int value)
	{
		tickCount++;
		perSecondSum += value;
		if (tickCount >= 20)
		{
			position++;
			if (position >= window.length) position = 0;
			total -= window[position];
			window[position] = perSecondSum;
			total += perSecondSum;

			tickCount = 0;
			perSecondSum = 0;
		}
	}

	public int getLength()
	{
		return window == null ? 0 : window.length;
	}

	public float getAverage()
	{
		return window == null ? 0 : (float)total / (float)window.length;
	}
}
