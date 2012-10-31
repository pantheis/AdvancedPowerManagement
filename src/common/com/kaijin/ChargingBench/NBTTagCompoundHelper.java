/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.ChargingBench;

import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;

public class NBTTagCompoundHelper
{
	public static NBTTagCompound getTAGfromItemstack(ItemStack itemStack)
	{
		if (itemStack != null)
		{
			NBTTagCompound tag = itemStack.getTagCompound();
			if (tag == null)
			{
				tag = new NBTTagCompound();
				itemStack.setTagCompound(tag);
			}
			return tag;
		}
		return null;
	}

}
