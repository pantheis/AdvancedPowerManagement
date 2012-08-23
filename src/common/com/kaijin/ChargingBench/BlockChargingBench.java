package com.kaijin.ChargingBench;

import net.minecraft.src.ic2.*;

import java.util.Random;

import net.minecraft.src.forge.*;
import net.minecraft.src.*;
import net.minecraft.src.ic2.api.*;
import net.minecraft.src.ic2.common.*;
import net.minecraft.src.ic2.platform.*;

public class BlockChargingBench extends Block implements ITextureProvider
{
    public BlockChargingBench(int var1)
    {
        super(var1, Material.wood);
        this.setHardness(1.0F);
    }

    public boolean blockActivated(World var1, int var2, int var3, int var4, EntityPlayer var5)
    {
        return Platform.isSimulating() ? ChargingBenchMod.launchGUI(var5, var1.getBlockTileEntity(var2, var3, var4)) : true;
    }

    public TileEntityBlock getBlockEntity(int var1)
    {
        switch (var1)
        {
            case 0:
                return new TileEntityChargingBench1();

            case 1:
                return new TileEntityChargingBench2();

            case 2:
                return new TileEntityChargingBench3();

            default:
                return null;
        }
    }

    public String getTextureFile()
    {
        return "/ic2/sprites/ChargingBench.png";
    }

    public int idDropped(int var1, Random var2, int var3)
    {
        return this.blockID;
    }

    protected int damageDropped(int var1)
    {
        return var1;
    }

    public TileEntity getTileEntity(int var1)
    {
        return this.getBlockEntity(var1);
    }
}
