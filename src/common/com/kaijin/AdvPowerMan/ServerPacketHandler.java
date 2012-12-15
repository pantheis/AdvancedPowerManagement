/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/

package com.kaijin.AdvPowerMan;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Level;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class ServerPacketHandler implements IPacketHandler
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
	 * Client-to-Server:
	 * 0 = GUI button command
	 *     4: int      Button ID clicked
	 */
	
	@Override
	public void onPacketData(INetworkManager network, Packet250CustomPayload packet, Player player)
	{
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet.data));

		// Determine packet type and coordinates of affected tile entity 
		int packetType = -1;
		int x;
		int y;
		int z;
		try
		{
			packetType = stream.readInt();
			x = stream.readInt();
			y = stream.readInt();
			z = stream.readInt();
		}
		catch (IOException e)
		{
			FMLLog.getLogger().info(Info.TITLE_LOG + "Failed to read packet from client. (Details: " + e.toString() + ")");
			return;
		}

		if (packetType == 0)
		{
			Exception e;
			try
			{
				World world = ((EntityPlayerMP)player).worldObj;
				TileEntity tile = world.getBlockTileEntity(x, y, z);

				int buttonID = stream.readInt();

				((TECommon)tile).receiveGuiButton(buttonID);
				return;
			}
			catch (ClassCastException ex) { e = ex; }
			catch (NullPointerException ex) { e = ex; }
			catch (IOException ex) { e = ex; }

			FMLLog.getLogger().info(Info.TITLE_LOG + "Server received GUI button packet for " + x + ", " + y + ", " + z + 
				" but couldn't deliver to tile entity. (Details: " + e.toString() + ")");
			return;
		}
	}
}
