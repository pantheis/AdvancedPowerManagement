package com.kaijin.ChargingBench;

import net.minecraft.src.*;
import net.minecraft.src.ic2.platform.ItemBlockCommon;

public class ItemChargingBench extends ItemBlockCommon
{
    public ItemChargingBench(int var1)
    {
        super(var1);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    public int getMetadata(int var1)
    {
        return var1;
    }

    public String getItemNameIS(ItemStack var1)
    {
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
