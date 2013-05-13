/*******************************************************************************
 * Copyright (c) 2012-2013 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class SlotDrainable extends SlotCustom
{
	protected int powerTier;

	public SlotDrainable(IInventory inv, int index, int xpos, int ypos, int tier)
	{
		super(inv, index, xpos, ypos);
		powerTier = tier;
	}

	/**
	 * Check if the stack is a valid item for this slot.
	 */
	@Override
	public boolean isItemValid(ItemStack stack)
	{
		// Decide if the item is a valid IC2 power source
		return Utils.isItemDrainable(stack, powerTier);
	}

	@Override
	public int getSlotStackLimit()
	{
		return 1;
	}

	@Override
	public Icon getBackgroundIconIndex()
	{
		return Info.iconSlotDrainable;
		// return -1; // 232;
	}
}
