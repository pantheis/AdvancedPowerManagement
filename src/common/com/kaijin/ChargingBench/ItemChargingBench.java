package com.kaijin.ChargingBench;


import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;

public class ItemChargingBench extends ItemBlock
{
    public ItemChargingBench(int var1)
    {
    	super(var1);
    	if (Utils.isDebug()) System.out.println("ItemChargingBench");
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    public int getMetadata(int meta)
    {
    	if (Utils.isDebug()) System.out.println("ItemChargingBench.getMetadata");
        return meta;
    }

    public String getItemNameIS(ItemStack var1)
    {
    	//TODO Use an array to store the name strings (faster lookup)
    	// if (Utils.isDebug()) System.out.println("ItemChargingBench.getItemNameIS");
        int var2 = var1.getItemDamage();

        switch (var2)
        {
            case 0:
                return "blockChargingBench1";
            case 1:
                return "blockChargingBench2";
            case 2:
                return "blockChargingBench3";
            case 3:
                return "blockEmitterBlock1";
            case 4:
                return "blockEmitterBlock2";
            case 5:
                return "blockEmitterBlock3";
            case 6:
                return "blockEmitterBlock4";
            case 7:
                return "blockBatteryStation1";
            case 8:
            	return "blockBatteryStation2";
            case 9:
            	return "blockBatteryStation3";

            default:
                return null;
        }
    }
}
