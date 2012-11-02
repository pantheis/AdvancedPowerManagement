/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.ChargingBench;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ICrafting;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

public class ContainerAdvEmitter extends Container
{
	private final int playerInventoryStartSlot = 1;

	public TEAdvEmitter te;
	public int outputRate;
	public int packetSize;

	public ContainerAdvEmitter(InventoryPlayer player, TEAdvEmitter tile)
	{
		if (ChargingBench.isDebugging) System.out.println("ContainerAdvEmitter");
		te = tile;
		outputRate = -1;
		packetSize = -1;
	}

	@Override
	public void updateCraftingResults()
	{
		// if (ChargingBench.isDebugging) System.out.println("ContainerChargingBench.updateCraftingResults");
		super.updateCraftingResults();

		for (int crafterIndex = 0; crafterIndex < crafters.size(); ++crafterIndex)
		{
			ICrafting crafter = (ICrafting)crafters.get(crafterIndex);

			if (this.outputRate != te.outputRate)
			{
				crafter.updateCraftingInventoryInfo(this, 0, te.outputRate & 65535);
				crafter.updateCraftingInventoryInfo(this, 1, te.outputRate >>> 16);
			}

			if (this.packetSize != te.packetSize)
			{
				crafter.updateCraftingInventoryInfo(this, 2, te.packetSize & 65535);
				crafter.updateCraftingInventoryInfo(this, 3, te.packetSize >>> 16);
			}
		}
		
		// Done sending updates, record the new current values
		this.outputRate = te.outputRate;
		this.packetSize = te.packetSize;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void updateProgressBar(int param, int value)
	{
		super.updateProgressBar(param, value);

		switch (param)
		{
		case 0:
			//if (ChargingBench.isDebugging) System.out.println("ContainerChargingBench.updateProgressBar case 0 tileentity.currentEnergy = " + (this.tileentity.currentEnergy & -65536) + " | " + value);
			te.outputRate = te.outputRate & -65536 | value;
			break;

		case 1:
			//if (ChargingBench.isDebugging) System.out.println("ContainerChargingBench.updateProgressBar case 1 tileentity.currentEnergy = " + (this.tileentity.currentEnergy & 65535) + " | " + (value << 16));
			te.outputRate = te.outputRate & 65535 | (value << 16);
			break;

		case 2:
			//if (ChargingBench.isDebugging) System.out.println("ContainerChargingBench.updateProgressBar case 3 tileentity.adjustedStorage = " + (this.tileentity.adjustedStorage & -65536) + " | " + value);
			te.packetSize = te.packetSize & -65536 | value;
			break;

		case 3:
			//if (ChargingBench.isDebugging) System.out.println("ContainerChargingBench.updateProgressBar case 4 tileentity.adjustedStorage = " + (this.tileentity.adjustedStorage & 65535) + " | " + (value << 16));
			te.packetSize = te.packetSize & 65535 | (value << 16);
			break;

		default:
			System.out.println("ContainerAdvEmitter.updateProgressBar - Warning: default case!");
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer var1)
	{
		return te.isUseableByPlayer(var1);
	}
}
