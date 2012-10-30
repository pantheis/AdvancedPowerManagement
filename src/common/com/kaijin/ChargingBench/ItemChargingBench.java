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
		"blockEmitterBlock1", "blockEmitterBlock2", "blockEmitterBlock3", "blockEmitterBlock4",
		"blockBatteryStation1", "blockBatteryStation2", "blockBatteryStation3"};

	public ItemChargingBench(int var1)
	{
		super(var1);
		//if (Utils.isDebug()) System.out.println("ItemChargingBench");
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	public int getMetadata(int meta)
	{
		//if (Utils.isDebug()) System.out.println("ItemChargingBench.getMetadata");
		return meta;
	}

	public String getItemNameIS(ItemStack var1)
	{
		// if (Utils.isDebug()) System.out.println("ItemChargingBench.getItemNameIS");
		int var2 = var1.getItemDamage();

		if (var2 >= 0 && var2 <= ChargingBench.lastMetaValue) return itemNames[var2];

		return null;
	}
}
