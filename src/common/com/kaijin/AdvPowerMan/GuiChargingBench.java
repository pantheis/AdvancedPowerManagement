/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiChargingBench extends GuiContainer
{
	IInventory playerInventory;
	public TEChargingBench tile;

	private static final int GREEN = 0x55FF55;
	private static final int GREENGLOW = Utils.multiplyColorComponents(GREEN, 0.16F);

	public GuiChargingBench(InventoryPlayer player, TEChargingBench tileentity)
	{
		super(new ContainerChargingBench(player, tileentity));
		tile = tileentity;
		xSize = 176; // The X size of the GUI window in pixels.
		ySize = 198; // The Y size of the GUI window in pixels.
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3)
	{
		int textureID = mc.renderEngine.getTexture(Info.GUI1_PNG);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(textureID);

		int xLoc = (width - xSize) / 2;
		int yLoc = (height - ySize) / 2;
		int xCenter = xLoc + xSize / 2;

		// Draw GUI background
		this.drawTexturedModalRect(xLoc, yLoc, 0, 0, xSize, ySize);

		if (tile.currentEnergy > 0)
		{
			// Make each box light up all at once like a LED instead of gradually using barLength = tile.gaugeEnergyScaled(66); 
			int barLength = 5 * tile.gaugeEnergyScaled(13);
			if (barLength > 0) barLength++;
			this.drawTexturedModalRect(xLoc + 32, yLoc + 108 - barLength, 176, 66 - barLength, 12, barLength);
		}

		// Draw tier and title
		Utils.drawCenteredText(fontRenderer, tile.getInvName(), xCenter, yLoc + 8, 4210752);

		// Draw current and max storage
		Utils.drawRightAlignedGlowingText(fontRenderer, Integer.toString(tile.currentEnergy), xCenter - 7, yLoc + 24, GREEN, GREENGLOW);
		Utils.drawGlowingText(fontRenderer, " / " + Integer.toString(tile.adjustedStorage), xCenter - 7, yLoc + 24, GREEN, GREENGLOW);

		// Test separator		
		//Utils.drawCenteredText(fontRenderer, " / ", xCenter, yLoc + 24, 4210752);
	}
}
