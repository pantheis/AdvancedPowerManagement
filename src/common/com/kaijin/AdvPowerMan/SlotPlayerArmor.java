/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.Slot;

class SlotPlayerArmor extends SlotCustom
{
	// The armor type that can be placed on that slot, it uses the same values of armorType field on ItemArmor.
	final int armorType;

	SlotPlayerArmor(IInventory inv, int index, int xpos, int ypos, int armorType)
	{
		super(inv, index, xpos, ypos);
		this.armorType = armorType;
	}

	@Override
	public int getSlotStackLimit()
	{
		return 1;
	}

	@Override
	public boolean isItemValid(ItemStack stack)
	{
		if (stack == null) return false;
		return stack.getItem() instanceof ItemArmor ? ((ItemArmor)stack.getItem()).armorType == this.armorType : (stack.getItem().shiftedIndex == Block.pumpkin.blockID ? this.armorType == 0 : false);
	}

	@Override
	public int getBackgroundIconIndex()
	{
		return 240 + armorType;
	}
}
