package com.kaijin.AdvPowerMan;

import net.minecraft.src.IInventory;

public class SlotDrainable extends SlotPowerSource
{

	public SlotDrainable(IInventory inv, int index, int xpos, int ypos, int tier)
	{
		super(inv, index, xpos, ypos, tier);
	}

	@Override
	public int getBackgroundIconIndex()
	{
		return 249;
	}
}
