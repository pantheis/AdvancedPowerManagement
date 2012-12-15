/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import ic2.api.IElectricItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.Slot;

public class SlotChargeable extends SlotCustom
{
	public int chargeTier;
	
	public SlotChargeable(IInventory inv, int index, int xpos, int ypos, int tier)
	{
		super(inv, index, xpos, ypos);
		chargeTier = tier;
	}

	@Override
	public boolean isItemValid(ItemStack stack)
	{
		// Decide if the item is a valid IC2 electrical item
		if (stack != null && stack.getItem() instanceof IElectricItem)
		{
			IElectricItem item = (IElectricItem)(stack.getItem());
			if (item.getTier() <= chargeTier) return true;
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
		return 247;
	}
}

