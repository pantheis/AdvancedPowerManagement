/*******************************************************************************
 * Copyright (c) 2012-2013 Yancarlo Ramsey and CJ Bowman
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

	public TEAdjustableTransformer tile;
	public int outputRate;
	public int packetSize;
	public byte[] sideSettings = {0, 0, 0, 0, 0, 0}; // DOWN, UP, NORTH, SOUTH, WEST, EAST
	public int outputAvg;
	public int inputAvg;
	public int energyBuffer;

	public ContainerAdjustableTransformer(TEAdjustableTransformer tileentity)
	{
		if (Info.isDebugging) System.out.println("ContainerAdjustableTransformer");
		tile = tileentity;
		outputRate = -1;
		packetSize = -1;
		for (int i : sideSettings)
			i = (byte)255;
		outputAvg = -1;
		inputAvg = -1;
		energyBuffer = -1;
	}

	@Override
	public void detectAndSendChanges()
	{
		final int syncOutAvg = (int)(tile.outputTracker.getAverage() * 100);
		final int syncInAvg = (int)(tile.inputTracker.getAverage() * 100);

		for (int crafterIndex = 0; crafterIndex < crafters.size(); ++crafterIndex)
		{
			ICrafting crafter = (ICrafting)crafters.get(crafterIndex);

			if (this.outputRate != tile.outputRate)
			{
				crafter.sendProgressBarUpdate(this, 0, tile.outputRate & 65535);
				crafter.sendProgressBarUpdate(this, 1, tile.outputRate >>> 16);
			}

			if (this.packetSize != tile.packetSize)
			{
				crafter.sendProgressBarUpdate(this, 2, tile.packetSize & 65535);
				crafter.sendProgressBarUpdate(this, 3, tile.packetSize >>> 16);
			}

			for (int i = 0; i < 6; i++)
				if (this.sideSettings[i] != tile.sideSettings[i])
			{
				crafter.sendProgressBarUpdate(this, 4 + i, tile.sideSettings[i]);
			}

			if (outputAvg != syncOutAvg)
			{
				crafter.sendProgressBarUpdate(this, 10, syncOutAvg & 65535);
				crafter.sendProgressBarUpdate(this, 11, syncOutAvg >>> 16);
			}

			if (inputAvg != syncInAvg)
			{
				crafter.sendProgressBarUpdate(this, 12, syncInAvg & 65535);
				crafter.sendProgressBarUpdate(this, 13, syncInAvg >>> 16);
			}

			if (this.energyBuffer != tile.energyBuffer)
			{
				crafter.sendProgressBarUpdate(this, 14, tile.energyBuffer & 65535);
				crafter.sendProgressBarUpdate(this, 15, tile.energyBuffer >>> 16);
			}
		}

		// Done sending updates, record the new current values
		this.outputRate = tile.outputRate;
		this.packetSize = tile.packetSize;
		for (int i = 0; i < 6; i++)
		{
			this.sideSettings[i] = tile.sideSettings[i];
		}
		outputAvg = syncOutAvg;
		inputAvg = syncInAvg;
		this.energyBuffer = tile.energyBuffer;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void updateProgressBar(int param, int value)
	{
		switch (param)
		{
		case 0:
			tile.outputRate = tile.outputRate & -65536 | value;
			break;

		case 1:
			tile.outputRate = tile.outputRate & 65535 | (value << 16);
			break;

		case 2:
			tile.packetSize = tile.packetSize & -65536 | value;
			break;

		case 3:
			tile.packetSize = tile.packetSize & 65535 | (value << 16);
			break;

		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
			tile.sideSettings[param - 4] = (byte)value;
			break;

		case 10:
			outputAvg = outputAvg & -65536 | value;
			break;

		case 11:
			outputAvg = outputAvg & 65535 | (value << 16);
			break;

		case 12:
			inputAvg = inputAvg & -65536 | value;
			break;

		case 13:
			inputAvg = inputAvg & 65535 | (value << 16);
			break;

		case 14:
			tile.energyBuffer = tile.energyBuffer & -65536 | value;
			break;

		case 15:
			tile.energyBuffer = tile.energyBuffer & 65535 | (value << 16);
			break;

		default:
			System.out.println("ContainerAdvEmitter.updateProgressBar - Warning: default case!");
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer var1)
	{
		return tile.isUseableByPlayer(var1);
	}
}
