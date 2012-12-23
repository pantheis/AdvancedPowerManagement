/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import java.text.DecimalFormat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StringTranslate;
import com.kaijin.AdvPowerMan.CButton;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiBatteryStation extends GuiContainer
{
	IInventory playerInventory;
	public TEBatteryStation tile;
	private CButton button;
	private int mode = -1;

	private int xLoc;
	private int yLoc;
	private int xCenter;

	private DecimalFormat fraction = new DecimalFormat("##0.00");
	private DecimalFormat time = new DecimalFormat("00");
	private DecimalFormat days = new DecimalFormat("#0");
	private DecimalFormat dayFrac = new DecimalFormat("0.#");

	private static final int GREEN = 0x55FF55;
	private static final int GREENGLOW = Utils.multiplyColorComponents(GREEN, 0.16F);

	protected static StringTranslate lang = StringTranslate.getInstance();

	public GuiBatteryStation(InventoryPlayer player, TEBatteryStation tileentity)
	{
		super(new ContainerBatteryStation(player, tileentity));
		tile = tileentity;
		xSize = 176; // The X size of the GUI window in pixels.
		ySize = 182; // The Y size of the GUI window in pixels.
		button = new CButton(0, 0, 0, 18, 12, 30, 200, 30, 200, "", 4210752, 16777120, Info.GUI2_PNG);
	}

	@Override
	public void initGui()
	{
		super.initGui(); // Don't forget this or MC will crash

		// Upper left corner of GUI panel
		xLoc = (width - xSize) / 2; // Half the difference between screen width and GUI width
		yLoc = (height - ySize) / 2; // Half the difference between screen height and GUI height
		xCenter = width / 2;
		button.xPosition = xLoc + 16;
		button.yPosition = yLoc + 44;
		mode = -1;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int mouseX, int mouseY)
	{
		final int textureID = mc.renderEngine.getTexture(Info.GUI2_PNG);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(textureID);

		this.drawTexturedModalRect(xLoc, yLoc, 0, 0, xSize, ySize);

		// Draw title text
		Utils.drawCenteredText(fontRenderer, lang.translateKey(tile.getInvName()), xCenter, yLoc + 8, 4210752);

		if (mode != ((ContainerBatteryStation)inventorySlots).opMode)
		{
			mode = ((ContainerBatteryStation)inventorySlots).opMode;
			if (mode == 0)
			{
				button.vLoc = 200;
				button.vHoverLoc = 200;
			}
			else
			{
				button.vLoc = 185;
				button.vHoverLoc = 185;
			}
		}

		Utils.drawLeftAlignedText(fontRenderer, lang.translateKey(Info.KEY_DISCHARGER_MODE_LINE1), xLoc + 7, yLoc + 59, 4210752);
		Utils.drawLeftAlignedText(fontRenderer, lang.translateKey(Info.KEY_DISCHARGER_MODE_LINE2), xLoc + 7, yLoc + 70, 4210752);
		Utils.drawCenteredText(fontRenderer, lang.translateKey(Info.KEY_DISCHARGER_AVERAGE), xLoc + 144, yLoc + 27, 4210752);
		Utils.drawCenteredText(fontRenderer, lang.translateKey(Info.KEY_DISCHARGER_REMAINING), xLoc + 144, yLoc + 65, 4210752);

		// Factor of 100 because data is in fixed point (x100)
		final float rate = (float)(((ContainerBatteryStation)inventorySlots).average) / 100F;
		Utils.drawRightAlignedGlowingText(fontRenderer, fraction.format(rate), xLoc + 166, yLoc + 41, GREEN, GREENGLOW);

		String clock;
		if (rate > 0)
		{
			// Rate * 20 to convert per tick to per second
			int timeScratch = (int)((float)(((ContainerBatteryStation)inventorySlots).itemsEnergyTotal) / (rate * 20));
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
				clock = (dayScratch < 10F ? dayFrac.format(dayScratch) : dayScratch < 100 ? days.format((int)dayScratch) : "??") + lang.translateKey(Info.KEY_DISCHARGER_DISPLAY_DAYS);
			}
		}
		else clock = lang.translateKey(Info.KEY_DISCHARGER_DISPLAY_UNKNOWN);
		Utils.drawRightAlignedGlowingText(fontRenderer, clock, xLoc + 166, yLoc + 51, GREEN, GREENGLOW);
		
		button.drawButton(mc, mouseX, mouseY);
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3)
	{
		if (par3 == 0) // On a left click,
		{
			if (button.enabled && button.mousePressed(this.mc, par1, par2)) // if it's enabled and was under the pointer,
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F); // provide audio feedback,
				tile.sendGuiButton(button.id); // and inform the server of the button click.
			}
		}
		super.mouseClicked(par1, par2, par3); // Finally, do all that other normal stuff. 
	}

}
