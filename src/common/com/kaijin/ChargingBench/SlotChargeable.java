/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.ChargingBench;

import ic2.api.IElectricItem;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

public class SlotChargeable extends Slot
{
	public int invIndex;
	public int chargeTier;
	
	public SlotChargeable(IInventory inv, int index, int xpos, int ypos)
	{
		super(inv, index, xpos, ypos);
		this.invIndex = index;
	}

	@Override
	public boolean isItemValid(ItemStack stack)
	{
		// Decide if the item is a valid IC2 electrical item
		if (stack != null && stack.getItem() instanceof IElectricItem)
		{
			IElectricItem item = (IElectricItem)(stack.getItem());
			if (item.getTier() <= ((TECommonBench)inventory).baseTier) return true;
		}
		return false;
	}

	/**
	 * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the case
	 * of armor slots)
	 */
	@Override
	public int getSlotStackLimit()
	{
		return 1;
	}

	@Override
	public void onSlotChanged()
	{
		if (this.inventory instanceof TECommonBench)
		{
			((TECommonBench)this.inventory).onInventoryChanged(this.invIndex);
		}
		else
		{
			this.inventory.onInventoryChanged();
		}
	}
}

