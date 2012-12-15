package com.kaijin.AdvPowerMan;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class SlotCustom extends Slot
{
	public SlotCustom(IInventory inv, int index, int xpos, int ypos)
	{
		super(inv, index, xpos, ypos);
	}

	@Override
	public String getBackgroundIconTexture()
	{
		return Info.ITEM_PNG;
	}

	@Override
	public void onSlotChanged()
	{
		if (inventory instanceof TECommon)
		{
			((TECommon)inventory).onInventoryChanged(this.getSlotIndex());
		}
		else
		{
			inventory.onInventoryChanged();
		}
	}
}
