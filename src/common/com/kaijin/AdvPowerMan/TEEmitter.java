/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import ic2.api.Direction;
import ic2.api.EnergyNet;
import ic2.api.IEnergySource;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

public class TEEmitter extends TileEntity implements IEnergySource
{
	protected boolean initialized;

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
		outputRate = (int)Math.pow(2.0D, (double)(2 * baseTier + 3));
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		if (!AdvancedPowerManagement.proxy.isClient())
		{
			super.readFromNBT(nbttagcompound);
			baseTier = nbttagcompound.getInteger("baseTier");

			//Max Input math = 32 for tier 1, 128 for tier 2, 512 for tier 3
			outputRate = (int)Math.pow(2.0D, (double)(2 * baseTier + 3));
		}
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		if (!AdvancedPowerManagement.proxy.isClient())
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
		if (AdvancedPowerManagement.proxy.isClient()) return;

		if (!initialized)
		{
			if (worldObj == null) return;

			// Prevent old emitters from misbehaving if they were placed before they saved their tier in NBT data
			if (baseTier == 0)
			{
				if (Info.isDebugging) System.out.println("baseTier is zero!");
				if (worldObj.getBlockId(xCoord, yCoord, zCoord) == AdvancedPowerManagement.blockIDAdvPwrMan)
				{
					baseTier = worldObj.getBlockMetadata(xCoord, yCoord, zCoord) - 2;
					outputRate = (int)Math.pow(2.0D, (double)(2 * baseTier + 3));
					if (Info.isDebugging) System.out.println("baseTier is now: " + baseTier);
					if (Info.isDebugging) System.out.println("output is now: " + outputRate);
				}
				else
				{
					// Just in case there's a stale tile entity somehow...
					this.invalidate();
					return;
				}
			}

			EnergyNet.getForWorld(worldObj).addTileEntity(this);
			initialized = true;
		}

		if (receivingRedstoneSignal())
		{
			EnergyNet.getForWorld(worldObj).emitEnergyFrom(this, outputRate);
		}
	}

	protected boolean receivingRedstoneSignal()
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
