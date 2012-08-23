package com.kaijin.ChargingBench;

import net.minecraft.src.*;

import org.lwjgl.opengl.GL11;
import net.minecraft.src.forge.*;
import com.kaijin.ChargingBench.*;

public class GuiChargingBench extends GuiContainer
{
	IInventory playerInventory;
	TileEntityChargingBench tile;
	private GuiButton selectedButton = null;
	
	private GuiButton button = null;
	
	public GuiChargingBench(IInventory playerInventory, TileEntityChargingBench tileentitychargingbench, EntityPlayer player)
	{
		super(new ContainerChargingBench(playerInventory, tileentitychargingbench));
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2,
			int var3) {
		// TODO Auto-generated method stub
		
	}

}
