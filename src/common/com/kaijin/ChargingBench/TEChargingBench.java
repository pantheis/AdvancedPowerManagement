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

public class TEChargingBench extends TileEntity implements IEnergySink, IWrenchable, IInventory, ISidedInventory
{
	private ItemStack[] contents = new ItemStack[this.getSizeInventory()];
	private boolean initialized;

	// Base values
	public int baseMaxInput;
	public int baseStorage;
	public int baseTier;
	public int baseChargeRate;

	// Adjustable values that need communicating via container
	public int currentEnergy;
	public int adjustedMaxInput;
	public int adjustedStorage;
	public int adjustedChargeRate;

	public boolean overCurrent = false;

	public TEChargingBench(int i)
	{
		//Max Input math = 32 for tier 1, 128 for tier 2, 512 for tier 3
		this.baseMaxInput = (int)Math.pow(2.0D, (double)(2*i+5));

		//base tier = what we're passed, so 1, 2 or 3
		this.baseTier = i;
		if (Utils.isDebug()) System.out.println("BaseTier: " + this.baseTier);
		if (Utils.isDebug()) System.out.println("BaseMaxInput: " + this.baseMaxInput);

		//Max energy stored is 32(Tier 1), 128(Tier 2) or 512(Tier 3) * 20 ticks * 5 seconds.
		//Total of 3200(Tier 1), 12800(Tier 2), 51200(Tier 3)
		switch(baseTier)
		{
		case 0:
			this.baseStorage = 40000;
			break;
		case 1:
			this.baseStorage = 600000;
			break;
		case 2:
			this.baseStorage = 10000000;
			break;
		default:
			this.baseStorage = 0;
		}
		if (Utils.isDebug()) System.out.println("BaseStorage: " + this.baseStorage);

		//commented out, we're changing the way this thing works!
		//this.maxEnergy = (((int)Math.pow(2.0D, (double)(i * 2 + 3)) * 20) * 5);

		//Energy used per tick is 32(Tier 1), 128(Tier 2), or 512(Tier 3). This will be used
		//to output energy back to the grid when powered by redstone
		this.baseMaxInput = (int)Math.pow(2.0D, (double)(2*i+5));

		//setup Adjusted variables to = defaults, we'll be adjusting them in entityUpdate
		this.adjustedChargeRate = this.baseChargeRate;
		this.adjustedMaxInput = this.baseMaxInput;
		this.adjustedStorage = this.baseStorage;
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
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean demandsEnergy()
	{
		return this.currentEnergy < this.adjustedStorage;
	}

	/**
	 * This will cause the block to drop anything inside it, create a new item in the
	 * world of its type, invalidate the tile entity, remove itself from the IC2
	 * EnergyNet and clear the block space (set it to air)
	 */
	private void selfDestroy()
	{
		BlockChargingBench.preDestroyBlock(worldObj, xCoord, yCoord, zCoord);
		ItemStack stack = new ItemStack(ChargingBench.ChargingBench, 1, this.baseTier);
		BlockChargingBench.dropItem(worldObj, stack, xCoord, yCoord, zCoord);
		worldObj.setBlockAndMetadataWithUpdate(xCoord, yCoord, zCoord, 0, 0, true);
	}

	@Override
	public int injectEnergy(Direction directionFrom, int supply)
	{
		int surplus = 0;
		if (ChargingBench.proxy.isServer())
		{
			// if supply is greater than the max we can take per tick
			if(supply > adjustedMaxInput)
			{
				//If the supplied EU is over the baseMaxInput, we're getting
				//supplied higher than acceptable current. Set overCurrent flag
				//and return all of the EU as surplus
				selfDestroy();
				return 0;

				/*
				 * Commenting out the following so we can start adding in current restrictions				
				 */

				//				// add the max we can take per tick to our current energy level
				//				this.currentEnergy += baseMaxInput;
				//				// check if our current energy level is now over the max energy level
				//				if (currentEnergy > baseStorage)
				//				{
				//					//if so, our surplus to return is equal to that amount over
				//					surplus = currentEnergy - baseStorage;
				//					//and set our current energy level TO our max energy level
				//					this.currentEnergy = baseStorage;
				//				}
				//				//surplus may be zero or greater here
				//				surplus += (supply - baseMaxInput);
			}
			else
			{
				this.currentEnergy += supply;
				// check if our current energy level is now over the max energy level
				if (currentEnergy > adjustedStorage)
				{
					//if so, our surplus to return is equal to that amount over
					surplus = currentEnergy - adjustedStorage;
					//and set our current energy level TO our max energy level
					this.currentEnergy = adjustedStorage;
				}
				//surplus may be zero or greater here
			}
		}
		return surplus;
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
	public int getSizeInventory()
	{
		// TODO Auto-generated method stub
		return 19;
	}

	@Override
	public int getStartInventorySide(ForgeDirection side)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		// TODO Auto-generated method stub
		return contents[i];
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2)
	{
		// TODO Auto-generated method stub
		if (this.contents[var1] != null)
		{
			ItemStack var3;

			if (this.contents[var1].stackSize <= var2)
			{
				var3 = this.contents[var1];
				this.contents[var1] = null;
				this.onInventoryChanged();
				return var3;
			}
			else
			{
				var3 = this.contents[var1].splitStack(var2);

				if (this.contents[var1].stackSize == 0)
				{
					this.contents[var1] = null;
				}

				this.onInventoryChanged();
				return var3;
			}
		}
		else
		{
			return null;
		}
	}

	public ItemStack getStackInSlotOnClosing(int var1)
	{
		if (this.contents[var1] == null)
		{
			return null;
		}

		ItemStack stack = this.contents[var1];
		this.contents[var1] = null;
		return stack;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		this.contents[i] = itemstack;

		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit())
		{
			itemstack.stackSize = getInventoryStackLimit();
		}
		this.onInventoryChanged();
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		//TODO
		if(!ChargingBench.proxy.isClient())
		{
			super.readFromNBT(nbttagcompound);

			// Read extra NBT stuff here
			currentEnergy = nbttagcompound.getInteger("currentEnergy");
			if (Utils.isDebug()) System.out.println("ReadNBT.CurrentEergy: " + this.currentEnergy);
			baseMaxInput = nbttagcompound.getInteger("maxInput");
			baseStorage = nbttagcompound.getInteger("baseStorage");
			baseTier = nbttagcompound.getInteger("baseTier");
			baseChargeRate = nbttagcompound.getInteger("energyUsedPerTick");

			NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
			NBTTagList nbttagextras = nbttagcompound.getTagList("remoteSnapshot");

			contents = new ItemStack[this.getSizeInventory()];

			// Our inventory
			for (int i = 0; i < nbttaglist.tagCount(); ++i)
			{
				NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
				int j = nbttagcompound1.getByte("Slot") & 255;

				if (j >= 0 && j < contents.length)
				{
					contents[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
				}
			}
		}
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		if(!ChargingBench.proxy.isClient())
		{
			super.writeToNBT(nbttagcompound);

			// Our inventory
			NBTTagList nbttaglist = new NBTTagList();
			for (int i = 0; i < contents.length; ++i)
			{
				if (this.contents[i] != null)
				{
					NBTTagCompound nbttagcompound1 = new NBTTagCompound();
					nbttagcompound1.setByte("Slot", (byte)i);
					contents[i].writeToNBT(nbttagcompound1);
					nbttaglist.appendTag(nbttagcompound1);
				}
			}
			nbttagcompound.setTag("Items", nbttaglist);

			//write extra NBT stuff here

			nbttagcompound.setInteger("currentEnergy", currentEnergy);
			if (Utils.isDebug()) System.out.println("WriteNBT.CurrentEergy: " + this.currentEnergy);
			nbttagcompound.setInteger("maxInput", baseMaxInput);
			nbttagcompound.setInteger("baseStorage", baseStorage);
			nbttagcompound.setInteger("baseTier", baseTier);
			nbttagcompound.setInteger("energyUsedPerTick", baseChargeRate);
		}
	}


	@Override
	public String getInvName()
	{
		return "ChargingBench";
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this)
		{
			return false;
		}

		return entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64D;
	}

