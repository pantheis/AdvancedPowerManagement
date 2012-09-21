package com.kaijin.ChargingBench;


import net.minecraft.src.*;
import ic2.api.*;
import com.kaijin.ChargingBench.*;

public class ItemChargingBench extends ItemBlock
{
    public ItemChargingBench(int var1)
    {
    	super(var1);
    	if (Utils.isDebug()) System.out.println("ItemChargingBench");
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    public int getMetadata(int var1)
    {
    	if (Utils.isDebug()) System.out.println("ItemChargingBench.getMetadata");
        return var1;
    }

    public String getItemNameIS(ItemStack var1)
    {
    	if (Utils.isDebug()) System.out.println("ItemChargingBench.getItemNameIS");
        int var2 = var1.getItemDamage();

        switch (var2)
        {
            case 0:
                return "blockChargingBench1";

            case 1:
                return "blockChargingBench2";

            case 2:
                return "blockChargingBench3";

            default:
                return null;
        }
    }
}
