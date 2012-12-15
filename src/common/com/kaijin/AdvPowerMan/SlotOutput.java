/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.Slot;

public class SlotOutput extends SlotCustom
{
	public SlotOutput(IInventory inv, int index, int xpos, int ypos)
	{
		super(inv, index, xpos, ypos);
	}

	/**
	 * Check if the stack is a valid item for this slot.
	 */
	@Override
	public boolean isItemValid(ItemStack stack)
	{
		// No items may be placed here, parameter is ignored 
		return false; 
	}

	@Override
	public int getBackgroundIconIndex()
	{
		return -1; // 250
	}
}
