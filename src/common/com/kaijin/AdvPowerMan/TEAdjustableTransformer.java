/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import ic2.api.Direction;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileSourceEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLLog;

public class TEAdjustableTransformer extends TECommon implements IEnergySource, IEnergySink
{
	protected boolean initialized;

	protected int maxInput = 8192;
	protected int energyBuffer = 0;

	public int outputRate = 32;
	public int packetSize = 32;

	public byte[] sideSettings = {0, 0, 0, 0, 0, 0}; // DOWN, UP, NORTH, SOUTH, WEST, EAST

	public TEAdjustableTransformer() // Constructor used when placing a new tile entity, to set up correct parameters
	{
		super();
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);

		outputRate = nbttagcompound.getInteger("outputRate");
		packetSize = nbttagcompound.getInteger("packetSize");
		energyBuffer = nbttagcompound.getInteger("energyBuffer");
		if (packetSize > Info.AE_MAX_PACKET) packetSize = Info.AE_MAX_PACKET;
		if (packetSize < Info.AE_MIN_PACKET) packetSize = Info.AE_MIN_PACKET;
		if (outputRate > packetSize * Info.AE_PACKETS_TICK) outputRate = packetSize * Info.AE_PACKETS_TICK;
		if (outputRate > Info.AE_MAX_OUTPUT) outputRate = Info.AE_MAX_OUTPUT;
		if (outputRate < Info.AE_MIN_OUTPUT) outputRate = Info.AE_MIN_OUTPUT;
		if (energyBuffer > packetSize * Info.AE_PACKETS_TICK) energyBuffer = packetSize * Info.AE_PACKETS_TICK;

