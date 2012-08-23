/* Inventory Stocker
*  Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
*  Licensed as open source with restrictions. Please see attached LICENSE.txt.
*/

package com.kaijin.InventoryStocker;

import java.io.File;
import net.minecraft.src.*;
import net.minecraft.src.forge.Configuration;

public class CommonProxy
{
    public static void load()
    {

    }

    public static Configuration getConfiguration()
    {
        return new Configuration(new File("config/IC2_ChargingBench.cfg"));
    }

    public static World PacketHandlerGetWorld(NetworkManager network)
    {
        //server side needs to grab the world entity
        return ((NetServerHandler)network.getNetHandler()).getPlayerEntity().worldObj;
    }

    public static boolean isClient(World world)
    {
        return false;
    }

    public static boolean isServer()
    {
        return true;
    }

    public static void sendPacketToPlayer(String playerName, Packet250CustomPayload packet)
    {
        ModLoader.getMinecraftServerInstance().configManager.sendPacketToPlayer(playerName, packet);
    }

    public static void sendPacketToServer(Packet250CustomPayload packet)
    {
        // ModLoader.sendPacket(packet);
    }
}