	@Override
	public void openChest() {}

	@Override
	public void closeChest() {}

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
			//System.out.printf("TEFloodlightIC2.updateEntity: using %d energy from %d\n",
			//	energyUsedPerTick, energy);
			currentEnergy -= adjustedChargeRate;
			if (Utils.isDebug()) System.out.println("updateEntity.CurrentEergy: " + this.currentEnergy);
			if (currentEnergy < 0)
				currentEnergy = 0;
		}

	}

	public boolean isActive()
	{
		return currentEnergy > 0 && receivingRedstoneSignal();
	}

	boolean receivingRedstoneSignal()
	{
		return worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
	}

	public int gaugeEnergyScaled(int var1)
	{
		if (Utils.isDebug()) System.out.println("TileEntityChargingBench.gaugeEnergyScaled");
		if (Utils.isDebug()) System.out.println("currentEnergy: " + currentEnergy);
		if (Utils.isDebug()) System.out.println("this.currentEnergy: " + this.currentEnergy);
		if (this.currentEnergy <= 0)
		{
			return 0;
		}
		else
		{
			int var2 = currentEnergy * var1 / adjustedStorage;

			if (var2 > var1)
			{
				var2 = var1;
			}

			return var2;
		}
	}

	@Override
	public void invalidate()
	{
		if (worldObj!=null && initialized)
		{
			EnergyNet.getForWorld(worldObj).removeTileEntity(this);
		}
		super.invalidate();
	}

	//Networking stuff

}
