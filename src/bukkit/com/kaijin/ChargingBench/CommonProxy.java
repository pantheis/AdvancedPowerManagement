package com.kaijin.ChargingBench;

import forge.Configuration;
import java.io.File;
import net.minecraft.server.ModLoader;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet250CustomPayload;
import net.minecraft.server.World;

public class CommonProxy
{
    public static void load() {}

    public static Configuration getConfiguration()
    {
        return new Configuration(new File("config/ChargingBench.cfg"));
    }

    public static World PacketHandlerGetWorld(NetworkManager var0)
    {
        return ((NetServerHandler)var0.getNetHandler()).getPlayerEntity().world;
    }

    public static boolean isClient(World var0)
    {
        return false;
    }

    public static boolean isServer()
    {
        return true;
    }

    public static void sendPacketToPlayer(String var0, Packet250CustomPayload var1)
    {
        ModLoader.getMinecraftServerInstance().serverConfigurationManager.a(var0, var1);
    }

    public static void sendPacketToServer(Packet250CustomPayload var0) {}
}
