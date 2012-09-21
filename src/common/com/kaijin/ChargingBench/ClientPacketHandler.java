/* Inventory Stocker
 *  Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 *  Licensed as open source with restrictions. Please see attached LICENSE.txt.
 */

package com.kaijin.ChargingBench;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import com.kaijin.ChargingBench.*;

import net.minecraft.src.*;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import cpw.mods.fml.common.network.*;

public class ClientPacketHandler implements IPacketHandler
{
	int packetType = -1;
	int x = 0;
	int y = 0;
	int z = 0;
	int Metainfo = 0;

	boolean snapshot = false;


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
	 *             byte 4: boolean information, false = no valid snapshot, true = valid snapshot
	 *         1=@Deprecated
	 *             byte 1: x location of TileEntity
	 *             byte 2: y location of TileEntity
	 *             byte 3: z location of TileEntity
	 *             byte 4: int "metadata", sync client TE rotation and lights with server
	 *             
	 * remaining bytes: data for packet
	 */

	@Override
	public void onPacketData(NetworkManager network, Packet250CustomPayload packet, Player player)
	{
		if (Utils.isDebug()) System.out.println("ClientPacketHandler.onPacketData");
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
		/*
		 * each packet type needs to implement an if and then whatever other read functions it needs
		 * complete with try/catch blocks
		 */
		if (this.packetType == 0)
		{
			try
			{
				this.x = stream.readInt();
				this.y = stream.readInt();
				this.z = stream.readInt();
				this.snapshot = stream.readBoolean();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

			if (Utils.isDebug()) System.out.println("ClientPacketHandler: Attempting to get theWorld");
			World world = FMLClientHandler.instance().getClient().theWorld;
			TileEntity tile = world.getBlockTileEntity(x, y, z);

			//check if the tile we're looking at is an Inventory Stocker tile
			if (tile instanceof TileEntityInventoryStocker)
			{
				//				String s = new Boolean(snapshot).toString();
				//				if (Utils.isDebug()) System.out.println("ClientPacketHandler: tile.setSnapshotState: " + s + ", guid: " + ((TileEntityInventoryStocker)tile).myGUID);
				//snapshot state message from server
				((TileEntityInventoryStocker)tile).setSnapshotState(snapshot);
			}
		}

		if (this.packetType == 1)
		{
			try
			{
				this.x = stream.readInt();
				this.y = stream.readInt();
				this.z = stream.readInt();
				this.Metainfo = stream.readInt();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			World world = FMLClientHandler.instance().getClient().theWorld;
			TileEntity tile = world.getBlockTileEntity(x, y, z);

			//check if the tile we're looking at is an Inventory Stocker tile
			if (tile instanceof TileEntityInventoryStocker)
			{
				((TileEntityInventoryStocker)tile).Metainfo = this.Metainfo;
				world.markBlockNeedsUpdate(x, y, z);
			}
		}
	}
}
