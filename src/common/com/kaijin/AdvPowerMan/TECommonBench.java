/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import ic2.api.energy.EnergyNet;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public abstract class TECommonBench extends TECommon implements IInventory
{
	protected ItemStack[] contents;

	protected boolean initialized;

	public int baseTier;
	public int powerTier; // Transformer upgrades allow charging from energy crystals and lapotrons

	//For outside texture display
	protected boolean doingWork;

	boolean receivingRedstoneSignal()
	{
		return worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
	}

	@Override
	public void invalidate()
	{
		if (worldObj != null && initialized)
		{
			EnergyNet.getForWorld(worldObj).removeTileEntity(this);
		}
		super.invalidate();
	}

	// IC2 API functions
	public boolean isAddedToEnergyNet()
	{
		return initialized;
	}

	/**
	 * This will cause the block to drop anything inside it, create a new item in the
	 * world of its type, invalidate the tile entity, remove itself from the IC2
	 * EnergyNet and clear the block space (set it to air)
	 */
	protected abstract void selfDestroy();

	public void dropItem(ItemStack item)
	{
		EntityItem entityitem = new EntityItem(worldObj, (double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D, item);
		entityitem.delayBeforeCanPickup = 10;
		worldObj.spawnEntityInWorld(entityitem);
	}

	@Override
	public void dropContents()
	{
		ItemStack item;
		int i;
		for (i = 0; i < contents.length; ++i)
		{
			item = contents[i];
			contents[i] = null;
			if (item != null && item.stackSize > 0) dropItem(item);
		}
	}

    public abstract int getSizeInventory();

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return contents[i];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		if (this.contents[slot] != null)
		{
			ItemStack output;

			if (this.contents[slot].stackSize <= amount)
			{
				output = this.contents[slot];
				this.contents[slot] = null;
				this.onInventoryChanged(slot);
				return output;
			}
			else
			{
				output = this.contents[slot].splitStack(amount);

				if (this.contents[slot].stackSize <= 0)
				{
					this.contents[slot] = null;
				}
				this.onInventoryChanged(slot);
				return output;
			}
		}
		else
		{
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		if (this.contents[slot] == null)
		{
			return null;
		}

		ItemStack stack = this.contents[slot];
		this.contents[slot] = null;
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack)
	{
		this.contents[slot] = itemstack;

		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit())
		{
			itemstack.stackSize = getInventoryStackLimit();
		}
		this.onInventoryChanged(slot);
	}
	
    /**
     * Returns the name of the inventory.
     */
	@Override
    public abstract String getInvName();

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this)
		{
			return false;
		}

		return entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64D;
	}

	@Override
	public void openChest() {}

	@Override
	public void closeChest() {}

}
