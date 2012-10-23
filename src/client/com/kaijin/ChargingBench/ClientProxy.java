/* Inventory Stocker
 *  Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 *  Licensed as open source with restrictions. Please see attached LICENSE.txt.
 */

package com.kaijin.ChargingBench;

import java.io.File;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.src.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.Configuration;

public class ClientProxy extends CommonProxy
{
	@Override
	public void load()
	{
		MinecraftForgeClient.preloadTexture(ChargingBench.proxy.BLOCK_PNG);
		MinecraftForgeClient.preloadTexture(ChargingBench.proxy.GUI1_PNG);
		MinecraftForgeClient.preloadTexture(ChargingBench.proxy.GUI2_PNG);
	}
}
