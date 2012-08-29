package com.kaijin.ChargingBench;

import ic2.common.ContainerIC2;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.ICrafting;
import net.minecraft.server.PlayerInventory;
import net.minecraft.server.Slot;

public class ContainerChargingBench extends ContainerIC2
{
    public TileEntityChargingBench tileentity;
    public int energy;
    public short maxInput;

    public ContainerChargingBench(EntityHuman var1, TileEntityChargingBench var2)
    {
    	super(var1, var2);
        this.tileentity = var2;
        this.energy = 0;
        this.maxInput = 0;
        PlayerInventory playerinventory = var1.inventory;
        this.a(new Slot(var2, 0, 61, 19));
        this.a(new Slot(var2, 1, 79, 19));
        this.a(new Slot(var2, 2, 97, 19));
        this.a(new Slot(var2, 3, 115, 19));
        this.a(new Slot(var2, 4, 61, 37));
        this.a(new Slot(var2, 5, 79, 37));
        this.a(new Slot(var2, 6, 97, 37));
        this.a(new Slot(var2, 7, 115, 37));
        this.a(new Slot(var2, 8, 61, 55));
        this.a(new Slot(var2, 9, 79, 55));
        this.a(new Slot(var2, 10, 97, 55));
        this.a(new Slot(var2, 11, 115, 55));
        this.a(new Slot(var2, 12, 24, 40));
        this.a(new Slot(var2, 13, 152, 8));
        this.a(new Slot(var2, 14, 152, 26));
        this.a(new Slot(var2, 15, 152, 44));
        this.a(new Slot(var2, 16, 152, 62));
        int var3;

        for (var3 = 0; var3 < 3; ++var3)
        {
            for (int var4 = 0; var4 < 9; ++var4)
            {
                this.a(new Slot(playerinventory, var4 + var3 * 9 + 9, 8 + var4 * 18, 84 + var3 * 18));
            }
        }

        for (var3 = 0; var3 < 9; ++var3)
        {
            this.a(new Slot(playerinventory, var3, 8 + var3 * 18, 142));
        }
    }

    /**
     * Updates crafting matrix; called from onCraftMatrixChanged. Args: none
     */
    public void a()
    {
        super.a();

        for (int var1 = 0; var1 < this.listeners.size(); ++var1)
        {
            ICrafting var2 = (ICrafting)this.listeners.get(var1);

            if (this.energy != this.tileentity.energy)
            {
                var2.setContainerData(this, 0, this.tileentity.energy & 65535);
                var2.setContainerData(this, 1, this.tileentity.energy >>> 16);
            }

            if (this.maxInput != this.tileentity.maxInput)
            {
                var2.setContainerData(this, 2, this.tileentity.maxInput);
            }
        }

        this.energy = this.tileentity.energy;
        this.maxInput = (short)this.tileentity.maxInput;
    }

    public void updateProgressBar(int var1, int var2)
    {
        switch (var1)
        {
            case 0:
                this.tileentity.energy = this.tileentity.energy & -65536 | var2;
                break;

            case 1:
                this.tileentity.energy = this.tileentity.energy & 65535 | var2 << 16;
                break;

            case 2:
                this.tileentity.maxInput = var2;
        }
    }

    public int guiInventorySize()
    {
        return 17;
    }

    public int getInput()
    {
        return 0;
    }

    public boolean b(EntityHuman var1)
    {
        return this.tileentity.a(var1);
    }
}
