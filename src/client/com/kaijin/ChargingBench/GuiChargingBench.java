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
        this.fontRenderer.drawString("Charging Bench", 54, 6, 4210752);
        this.fontRenderer.drawString("Inventory", 8, this.ySize - 96 + 2, 4210752);
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3)
    {
        int var4 = this.mc.renderEngine.getTexture("/com/kaijin/ChargingBench/sprites/GUIChargingBench.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(var4);
        int var5 = (this.width - this.xSize) / 2;
        int var6 = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(var5, var6, 0, 0, this.xSize, this.ySize);

        if (this.tile.energy > 0)
        {
            int var7 = this.tile.gaugeEnergyScaled(14);
            this.drawTexturedModalRect(var5 + 24, var6 + 23 + 14 - var7, 176, 14 - var7, 14, var7);
        }
    }
}
