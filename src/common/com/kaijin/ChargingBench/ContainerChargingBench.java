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
	public int currentEnergy;
	public short maxInput;

	public ContainerChargingBench(InventoryPlayer player, TEChargingBench tile)
	{
		if (Utils.isDebug()) System.out.println("ContainerChargingBench");
		this.tileentity = tile;
		this.currentEnergy = 0;
		this.maxInput = 0;
		
		//Input charging slots
		this.addSlotToContainer(new Slot(tile, 0, 52, 23));
		this.addSlotToContainer(new Slot(tile, 1, 70, 23));
		this.addSlotToContainer(new Slot(tile, 2, 88, 23));
		this.addSlotToContainer(new Slot(tile, 3, 52, 41));
		this.addSlotToContainer(new Slot(tile, 4, 70, 41));
		this.addSlotToContainer(new Slot(tile, 5, 88, 41));
		this.addSlotToContainer(new Slot(tile, 6, 52, 59));
		this.addSlotToContainer(new Slot(tile, 7, 70, 59));
		this.addSlotToContainer(new Slot(tile, 8, 88, 59));
		this.addSlotToContainer(new Slot(tile, 9, 52, 77));
		this.addSlotToContainer(new Slot(tile, 10, 70, 77));
		this.addSlotToContainer(new Slot(tile, 11, 88, 77));
		
		//Power source slot
		this.addSlotToContainer(new Slot(tile, 12, 8, 77));
		
		//Overclock slots
		this.addSlotToContainer(new Slot(tile, 13, 152, 23));
		this.addSlotToContainer(new Slot(tile, 14, 152, 41));
		this.addSlotToContainer(new Slot(tile, 15, 152, 59));
		this.addSlotToContainer(new Slot(tile, 16, 152, 77));
		
		//Output slot
		this.addSlotToContainer(new Slot(tile, 17, 130, 50));
		int var3;

		// player inventory
		for (var3 = 0; var3 < 3; ++var3)
		{
			for (int var4 = 0; var4 < 9; ++var4)
			{
				this.addSlotToContainer(new Slot(player, var4 + var3 * 9 + 9, 8 + var4 * 18, 99 + var3 * 18));
			}
		}

		// player hotbar
		for (var3 = 0; var3 < 9; ++var3)
		{
			this.addSlotToContainer(new Slot(player, var3, 8 + var3 * 18, 157));
		}
	}

	public void updateCraftingResults()
	{
		if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateCraftingResults");
		super.updateCraftingResults();

		for (int var1 = 0; var1 < this.crafters.size(); ++var1)
		{
			ICrafting var2 = (ICrafting)this.crafters.get(var1);

			if (this.currentEnergy != this.tileentity.currentEnergy)
			{
				var2.updateCraftingInventoryInfo(this, 0, this.tileentity.currentEnergy & 65535);
				var2.updateCraftingInventoryInfo(this, 1, this.tileentity.currentEnergy >>> 16);
			}

			if (this.maxInput != this.tileentity.baseMaxInput)
			{
				var2.updateCraftingInventoryInfo(this, 2, this.tileentity.baseMaxInput);
			}
		}
		this.currentEnergy = this.tileentity.currentEnergy;
		this.maxInput = (short)this.tileentity.baseMaxInput;
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
			this.tileentity.currentEnergy = this.tileentity.currentEnergy & -65536 | var2;
			break;

		case 1:
			if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar.case1");
			this.tileentity.currentEnergy = this.tileentity.currentEnergy & 65535 | var2 << 16;
			break;

		case 2:
			if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar.case2");
			this.tileentity.baseMaxInput = var2;
		}
	}

	public int guiInventorySize()
	{
		if (Utils.isDebug()) System.out.println("ContainerChargingBench.guiInventorySize");
		return 18;
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

	public boolean canInteractWith(EntityPlayer var1)
	{
		if (Utils.isDebug()) System.out.println("ContainerChargingBench.canInteractWith");
		return this.tileentity.isUseableByPlayer(var1);
	}
}
