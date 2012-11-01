/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/

package com.kaijin.ChargingBench;

import net.minecraftforge.client.MinecraftForgeClient;

public class ClientProxy extends CommonProxy
{
	@Override
	public void load()
	{
		MinecraftForgeClient.preloadTexture(ITEM_PNG);
		MinecraftForgeClient.preloadTexture(BLOCK_PNG);
		MinecraftForgeClient.preloadTexture(GUI1_PNG);
		MinecraftForgeClient.preloadTexture(GUI2_PNG);
		MinecraftForgeClient.preloadTexture(GUI3_PNG);
		MinecraftForgeClient.preloadTexture(GUI4_PNG);
	}
}
