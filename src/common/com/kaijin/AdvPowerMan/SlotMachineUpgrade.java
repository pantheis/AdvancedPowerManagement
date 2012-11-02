/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

public class SlotMachineUpgrade extends Slot
{
	public int invIndex;

	public SlotMachineUpgrade(IInventory inv, int index, int xpos, int ypos)
	{
		super(inv, index, xpos, ypos);
		invIndex = index;
	}

    /**
     * Check if the stack is a valid item for this slot.
     */
	@Override
    public boolean isItemValid(ItemStack stack)
    {
    	// Decide if the item is a valid IC2 machine upgrade 
    	if (stack == null) return false;
    	if (stack.isItemEqual(Info.ic2overclockerUpg) || stack.isItemEqual(Info.ic2transformerUpg) || stack.isItemEqual(Info.ic2storageUpg))
    	{
    		return true;
    	}
        return false; 
    }

	@Override
    public void onSlotChanged()
    {
    	if (inventory instanceof TECommonBench)
    	{
            ((TECommonBench)inventory).onInventoryChanged(invIndex);
    	}
    	else
    	{
            inventory.onInventoryChanged();
    	}
    }
}
