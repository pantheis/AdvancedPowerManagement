/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/

package com.kaijin.AdvPowerMan;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Level;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class ClientPacketHandler implements IPacketHandler
{
	/*
	 * Packet format:
	 * 0: byte  Packet Type
	 * 1: int   x location of TileEntity
	 * 2: int   y location of TileEntity
	 * 3: int   z location of TileEntity
	 *  
	 * Currently used packet types
	 *         
	 * Server-to-Client:
	 * 0 = Universal description packet
     *   Charging Bench:
	 *     4: int      charge level for texture
	 *     5: boolean  activity state for texture
	 *
	 *   Battery Station:
	 *     4: boolean  activity state for texture
	 *
	 *   Storage Monitor:
	 *     4: int      charge level for texture
	 *     5: boolean  power state for texture
	 *     6: boolean  valid state for texture
	 */

	@Override
	public void onPacketData(INetworkManager network, Packet250CustomPayload packet, Player player)
	{
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet.data));

		// Determine packet type and coordinates of affected tile entity 
		int packetType = -1;
		int x = 0;
		int y = 0;
		int z = 0;
		try
		{
			packetType = stream.readInt();
			x = stream.readInt();
			y = stream.readInt();
			z = stream.readInt();
		}
		catch (IOException e)
		{
			FMLLog.getLogger().info(Info.TITLE_LOG + "Failed to read packet from server. (Details: " + e.toString() + ")");
			return;
		}

		if (packetType == 0)
		{
			World world = FMLClientHandler.instance().getClient().theWorld;
			TileEntity tile = world.getBlockTileEntity(x, y, z);

			Exception e;
			try
			{
				((TECommon)tile).receiveDescriptionData(packetType, stream);
				return;
			}
			catch (ClassCastException ex)
			{
				e = ex;
			}
			catch (NullPointerException ex)
			{
				e = ex;
			}
			FMLLog.getLogger().info(Info.TITLE_LOG + "Client received description packet for " + x + ", " + y + ", " + z + 
				" but couldn't deliver to tile entity. (Details: " + e.toString() + ")");
			return;
		}
	}
}
