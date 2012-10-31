/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.ChargingBench;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;

public class ItemCardBase extends Item
{

	protected ItemCardBase(int id)
	{
		super(id);
	}

	@Override
	public String getTextureFile()
	{
		return ChargingBench.proxy.ITEM_PNG;
	}

	@Override
	public boolean isRepairable()
	{
		return false;
	}

	public static int[] getCoordinates(ItemStack itemStack)
	{
		if (!(itemStack.getItem() instanceof ItemStorageLinkCard))
			return null;
		NBTTagCompound nbtTagCompound = itemStack.getTagCompound();
		if (nbtTagCompound == null)
		{
			return null;
		}
		int[] coordinates = new int[]{
				nbtTagCompound.getInteger("x"),  
				nbtTagCompound.getInteger("y"),  
				nbtTagCompound.getInteger("z")  
		};
		return coordinates;
	}
	
	public String getTitle(ItemStack stack)
	{
		if (!(stack.getItem() instanceof ItemStorageLinkCard))
			return "";
		NBTTagCompound nbtTagCompound = stack.getTagCompound();
		if (nbtTagCompound == null)
			return "";
		return nbtTagCompound.getString("title");
	}

	public void setTitle(ItemStack stack, String title)
	{
		Utils.getOrCreateStackTag(stack).setString("title", title);
	}
}
