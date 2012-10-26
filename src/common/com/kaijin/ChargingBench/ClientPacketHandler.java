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
	int chargeLevel = 0;
	boolean working = false;

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
		//if (Utils.isDebug()) System.out.println("ClientPacketHandler onPacketData");
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet.data));
		//Read the first int to determine packet type
		try
		{
			this.packetType = stream.readInt();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		// each packet type needs to implement an if and then whatever other read functions it needs complete with try/catch blocks
		if (this.packetType == 0)
		{
			try
			{
				this.x = stream.readInt();
				this.y = stream.readInt();
				this.z = stream.readInt();
				this.chargeLevel = stream.readInt();
				this.working = stream.readBoolean();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

			World world = FMLClientHandler.instance().getClient().theWorld;
			TileEntity tile = world.getBlockTileEntity(x, y, z);
			if (tile instanceof TEChargingBench)
			{
				((TEChargingBench)tile).chargeLevel = this.chargeLevel;
				((TEChargingBench)tile).doingWork = this.working;
				if (Utils.isDebug()) System.out.println("ClientPacketHandler chargeLevel: " + this.chargeLevel + " working: " + this.working);
				world.markBlockNeedsUpdate(x, y, z);
			}
		}
		if (this.packetType == 1)
		{
			try
			{
				this.x = stream.readInt();
				this.y = stream.readInt();
				this.z = stream.readInt();
				this.working = stream.readBoolean();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

			World world = FMLClientHandler.instance().getClient().theWorld;
			TileEntity tile = world.getBlockTileEntity(x, y, z);
			if (tile instanceof TEBatteryStation)
			{
				((TEBatteryStation)tile).doingWork = this.working;
				if (Utils.isDebug()) System.out.println("ClientPacketHandler working: " + this.working);
				world.markBlockNeedsUpdate(x, y, z);
			}
		}

	}
}
