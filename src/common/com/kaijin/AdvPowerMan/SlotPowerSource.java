/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class SlotPowerSource extends SlotCustom
{
	private int powerTier;
	//private int iconIndex;

	public SlotPowerSource(IInventory inv, int index, int xpos, int ypos, int tier)
	{
		super(inv, index, xpos, ypos);
		setTier(tier);
	}

	public void setTier(int tier)
	{
		if (tier < 1) tier = 1;
		if (tier > 3) tier = 3;
		powerTier = tier;
		//iconIndex = 223 + tier;
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
		return Info.iconSlotPowerSource[powerTier - 1];
		// return iconIndex;
	}
}
