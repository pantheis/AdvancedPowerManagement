package com.kaijin.ChargingBench;

import net.minecraft.src.*;

import org.lwjgl.opengl.GL11;

public class GuiChargingBench extends GuiContainer
{
	IInventory playerInventory;
	public TEChargingBench tile;
	public EntityPlayer player;
	private GuiButton selectedButton = null;

	private GuiButton button = null;
	
	public GuiChargingBench(InventoryPlayer player, TEChargingBench tile)
	{
		super(new ContainerChargingBench(player, tile));
		if (Utils.isDebug()) System.out.println("GuiChargingBench");
		this.tile = tile;
	    /** The X size of the inventory window in pixels. */
	    xSize = 176;

	    /** The Y size of the inventory window in pixels. */
	    ySize = 181;

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
		this.fontRenderer.drawString(type + " Charging Bench", 43, 7, 4210752);
//		this.fontRenderer.drawString("Inventory", 8, this.ySize - 96 + 2, 4210752);
//		this.fontRenderer.drawString(Integer.toString(tile.currentEnergy), 5, this.ySize - 163 +2, 4210752);
//		this.fontRenderer.drawString("/" + Integer.toString(tile.baseStorage), 5, this.ySize - 153 +2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3)
	{
		int textureID = this.mc.renderEngine.getTexture(ChargingBench.proxy.GUI_PNG);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(textureID);
		int xLoc = (this.width - this.xSize) / 2;
		int yLoc = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(xLoc, yLoc, 0, 0, this.xSize, this.ySize);

		if (this.tile.currentEnergy > 0)
		{
			// Should make each box light up all at once instead of gradually using barLength = this.tile.gaugeEnergyScaled(66); 
			int barLength = 5 * this.tile.gaugeEnergyScaled(13);
			if (barLength > 0) barLength++;
			this.drawTexturedModalRect(xLoc + 32, yLoc + 91 - barLength, 176, 66 - barLength, 66, barLength);
		}
	}
}
