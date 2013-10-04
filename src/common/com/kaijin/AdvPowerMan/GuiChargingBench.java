/*******************************************************************************
 * Copyright (c) 2012-2013 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import java.text.DecimalFormat;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiChargingBench extends GuiContainer
{
	IInventory playerInventory;
	public TEChargingBench tile;

	private int xLoc;
	private int yLoc;
	private int xCenter;

	private DecimalFormat fraction = new DecimalFormat("##0.00");
	private DecimalFormat time = new DecimalFormat("00");
	private DecimalFormat days = new DecimalFormat("#0");
	private DecimalFormat dayFrac = new DecimalFormat("0.#");

	private static final int GREEN = 0x55FF55;
	private static final int GREENGLOW = Utils.multiplyColorComponents(GREEN, 0.16F);

	public GuiChargingBench(InventoryPlayer player, TEChargingBench tileentity)
	{
		super(new ContainerChargingBench(player, tileentity));
		tile = tileentity;
		xSize = 176; // The X size of the GUI window in pixels.
		ySize = 226; // The Y size of the GUI window in pixels.
	}

	@Override
	public void initGui()
	{
		super.initGui(); // Don't forget this or MC will crash

		// Upper left corner of GUI panel
		xLoc = (width - xSize) / 2; // Half the difference between screen width and GUI width
		yLoc = (height - ySize) / 2; // Half the difference between screen height and GUI height
		xCenter = width / 2;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(new ResourceLocation(Info.TITLE_PACKED.toLowerCase(), Info.GUI_TEX_CHARGING_BENCH));

		// Draw GUI background
		drawTexturedModalRect(xLoc, yLoc, 0, 0, xSize, ySize);

		// Energy bar
		if (tile.currentEnergy > 0)
		{
			// Make each box light up all at once like a LED instead of gradually using barLength = tile.gaugeEnergyScaled(66); 
			int barLength = 5 * tile.gaugeEnergyScaled(13);
			if (barLength > 0) barLength++;
			drawTexturedModalRect(xLoc + 32, yLoc + 136 - barLength, 176, 66 - barLength, 12, barLength);
		}

		// Redstone power indicator
		drawTexturedModalRect(xLoc + 129, yLoc + 48, tile.receivingRedstoneSignal() ? 188 : 206, 0, 18, 15);

		// Draw labels
		Utils.drawCenteredText(fontRenderer, I18n.getString(tile.getInvName()), xCenter, yLoc + 7, 4210752);

		Utils.drawRightAlignedText(fontRenderer, I18n.getString(Info.KEY_EU), xLoc + 25, yLoc + 23, 4210752);
		Utils.drawLeftAlignedText(fontRenderer, I18n.getString(Info.KEY_CHARGER_MAX), xLoc + 151, yLoc + 23, 4210752);

		Utils.drawRightAlignedText(fontRenderer, I18n.getString(Info.KEY_CHARGER_REQ), xLoc + 25, yLoc + 33, 4210752);
		Utils.drawLeftAlignedText(fontRenderer, I18n.getString(Info.KEY_CHARGER_ETC), xLoc + 151, yLoc + 33, 4210752);

		Utils.drawRightAlignedText(fontRenderer, I18n.getString(Info.KEY_CHARGER_AVG), xLoc + 70, yLoc + 52, 4210752);
		Utils.drawLeftAlignedText(fontRenderer, I18n.getString(Info.KEY_CHARGER_PWR), xLoc + 151, yLoc + 52, 4210752);

		// Draw current and max storage
		Utils.drawRightAlignedGlowingText(fontRenderer, Integer.toString(tile.currentEnergy), xCenter - 7, yLoc + 23, GREEN, GREENGLOW);
		Utils.drawGlowingText(fontRenderer, " / " + Integer.toString(tile.adjustedStorage), xCenter - 7, yLoc + 23, GREEN, GREENGLOW);

		// Factor of 100 because data is in fixed point (x100)
		final float rate = (float)(((ContainerChargingBench)inventorySlots).averageInput) / 100F;
		Utils.drawRightAlignedGlowingText(fontRenderer, fraction.format(rate), xLoc + 122, yLoc + 52, GREEN, GREENGLOW);

		// Charging stats (only displayed while charging items)
		if (tile.energyRequired > 0)
		{
			final String clock;
			if (tile.ticksRequired > 0)
			{
				int timeScratch = tile.ticksRequired / 20;
				if (timeScratch <= 345600) // 60 * 60 * 96 or 4 days
				{
					final int sec = timeScratch % 60;
					timeScratch /= 60;
					final int min = timeScratch % 60;
					timeScratch /= 60;
					clock = time.format(timeScratch) + ":" + time.format(min) + ":" + time.format(sec);
				}
				else
				{
					float dayScratch = ((float)timeScratch) / 86400F; // 60 * 60 * 24 or 1 day
					clock = (dayScratch < 10F ? dayFrac.format(dayScratch) : dayScratch < 100 ? days.format((int)dayScratch) : "??") + I18n.getString(Info.KEY_STATS_DISPLAY_DAYS);
				}
			}
			else clock = I18n.getString(Info.KEY_STATS_DISPLAY_UNKNOWN);
			final String energyReq = tile.energyRequired > 9999999 ? dayFrac.format(((float)tile.energyRequired) / 1000000F) + "M" : Integer.toString(tile.energyRequired);
			Utils.drawRightAlignedGlowingText(fontRenderer, energyReq, xCenter - 7, yLoc + 33, GREEN, GREENGLOW);
			Utils.drawRightAlignedGlowingText(fontRenderer, clock, xLoc + 144, yLoc + 33, GREEN, GREENGLOW);
		}
	}
}
