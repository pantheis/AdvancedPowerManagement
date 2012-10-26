/* Inventory Stocker
 *  Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 *  Licensed as open source with restrictions. Please see attached LICENSE.txt.
 */

package com.kaijin.ChargingBench;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class CommonProxy implements IGuiHandler
{
	public static final String ITEM_PNG = "/com/kaijin/ChargingBench/textures/ChargingBenchItems.png";
	public static final String BLOCK_PNG = "/com/kaijin/ChargingBench/textures/ChargingBench.png";
	public static final String GUI1_PNG = "/com/kaijin/ChargingBench/textures/GUIChargingBench.png";
	public static final String GUI2_PNG = "/com/kaijin/ChargingBench/textures/GUIBatteryStation.png";

	public void load()
	{

	}

	public boolean isClient()
	{
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		if (side == Side.CLIENT)
		{
			return true;
		}
		return false;
	}

	public boolean isServer()
	{
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		if (side == Side.SERVER)
		{
			return true;
		}
		return false;
	}

	public static void sendPacketToPlayer(Packet250CustomPayload packet, EntityPlayerMP player)
	{
		PacketDispatcher.sendPacketToPlayer(packet, (Player)player);
	}

	public static void sendPacketToServer(Packet250CustomPayload packet)
	{
		PacketDispatcher.sendPacketToServer(packet);
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) 
	{
		if (!world.blockExists(x, y, z))
		{
			return null;
		}

		if(ID == 1)
		{
			TileEntity tile = world.getBlockTileEntity(x, y, z);

			if (!(tile instanceof TEChargingBench))
			{
				return null;
			}

			return new ContainerChargingBench(player.inventory, (TEChargingBench)tile);
		}
		else if(ID == 2)
		{
			TileEntity tile = world.getBlockTileEntity(x, y, z);

			if (!(tile instanceof TEBatteryStation))
			{
				return null;
			}

			return new ContainerBatteryStation(player.inventory, (TEBatteryStation)tile);
		}
		else
		{
			return null;
		}
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) 
	{
		if (!world.blockExists(x, y, z))
		{
			return null;
		}

		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (ID == 1)
		{
			if (!(tile instanceof TEChargingBench))
			{
				return null;
			}
			return new GuiChargingBench(player.inventory, (TEChargingBench)tile);
		}
		else if(ID == 2)
		{
			if (!(tile instanceof TEBatteryStation))
			{
				return null;
			}
			return new GuiBatteryStation(player.inventory, (TEBatteryStation)tile);

		}
		else
		{
			return null;
		}
	}
}
