/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import net.minecraft.src.GuiContainer;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiStorageMonitor extends GuiContainer
{
	IInventory playerInventory;
	public TEStorageMonitor tile;
	private CButton buttons[] = new CButton[8];

	private static final String DISPLAYSTRINGS[] = {"-10", "-1", "+1", "+10"};
	private static final int HORIZONTALOFFSETS[] = {-57, -33, 25, 49};
	private static final int RED = 0xFF5555;
	private static final int GREEN = 0x55FF55;
	private static final int REDGLOW = Utils.multiplyColorComponents(RED, 0.16F);
	private static final int GREENGLOW = Utils.multiplyColorComponents(GREEN, 0.16F);

	public GuiStorageMonitor(InventoryPlayer player, TEStorageMonitor tileentity)
	{
		super(new ContainerStorageMonitor(player, tileentity));
		tile = tileentity;
		xSize = 176; // The X size of the GUI window in pixels.
		ySize = 190; // The Y size of the GUI window in pixels.

		//Button definition - mouse over CButton for details
		for (int i = 0; i < buttons.length; i++)
		{
			//16777120 old highlight color code, saved here for reference
			buttons[i] = new CButton(i, 0, 0, 24, 13, 1, 192, 1, 207, DISPLAYSTRINGS[i % 4], 4210752, 0xFFFFAF, Info.GUI3_PNG);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int mouseX, int mouseY)
	{
		final int textureID = mc.renderEngine.getTexture(Info.GUI3_PNG);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(textureID);

		int xLoc = (width - xSize) / 2;
		int yLoc = (height - ySize) / 2;

		// Draw GUI background
		drawTexturedModalRect(xLoc, yLoc, 0, 0, xSize, ySize);

		// Draw energy meter
		if (tile.energyStored > 0)
		{
			final int offset = tile.isPowering ? 12 : 0;
			// Make each box light up all at once like a LED instead of gradually using barLength = this.tile.gaugeEnergyScaled(66); 
			int barLength = 5 * tile.gaugeEnergyScaled(13);
			if (barLength > 0) barLength++;

			drawTexturedModalRect(xLoc + 10, yLoc + 100 - barLength, 176 + offset, 66 - barLength, 12, barLength);
		}

		// Draw title text
		Utils.drawCenteredText(fontRenderer, Info.MONITOR_NAME, xLoc + 96, yLoc + 12, 4210752);

		if (tile.energyStored == -1 || tile.energyCapacity == -1)
		{
			Utils.drawCenteredGlowingText(fontRenderer, "No Valid Link", xLoc + 96, yLoc + 35, RED, REDGLOW);
		}
		else
		{
			// Draw right-aligned current energy number
			Utils.drawRightAlignedGlowingText(fontRenderer, Integer.toString(tile.energyStored), xLoc + 90, yLoc + 35, GREEN, GREENGLOW);

			// Draw separator and left-aligned max energy number
			Utils.drawGlowingText(fontRenderer, " / " + Integer.toString(tile.energyCapacity), xLoc + 90, yLoc + 35, GREEN, GREENGLOW);

			// Test strings
			//Utils.drawCenteredGlowingText(fontRenderer, " / ", xLoc + 96, yLoc + 35, 0x55FF55, glowFactor);
			//Utils.drawRightAlignedGlowingText(fontRenderer, "123456789", xLoc + 90, yLoc + 35, 0x55FF55, 0.15F);
			//Utils.drawGlowingText(fontRenderer, " / 123456789", xLoc + 90, yLoc + 35, 0x55FF55, 0.15F);
		}
		
		Utils.drawCenteredText(fontRenderer, "Upper Threshold (Off)", xLoc + 96, yLoc + 49, 0xB00000);
		Utils.drawRightAlignedGlowingText(fontRenderer, Integer.toString(tile.upperBoundary) + "%", xLoc + 109, yLoc + 63, GREEN, GREENGLOW);
		
		Utils.drawCenteredText(fontRenderer, "Lower Threshold (On)", xLoc + 96, yLoc + 78, 0xB00000);
		Utils.drawRightAlignedGlowingText(fontRenderer, Integer.toString(tile.lowerBoundary) + "%", xLoc + 109, yLoc + 92, GREEN, GREENGLOW);

		//Buttons MUST be drawn after other texture stuff or it will not draw the battery meter correctly
		for (int i = 0; i < 8; i++)
		{
			buttons[i].xPosition = width / 2 + HORIZONTALOFFSETS[i % 4];
			buttons[i].yPosition = yLoc + 60 + 29 * (i / 4);
			buttons[i].drawButton(mc, mouseX, mouseY);
		}
	}
	
	@Override
	protected void mouseClicked(int par1, int par2, int par3)
	{
		if (par3 == 0) // On a left click,
		{
			for (CButton b : buttons) // For each item in buttons,
			{
				if (b.enabled && b.mousePressed(this.mc, par1, par2)) // if it's enabled and was under the pointer,
				{
					mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F); // provide audio feedback,
					tile.sendGuiCommand(b.id); // and inform the server of the button click.
				}
			}
		}
		super.mouseClicked(par1, par2, par3); // Finally, do all that other normal stuff. 
	}

}
