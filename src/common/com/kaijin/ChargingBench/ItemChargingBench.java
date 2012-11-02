/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.ChargingBench;


import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;

public class ItemChargingBench extends ItemBlock
{
	protected static final String[] itemNames = new String[] {"blockChargingBench1", "blockChargingBench2", "blockChargingBench3",
		"blockEmitterBlock1", "blockEmitterBlock2", "blockEmitterBlock3", "blockEmitterBlock4", "blockEmitterAdjustable",
		"blockBatteryStation1", "blockBatteryStation2", "blockBatteryStation3", "blockStorageMonitor"};

	public ItemChargingBench(int var1)
	{
		super(var1);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	public int getMetadata(int meta)
	{
		return meta;
	}

	public String getItemNameIS(ItemStack var1)
	{
		int var2 = var1.getItemDamage();

		if (var2 >= 0 && var2 <= Info.LAST_META_VALUE) return itemNames[var2];

		return null;
	}
}
