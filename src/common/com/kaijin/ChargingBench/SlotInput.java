package com.kaijin.ChargingBench;

import ic2.api.IElectricItem;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

public class SlotInput extends Slot
{
	public int chargeTier;

	public SlotInput(IInventory inv, int index, int xpos, int ypos, int chargingTier)
	{
		super(inv, index, xpos, ypos);
		chargeTier = chargingTier;
	}

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
}
