/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StringTranslate;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiStorageMonitor extends GuiContainer
{
	IInventory playerInventory;
	public TEStorageMonitor tile;
	private CButton buttons[] = new CButton[8];

	private int xLoc;
	private int yLoc;

	protected static StringTranslate lang = StringTranslate.getInstance();

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
	public void initGui()
	{
		super.initGui(); // Don't forget this or MC will crash

		// Upper left corner of GUI panel
		xLoc = (width - xSize) / 2; // Half the difference between screen width and GUI width
		yLoc = (height - ySize) / 2; // Half the difference between screen height and GUI height

		// Reposition buttons
		for (int i = 0; i < 8; i++)
		{
			buttons[i].xPosition = width / 2 + HORIZONTALOFFSETS[i % 4];
			buttons[i].yPosition = yLoc + 60 + 29 * (i / 4);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int mouseX, int mouseY)
	{
		final int textureID = mc.renderEngine.getTexture(Info.GUI3_PNG);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(textureID);

		// Draw GUI background
		drawTexturedModalRect(xLoc, yLoc, 0, 0, xSize, ySize);

		// Draw energy meter
		if (tile.energyStored > 0)
		{
			// Which color energy meter should be used?
			final int offset = tile.isPowering ? 12 : 0;

			// Make each box light up all at once like a LED instead of gradually using barLength = this.tile.gaugeEnergyScaled(66); 
			int barLength = 5 * tile.gaugeEnergyScaled(13);
			if (barLength > 0) barLength++;

			drawTexturedModalRect(xLoc + 10, yLoc + 100 - barLength, 176 + offset, 66 - barLength, 12, barLength);
		}

		// Draw title text
		Utils.drawCenteredText(fontRenderer, lang.translateKey(tile.getInvName()), xLoc + 96, yLoc + 12, 4210752);

		if (tile.energyCapacity <= 0)
		{
			// Error message: No card or storage unit not found
			Utils.drawCenteredGlowingText(fontRenderer, lang.translateKey(Info.KEY_MONITOR_INVALID), xLoc + 96, yLoc + 35, RED, REDGLOW);
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

		// Draw control section labels and readouts
		Utils.drawCenteredText(fontRenderer, lang.translateKey(Info.KEY_MONITOR_UPPER), xLoc + 96, yLoc + 49, 0xB00000);
		Utils.drawRightAlignedGlowingText(fontRenderer, Integer.toString(tile.upperBoundary) + "%", xLoc + 109, yLoc + 63, GREEN, GREENGLOW);
		
		Utils.drawCenteredText(fontRenderer, lang.translateKey(Info.KEY_MONITOR_LOWER), xLoc + 96, yLoc + 78, 0xB00000);
		Utils.drawRightAlignedGlowingText(fontRenderer, Integer.toString(tile.lowerBoundary) + "%", xLoc + 109, yLoc + 92, GREEN, GREENGLOW);

		for (CButton button : /* Who's got the */ buttons)
		{
			// Draw ALL of the things?! :o
			button.drawButton(mc, mouseX, mouseY);
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
					tile.sendGuiButton(b.id); // and inform the server of the button click.
				}
			}
		}
		super.mouseClicked(par1, par2, par3); // Finally, do all that other normal stuff. 
	}
}
