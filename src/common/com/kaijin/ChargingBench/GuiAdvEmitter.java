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
public class GuiAdvEmitter extends GuiContainer
{
	IInventory playerInventory;
	public TEAdvEmitter tile;

	private final String displayStrings[] = {"-100", "-10", "-1", "+1", "+10", "+100"};

	private CButton buttons[] = new CButton[12];

	public GuiAdvEmitter(InventoryPlayer player, TEAdvEmitter tile)
	{
		super(new ContainerAdvEmitter(player, tile));
		if (ChargingBench.isDebugging) System.out.println("GuiAdvEmitter");
		this.tile = tile;
		/** The X size of the inventory window in pixels. */
		xSize = 176;

		/** The Y size of the inventory window in pixels. */
		ySize = 190;

		//Button definition - mouse over CButton for details
		for (int i = 0; i < buttons.length; i++)
		{
			//16777120 old highlight color code, saved here for reference
			buttons[i] = new CButton(i, 0, 0, 22, 12, 0, 192, 0, 206, displayStrings[i % 6], 0xFFFFFF, 0xFFFFFF, ChargingBench.proxy.GUI4_PNG);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y)
	{
		// Draw tier and title
		fontRenderer.drawString("Advanced Emitter", 54, 7, 4210752);

		final String upper = Integer.toString(tile.packetSize) + " eu/T";
		final String lower = Integer.toString(tile.outputRate) + " eu/T";

		fontRenderer.drawString("Packet Size", 41, 38, 0xA03333);
		Utils.drawRightAlignedText(fontRenderer, upper, 109, 53, 0x55FF55);

		fontRenderer.drawString("Total EU", 41, 73, 0xA03333);
		Utils.drawRightAlignedText(fontRenderer, lower, 109, 88, 0x55FF55);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int mouseX, int mouseY)
	{
		final int textureID = mc.renderEngine.getTexture(ChargingBench.proxy.GUI4_PNG);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(textureID);

		int xLoc = (width - xSize) / 2;
		int yLoc = (height - ySize) / 2;

		drawTexturedModalRect(xLoc, yLoc, 0, 0, xSize, ySize);

		//Buttons MUST be drawn after other texture stuff or it will not draw the battery meter correctly
		final int horizOffs[] = {-70, -55, -31, 25, 49, 65};

		for (int i = 0; i < 12; i++)
		{
			buttons[i].xPosition = width / 2 + horizOffs[i % 4];
			buttons[i].yPosition = yLoc + 50 + 35 * (i / 4);
		}

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
