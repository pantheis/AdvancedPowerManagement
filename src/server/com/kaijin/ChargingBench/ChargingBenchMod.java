package com.kaijin.ChargingBench;

import ic2chargingbench.common.TileEntityChargingBench;
import net.minecraft.src.BaseModMp;
import net.minecraft.src.EntityHuman;
import net.minecraft.src.IInventory;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mod_IC2_ChargingBench;

public abstract class ChargingBenchMod extends BaseModMp
{
    public static boolean launchGUI(EntityHuman var0, TileEntity var1)
    {
        ModLoader.openGUI(var0, mod_IC2_ChargingBench.guiIdChargingBench, (IInventory)var1, ((TileEntityChargingBench)var1).getGuiContainer(var0.inventory));
        return true;
    }
}
