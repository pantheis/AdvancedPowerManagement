package com.kaijin.AdvPowerMan;

public class MovingAverage
{
	protected int packets[] = null;
	protected int delays[] = null;
	protected int position;
	//protected int packetTotal;
	//protected int delayTotal;
	protected int delay;
	protected int window;
	protected float average;
	protected float delta;

	public MovingAverage(int size)
	{
		packets = new int[size];
		delays = new int[size];
		position = 0;
		//packetTotal = 0;
		//delayTotal = size;
		delay = 1;
		window = size;
		average = 0F;
		delta = 0F;
		for (int i = 0; i < size; i++)
		{
			packets[i] = 0;
			delays[i] = 600;
		}
	}

	public void tick(int value)
	{
		if (value > 0 || delay >= 600) // 600 ticks (30 sec) is long enough for 1 EU/t to have triggered a 512 EU packet by now
		{
			position++;
			if (position >= packets.length) position = 0;

			//packetTotal -= packet[position];
			packets[position] = value;
			//packetTotal += value;

			//delayTotal -= time[position];
			delays[position] = delay;
			//delayTotal += delay;
			delay = 1;

			window = sumDelays();
			final float newAvg = ((float)sumPackets()) / ((float)window);
			delta = newAvg - average;
			average = newAvg;
		}
		else delay++;
	}

	protected int sumDelays()
	{
		if (delays == null) return 1;
		int delayTotal = 0;
		for (int d : delays) delayTotal += d;
		return delayTotal;
	}

	protected int sumPackets()
	{
		if (packets == null) return 0;
		int packetTotal = 0;
		for (int p : packets) packetTotal += p;
		return packetTotal;
	}

	public float getAverage()
	{
		return average;
	}

	public int getWindow()
	{
		return window;
	}
}
