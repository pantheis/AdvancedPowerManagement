/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.ChargingBench;

import java.text.DecimalFormat;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;
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
	//public EntityPlayer player;
	//private CButton selectedButton = null;

	private final String displayStrings[] = {"-10", "-1", "+1", "+10"};

	private CButton buttons[] = new CButton[8];

	public GuiStorageMonitor(InventoryPlayer player, TEStorageMonitor tile)
	{
		super(new ContainerStorageMonitor(player, tile));
		if (ChargingBench.isDebugging) System.out.println("GuiStorageMonitor");
		this.tile = tile;
		/** The X size of the inventory window in pixels. */
		xSize = 176;

		/** The Y size of the inventory window in pixels. */
		ySize = 190;
		
		//Button definition - mouse over CButton for details
		for (int i = 0; i < buttons.length; i++)
		{
			//16777120 old highlight color code, saved here for reference
			buttons[i] = new CButton(i, 0, 0, 22, 12, 0, 192, 0, 206, displayStrings[i % 4], 0xFFFFFF, 0xFFFFFF, ChargingBench.proxy.GUI3_PNG);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y)
	{
		// Draw tier and title
		fontRenderer.drawString("Storage Monitor", 54, 7, 4210752);

		if (tile.energyStored == -1 || tile.energyCapacity == -1)
		{
			Utils.drawCenteredText(fontRenderer, "No Valid Link", 90, 20, 0xFF5555);
		}
		else
		{
			// Draw Right-aligned current energy number
			Utils.drawRightAlignedText(fontRenderer, Integer.toString(tile.energyStored), 90, 21, 0x55FF55);

			// Draw separator and left-aligned max energy number
			fontRenderer.drawString(" / " + Integer.toString(tile.energyCapacity), 90, 21, 0x55FF55);

			// Draw Right-aligned current energy number
			// Test string with 9 digits
//			Utils.drawRightAlignedText(fontRenderer, "123456789", 85, 21, 4210752);

			// Draw separator and left-aligned max energy number
			// Test string with 9 digits
//			fontRenderer.drawString(" / " + "123456789", 85, 21, 4210752);

			
		}
		final String upper = Integer.toString(tile.upperBoundary) + "%";
		final String lower = Integer.toString(tile.lowerBoundary) + "%";
		
		fontRenderer.drawString("Upper Threshold (Off)", 41, 38, 0xA03333);
		Utils.drawRightAlignedText(fontRenderer, upper, 109, 53, 0x55FF55);
		
		fontRenderer.drawString("Lower Threshold (On)", 41, 73, 0xA03333);
		Utils.drawRightAlignedText(fontRenderer, lower, 109, 88, 0x55FF55);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int mouseX, int mouseY)
	{
		final int textureID = mc.renderEngine.getTexture(ChargingBench.proxy.GUI3_PNG);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(textureID);

		int xLoc = (width - xSize) / 2;
		int yLoc = (height - ySize) / 2;

		drawTexturedModalRect(xLoc, yLoc, 0, 0, xSize, ySize);

		if (tile.energyStored > 0)
		{
			final int offset = tile.isPowering ? 12 : 0;
			// Make each box light up all at once like a LED instead of gradually using barLength = this.tile.gaugeEnergyScaled(66); 
			int barLength = 5 * tile.gaugeEnergyScaled(13);
			if (barLength > 0) barLength++;

			drawTexturedModalRect(xLoc + 10, yLoc + 100 - barLength, 176 + offset, 66 - barLength, 12, barLength);
		}

		//Buttons MUST be drawn after other texture stuff or it will not draw the battery meter correctly
		final int horizOffs[] = {-55, -31, 25, 49};

		for (int i = 0; i < 8; i++)
		{
			buttons[i].xPosition = width / 2 + horizOffs[i % 4];
			buttons[i].yPosition = yLoc + 50 + 35 * (i / 4);
		}

		/*
		buttons[0].xPosition = (this.width / 2) - 50;
		buttons[0].yPosition = yLoc + 52;

		buttons[1].xPosition = (this.width / 2) - 28;
		buttons[1].yPosition = yLoc + 52;

		buttons[2].xPosition = (this.width / 2) + 25;
		buttons[2].yPosition = yLoc + 52;

		buttons[3].xPosition = (this.width / 2) + 47;
		buttons[3].yPosition = yLoc + 52;

		buttons[4].xPosition = (this.width / 2) - 50;
		buttons[4].yPosition = yLoc + 87;

		buttons[5].xPosition = (this.width / 2) - 28;
		buttons[5].yPosition = yLoc + 87;

		buttons[6].xPosition = (this.width / 2) + 25;
		buttons[6].yPosition = yLoc + 87;

		buttons[7].xPosition = (this.width / 2) + 47;
		buttons[7].yPosition = yLoc + 87;
		*/

		// Draw ALL of the buttons?! :o
		for (CButton b : buttons)
		{
			b.drawButton(mc, mouseX, mouseY);
		}

	}
	
	//Copied mouseClicked function to get our button to make the "click" noise when clicked
	@Override
	protected void mouseClicked(int par1, int par2, int par3)
	{
		if (par3 == 0) // On a left click,
		{
			for (CButton b : buttons) // For each item in buttons,
			{
				if (b.mousePressed(this.mc, par1, par2) && b.enabled) // if it was pressed and is enabled,
				{
					//selectedButton = b; // select it,
					mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F); // provide audio feedback,
					tile.sendGuiCommand(b.id); // and inform the server of the button click.
				}
			}
		}
		super.mouseClicked(par1, par2, par3); // Finally, do all that other normal stuff. 
	}

}
