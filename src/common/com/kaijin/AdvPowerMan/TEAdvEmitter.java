/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import ic2.api.Direction;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySource;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cpw.mods.fml.common.FMLLog;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;

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

	public TEAdvEmitter(int i) // Constructor used when placing a new tile entity, to set up correct parameters
	{
		super();
		packetSize = outputRate = (int)Math.pow(2.0D, (double)(2 * i + 3));
		FMLLog.info(Info.TITLE_LOG + "Updating old Emitter block of tier " + i);
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		if (AdvancedPowerManagement.proxy.isServer())
		{
			super.readFromNBT(nbttagcompound);

			// Test if block used to be an old style emitter and if so use appropriate settings
			int baseTier = nbttagcompound.getInteger("baseTier");
			if (baseTier > 0)
			{
				packetSize = outputRate = (int)Math.pow(2.0D, (double)(2 * baseTier + 3));
				FMLLog.info(Info.TITLE_LOG + "Loading NBT data for old Emitter block with baseTier of " + baseTier + " and setting output to " + packetSize);
				
			}
			else
			{
				// Normal load
				outputRate = nbttagcompound.getInteger("outputRate");
				packetSize = nbttagcompound.getInteger("packetSize");
				energyBuffer = nbttagcompound.getInteger("energyBuffer");
				if (packetSize > Info.AE_MAX_PACKET) packetSize = Info.AE_MAX_PACKET;
				if (packetSize < Info.AE_MIN_PACKET) packetSize = Info.AE_MIN_PACKET;
				if (outputRate > packetSize * Info.AE_PACKETS_TICK) outputRate = packetSize * Info.AE_PACKETS_TICK;
				if (outputRate > Info.AE_MAX_OUTPUT) outputRate = Info.AE_MAX_OUTPUT;
				if (outputRate < Info.AE_MIN_OUTPUT) outputRate = Info.AE_MIN_OUTPUT;
				if (energyBuffer > packetSize * Info.AE_PACKETS_TICK) energyBuffer = packetSize * Info.AE_PACKETS_TICK;
			}
		}
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		if (AdvancedPowerManagement.proxy.isServer())
		{
			super.writeToNBT(nbttagcompound);
			nbttagcompound.setInteger("outputRate", outputRate);
			nbttagcompound.setInteger("packetSize", packetSize);
			nbttagcompound.setInteger("energyBuffer", energyBuffer);
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
	public int getGuiID()
	{
		return 2;
	}

	@Override
	public void updateEntity()
	{
		if (AdvancedPowerManagement.proxy.isClient()) return;

		if (!initialized)
		{
			if (worldObj == null) return;

			// Test if this is an old emitter block and needs its meta value adjusted
			final int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
			if (meta != 7)
			{
				FMLLog.info(Info.TITLE_LOG + "Resetting Emitter block meta value from " + meta + " to 7");
				worldObj.setBlockMetadata(xCoord, yCoord, zCoord, 7);
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				return;
			}
			EnergyNet.getForWorld(worldObj).addTileEntity(this);
			initialized = true;
		}

		if (receivingRedstoneSignal())
		{
			energyBuffer += outputRate;
			EnergyNet net = EnergyNet.getForWorld(worldObj);
			while (energyBuffer >= packetSize)
			{
				net.emitEnergyFrom(this, packetSize); // No reason to save any surplus. Output is always the same.
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
		}
	}
}
