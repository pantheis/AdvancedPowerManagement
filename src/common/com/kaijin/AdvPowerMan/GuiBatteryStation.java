/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import java.text.DecimalFormat;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.StringTranslate;
import com.kaijin.AdvPowerMan.CButton;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

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

	private DecimalFormat df = new DecimalFormat("##0.00");

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

		//Utils.drawLeftAlignedText(fontRenderer, "Input mode", xLoc + 7, yLoc + 60, 4210752);
		//Utils.drawLeftAlignedText(fontRenderer, "Mode", xLoc + 35, yLoc + 46, 4210752);
		Utils.drawLeftAlignedText(fontRenderer, "Only when ", xLoc + 7, yLoc + 59, 4210752);
		Utils.drawLeftAlignedText(fontRenderer, "required", xLoc + 7, yLoc + 70, 4210752);
		//Utils.drawLeftAlignedText(fontRenderer, "Input", xLoc + 7, yLoc + 80, 4210752);
		Utils.drawCenteredText(fontRenderer, "Avg. EU/t", xLoc + 144, yLoc + 27, 4210752);
		Utils.drawCenteredText(fontRenderer, "Remaining", xLoc + 144, yLoc + 65, 4210752);

		// Factor of 2000 because data is in fixed point (x100) and EU per second (x20)
		Utils.drawRightAlignedGlowingText(fontRenderer, df.format((float)(((ContainerBatteryStation)inventorySlots).average) / 2000F), xLoc + 165, yLoc + 41, GREEN, GREENGLOW);
		Utils.drawRightAlignedGlowingText(fontRenderer, "01:23:45", xLoc + 165, yLoc + 51, GREEN, GREENGLOW);
		
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
