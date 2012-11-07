/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import ic2.api.Direction;
import ic2.api.EnergyNet;
import ic2.api.IEnergySource;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import cpw.mods.fml.common.FMLLog;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;

public class TEAdvEmitter extends TECommon implements IEnergySource
{
	protected boolean initialized;

	public int outputRate = 32;
	public int packetSize = 32;
	private int energyBuffer = 0;

	public TEAdvEmitter() // Constructor used when placing a new tile entity, to set up correct parameters
	{
		super();
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
			outputRate = nbttagcompound.getInteger("outputRate");
			packetSize = nbttagcompound.getInteger("packetSize");
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
			nbttagcompound.setInteger("outputRate", outputRate);
			nbttagcompound.setInteger("packetSize", packetSize);
		}
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
	public boolean canUpdate()
	{
		return true;
	}

	@Override
	public void updateEntity()
	{
		if (AdvancedPowerManagement.proxy.isClient()) return;

		if (!initialized)
		{
			if (worldObj == null) return;
			EnergyNet.getForWorld(worldObj).addTileEntity(this);
			initialized = true;
		}

		if (receivingRedstoneSignal())
		{
			energyBuffer += outputRate;
			EnergyNet net = EnergyNet.getForWorld(worldObj);
			while (energyBuffer >= packetSize)
			{
				net.emitEnergyFrom(this, packetSize);
				energyBuffer -= packetSize;
			}
		}
	}

	protected boolean receivingRedstoneSignal()
	{
		return worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
	}

	public String getInvName()
	{
		return Info.KEY_BLOCK_NAMES[7] + Info.KEY_NAME_SUFFIX;
	}

	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this)
		{
			return false;
		}

		return entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64D;
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
		return true;
	}

	@Override
	public int getMaxEnergyOutput()
	{
		return Integer.MAX_VALUE;
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
			if (packetSize > 8192) packetSize = 8192;
			break;
		case 1:
			packetSize += 10;
			if (packetSize > 8192) packetSize = 8192;
			break;
		case 2:
			packetSize += 64;
			if (packetSize == 68) packetSize = 64;
			if (packetSize > 8192) packetSize = 8192;
			break;
		case 3:
			packetSize *= 2;
			if (packetSize > 8192) packetSize = 8192;
			break;
		case 4:
			packetSize -= 1;
			if (packetSize < 4) packetSize = 4;
			if (outputRate > packetSize * 64) outputRate = packetSize * 64;
			break;
		case 5:
			packetSize -= 10;
			if (packetSize < 4) packetSize = 4;
			if (outputRate > packetSize * 64) outputRate = packetSize * 64;
			break;
		case 6:
			packetSize -= 64;
			if (packetSize < 4) packetSize = 4;
			if (outputRate > packetSize * 64) outputRate = packetSize * 64;
			break;
		case 7:
			packetSize /= 2;
			if (packetSize < 4) packetSize = 4;
			if (outputRate > packetSize * 64) outputRate = packetSize * 64;
			break;
		case 8:
			outputRate += 1;
			if (outputRate > packetSize * 64) outputRate = packetSize * 64;
			if (outputRate > 65536) outputRate = 65536;
			break;
		case 9:
			outputRate += 10;
			if (outputRate > packetSize * 64) outputRate = packetSize * 64;
			if (outputRate > 65536) outputRate = 65536;
			break;
		case 10:
			outputRate += 64;
			if (outputRate == 65) outputRate = 64;
			if (outputRate > packetSize * 64) outputRate = packetSize * 64;
			if (outputRate > 65536) outputRate = 65536;
			break;
		case 11:
			outputRate *= 2;
			if (outputRate > packetSize * 64) outputRate = packetSize * 64;
			if (outputRate > 65536) outputRate = 65536;
			break;
		case 12:
			outputRate -= 1;
			if (outputRate < 1) outputRate = 1;
			break;
		case 13:
			outputRate -= 10;
			if (outputRate < 1) outputRate = 1;
			break;
		case 14:
			outputRate -= 64;
			if (outputRate < 1) outputRate = 1;
			break;
		case 15:
			outputRate /= 2;
			if (outputRate < 1) outputRate = 1;
			break;
		}
	}
}
