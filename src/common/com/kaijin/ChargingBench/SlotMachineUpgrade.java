package com.kaijin.ChargingBench;

import ic2.api.Items;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

public class SlotMachineUpgrade extends Slot
{
	static final ItemStack overclock = Items.getItem("overclockerUpgrade").copy();
	static final ItemStack transformer = Items.getItem("transformerUpgrade").copy();
	static final ItemStack storage = Items.getItem("energyStorageUpgrade").copy();

	public SlotMachineUpgrade(IInventory inv, int index, int xpos, int ypos)
	{
		super(inv, index, xpos, ypos);
	}

    /**
     * Check if the stack is a valid item for this slot.
     */
    public boolean isItemValid(ItemStack stack)
    {
    	// Decide if the item is a valid IC2 machine upgrade 
    	if (stack == null) return false;
    	if (stack.isItemEqual(overclock) || stack.isItemEqual(transformer) || stack.isItemEqual(storage))
    	{
    		return true;
    	}
        return false; 
    }
}
