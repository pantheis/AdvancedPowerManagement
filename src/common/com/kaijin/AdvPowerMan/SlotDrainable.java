package com.kaijin.AdvPowerMan;

import ic2.api.IElectricItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

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
		if (stack != null && stack.getItem() instanceof IElectricItem)
		{
			IElectricItem item = (IElectricItem)(stack.getItem());
			if (item.canProvideEnergy() && item.getTier() <= powerTier) return true;
		}
		return false;
	}

	@Override
	public int getBackgroundIconIndex()
	{
		return -1; // 232;
	}
}
