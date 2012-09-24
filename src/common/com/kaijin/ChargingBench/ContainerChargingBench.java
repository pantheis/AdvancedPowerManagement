package com.kaijin.ChargingBench;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ICrafting;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;
import cpw.mods.fml.common.network.Player;

public class ContainerChargingBench extends Container
{
    public TEChargingBench tileentity;
    public int energy;
    public short maxInput;

    public ContainerChargingBench(InventoryPlayer player, TEChargingBench tile)
    {
    	if (Utils.isDebug()) System.out.println("ContainerChargingBench");
        this.tileentity = tile;
        this.energy = 0;
        this.maxInput = 0;
		this.addSlotToContainer(new Slot(tile, 0, 61, 19));
        this.addSlotToContainer(new Slot(tile, 1, 79, 19));
        this.addSlotToContainer(new Slot(tile, 2, 97, 19));
        this.addSlotToContainer(new Slot(tile, 3, 115, 19));
        this.addSlotToContainer(new Slot(tile, 4, 61, 37));
        this.addSlotToContainer(new Slot(tile, 5, 79, 37));
        this.addSlotToContainer(new Slot(tile, 6, 97, 37));
        this.addSlotToContainer(new Slot(tile, 7, 115, 37));
        this.addSlotToContainer(new Slot(tile, 8, 61, 55));
        this.addSlotToContainer(new Slot(tile, 9, 79, 55));
        this.addSlotToContainer(new Slot(tile, 10, 97, 55));
        this.addSlotToContainer(new Slot(tile, 11, 115, 55));
        this.addSlotToContainer(new Slot(tile, 12, 24, 40));
        this.addSlotToContainer(new Slot(tile, 13, 152, 8));
        this.addSlotToContainer(new Slot(tile, 14, 152, 26));
        this.addSlotToContainer(new Slot(tile, 15, 152, 44));
        this.addSlotToContainer(new Slot(tile, 16, 152, 62));
        int var3;

        for (var3 = 0; var3 < 3; ++var3)
        {
            for (int var4 = 0; var4 < 9; ++var4)
            {
                this.addSlotToContainer(new Slot(player, var4 + var3 * 9 + 9, 8 + var4 * 18, 84 + var3 * 18));
            }
        }

        for (var3 = 0; var3 < 9; ++var3)
        {
            this.addSlotToContainer(new Slot(player, var3, 8 + var3 * 18, 142));
        }
    }

    public void updateCraftingResults()
    {
    	if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateCraftingResults");
        super.updateCraftingResults();

        for (int var1 = 0; var1 < this.crafters.size(); ++var1)
        {
            ICrafting var2 = (ICrafting)this.crafters.get(var1);
            if (Utils.isDebug()) System.out.println("ConCurrentEnergy: " + this.energy);
            if (Utils.isDebug()) System.out.println("TEcurrentEnergy: " + this.tileentity.getStored());

            if (this.energy != this.tileentity.getStored())
            {
            	if (Utils.isDebug()) System.out.println("ContainerChargingBench.enery != tile.currentEnergy");
                var2.updateCraftingInventoryInfo(this, 0, this.tileentity.getStored() & 65535);
                var2.updateCraftingInventoryInfo(this, 1, this.tileentity.getStored() >>> 16);
            }

            if (this.maxInput != this.tileentity.maxInput)
            {
            	if (Utils.isDebug()) System.out.println("ContainerChargingBench.maxInput != tile.maxInput");
                var2.updateCraftingInventoryInfo(this, 2, this.tileentity.maxInput);
            }
        }
        this.energy = this.tileentity.getStored();
        this.maxInput = (short)this.tileentity.maxInput;
    }
    
    @Override
    public void updateProgressBar(int var1, int var2)
    {
    	super.updateProgressBar(var1, var2);
    	if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar");
        switch (var1)
        {
            case 0:
            	if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar.case0");
            	this.tileentity.setStored(this.tileentity.getStored() & -65536 | var2);
                if (Utils.isDebug()) System.out.println("ContainerCB.currentEnergy: " + this.tileentity.getStored());
                break;

            case 1:
            	if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar.case1");
                this.tileentity.setStored(this.tileentity.getStored() & 65535 | var2 << 16);
                if (Utils.isDebug()) System.out.println("ContainerCB.currentEnergy: " + this.tileentity.getStored());
                break;

            case 2:
            	if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar.case2");
            	if (Utils.isDebug()) System.out.println("ContainerCB.currentEnergy: " + this.tileentity.getStored());
                this.tileentity.maxInput = var2;
        }
    }

    public int guiInventorySize()
    {
    	if (Utils.isDebug()) System.out.println("ContainerChargingBench.guiInventorySize");
        return 17;
    }

	public ItemStack transferStackInSlot(int par1)
	{
		ItemStack var2 = null;
		Slot var3 = (Slot)this.inventorySlots.get(par1);

		if (var3 != null && var3.getHasStack())
		{
			ItemStack var4 = var3.getStack();
			var2 = var4.copy();

			if (par1 < 18)
			{
				if (!this.mergeItemStack(var4, 18, this.inventorySlots.size(), true))
				{
					return null;
				}
			}
			else if (!this.mergeItemStack(var4, 0, 18, false))
			{
				return null;
			}

			if (var4.stackSize == 0)
			{
				var3.putStack((ItemStack)null);
			}
			else
			{
				var3.onSlotChanged();
			}
		}
		return var2;
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
