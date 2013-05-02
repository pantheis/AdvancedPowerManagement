/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerAdjustableTransformer extends Container
{
	private final int playerInventoryStartSlot = 1;

	public TEAdjustableTransformer te;
	public int outputRate;
	public int packetSize;
	public byte[] sideSettings = {0, 0, 0, 0, 0, 0}; // DOWN, UP, NORTH, SOUTH, WEST, EAST

	public ContainerAdjustableTransformer(TEAdjustableTransformer tile)
	{
		if (Info.isDebugging) System.out.println("ContainerAdjustableTransformer");
		te = tile;
		outputRate = -1;
		packetSize = -1;
		for (int i : sideSettings)
			i = (byte)255;
	}

	@Override
	public void detectAndSendChanges()
	{
		for (int crafterIndex = 0; crafterIndex < crafters.size(); ++crafterIndex)
		{
			ICrafting crafter = (ICrafting)crafters.get(crafterIndex);

			if (this.outputRate != te.outputRate)
			{
				crafter.sendProgressBarUpdate(this, 0, te.outputRate & 65535);
				crafter.sendProgressBarUpdate(this, 1, te.outputRate >>> 16);
			}

			if (this.packetSize != te.packetSize)
			{
				crafter.sendProgressBarUpdate(this, 2, te.packetSize & 65535);
				crafter.sendProgressBarUpdate(this, 3, te.packetSize >>> 16);
			}

			for (int i = 0; i < 6; i++)
				if (this.sideSettings[i] != te.sideSettings[i])
			{
				crafter.sendProgressBarUpdate(this, 4 + i, te.sideSettings[i]);
			}
		}

		// Done sending updates, record the new current values
		this.outputRate = te.outputRate;
		this.packetSize = te.packetSize;
		for (int i = 0; i < 6; i++)
		{
			this.sideSettings[i] = te.sideSettings[i];
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void updateProgressBar(int param, int value)
	{
		switch (param)
		{
		case 0:
			te.outputRate = te.outputRate & -65536 | value;
			break;

		case 1:
			te.outputRate = te.outputRate & 65535 | (value << 16);
			break;

		case 2:
			te.packetSize = te.packetSize & -65536 | value;
			break;

		case 3:
			te.packetSize = te.packetSize & 65535 | (value << 16);
			break;

		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
			te.sideSettings[param - 4] = (byte)value;
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
