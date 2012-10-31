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

	private CButton button1 = null;
	private CButton button2 = null;
	private CButton button3 = null;
	private CButton button4 = null;
	
	private CButton button5 = null;
	private CButton button6 = null;
	private CButton button7 = null;
	private CButton button8 = null;


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
		button1 = new CButton(0, 0, 0, 20, 10, 0, 0, 0, 0, "", 0xFFFFFF, 16777120, ChargingBench.proxy.GUI3_PNG);
		button2 = new CButton(1, 0, 0, 20, 10, 0, 0, 0, 0, "", 0xFFFFFF, 16777120, ChargingBench.proxy.GUI3_PNG);
		button3 = new CButton(2, 0, 0, 20, 10, 0, 0, 0, 0, "", 0xFFFFFF, 16777120, ChargingBench.proxy.GUI3_PNG);
		button4 = new CButton(3, 0, 0, 20, 10, 0, 0, 0, 0, "", 0xFFFFFF, 16777120, ChargingBench.proxy.GUI3_PNG);

		button5 = new CButton(4, 0, 0, 20, 10, 0, 0, 0, 0, "", 0xFFFFFF, 16777120, ChargingBench.proxy.GUI3_PNG);
		button6 = new CButton(5, 0, 0, 20, 10, 0, 0, 0, 0, "", 0xFFFFFF, 16777120, ChargingBench.proxy.GUI3_PNG);
		button7 = new CButton(6, 0, 0, 20, 10, 0, 0, 0, 0, "", 0xFFFFFF, 16777120, ChargingBench.proxy.GUI3_PNG);
		button8 = new CButton(7, 0, 0, 20, 10, 0, 0, 0, 0, "", 0xFFFFFF, 16777120, ChargingBench.proxy.GUI3_PNG);
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
		button1.xPosition = (this.width / 2) - 50;
		button1.yPosition = yLoc + 52;
		button1.vLoc = 192;
		button1.displayString = "-10";
		button1.drawButton(mc, mouseX, mouseY);
		
		button2.xPosition = (this.width / 2) - 28;
		button2.yPosition = yLoc + 52;
		button2.vLoc = 192;
		button2.displayString = "-1";
		button2.drawButton(mc, mouseX, mouseY);

		button3.xPosition = (this.width / 2) + 25;
		button3.yPosition = yLoc + 52;
		button3.vLoc = 192;
		button3.displayString = "+1";
		button3.drawButton(mc, mouseX, mouseY);

		button4.xPosition = (this.width / 2) + 47;
		button4.yPosition = yLoc + 52;
		button4.vLoc = 192;
		button4.displayString = "+10";
		button4.drawButton(mc, mouseX, mouseY);
		
		button5.xPosition = (this.width / 2) - 50;
		button5.yPosition = yLoc + 87;
		button5.vLoc = 192;
		button5.displayString = "-10";
		button5.drawButton(mc, mouseX, mouseY);
		
		button6.xPosition = (this.width / 2) - 28;
		button6.yPosition = yLoc + 87;
		button6.vLoc = 192;
		button6.displayString = "-1";
		button6.drawButton(mc, mouseX, mouseY);

		button7.xPosition = (this.width / 2) + 25;
		button7.yPosition = yLoc + 87;
		button7.vLoc = 192;
		button7.displayString = "+1";
		button7.drawButton(mc, mouseX, mouseY);

		button8.xPosition = (this.width / 2) + 47;
		button8.yPosition = yLoc + 87;
		button8.vLoc = 192;
		button8.displayString = "+10";
		button8.drawButton(mc, mouseX, mouseY);


	}
	
	//Copied mouseClicked function to get our button to make the "click" noise when clicked
	@Override
	protected void mouseClicked(int par1, int par2, int par3)
	{
		if (par3 == 0)
		{
			if (button1.mousePressed(this.mc, par1, par2))
			{
				this.selectedButton = button1;
				this.mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				this.actionPerformed(button1);
			}
		}
		super.mouseClicked(par1, par2, par3);
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
		if (button.id == 0)
		{
		}
	}
}
