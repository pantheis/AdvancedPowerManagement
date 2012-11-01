/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/

package com.kaijin.ChargingBench;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class ServerPacketHandler implements IPacketHandler
{
	int packetType = -1;
	int x = 0;
	int y = 0;
	int z = 0;

	/*
	 * Packet format:
	 * 0: byte  Packet Type
	 * 1: int   x location of TileEntity
	 * 2: int   y location of TileEntity
	 * 3: int   z location of TileEntity
	 *  
	 * Currently available packet types
	 *         
	 * Client-to-Server:
	 * 0 = Storage Monitor GUI command info
	 *     4: int      Button ID clicked
	 */
	
	@Override
	public void onPacketData(INetworkManager network, Packet250CustomPayload packet, Player player)
	{
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet.data));
		// Determine packet type and coordinates of affected tile entity 
		packetType = -1;
		try
		{
			packetType = stream.readInt();
			x = stream.readInt();
			y = stream.readInt();
			z = stream.readInt();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return;
		}
		if (packetType < 0) return;

		World world = ((EntityPlayerMP)player).worldObj;
		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (packetType == 0)
		{
			if (ChargingBench.isDebugging) System.out.println("Packet 0");
			try
			{
				int buttonID = stream.readInt();
				if (ChargingBench.isDebugging) System.out.println("Button " + buttonID);
				if (tile instanceof TEStorageMonitor)
				{
					if (ChargingBench.isDebugging) System.out.println("Storage Monitor command sent");
					((TEStorageMonitor)tile).receiveGuiCommand(buttonID);
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		if (packetType == 1)
		{
			if (ChargingBench.isDebugging) System.out.println("Packet 1");
			try
			{
				int buttonID = stream.readInt();
				if (ChargingBench.isDebugging) System.out.println("Button " + buttonID);
				if (tile instanceof TEAdvEmitter)
				{
					if (ChargingBench.isDebugging) System.out.println("Advanced Emitter command sent");
					((TEAdvEmitter)tile).receiveGuiCommand(buttonID);
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
}
