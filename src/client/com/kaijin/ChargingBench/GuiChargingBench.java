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
		this.fontRenderer.drawString("Charging Bench", 57, 6, 4210752);
		this.fontRenderer.drawString("Inventory", 8, this.ySize - 96 + 2, 4210752);
		this.fontRenderer.drawString(Integer.toString(tile.currentEnergy), 5, this.ySize - 163 +2, 4210752);
		this.fontRenderer.drawString("/" + Integer.toString(tile.baseStorage), 5, this.ySize - 153 +2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3)
	{
		int var4 = this.mc.renderEngine.getTexture(ChargingBench.proxy.GUI_PNG);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(var4);
		int var5 = (this.width - this.xSize) / 2;
		int var6 = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(var5, var6, 0, 0, this.xSize, this.ySize);

		if (this.tile.currentEnergy > 0)
		{
			int var7 = this.tile.gaugeEnergyScaled(14);
			this.drawTexturedModalRect(var5 + 24, var6 + 23 + 14 - var7, 176, 14 - var7, 14, var7);
		}
	}
}
