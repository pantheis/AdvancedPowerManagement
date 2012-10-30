package com.kaijin.ChargingBench;

import ic2.api.Direction;
import ic2.api.EnergyNet;
import ic2.api.IEnergySource;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.TileEntity;

public class TEEmitter extends TileEntity implements IEnergySource
{
	private boolean initialized;

	public int baseTier;
	public int outputRate;

	public TEEmitter() // Default constructor used only when loading tile entity from world save
	{
		super();
		// Do nothing else; Creating the inventory array and loading previous values will be handled in NBT read method momentarily. 
	}

	public TEEmitter(int i) // Constructor used when placing a new tile entity, to set up correct parameters
	{
		super();

		//base tier = what we're passed, so 1, 2 or 3 (or 4)
		baseTier = i;

		//Max Input math = 32 for tier 1, 128 for tier 2, 512 for tier 3
		outputRate = (int)Math.pow(2.0D, (double)(2* this.baseTier + 3));
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		if (!ChargingBench.proxy.isClient())
		{
			super.readFromNBT(nbttagcompound);

			if (Utils.isDebug()) System.out.println("Em ID: " + nbttagcompound.getString("id"));

			baseTier = nbttagcompound.getInteger("baseTier");
			if (baseTier == 0)
			{
				// Prevent old emitters from failing to initialize properly if they were placed before they had NBT data
				baseTier = worldObj.getBlockMetadata(xCoord, yCoord, zCoord) - 2;
			}

			//Max Input math = 32 for tier 1, 128 for tier 2, 512 for tier 3
			outputRate = (int)Math.pow(2.0D, (double)(2* this.baseTier + 3));
		}
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		if (!ChargingBench.proxy.isClient())
		{
			super.writeToNBT(nbttagcompound);
			nbttagcompound.setInteger("baseTier", baseTier);
		}
	}

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	@Override
	public boolean isAddedToEnergyNet()
	{
		return initialized;
	}

	@Override
	public void updateEntity()
	{
		if (ChargingBench.proxy.isClient())
		{
			return;
		}
		if (!initialized && worldObj != null)
		{
			EnergyNet.getForWorld(worldObj).addTileEntity(this);
			initialized = true;
		}
		if (isActive())
		{
			EnergyNet.getForWorld(worldObj).emitEnergyFrom(this, outputRate);
		}

	}

	public boolean isActive()
	{
		return receivingRedstoneSignal();
	}

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

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, Direction direction)
	{
		return true;
	}

	@Override
	public int getMaxEnergyOutput()
	{
		return Integer.MAX_VALUE;
	}
}
