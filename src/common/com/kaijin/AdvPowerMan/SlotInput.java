/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import ic2.api.IElectricItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class SlotInput extends SlotCustom
{
	public int chargeTier;

	public SlotInput(IInventory inv, int index, int xpos, int ypos, int tier)
	{
		super(inv, index, xpos, ypos);
		chargeTier = tier;
	}

	@Override
	public boolean isItemValid(ItemStack stack)
	{
		// Decide if the item is a valid IC2 electrical item
		return Utils.isItemChargeable(stack, chargeTier);
	}

	@Override
	public Icon getBackgroundIconIndex()
	{
		return Info.iconSlotInput;
		// return 249;
	}
}
