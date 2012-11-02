/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import java.util.List;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

public class ItemStorageLinkCard extends ItemCardBase
{
	private static final String HINT_TEMPLATE = "x: %d, y: %d, z: %d";

	public ItemStorageLinkCard(int id)
	{
		super(id);
		setIconIndex(16);
		setMaxStackSize(1);
		// This shouldn't be easily spawnable, so don't show in creative tabs
		// setTabToDisplayOn(CreativeTabs.tabMisc);
	}

	@Override
	@SideOnly(Side.SERVER)
	public boolean getShareTag()
	{
		return true;
	} 

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addInformation(ItemStack itemStack, EntityPlayer player, List info, boolean bool) 
	{
		int[] coordinates = getCoordinates(itemStack);
		if (coordinates != null)
		{
			NBTTagCompound nbtTagCompound = itemStack.getTagCompound();
			String title = nbtTagCompound.getString("title");
			if (title != null && !title.isEmpty())
			{
				info.add(title);
			}
			String hint = String.format(HINT_TEMPLATE, coordinates[0], coordinates[1], coordinates[2]);
			info.add(hint);
		}
	}

	/**
	 * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
	 * @return The ItemStack to replace it with.
	 */
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		if (player.isSneaking())
		{
			return new ItemStack(AdvancedPowerManagement.itemStorageLinkCardCreator);
		}
		else
		{
			return stack;
		}
	}
}
