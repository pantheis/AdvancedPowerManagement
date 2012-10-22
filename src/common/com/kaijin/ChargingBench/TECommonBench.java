package com.kaijin.ChargingBench;

import ic2.api.EnergyNet;
import net.minecraft.src.EntityItem;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;

public abstract class TECommonBench extends TileEntity
{
	protected boolean initialized;

	public int baseTier;
	public int powerTier; // Transformer upgrades allow charging from energy crystals and lapotrons

	//For outside texture display
	protected int chargeLevel;
	protected boolean doingWork;

	/**
	 * This will cause the block to drop anything inside it, create a new item in the
	 * world of its type, invalidate the tile entity, remove itself from the IC2
	 * EnergyNet and clear the block space (set it to air)
	 */
	protected abstract void selfDestroy();
	abstract int gaugeEnergyScaled(int gaugeSize);
	public abstract void onInventoryChanged(int slot);
	public abstract void dropContents();

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



	public void dropItem(ItemStack item)
	{
		EntityItem entityitem = new EntityItem(worldObj, (double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D, item);
		entityitem.delayBeforeCanPickup = 10;
		worldObj.spawnEntityInWorld(entityitem);
	}
}
