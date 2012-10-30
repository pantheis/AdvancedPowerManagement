/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/

package com.kaijin.ChargingBench;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class ServerPacketHandler implements IPacketHandler
{
	int packetType = -1;
	int x = 0;
	int y = 0;
	int z = 0;
	int Metainfo = 0;

	boolean snapshot = false;
	boolean rotateRequest = false;

	/*
	 * Packet format:
	 * byte 0: Packet Type
	 *     Currently available packet types
	 *         Client:
	 *         0=
	 *             byte 1: x location of TileEntity
	 *             byte 2: y location of TileEntity
	 *             byte 3: z location of TileEntity
	 *             byte 4: boolean request, false = clear snapshot, true = take snapshot
	 *         1=
	 *             byte 1: x location of TileEntity
	 *             byte 2: y location of TileEntity
	 *             byte 3: z location of TileEntity
	 *             byte 4: boolean request, false = not used, true = rotate request
	 *         
	 *         Server:
	 *         0=
	 *             byte 1: x location of TileEntity
	 *             byte 2: y location of TileEntity
	 *             byte 3: z location of TileEntity
	 *             byte 4: int, charge level for texture
	 */
	
	@Override
	public void onPacketData(INetworkManager network, Packet250CustomPayload packet, Player player)
	{
		//if (Utils.isDebug()) System.out.println("ServerPacketHandler.onPacketData");
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet.data));
		//Read the first int to determine packet type
		try
		{
			packetType = stream.readInt();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		/*
		 * each packet type needs to implement an if and then whatever other read functions it needs
		 * complete with try/catch blocks
		 */
		if (packetType == 0)
		{
			try
			{
				x = stream.readInt();
				y = stream.readInt();
				z = stream.readInt();
				snapshot = stream.readBoolean();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		if (packetType == 1)
		{
			try
			{
				x = stream.readInt();
				y = stream.readInt();
				z = stream.readInt();
				rotateRequest = stream.readBoolean();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
}
