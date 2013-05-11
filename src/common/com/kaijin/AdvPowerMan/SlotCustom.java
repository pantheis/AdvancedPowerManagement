/*******************************************************************************
 * Copyright (c) 2012-2013 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class SlotCustom extends Slot
{
	public SlotCustom(IInventory inv, int index, int xpos, int ypos)
	{
		super(inv, index, xpos, ypos);
	}

	@Override
	public void onSlotChanged()
	{
		if (inventory instanceof TECommon)
		{
			((TECommon)inventory).onInventoryChanged(this.getSlotIndex());
		}
		else
		{
			inventory.onInventoryChanged();
		}
	}
}
