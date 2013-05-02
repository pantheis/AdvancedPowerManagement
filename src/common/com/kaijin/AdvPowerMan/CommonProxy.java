/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/

package com.kaijin.AdvPowerMan;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

public class CommonProxy implements IGuiHandler
{
	public void load() {}

	public boolean isClient()
	{
		return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
	}

	public boolean isServer()
	{
		return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER;
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
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) 
	{
		if (!world.blockExists(x, y, z)) return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (ID == Info.GUI_ID_CHARGING_BENCH && tile instanceof TEChargingBench)
		{
			return new ContainerChargingBench(player.inventory, (TEChargingBench)tile);
		}
		else if (ID == Info.GUI_ID_BATTERY_STATION && tile instanceof TEBatteryStation)
		{
			return new ContainerBatteryStation(player.inventory, (TEBatteryStation)tile);
		}
		else if (ID == Info.GUI_ID_STORAGE_MONITOR && tile instanceof TEStorageMonitor)
		{
			return new ContainerStorageMonitor(player.inventory, (TEStorageMonitor)tile);
		}
		else if (ID == Info.GUI_ID_ADJUSTABLE_EMITTER && tile instanceof TEAdvEmitter)
		{
			return new ContainerAdvEmitter((TEAdvEmitter)tile);
		}
		else if (ID == Info.GUI_ID_ADJUSTABLE_TRANSFORMER && tile instanceof TEAdjustableTransformer)
		{
			return new ContainerAdjustableTransformer((TEAdjustableTransformer)tile);
		}

		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) 
	{
		if (!world.blockExists(x, y, z)) return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (ID == Info.GUI_ID_CHARGING_BENCH && tile instanceof TEChargingBench)
		{
			return new GuiChargingBench(player.inventory, (TEChargingBench)tile);
		}
		else if (ID == Info.GUI_ID_BATTERY_STATION && tile instanceof TEBatteryStation)
		{
			return new GuiBatteryStation(player.inventory, (TEBatteryStation)tile);
		}
		else if (ID == Info.GUI_ID_STORAGE_MONITOR && tile instanceof TEStorageMonitor)
		{
			return new GuiStorageMonitor(player.inventory, (TEStorageMonitor)tile);
		}
		else if (ID == Info.GUI_ID_ADJUSTABLE_EMITTER && tile instanceof TEAdvEmitter)
		{
			return new GuiAdvEmitter((TEAdvEmitter)tile);
		}
		else if (ID == Info.GUI_ID_ADJUSTABLE_TRANSFORMER && tile instanceof TEAdjustableTransformer)
		{
			return new GuiAdjustableTransformer((TEAdjustableTransformer)tile);
		}

		return null;
	}
}
