package com.kaijin.ChargingBench;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import ic2.api.*;
import net.minecraft.src.Chunk;
import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import com.kaijin.ChargingBench.*;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

public class TEEmitter extends TileEntity implements IEnergySource, IWrenchable
{
	private boolean initialized;

	public int baseTier;
	public int outputRate;

	public TEEmitter(int i)
	{
		//Max Input math = 32 for tier 1, 128 for tier 2, 512 for tier 3
		this.outputRate = (int)Math.pow(2.0D, (double)(2*i + 3));

		//base tier = what we're passed, so 1, 2 or 3 (or 4)
		this.baseTier = i;
		if (Utils.isDebug()) System.out.println("BaseTier: " + this.baseTier + " ;baseOutput: " + outputRate);
	}

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	@Override
	public boolean isAddedToEnergyNet()
	{
		// TODO Auto-generated method stub
		return initialized;
	}

	@Override
	public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int side)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public short getFacing()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setFacing(short facing)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean wrenchCanRemove(EntityPlayer entityPlayer)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public float getWrenchDropRate()
	{
		// TODO Auto-generated method stub
		return 1.0F;
	}

	@Override
	public void updateEntity()
	{
		if (ChargingBench.proxy.isClient())
		{
			return;
		}
		if (!initialized && worldObj != null)
		{
			EnergyNet.getForWorld(worldObj).addTileEntity(this);
			initialized = true;
		}
		if (isActive())
		{
			EnergyNet.getForWorld(worldObj).emitEnergyFrom(this, outputRate);
		}

	}

	public boolean isActive()
	{
		return receivingRedstoneSignal();
	}

	boolean receivingRedstoneSignal()
	{
		return worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
	}

	@Override
	public void invalidate()
	{
		if (worldObj != null && initialized)
		{
			EnergyNet.getForWorld(worldObj).removeTileEntity(this);
		}
		super.invalidate();
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, Direction direction)
	{
		return true;
	}

	@Override
	public int getMaxEnergyOutput()
	{
		return Integer.MAX_VALUE;
	}
}
