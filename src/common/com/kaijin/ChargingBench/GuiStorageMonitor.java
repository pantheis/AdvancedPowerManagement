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
	public EntityPlayer player;
	private CButton selectedButton = null;

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
			buttons[i] = new CButton(i, 0, 0, 20, 10, 0, 0, 0, 0, "", 0xFFFFFF, 16777120, ChargingBench.proxy.GUI3_PNG);
		}
	}

	/**
	 * 
	 * @param fr    - Font Renderer handle
	 * @param text  - Text to display
	 * @param xLoc  - x location
	 * @param yLoc  - y location
	 * @param color - Color
	 */
	protected void drawCenteredText(FontRenderer fr, String text, int xLoc, int yLoc, int color)
	{
		fr.drawString(text, xLoc - fr.getStringWidth(text) / 2, yLoc, color);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y)
	{
		DecimalFormat df = new DecimalFormat("#,##0%");
		// Draw tier and title
		this.fontRenderer.drawString("Storage Monitor", 50, 7, 4210752);

		// Compute strings for current and max storage
		String s1 = "";
		String s2 = "";
		if (tile.energyStored == -1 || tile.energyCapacity == -1)
		{
			s1 = "Invalid";
			s2 = "Remote";
		}
		else
		{
			s1 = (Integer.toString(tile.energyStored));
			s2 = (Integer.toString(tile.energyCapacity));
		}
		
		String s3 = df.format(tile.lowerBoundary);
		String s4 = df.format(tile.upperBoundary);
		
		// Draw Right-aligned current energy number
		this.fontRenderer.drawString(s1, (85 - this.fontRenderer.getStringWidth(s1)), 20, 4210752);
		// Draw left-aligned max energy number
		this.fontRenderer.drawString(s2, 98, 20, 4210752);
		// Draw separator
		this.fontRenderer.drawString(" / ", 85, 20, 4210752);
		
		this.fontRenderer.drawString("Upper Threshold (Off)", 43, 40, 0xA03333);
		drawCenteredText(this.fontRenderer, s3, 97, 53, 4210752);
		
		this.fontRenderer.drawString("Lower Threshold (On)", 43, 75, 0xA03333);
		drawCenteredText(this.fontRenderer, s4, 97, 88, 4210752);
		
		
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int mouseX, int mouseY)
	{
		int textureID = this.mc.renderEngine.getTexture(ChargingBench.proxy.GUI3_PNG);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(textureID);
		int xLoc = (this.width - this.xSize) / 2;
		int yLoc = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(xLoc, yLoc, 0, 0, this.xSize, this.ySize);

		if (this.tile.energyStored > 0)
		{
			int offset = 0;
			if (this.tile.isPowering)
			{
				offset = 12;
			}
			else
			{
				offset = 0;
			}
			// Make each box light up all at once like a LED instead of gradually using barLength = this.tile.gaugeEnergyScaled(66); 
			int barLength = 5 * this.tile.gaugeEnergyScaled(13);
			if (barLength > 0) barLength++;

			this.drawTexturedModalRect(xLoc + 10, yLoc + 100 - barLength, 176 + offset, 66 - barLength, 12, barLength);
		}

		//Buttons MUST be drawn after other texture stuff or it will not draw the battery meter correctly
		buttons[0].xPosition = (this.width / 2) - 50;
		buttons[0].yPosition = yLoc + 52;
		buttons[0].vLoc = 192;
		buttons[0].displayString = "-10";
		buttons[0].drawButton(mc, mouseX, mouseY);

		buttons[1].xPosition = (this.width / 2) - 28;
		buttons[1].yPosition = yLoc + 52;
		buttons[1].vLoc = 192;
		buttons[1].displayString = "-1";
		buttons[1].drawButton(mc, mouseX, mouseY);

		buttons[2].xPosition = (this.width / 2) + 25;
		buttons[2].yPosition = yLoc + 52;
		buttons[2].vLoc = 192;
		buttons[2].displayString = "+1";
		buttons[2].drawButton(mc, mouseX, mouseY);

		buttons[3].xPosition = (this.width / 2) + 47;
		buttons[3].yPosition = yLoc + 52;
		buttons[3].vLoc = 192;
		buttons[3].displayString = "+10";
		buttons[3].drawButton(mc, mouseX, mouseY);

		buttons[4].xPosition = (this.width / 2) - 50;
		buttons[4].yPosition = yLoc + 87;
		buttons[4].vLoc = 192;
		buttons[4].displayString = "-10";
		buttons[4].drawButton(mc, mouseX, mouseY);

		buttons[5].xPosition = (this.width / 2) - 28;
		buttons[5].yPosition = yLoc + 87;
		buttons[5].vLoc = 192;
		buttons[5].displayString = "-1";
		buttons[5].drawButton(mc, mouseX, mouseY);

		buttons[6].xPosition = (this.width / 2) + 25;
		buttons[6].yPosition = yLoc + 87;
		buttons[6].vLoc = 192;
		buttons[6].displayString = "+1";
		buttons[6].drawButton(mc, mouseX, mouseY);

		buttons[7].xPosition = (this.width / 2) + 47;
		buttons[7].yPosition = yLoc + 87;
		buttons[7].vLoc = 192;
		buttons[7].displayString = "+10";
		buttons[7].drawButton(mc, mouseX, mouseY);
		//TODO Simplify these definitions - see if a for loop can be used for some of this.

	}
	
	//Copied mouseClicked function to get our button to make the "click" noise when clicked
	@Override
	protected void mouseClicked(int par1, int par2, int par3)
	{
		if (par3 == 0) // On a left click,
		{
			for (CButton b : buttons) // For each item in buttons,
			{
				if (b.mousePressed(this.mc, par1, par2)) // if it was pressed,
				{
					selectedButton = b; // select it,
					mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F); // provide audio feedback,
					actionPerformed(b); // and call its action.
				}
			}
		}
		super.mouseClicked(par1, par2, par3); // Finally, do all that other normal stuff. 
	}

	/*
	 * This function actually handles what happens when you click on a button, by ID
	 */
	@Override
	public void actionPerformed(GuiButton button)
	{
		if (!button.enabled)
		{
			return;
		}

		switch (button.id) //TODO Fill out action behavior for each button
		{
		case 0: // UPPER -10
			break;
		case 1: // UPPER -1
			break;
		case 2: // UPPER +1
			break;
		case 3: // UPPER +10
			break;
		case 4: // LOWER -10
			break;
		case 5: // LOWER -1
			break;
		case 6: // LOWER +1
			break;
		case 7: // LOWER +10
			break;
		}
	}
}
