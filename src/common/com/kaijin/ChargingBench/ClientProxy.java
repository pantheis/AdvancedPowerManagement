/* Inventory Stocker
 *  Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 *  Licensed as open source with restrictions. Please see attached LICENSE.txt.
 */

package com.kaijin.ChargingBench;

import net.minecraftforge.client.MinecraftForgeClient;

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
