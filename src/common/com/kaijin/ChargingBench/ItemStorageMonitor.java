/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.ChargingBench;


import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;

public class ItemStorageMonitor extends ItemBlock
{
    public ItemStorageMonitor(int var1)
    {
    	super(var1);
    	if (Utils.isDebug()) System.out.println("ItemStorageMonitor");
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    public int getMetadata(int meta)
    {
    	if (Utils.isDebug()) System.out.println("ItemStorageMonitor.getMetadata");
        return meta;
    }

    public String getItemNameIS(ItemStack var1)
    {
    	// if (Utils.isDebug()) System.out.println("ItemChargingBench.getItemNameIS");
        int var2 = var1.getItemDamage();

        switch (var2)
        {
            case 0:
                return "blockStorageMonitor1";
            default:
                return null;
        }
    }
}
