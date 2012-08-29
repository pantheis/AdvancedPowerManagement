package com.kaijin.ChargingBench;

import ic2.platform.ItemBlockCommon;
import net.minecraft.server.ItemStack;

public class ItemChargingBench extends ItemBlockCommon
{
    public ItemChargingBench(int var1)
    {
        super(var1);

        if (Utils.isDebug())
        {
            System.out.println("ItemChargingBench");
        }

        this.setMaxDurability(0);
        this.a(true);
    }

    /**
     * Returns the metadata of the block which this Item (ItemBlock) can place
     */
    public int filterData(int var1)
    {
        if (Utils.isDebug())
        {
            System.out.println("ItemChargingBench.getMetadata");
        }

        return var1;
    }

    public String a(ItemStack var1)
    {
        if (Utils.isDebug())
        {
            System.out.println("ItemChargingBench.getItemNameIS");
        }

        int var2 = var1.getData();

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
