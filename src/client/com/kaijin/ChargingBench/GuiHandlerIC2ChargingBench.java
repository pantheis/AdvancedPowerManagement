package com.kaijin.ChargingBench;

import net.minecraft.src.*;
import net.minecraft.src.forge.*;

public class GuiHandlerIC2ChargingBench implements IGuiHandler
{
	@Override
	public Object getGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z)
	{
		if (!world.blockExists(x, y, z))
		{
			return null;
		}
				
		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (!(tile instanceof TileEntityChargingBench))
		{
			return null;
		}

		return new GuiChargingBench(player, (TileEntityChargingBench)tile);
	}
}
