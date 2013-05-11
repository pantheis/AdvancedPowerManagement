/*******************************************************************************
 * Copyright (c) 2012-2013 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemStorageLinkCard extends ItemCardBase
{
	private static final String HINT_TEMPLATE = "X: %d, Y: %d, Z: %d, Dim: %d";

	public ItemStorageLinkCard(int id)
	{
		super(id);
		setMaxStackSize(1);
		// This shouldn't be easily spawnable, so don't show in creative tabs
		// setTabToDisplayOn(CreativeTabs.tabMisc);
	}

	@Override
	public void registerIcons(IconRegister iconRegister)
	{
		itemIcon = iconRegister.registerIcon(Info.TITLE_PACKED + ":LinkCard");
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
			String hint = String.format(HINT_TEMPLATE, coordinates[0], coordinates[1], coordinates[2], coordinates [3]);
			info.add(hint);
		}
	}

	/**
	 * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
	 * @return The ItemStack to replace it with.
	 */
	@Override
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
