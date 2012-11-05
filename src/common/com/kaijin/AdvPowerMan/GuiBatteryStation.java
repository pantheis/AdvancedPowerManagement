/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

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

	private int xLoc;
	private int yLoc;
	private int xCenter;

	protected static StringTranslate lang = StringTranslate.getInstance();

	public GuiBatteryStation(InventoryPlayer player, TEBatteryStation tileentity)
	{
		super(new ContainerBatteryStation(player, tileentity));
		tile = tileentity;
		xSize = 176; // The X size of the GUI window in pixels.
		ySize = 182; // The Y size of the GUI window in pixels.
		button = new CButton(0, 0, 0, 26, 15, 30, 200, 30, 200, "", 4210752, 16777120, Info.GUI2_PNG);
	}

	@Override
	public void initGui()
	{
		super.initGui(); // Don't forget this or MC will crash

		// Upper left corner of GUI panel
		xLoc = (width - xSize) / 2; // Half the difference between screen width and GUI width
		yLoc = (height - ySize) / 2; // Half the difference between screen height and GUI height
		xCenter = width / 2;
		button.xPosition = xLoc + 12;
		button.yPosition = yLoc + 45;
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
		if(tile.opMode == 1)
		{
			button.vLoc = 185;
			button.vHoverLoc = 185;
		}
		else
		{
			button.vLoc = 200;
			button.vHoverLoc = 200;
		}
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
				tile.sendGuiCommand(button.id); // and inform the server of the button click.
			}
		}
		super.mouseClicked(par1, par2, par3); // Finally, do all that other normal stuff. 
	}

}
