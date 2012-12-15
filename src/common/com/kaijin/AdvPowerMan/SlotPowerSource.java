/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import ic2.api.IElectricItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.Slot;

public class SlotPowerSource extends SlotCustom
{
	private int powerTier;
	private int iconIndex;

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
		iconIndex = 223 + tier;
	}

	/**
	 * Check if the stack is a valid item for this slot.
	 */
	@Override
	public boolean isItemValid(ItemStack stack)
	{
		// Decide if the item is a valid IC2 power source
		if (stack != null && stack.getItem() instanceof IElectricItem)
		{
			IElectricItem item = (IElectricItem)(stack.getItem());
			if (item.canProvideEnergy() && item.getTier() <= powerTier) return true;
		}
		return false;
	}

	@Override
	public int getSlotStackLimit()
	{
		return 1;
	}

	@Override
	public int getBackgroundIconIndex()
	{
		return iconIndex;
	}
}
