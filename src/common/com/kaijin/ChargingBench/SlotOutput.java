package com.kaijin.ChargingBench;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

public class SlotOutput extends Slot
{
	public int invIndex;

	public SlotOutput(IInventory inv, int index, int xpos, int ypos)
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
    	// No items may be placed here, parameter is ignored 
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
