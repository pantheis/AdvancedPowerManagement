package com.kaijin.ChargingBench;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

public class GuiBatteryStation extends GuiContainer
{
	IInventory playerInventory;
	public TEBatteryStation tile;
	public EntityPlayer player;
	private GuiButton selectedButton = null;

	private GuiButton button = null;

	public GuiBatteryStation(InventoryPlayer player, TEBatteryStation tile)
	{
		super(new ContainerBatteryStation(player, tile));
		if (Utils.isDebug()) System.out.println("GuiDischargingBench");
		this.tile = tile;
		/** The X size of the inventory window in pixels. */
		xSize = 176;

		/** The Y size of the inventory window in pixels. */
		ySize = 190;

	}

	protected void drawGuiContainerForegroundLayer()
	{
		String type = "";
		switch(tile.baseTier)
		{
		case 1:
			type = "LV";
			break;
		case 2:
			type = "MV";
			break;
		case 3:
			type = "HV";
			break;
		default:
		}
		// Draw tier and title
		this.fontRenderer.drawString(type + " Battery Station", 43, 7, 4210752);

//Not needed, left for reference
/*
		// Compute strings for current and max storage
		String s1 = (Integer.toString(tile.currentEnergy));
		String s2 = (Integer.toString(tile.adjustedStorage));
		// Draw Right-aligned current energy number
		this.fontRenderer.drawString(s1, (80 - this.fontRenderer.getStringWidth(s1)), 20, 4210752);
		// Draw left-aligned max energy number
		this.fontRenderer.drawString(s2, 93, 20, 4210752);
		// Draw separator		
		this.fontRenderer.drawString(" / ", 80, 20, 4210752);
*/
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3)
	{
		int textureID = this.mc.renderEngine.getTexture(ChargingBench.proxy.GUI2_PNG);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(textureID);
		int xLoc = (this.width - this.xSize) / 2;
		int yLoc = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(xLoc, yLoc, 0, 0, this.xSize, this.ySize);

//Not needed, left for reference
/*
		if (this.tile.currentEnergy > 0)
		{
			// Make each box light up all at once like a LED instead of gradually using barLength = this.tile.gaugeEnergyScaled(66); 
			int barLength = 5 * this.tile.gaugeEnergyScaled(13);
			if (barLength > 0) barLength++;
			this.drawTexturedModalRect(xLoc + 32, yLoc + 100 - barLength, 176, 66 - barLength, 66, barLength);
		}
*/
	}
}
