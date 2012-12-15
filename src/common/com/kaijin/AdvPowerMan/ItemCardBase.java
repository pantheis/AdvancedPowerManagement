/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemCardBase extends Item
{

	protected ItemCardBase(int id)
	{
		super(id);
	}

	@Override
	public String getTextureFile()
	{
		return Info.ITEM_PNG;
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
				nbtTagCompound.getInteger("z"),  
				nbtTagCompound.getInteger("dim")
		};
		return coordinates;
	}

	public static void setCoordinates(ItemStack itemStack, int[] coords)
	{
		final String tags[] = {"x", "y", "z", "dim"};
		NBTTagCompound nbtTagCompound = Utils.getOrCreateStackTag(itemStack);
		for (int i = 0; i < coords.length && i < 4; i++)
		{
			nbtTagCompound.setInteger(tags[i], coords[i]);
		}
	}

	public static void setCoordinates(ItemStack itemStack, int x, int y, int z, int dim)
	{
		NBTTagCompound nbtTagCompound = Utils.getOrCreateStackTag(itemStack);
		nbtTagCompound.setInteger("x", x);
		nbtTagCompound.setInteger("y", y);
		nbtTagCompound.setInteger("z", z);
		nbtTagCompound.setInteger("dim", dim);
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
