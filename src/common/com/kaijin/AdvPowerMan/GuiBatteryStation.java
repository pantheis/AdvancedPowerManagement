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
public class GuiBatteryStation extends GuiContainer
{
	IInventory playerInventory;
	public TEBatteryStation tile;

	public GuiBatteryStation(InventoryPlayer player, TEBatteryStation tileentity)
	{
		super(new ContainerBatteryStation(player, tileentity));
		tile = tileentity;
		xSize = 176; // The X size of the GUI window in pixels.
		ySize = 190; // The Y size of the GUI window in pixels.
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3)
	{
		int textureID = mc.renderEngine.getTexture(Info.GUI2_PNG);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(textureID);

		int xLoc = (width - xSize) / 2;
		int yLoc = (height - ySize) / 2;

		this.drawTexturedModalRect(xLoc, yLoc, 0, 0, xSize, ySize);

		// Draw title text
		Utils.drawCenteredText(fontRenderer, tile.getInvName(), width / 2, yLoc + 8, 4210752);
	}
}
