package com.kaijin.ChargingBench;

import forge.ITextureProvider;
import ic2.common.BlockMultiID;
import ic2.common.TileEntityBlock;
import ic2.platform.Platform;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.ItemStack;
import net.minecraft.server.Material;
import net.minecraft.server.TileEntity;
import net.minecraft.server.World;
import net.minecraft.server.mod_ChargingBench;

public class BlockChargingBench extends BlockMultiID implements ITextureProvider
{
    public BlockChargingBench(int var1)
    {
        super(var1, Material.WOOD);
        this.c(1.0F);

        if (Utils.isDebug())
        {
            System.out.println("BlockChargingBench");
        }
    }

    public void addCreativeItems(ArrayList var1)
    {
        if (Utils.isDebug())
        {
            System.out.println("BlockChargingBench.addCreativeItems");
        }

        var1.add(new ItemStack(this, 1, 0));
        var1.add(new ItemStack(this, 1, 1));
        var1.add(new ItemStack(this, 1, 2));
    }

    private boolean launchGUI(World var1, int var2, int var3, int var4, EntityHuman var5)
    {
        if (Utils.isDebug())
        {
            System.out.println("BlockChargingBench.launchGUI");
        }

        var5.openGui(mod_ChargingBench.instance, 1, var1, var2, var3, var4);
        return true;
    }

    /**
     * Called upon block activation (left or right click on the block.). The three integers represent x,y,z of the
     * block.
     */
    public boolean interact(World var1, int var2, int var3, int var4, EntityHuman var5)
    {
        if (Utils.isDebug())
        {
            System.out.println("BlockChargingBench.BlockActivated");
        }

        return Platform.isSimulating() ? this.launchGUI(var1, var2, var3, var4, var5) : true;
    }

    public TileEntityChargingBench getBlockEntity(int var1)
    {
        if (Utils.isDebug())
        {
            System.out.println("BlockChargingBench.getBlockEntity.var1: " + var1);
        }

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
        if (Utils.isDebug())
        {
            System.out.println("BlockChargingBench.getTextureFile");
        }

        return "/com/kaijin/ChargingBench/sprites/ChargingBench.png";
    }

    /**
     * Returns the ID of the items to drop on destruction.
     */
    public int getDropType(int var1, Random var2, int var3)
    {
        if (Utils.isDebug())
        {
            System.out.println("BlockChargingBench.idDropped");
        }

        return this.id;
    }

    /**
     * Determines the damage on the item the block drops. Used in cloth and wood.
     */
    protected int getDropData(int var1)
    {
        if (Utils.isDebug())
        {
            System.out.println("BlockChargingBench.damageDropped");
        }

        return var1;
    }

    public TileEntity getTileEntity(int var1)
    {
        if (Utils.isDebug())
        {
            System.out.println("BlockChargingBench.getTileEntity.var1: " + var1);
        }

        return this.getBlockEntity(var1);
    }
}
