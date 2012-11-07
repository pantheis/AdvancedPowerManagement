package com.kaijin.AdvPowerMan;

public class MovingAverage
{
	protected int packets[] = null;
	protected int delays[] = null;
	protected int position;
	//protected int packetTotal;
	//protected int delayTotal;
	protected int delay;

	public MovingAverage(int size)
	{
		packets = new int[size];
		delays = new int[size];
		position = 0;
		//packetTotal = 0;
		//delayTotal = size;
		delay = 1;
		for (int i = 0; i < size; i++)
		{
			packets[i] = 0;
			delays[i] = 1;
		}
	}

	public void tick(int value)
	{
		if (value > 0 || delay > 600) // 600 ticks (30 sec) is long enough for 1 EU/t to have triggered a 512 EU packet by now
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
		}
		else delay++;
	}

	public int getLength()
	{
		return packets == null ? 0 : packets.length;
	}

	public float getAverage()
	{
		if (packets == null) return 0;
		int packetTotal = 0;
		int delayTotal = 0;
		for (int p : packets) packetTotal += p;
		for (int d : delays) delayTotal += d;
		return ((float)packetTotal) / ((float)delayTotal);
	}
}
