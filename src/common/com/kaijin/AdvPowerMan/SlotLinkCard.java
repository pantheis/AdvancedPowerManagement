/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.Slot;

public class SlotLinkCard extends Slot
{
	public SlotLinkCard(IInventory inv, int index, int xpos, int ypos)
	{
		super(inv, index, xpos, ypos);
	}

	/**
	 * Check if the stack is a valid item for this slot.
	 */
	@Override
	public boolean isItemValid(ItemStack stack)
	{
		// Decide if the item is a link card
		if (stack != null && stack.getItem() instanceof ItemStorageLinkCard)
		{
			return true;
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
		return 246;
	}

	@Override
	public String getBackgroundIconTexture()
	{
		return Info.ITEM_PNG;
	}

	@Override
	public void onSlotChanged()
	{
		if (this.inventory instanceof TECommon)
		{
			((TECommon)inventory).onInventoryChanged(this.getSlotIndex());
		}
		else
		{
			inventory.onInventoryChanged();
		}
	}
}
