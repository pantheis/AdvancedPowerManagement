/* Inventory Stocker
 *  Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 *  Licensed as open source with restrictions. Please see attached LICENSE.txt.
 */

package com.kaijin.ChargingBench;

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
	 * 0 = Charging Bench description packet
	 *     4: int      charge level for texture
	 *     5: boolean  activity state for texture
	 * 
	 * 1 = Battery Station description packet
	 *     4: boolean  activity state for texture
	 *
	 * 2 = Storage Monitor description packet
	 *     4: int      charge level for texture
	 *     5: boolean  power state for texture
	 *     6: boolean  valid state for texture
	 *     
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
				int chargeLevel = stream.readInt();
				boolean doingWork = stream.readBoolean();
				if (tile instanceof TEChargingBench)
				{
					((TEChargingBench)tile).receiveDescriptionData(chargeLevel, doingWork);
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		else if (packetType == 1)
		{
			try
			{
				boolean working = stream.readBoolean();
				if (tile instanceof TEBatteryStation)
				{
					((TEBatteryStation)tile).receiveDescriptionData(working);
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		else if (packetType == 2)
		{
			try
			{
				int chargeLevel = stream.readInt();
				boolean isPowering = stream.readBoolean();
				boolean blockState = stream.readBoolean();
				if (tile instanceof TEStorageMonitor)
				{
					((TEStorageMonitor)tile).receiveDescriptionData(chargeLevel, isPowering, blockState);
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}

	}
}
