package com.kaijin.ChargingBench;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

public class SlotOutput extends Slot
{
	public SlotOutput(IInventory inv, int index, int xpos, int ypos)
	{
		super(inv, index, xpos, ypos);
	}

    /**
     * Check if the stack is a valid item for this slot.
     */
    public boolean isItemValid(ItemStack stack)
    {
    	// No items may be placed here, parameter is ignored 
        return false; 
    }
}
