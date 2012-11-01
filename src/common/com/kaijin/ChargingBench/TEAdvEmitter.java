/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.ChargingBench;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ic2.api.Direction;
import ic2.api.EnergyNet;
import ic2.api.IEnergySource;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;

public class TEAdvEmitter extends TileEntity implements IEnergySource
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
		if (!ChargingBench.proxy.isClient())
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
		if (!ChargingBench.proxy.isClient())
		{
			super.writeToNBT(nbttagcompound);
			nbttagcompound.setInteger("outputRate", outputRate);
			nbttagcompound.setInteger("packetSize", packetSize);
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
		if (ChargingBench.proxy.isClient()) return;

		if (!initialized)
		{
			if (worldObj == null) return;
			EnergyNet.getForWorld(worldObj).addTileEntity(this);
			initialized = true;
		}

		if (receivingRedstoneSignal())
		{
			energyBuffer += outputRate;
			while(energyBuffer >= packetSize)
			{
				EnergyNet.getForWorld(worldObj).emitEnergyFrom(this, packetSize);
				energyBuffer -= packetSize;
			}
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

	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this)
		{
			return false;
		}

		return entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64D;
	}
	
	/**
	 * Packet reception by server of what button was clicked on the client's GUI.
	 * @param id = the button ID
	 */
	public void receiveGuiCommand(int id)
	{
		switch (id)
		{
		case 0:
			packetSize -= 100;
			if (packetSize < 1) packetSize = 1;
			break;
		case 1:
			packetSize -= 10;
			if (packetSize < 1) packetSize = 1;
			break;
		case 2:
			packetSize -= 1;
			if (packetSize < 1) packetSize = 1;
			break;
		case 3:
			packetSize += 1;
			if (packetSize > 8192) packetSize = 8192;
			break;
		case 4:
			packetSize += 10;
			if (packetSize > 8192) packetSize = 8192;
			break;
		case 5:
			packetSize += 100;
			if (packetSize > 8192) packetSize = 8192;
			break;
		case 6:
			outputRate -= 100;
			if (outputRate < 1) outputRate = 1;
			break;
		case 7:
			outputRate -= 10;
			if (outputRate < 1) outputRate = 1;
			break;
		case 8:
			outputRate -= 1;
			if (outputRate < 1) outputRate = 1;
			break;
		case 9:
			outputRate += 1;
			if (outputRate > 1000000) outputRate = 1000000;
			break;
		case 10:
			outputRate += 10;
			if (outputRate > 1000000) outputRate = 1000000;
			break;
		case 11:
			outputRate += 100;
			if (outputRate > 1000000) outputRate = 1000000;
			break;
		}
	}

	
	/**
	 * Packet transmission from client to server of what button was clicked on the GUI.
	 * @param id = the button ID
	 */
	public void sendGuiCommand(int id)
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);
		try
		{
			data.writeInt(1); // Packet ID for Storage Monitor GUI button clicks
			data.writeInt(xCoord);
			data.writeInt(yCoord);
			data.writeInt(zCoord);
			data.writeInt(id);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = ChargingBench.packetChannel; // CHANNEL MAX 16 CHARS
		packet.data = bytes.toByteArray();
		packet.length = packet.data.length;

		ChargingBench.proxy.sendPacketToServer(packet);
	}

}
