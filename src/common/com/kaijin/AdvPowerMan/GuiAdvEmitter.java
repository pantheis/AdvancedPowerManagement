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
public class GuiAdvEmitter extends GuiContainer
{
	IInventory playerInventory;
	public TEAdvEmitter tile;
	private CButton buttons[] = new CButton[16];

	private static final String displayStrings[] = {"+1", "+10", "+64", "x2", "-1", "-10", "-64", "/2"};
	private static final int GREEN = 0x55FF55;
	private static final int GREENGLOW = Utils.multiplyColorComponents(GREEN, 0.16F);

	public GuiAdvEmitter(InventoryPlayer player, TEAdvEmitter tileentity)
	{
		super(new ContainerAdvEmitter(player, tileentity));
		tile = tileentity;
		xSize = 176; // The X size of the GUI window in pixels.
		ySize = 110; // The Y size of the GUI window in pixels.

		//Button definition - mouse over CButton for details
		for (int i = 0; i < buttons.length; i++)
		{
			//16777120 old highlight color code, saved here for reference
			buttons[i] = new CButton(i, 0, 0, 24, 13, 1, 192, 1, 207, displayStrings[i % 8], 4210752, 16777120, Info.GUI4_PNG);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int mouseX, int mouseY)
	{
		final int textureID = mc.renderEngine.getTexture(Info.GUI4_PNG);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(textureID);

		int xLoc = (width - xSize) / 2;
		int yLoc = (height - ySize) / 2;

		drawTexturedModalRect(xLoc, yLoc, 0, 0, xSize, ySize);

		// Draw title
		Utils.drawCenteredText(fontRenderer, "Advanced Emitter", width / 2, yLoc + 7, 4210752);

		Utils.drawCenteredText(fontRenderer, "Packet size (Voltage)", width / 2, yLoc + 21, 0xB00000);
		Utils.drawRightAlignedGlowingText(fontRenderer, Integer.toString(tile.packetSize), xLoc + 146, yLoc + 49, GREEN, GREENGLOW);
		fontRenderer.drawString("[4 - 8192]", xLoc + 110, yLoc + 35, 4210752);
		fontRenderer.drawString("EU", xLoc + 152, yLoc + 49, 4210752);

		Utils.drawCenteredText(fontRenderer, "Output / Tick (Max 64 Packets)", width / 2, yLoc + 64, 0xB00000);
		Utils.drawRightAlignedGlowingText(fontRenderer, Integer.toString(tile.outputRate), xLoc + 146, yLoc + 92, GREEN, GREENGLOW);
		fontRenderer.drawString("[1 - 65536]", xLoc + 110, yLoc + 78, 4210752);
		fontRenderer.drawString("EU", xLoc + 152, yLoc + 92, 4210752);

		//Buttons MUST be drawn after other texture stuff or it will not draw the battery meter correctly
		for (int i = 0; i < 16; i++)
		{
			buttons[i].xPosition = xLoc +  8 + 24 * (i % 4);
			buttons[i].yPosition = yLoc + 33 + 13 * (i / 4) + 17 * (i / 8);
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
