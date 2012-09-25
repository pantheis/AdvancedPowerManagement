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
	public int adjustedStorage;
	public short adjustedChargeRate;
	public short adjustedMaxInput;

	public ContainerChargingBench(InventoryPlayer player, TEChargingBench tile)
	{
		if (Utils.isDebug()) System.out.println("ContainerChargingBench");
		this.tileentity = tile;
		this.currentEnergy = 0;
		this.adjustedMaxInput = 0;
		this.adjustedStorage = 0;
		this.adjustedChargeRate = 0;

		int topOffset = 32; // Got tired of forgetting to manually alter ALL of the constants. (This won't affect the energy bar!)

		int xCol;
		int yRow;

		// Input charging slots
		for (yRow = 0; yRow < 4; ++yRow) // 4 rows high
		{
			for (xCol = 0; xCol < 3; ++xCol) // 3 columns across
			{
				this.addSlotToContainer(new Slot(tile, 3 * yRow + xCol, 52 + xCol * 18, topOffset + yRow * 18)); // 52, 32 is upper left input slot 
			}
		}

		// Upgrade slots (Overclocker, storage)
		this.addSlotToContainer(new Slot(tile, 12, 152, topOffset));
		this.addSlotToContainer(new Slot(tile, 13, 152, topOffset + 18));
		this.addSlotToContainer(new Slot(tile, 14, 152, topOffset + 36));
		this.addSlotToContainer(new Slot(tile, 15, 152, topOffset + 54));

		// Power source slot
		this.addSlotToContainer(new Slot(tile, 16, 8, topOffset + 54));

		// Input Slot
		this.addSlotToContainer(new Slot(tile, 17, 130, topOffset));

		// Output slot
		this.addSlotToContainer(new Slot(tile, 18, 130, topOffset + 54));

		// Player inventory
		for (yRow = 0; yRow < 3; ++yRow)
		{
			for (xCol = 0; xCol < 9; ++xCol)
			{
				this.addSlotToContainer(new Slot(player, xCol + yRow * 9 + 9, 8 + xCol * 18, topOffset + 76 + yRow * 18));
			}
		}

		// Player hot bar
		for (yRow = 0; yRow < 9; ++yRow)
		{
			this.addSlotToContainer(new Slot(player, yRow, 8 + yRow * 18, topOffset + 134));
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

			if (this.adjustedMaxInput != this.tileentity.adjustedMaxInput)
			{
				var2.updateCraftingInventoryInfo(this, 2, this.tileentity.adjustedMaxInput);
			}

			if (this.adjustedStorage != this.tileentity.adjustedStorage)
			{
				var2.updateCraftingInventoryInfo(this, 3, this.tileentity.adjustedStorage & 65535);
				var2.updateCraftingInventoryInfo(this, 4, this.tileentity.adjustedStorage >>> 16);
			}

			if (this.adjustedChargeRate != this.tileentity.adjustedChargeRate)
			{
				var2.updateCraftingInventoryInfo(this, 5, this.tileentity.adjustedChargeRate);
			}
		}
		this.currentEnergy = this.tileentity.currentEnergy;
		this.adjustedStorage = this.tileentity.adjustedStorage;
		this.adjustedMaxInput = (short)this.tileentity.adjustedMaxInput;
		this.adjustedChargeRate = (short)this.tileentity.adjustedChargeRate;
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
			this.tileentity.adjustedMaxInput = var2;

		case 3:
			if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar.case3");
			this.tileentity.adjustedStorage = this.tileentity.adjustedStorage & -65536 | var2;
			break;

		case 4:
			if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar.case4");
			this.tileentity.adjustedStorage = this.tileentity.adjustedStorage & 65535 | var2 << 16;
			break;

		case 5:
			if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar.case5");
			this.tileentity.adjustedChargeRate = var2;
		}
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
