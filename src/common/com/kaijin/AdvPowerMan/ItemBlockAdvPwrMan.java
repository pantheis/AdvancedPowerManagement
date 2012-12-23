/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;


import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockAdvPwrMan extends ItemBlock
{
	public ItemBlockAdvPwrMan(int var1)
	{
		super(var1);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	public int getMetadata(int meta)
	{
		//if (meta >= 3 && meta <= 6) return 7;
		return meta;
	}

	public String getItemNameIS(ItemStack var1)
	{
		int var2 = var1.getItemDamage();

		if (var2 >= 0 && var2 <= Info.LAST_META_VALUE) return Info.KEY_BLOCK_NAMES[var2];

		return null;
	}
}
