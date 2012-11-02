/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.ChargingBench;

import ic2.api.IEnergyStorage;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class ItemStorageLinkCardCreator extends ItemCardBase
{
	public ItemStorageLinkCardCreator(int id)
	{
		super(id);
		setIconIndex(17);
		setMaxStackSize(1);
		setCreativeTab(CreativeTabs.tabMisc);
	}

	private void setCoordinates(ItemStack itemStack, int x, int y, int z)
	{
		NBTTagCompound nbtTagCompound = Utils.getOrCreateStackTag(itemStack);
		nbtTagCompound.setInteger("x", x);
		nbtTagCompound.setInteger("y", y);
		nbtTagCompound.setInteger("z", z);
	}

	@Override
	public boolean onItemUseFirst(ItemStack itemstack, EntityPlayer entityplayer, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (entityplayer instanceof EntityPlayerMP && tile instanceof IEnergyStorage)
		{
			if (Info.isDebugging) System.out.println("Clicked on X:" + x + " Y:" + y + " Z:" + z);
			ItemStack newcard = new ItemStack(AdvancedPowerManagement.itemStorageLinkCard);
			setCoordinates(newcard, x, y, z);
			entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem] = newcard;
			return true;
		}
		return false;
	}
}
