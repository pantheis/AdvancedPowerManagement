package com.kaijin.ChargingBench;

import ic2.api.Items;
import net.minecraft.src.Block;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ItemArmor;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

class SlotPlayerArmor extends Slot
{

	/**
     * The armor type that can be placed on that slot, it uses the same values of armorType field on ItemArmor.
     */
    final int armorType;
    public int invIndex;

    SlotPlayerArmor(IInventory inv, int index, int xpos, int ypos, int armorType)
    {
        super(inv, index, xpos, ypos);
        this.armorType = armorType;
        this.invIndex = index;
    }

    /**
     * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the case
     * of armor slots)
     */
    public int getSlotStackLimit()
    {
        return 1;
    }

    /**
     * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
     */
    public boolean isItemValid(ItemStack stack)
    {
    	if (stack == null) return false;
    	return stack.getItem() instanceof ItemArmor ? ((ItemArmor)stack.getItem()).armorType == this.armorType : (stack.getItem().shiftedIndex == Block.pumpkin.blockID ? this.armorType == 0 : false);
    }
    
	@Override
	public void onSlotChanged()
	{
		if (this.inventory instanceof TEChargingBench)
		{
			((TEChargingBench)this.inventory).onInventoryChanged(this.invIndex);
		}
		else
		{
			this.inventory.onInventoryChanged();
		}
	}
}
