/*******************************************************************************
 * Copyright (c) 2012-2013 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import java.text.DecimalFormat;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiAdjustableTransformer extends GuiContainer
{
	IInventory playerInventory;
	public TEAdjustableTransformer tile;
	private CButton buttons[] = new CButton[16];
	private CButton dirButtons[] = new CButton[6];

	private int xLoc;
	private int yLoc;
	private final int yOff = 30;

	private static final String displayStrings[] = {"+1", "+10", "+64", "x2", "-1", "-10", "-64", "/2"};
	private static final int GREEN = 0x55FF55;
	private static final int GREENGLOW = Utils.multiplyColorComponents(GREEN, 0.16F);

	private DecimalFormat fraction = new DecimalFormat("##0.00");

	public GuiAdjustableTransformer(TEAdjustableTransformer tileentity)
	{
		super(new ContainerAdjustableTransformer(tileentity));
		tile = tileentity;
		xSize = 240; // The X size of the GUI window in pixels.
		ySize = 140; // The Y size of the GUI window in pixels.

		//Button definition - mouse over CButton for details
		for (int i = 0; i < buttons.length; i++)
		{
			//16777120 old highlight color code, saved here for reference
			buttons[i] = new CButton(i, 0, 0, 24, 13, 1, 192, 1, 207, displayStrings[i % 8], 4210752, 16777120, Info.GUI_TEX_ADJ_TRANSFORMER);
		}
		for (int i = 0; i < dirButtons.length; i++)
		{
			dirButtons[i] = new CButton(i + 16, 0, 0, 32, 13, 27, 192, 27, 207, I18n.func_135053_a(Info.KEY_DIRECTION_NAMES[i]), 4210752, 16777120, Info.GUI_TEX_ADJ_TRANSFORMER);
		}
	}

	@Override
	public void initGui()
	{
		super.initGui(); // Don't forget this or MC will crash

		// Upper left corner of GUI panel
		xLoc = (width - xSize) / 2; // Half the difference between screen width and GUI width
		yLoc = (height - ySize) / 2; // Half the difference between screen height and GUI height

		for (int i = 0; i < 16; i++)
		{
			buttons[i].xPosition = xLoc +  8 + 24 * (i % 4);
			buttons[i].yPosition = yLoc + yOff + 33 + 13 * (i / 4) + 17 * (i / 8);
		}
		for (int i = 0; i < 6; i++)
		{
			dirButtons[i].xPosition = xLoc + 173;
			dirButtons[i].yPosition = yLoc + yOff + 24 + 13 * i;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int mouseX, int mouseY)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.func_110577_a(new ResourceLocation(Info.TITLE_PACKED.toLowerCase(), Info.GUI_TEX_ADJ_TRANSFORMER));

		// Draw GUI background graphic
		drawTexturedModalRect(xLoc, yLoc, 0, 0, xSize, ySize);

		// Draw title text
		Utils.drawCenteredText(fontRenderer, I18n.func_135053_a(tile.getInvName()), width / 2, yLoc + 6, 4210752);

		// Draw stats text
		Utils.drawRightAlignedText(fontRenderer, I18n.func_135053_a(Info.KEY_STATS_AVERAGE_EU), xLoc + 180, yLoc + 26, 4210752);
		Utils.drawRightAlignedText(fontRenderer, I18n.func_135053_a(Info.KEY_STATS_AVERAGE_INPUT), xLoc + 180, yLoc + 36, 4210752);
		Utils.drawLeftAlignedText(fontRenderer, I18n.func_135053_a(Info.KEY_EU_BUFFERED), xLoc + 49, yLoc + 26, 4210752);

		Utils.drawRightAlignedGlowingText(fontRenderer, Integer.toString(tile.energyBuffer), xLoc + 44, yLoc + 26, GREEN, GREENGLOW);
		// Factor of 100 because data is in fixed point (x100)
		final float outAvg = (float)(((ContainerAdjustableTransformer)inventorySlots).outputAvg) / 100F;
		final float inAvg = (float)(((ContainerAdjustableTransformer)inventorySlots).inputAvg) / 100F;
		Utils.drawRightAlignedGlowingText(fontRenderer, fraction.format(outAvg), xLoc + 230, yLoc + 26, GREEN, GREENGLOW);
		Utils.drawRightAlignedGlowingText(fontRenderer, fraction.format(inAvg), xLoc + 230, yLoc + 36, GREEN, GREENGLOW);

		// Packet size section text
		Utils.drawCenteredText(fontRenderer, I18n.func_135053_a(Info.KEY_EMITTER_PACKET), xLoc + 88, yLoc + yOff + 21, 0xB00000);
		Utils.drawRightAlignedGlowingText(fontRenderer, Integer.toString(tile.packetSize), xLoc + 146, yLoc + yOff + 49, GREEN, GREENGLOW);
		fontRenderer.drawString(Info.AE_PACKET_RANGE, xLoc + 110, yLoc + yOff + 35, 4210752);
		fontRenderer.drawString(I18n.func_135053_a(Info.KEY_EU), xLoc + 152, yLoc + yOff + 49, 4210752);

		// Transfer rate section text
		Utils.drawCenteredText(fontRenderer, I18n.func_135053_a(Info.KEY_TRANSFORMER_OUTPUT), xLoc + 88, yLoc + yOff + 64, 0xB00000);
		Utils.drawRightAlignedGlowingText(fontRenderer, Integer.toString(tile.outputRate), xLoc + 146, yLoc + yOff + 92, GREEN, GREENGLOW);
		fontRenderer.drawString(Info.AE_OUTPUT_RANGE, xLoc + 110, yLoc + yOff + 78, 4210752);
		fontRenderer.drawString(I18n.func_135053_a(Info.KEY_EU), xLoc + 152, yLoc + yOff + 92, 4210752);

		// Side input/output settings text
		for (int i = 0; i < 6; i++)
		{
			Utils.drawGlowingText(fontRenderer, I18n.func_135053_a((tile.sideSettings[i] & 1) == 0 ? Info.KEY_IN : Info.KEY_OUT), xLoc + 214, yLoc + yOff + 27 + 13 * i, GREEN, GREENGLOW);
		}

		//Buttons MUST be drawn after other texture stuff or it will not draw the battery meter correctly
		for (CButton button : buttons)
		{
			button.drawButton(mc, mouseX, mouseY);
		}
		for (CButton button : dirButtons)
		{
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
			for (CButton b : dirButtons)
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
