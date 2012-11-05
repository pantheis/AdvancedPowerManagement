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

	protected static StringTranslate lang = StringTranslate.getInstance();

	public GuiBatteryStation(InventoryPlayer player, TEBatteryStation tileentity)
	{
		super(new ContainerBatteryStation(player, tileentity));
		tile = tileentity;
		xSize = 176; // The X size of the GUI window in pixels.
		ySize = 182; // The Y size of the GUI window in pixels.
		button = new CButton(0, 50, 50, 20, 10, 51, 185, 51, 185, "", 4210752, 16777120, Info.GUI2_PNG);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int mouseX, int mouseY)
	{
		final int textureID = mc.renderEngine.getTexture(Info.GUI2_PNG);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(textureID);

		int xLoc = (width - xSize) / 2;
		int yLoc = (height - ySize) / 2;

		this.drawTexturedModalRect(xLoc, yLoc, 0, 0, xSize, ySize);

		// Draw title text
		Utils.drawCenteredText(fontRenderer, lang.translateKey(tile.getInvName()), width / 2, yLoc + 8, 4210752);
		if(tile.opMode == 1)
		{
			button.uLoc = 30;
			button.uHoverLoc = 30;
		}
		else
		{
			button.uLoc = 51;
			button.uHoverLoc = 51;
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
