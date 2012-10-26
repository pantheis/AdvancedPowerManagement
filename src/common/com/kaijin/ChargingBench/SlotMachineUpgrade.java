package com.kaijin.ChargingBench;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

public class SlotMachineUpgrade extends Slot
{
	public int invIndex;

	public SlotMachineUpgrade(IInventory inv, int index, int xpos, int ypos)
	{
		super(inv, index, xpos, ypos);
		this.invIndex = index;
	}

    /**
     * Check if the stack is a valid item for this slot.
     */
	@Override
    public boolean isItemValid(ItemStack stack)
    {
    	// Decide if the item is a valid IC2 machine upgrade 
    	if (stack == null) return false;
    	if (stack.isItemEqual(ChargingBench.ic2overclockerUpg) || stack.isItemEqual(ChargingBench.ic2transformerUpg) || stack.isItemEqual(ChargingBench.ic2storageUpg))
    	{
    		return true;
    	}
        return false; 
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
