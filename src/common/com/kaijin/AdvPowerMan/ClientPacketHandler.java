/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/

package com.kaijin.AdvPowerMan;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class ClientPacketHandler implements IPacketHandler
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
	 * Server-to-Client:
	 * 0 = Universal description packet
	 *     4: int        packet ID
	 *     5: DataStream stream
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

		World world = FMLClientHandler.instance().getClient().theWorld;
		TileEntity tile = world.getBlockTileEntity(x, y, z);

		// each packet type needs to implement an if and then whatever other read functions it needs complete with try/catch blocks
		if (packetType == 0)
		{
			try
			{
				if (tile instanceof TECommon)
				{
					((TECommon)tile).receiveDescriptionData(packetType, stream);
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
}
