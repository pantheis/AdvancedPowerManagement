package com.kaijin.AdvPowerMan;

import ic2.api.IElectricItem;
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
	public Icon getBackgroundIconIndex()
	{
		return Info.iconSlotDrainable;
		// return -1; // 232;
	}
}