		NBTTagList nbttaglist = nbttagcompound.getTagList("SideSettings");
		for (int i = 0; i < nbttaglist.tagCount(); ++i)
		{
			NBTTagCompound entry = (NBTTagCompound)nbttaglist.tagAt(i);
			if (i >= 0 && i < sideSettings.length)
			{
				sideSettings[i] = (byte)(entry.getByte("Flags") & 255);
			}
		}
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("outputRate", outputRate);
		nbttagcompound.setInteger("packetSize", packetSize);
		nbttagcompound.setInteger("energyBuffer", energyBuffer);

		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0; i < sideSettings.length; ++i)
		{
			NBTTagCompound entry = new NBTTagCompound();
			entry.setByte("Flags", sideSettings[i]);
			nbttaglist.appendTag(entry);
		}
		nbttagcompound.setTag("SideSettings", nbttaglist);
	}

	@Override
	public void invalidate()
	{
		if (worldObj != null && initialized)
		{
			EnergyTileUnloadEvent unloadEvent = new EnergyTileUnloadEvent(this);
			MinecraftForge.EVENT_BUS.post(unloadEvent);
		}
		super.invalidate();
	}

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	@Override
	public int getGuiID()
	{
		return Info.GUI_ID_ADJUSTABLE_TRANSFORMER;
	}

	@Override
	public void updateEntity()
	{
		if (AdvancedPowerManagement.proxy.isClient()) return;

		if (!initialized)
		{
			if (worldObj == null) return;

			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			initialized = true;
		}

		if (!receivingRedstoneSignal() && energyBuffer >= packetSize)
		{
			EnergyNet net = EnergyNet.getForWorld(worldObj);
			boolean packetSent;
			do
			{
				packetSent = false;
				EnergyTileSourceEvent sourceEvent = new EnergyTileSourceEvent(this, packetSize);
				MinecraftForge.EVENT_BUS.post(sourceEvent);
				final int surplus = sourceEvent.amount;
				if (surplus < packetSize)
				{
					packetSent = true;
					energyBuffer += surplus - packetSize; // Subtracts transferred amount
				}
			}
			while (packetSent && energyBuffer >= packetSize);
		}
	}

	protected boolean receivingRedstoneSignal()
	{
		return worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
	}

	public String getInvName()
	{
		return Info.KEY_BLOCK_NAMES[6] + Info.KEY_NAME_SUFFIX;
	}

	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this)
		{
			return false;
		}

		return entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64D;
	}

	protected void selfDestroy()
	{
		//dropContents();
		ItemStack stack = new ItemStack(AdvancedPowerManagement.blockAdvPwrMan, 1, Info.AT_META);
		worldObj.setBlockToAir(xCoord, yCoord, zCoord);
		this.invalidate();
	}

	// IC2 API stuff

	@Override
	public boolean isAddedToEnergyNet()
	{
		return initialized;
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, Direction direction)
	{
		// TODO Side I/O
		//System.out.println("emit   - direction.toSideValue() = " + direction.toSideValue() + " setting = " + ((sideSettings[direction.toSideValue()] & 1) == 1));
		return (sideSettings[direction.toSideValue()] & 1) == 1;
	}

	@Override
	public int getMaxEnergyOutput()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public int getMaxSafeInput()
	{
		return maxInput;
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction)
	{
		// TODO Side I/O
		//System.out.println("accept - direction.toSideValue() = " + direction.toSideValue() + " setting = " + ((sideSettings[direction.toSideValue()] & 1) == 0));
		return (sideSettings[direction.toSideValue()] & 1) == 0;
	}

	@Override
	public int demandsEnergy()
	{
		if(!receivingRedstoneSignal())
		{
			final int amt = Math.max(outputRate - energyBuffer, 0); 
			//System.out.println("demandsEnergy: " + amt);
			return amt;
		}
		return 0;
	}

	@Override
	public int injectEnergy(Direction directionFrom, int supply)
	{
		//System.out.println("energyBuffer: " + energyBuffer);
		int surplus = 0;
		if (AdvancedPowerManagement.proxy.isServer())
		{
			// if supply is greater than the max we can take per tick
			if (supply > maxInput)
			{
				//If the supplied EU is over the baseMaxInput, we're getting
				//supplied higher than acceptable current. Pop ourselves off
				//into the world and return all but 1 EU, or if the supply
				//somehow was 1EU, return zero to keep IC2 from spitting out 
				//massive errors in the log
				selfDestroy();
				if (supply <= 1)
					return 0;
				else
					return supply - 1;
			}
			else
			{
				energyBuffer += supply;
				// check if our current energy level is now over the max energy level
				if (energyBuffer > outputRate)
				{
					//if so, our surplus to return is equal to that amount over
					surplus = energyBuffer - outputRate;
					//and set our current energy level TO our max energy level
					energyBuffer = outputRate;
				}
				//surplus may be zero or greater here
			}
		}
		return surplus;
	}

	// Networking stuff

	/**
	 * Packet reception by server of what button was clicked on the client's GUI.
	 * @param id = the button ID
	 */
	@Override
	public void receiveGuiButton(int id)
	{
		switch (id)
		{
		case 0:
			packetSize += 1;
			if (packetSize > Info.AE_MAX_PACKET) packetSize = Info.AE_MAX_PACKET;
			break;
		case 1:
			packetSize += 10;
			if (packetSize > Info.AE_MAX_PACKET) packetSize = Info.AE_MAX_PACKET;
			break;
		case 2:
			packetSize += 64;
			if (packetSize == 68) packetSize = 64;
			if (packetSize > Info.AE_MAX_PACKET) packetSize = Info.AE_MAX_PACKET;
			break;
		case 3:
			packetSize *= 2;
			if (packetSize > Info.AE_MAX_PACKET) packetSize = Info.AE_MAX_PACKET;
			break;
		case 4:
			packetSize -= 1;
			if (packetSize < Info.AE_MIN_PACKET) packetSize = Info.AE_MIN_PACKET;
			if (outputRate > packetSize * Info.AE_PACKETS_TICK) outputRate = packetSize * Info.AE_PACKETS_TICK;
			break;
		case 5:
			packetSize -= 10;
			if (packetSize < Info.AE_MIN_PACKET) packetSize = Info.AE_MIN_PACKET;
			if (outputRate > packetSize * Info.AE_PACKETS_TICK) outputRate = packetSize * Info.AE_PACKETS_TICK;
			break;
		case 6:
			packetSize -= 64;
			if (packetSize < Info.AE_MIN_PACKET) packetSize = Info.AE_MIN_PACKET;
			if (outputRate > packetSize * Info.AE_PACKETS_TICK) outputRate = packetSize * Info.AE_PACKETS_TICK;
			break;
		case 7:
			packetSize /= 2;
			if (packetSize < Info.AE_MIN_PACKET) packetSize = Info.AE_MIN_PACKET;
			if (outputRate > packetSize * Info.AE_PACKETS_TICK) outputRate = packetSize * Info.AE_PACKETS_TICK;
			break;
		case 8:
			outputRate += 1;
			if (outputRate > packetSize * Info.AE_PACKETS_TICK) outputRate = packetSize * Info.AE_PACKETS_TICK;
			if (outputRate > Info.AE_MAX_OUTPUT) outputRate = Info.AE_MAX_OUTPUT;
			break;
		case 9:
			outputRate += 10;
			if (outputRate > packetSize * Info.AE_PACKETS_TICK) outputRate = packetSize * Info.AE_PACKETS_TICK;
			if (outputRate > Info.AE_MAX_OUTPUT) outputRate = Info.AE_MAX_OUTPUT;
			break;
		case 10:
			outputRate += 64;
			if (outputRate == 65) outputRate = 64;
			if (outputRate > packetSize * Info.AE_PACKETS_TICK) outputRate = packetSize * Info.AE_PACKETS_TICK;
			if (outputRate > Info.AE_MAX_OUTPUT) outputRate = Info.AE_MAX_OUTPUT;
			break;
		case 11:
			outputRate *= 2;
			if (outputRate > packetSize * Info.AE_PACKETS_TICK) outputRate = packetSize * Info.AE_PACKETS_TICK;
			if (outputRate > Info.AE_MAX_OUTPUT) outputRate = Info.AE_MAX_OUTPUT;
			break;
		case 12:
			outputRate -= 1;
			if (outputRate < Info.AE_MIN_OUTPUT) outputRate = Info.AE_MIN_OUTPUT;
			break;
		case 13:
			outputRate -= 10;
			if (outputRate < Info.AE_MIN_OUTPUT) outputRate = Info.AE_MIN_OUTPUT;
			break;
		case 14:
			outputRate -= 64;
			if (outputRate < Info.AE_MIN_OUTPUT) outputRate = Info.AE_MIN_OUTPUT;
			break;
		case 15:
			outputRate /= 2;
			if (outputRate < Info.AE_MIN_OUTPUT) outputRate = Info.AE_MIN_OUTPUT;
			break;
		case 16:
		case 17:
		case 18:
		case 19:
		case 20:
		case 21:
			//TODO How can we make IC2 check the new emit/accept values without doing a reload?
			if (initialized) MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			initialized = false;
			sideSettings[id - 16] ^= 1;
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			initialized = true;
			break;
		}
	}
}
