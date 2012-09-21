package com.kaijin.ChargingBench;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ICrafting;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.Slot;

public class ContainerChargingBench extends Container
{
    public TileEntityChargingBench tileentity;
    public int energy;
    public short maxInput;

    public ContainerChargingBench(InventoryPlayer player, TEChargingBench var2)
    {
    	if (Utils.isDebug()) System.out.println("ContainerChargingBench");
        this.tileentity = var2;
        this.energy = 0;
        this.maxInput = 0;
        this.addSlot(new Slot(var2, 0, 61, 19));
        this.addSlot(new Slot(var2, 1, 79, 19));
        this.addSlot(new Slot(var2, 2, 97, 19));
        this.addSlot(new Slot(var2, 3, 115, 19));
        this.addSlot(new Slot(var2, 4, 61, 37));
        this.addSlot(new Slot(var2, 5, 79, 37));
        this.addSlot(new Slot(var2, 6, 97, 37));
        this.addSlot(new Slot(var2, 7, 115, 37));
        this.addSlot(new Slot(var2, 8, 61, 55));
        this.addSlot(new Slot(var2, 9, 79, 55));
        this.addSlot(new Slot(var2, 10, 97, 55));
        this.addSlot(new Slot(var2, 11, 115, 55));
        this.addSlot(new Slot(var2, 12, 24, 40));
        this.addSlot(new Slot(var2, 13, 152, 8));
        this.addSlot(new Slot(var2, 14, 152, 26));
        this.addSlot(new Slot(var2, 15, 152, 44));
        this.addSlot(new Slot(var2, 16, 152, 62));
        int var3;

        for (var3 = 0; var3 < 3; ++var3)
        {
            for (int var4 = 0; var4 < 9; ++var4)
            {
                this.addSlot(new Slot(player, var4 + var3 * 9 + 9, 8 + var4 * 18, 84 + var3 * 18));
            }
        }

        for (var3 = 0; var3 < 9; ++var3)
        {
            this.addSlot(new Slot(player, var3, 8 + var3 * 18, 142));
        }
    }

    public void updateCraftingResults()
    {
    	if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateCraftingResults");
        super.updateCraftingResults();

        for (int var1 = 0; var1 < this.crafters.size(); ++var1)
        {
            ICrafting var2 = (ICrafting)this.crafters.get(var1);

            if (this.energy != this.tileentity.energy)
            {
                var2.updateCraftingInventoryInfo(this, 0, this.tileentity.energy & 65535);
                var2.updateCraftingInventoryInfo(this, 1, this.tileentity.energy >>> 16);
            }

            if (this.maxInput != this.tileentity.maxInput)
            {
                var2.updateCraftingInventoryInfo(this, 2, this.tileentity.maxInput);
            }
        }

        this.energy = this.tileentity.energy;
        this.maxInput = (short)this.tileentity.maxInput;
    }

    public void updateProgressBar(int var1, int var2)
    {
    	if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar");
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
    	if (Utils.isDebug()) System.out.println("ContainerChargingBench.guiInventorySize");
        return 17;
    }

    public int getInput()
    {
    	if (Utils.isDebug()) System.out.println("ContainerChargingBench.getInput");
        return 0;
    }

    public boolean canInteractWith(EntityPlayer var1)
    {
    	if (Utils.isDebug()) System.out.println("ContainerChargingBench.canInteractWith");
        return this.tileentity.isUseableByPlayer(var1);
    }
}
