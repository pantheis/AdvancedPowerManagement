/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.ChargingBench;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryPlayer;

import org.lwjgl.opengl.GL11;

public class GuiBatteryStation extends GuiContainer
{
	IInventory playerInventory;
	public TEBatteryStation tile;
	public EntityPlayer player;
	private GuiButton selectedButton = null;

	private GuiButton button = null;

	public GuiBatteryStation(InventoryPlayer player, TEBatteryStation tile)
	{
		super(new ContainerBatteryStation(player, tile));
		//if (ChargingBench.isDebugging) System.out.println("GuiDischargingBench");
		this.tile = tile;
		/** The X size of the inventory window in pixels. */
		xSize = 176;

		/** The Y size of the inventory window in pixels. */
		ySize = 190;

	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y)
	{
		String type = "";
		switch(tile.baseTier)
		{
		case 1:
			type = "LV";
			break;
		case 2:
			type = "MV";
			break;
		case 3:
			type = "HV";
			break;
		default:
		}
		// Draw tier and title
		fontRenderer.drawString(type + " Battery Station", 43, 7, 4210752);

//Not needed, left for reference
/*
		// Compute strings for current and max storage
		String s1 = (Integer.toString(tile.currentEnergy));
		String s2 = (Integer.toString(tile.adjustedStorage));
		// Draw Right-aligned current energy number
		fontRenderer.drawString(s1, (80 - fontRenderer.getStringWidth(s1)), 20, 4210752);
		// Draw left-aligned max energy number
		fontRenderer.drawString(s2, 93, 20, 4210752);
		// Draw separator		
		fontRenderer.drawString(" / ", 80, 20, 4210752);
*/
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3)
	{
		int textureID = mc.renderEngine.getTexture(ChargingBench.proxy.GUI2_PNG);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(textureID);
		int xLoc = (width - xSize) / 2;
		int yLoc = (height - ySize) / 2;
		this.drawTexturedModalRect(xLoc, yLoc, 0, 0, xSize, ySize);

//Not needed, left for reference
/*
		if (tile.currentEnergy > 0)
		{
			// Make each box light up all at once like a LED instead of gradually using barLength = tile.gaugeEnergyScaled(66); 
			int barLength = 5 * tile.gaugeEnergyScaled(13);
			if (barLength > 0) barLength++;
			this.drawTexturedModalRect(xLoc + 32, yLoc + 100 - barLength, 176, 66 - barLength, 66, barLength);
		}
*/
	}
}
