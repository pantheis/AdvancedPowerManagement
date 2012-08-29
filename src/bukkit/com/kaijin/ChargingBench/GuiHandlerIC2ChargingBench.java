package com.kaijin.ChargingBench;

import forge.IGuiHandler;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.TileEntity;
import net.minecraft.server.World;

public class GuiHandlerIC2ChargingBench implements IGuiHandler
{
    public Object getGuiElement(int var1, EntityHuman var2, World var3, int var4, int var5, int var6)
    {
        if (Utils.isDebug())
        {
            System.out.println("GuiHandlerIC2ChargingBench.getGuiElement");
        }

        if (!var3.isLoaded(var4, var5, var6))
        {
            return null;
        }
        else
        {
            TileEntity var7 = var3.getTileEntity(var4, var5, var6);
            return !(var7 instanceof TileEntityChargingBench) ? null : new ContainerChargingBench(var2, (TileEntityChargingBench)var7);
        }
    }
}
