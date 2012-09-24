package com.kaijin.ChargingBench;

import java.util.*;

import ic2.api.*;
import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import com.kaijin.ChargingBench.*;

public class TEChargingBench extends TileEntity implements IEnergySource, IEnergySink, IWrenchable,
IEnergyStorage, IInventory, ISidedInventory
{
	private ItemStack[] contents = new ItemStack[this.getSizeInventory()];
	private boolean initialized;

	public int currentEnergy;
	public int maxInput;
	public int maxEnergy;
	public int baseTier;
	public int energyUsedPerTick;

	public TEChargingBench(int i)
	{
		//Max Input math = 32 for tier 1, 128 for tier 2, 512 for tier 3
		this.maxInput = (int)Math.pow(2.0D, (double)(i * 2 + 3));

		//base tier = what we're passed, so 1, 2 or 3
		this.baseTier = i;

		//Max energy stored is 32(Tier 1), 128(Tier 2) or 512(Tier 3) * 20 ticks * 5 seconds.
		this.maxEnergy = (((int)Math.pow(2.0D, (double)(i * 2 + 3)) * 20) * 5);

		//Energy used per tick is 32(Tier 1), 128(Tier 2), or 512(Tier 3). This will be used
		//to output energy back to the grid when powered by redstone
		this.energyUsedPerTick = (int)Math.pow(2.0D, (double)(i * 2 + 3));
	}

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, Direction direction) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAddedToEnergyNet() 
	{
		// TODO Auto-generated method stub
		return initialized;
	}

	@Override
	public int getMaxEnergyOutput() 
	{
		// TODO Auto-generated method stub
		return 0;
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
		return currentEnergy < maxEnergy;
	}

	@Override
	public int injectEnergy(Direction directionFrom, int supply)
	{
		int surplus = 0;
		// if supply is greater than the max we can take per tick
		if(supply - maxInput >= 0)
		{
			// add the max we can take per tick to our current energy level
			currentEnergy += maxInput;
			// check if our current energy level is now over the max energy level
			if (currentEnergy > maxEnergy)
			{
				//if so, our surplus to return is equal to that amount over
				surplus = currentEnergy - maxEnergy;
				//and set our current energy level TO our max energy level
				currentEnergy = maxEnergy;
			}
			//surplus may be zero or greater here
			surplus += (supply - maxInput);
		}
		else if(supply - maxInput <=0)
		{
			currentEnergy += supply;
			// check if our current energy level is now over the max energy level
			if (currentEnergy > maxEnergy)
			{
				//if so, our surplus to return is equal to that amount over
				surplus = currentEnergy - maxEnergy;
				//and set our current energy level TO our max energy level
				currentEnergy = maxEnergy;
			}
			//surplus may be zero or greater here
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
		return false;
	}

	@Override
	public float getWrenchDropRate() 
	{
		// TODO Auto-generated method stub
		return 1.0F;
	}

	@Override
	public int getStored() 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCapacity() 
	{
		// TODO Auto-generated method stub
		return 102400;
	}

	@Override
	public int getOutput() 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSizeInventory() 
	{
		// TODO Auto-generated method stub
		return 17;
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
			maxInput = nbttagcompound.getInteger("maxInput");
			maxEnergy = nbttagcompound.getInteger("maxEnergy");
			baseTier = nbttagcompound.getInteger("baseTier");
			energyUsedPerTick = nbttagcompound.getInteger("energyUsedPerTick");

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
		//TODO
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
			nbttagcompound.setInteger("maxInput", maxInput);
			nbttagcompound.setInteger("maxEnergy", maxEnergy);
			nbttagcompound.setInteger("baseTier", baseTier);
			nbttagcompound.setInteger("energyUsedPerTick", energyUsedPerTick);
		}
	}


	@Override
	public String getInvName() 
	{
		// TODO Auto-generated method stub
		return "ChargingBench";
	}

	@Override
	public int getInventoryStackLimit() 
	{
		// TODO Auto-generated method stub
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
	public void openChest() 
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void closeChest() 
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void updateEntity()
	{
		if (!initialized && worldObj != null)
		{
			EnergyNet.getForWorld(worldObj).addTileEntity(this);
			initialized = true;
		}
		if (isActive())
		{
			//System.out.printf("TEFloodlightIC2.updateEntity: using %d energy from %d\n",
			//	energyUsedPerTick, energy);
			currentEnergy -= energyUsedPerTick;
			if (currentEnergy < 0)
				currentEnergy = 0;
		}

	}

	public boolean isActive() 
	{
		//System.out.printf("TEFloodlightIC2.isActive: energy = %d\n", energy);
		return currentEnergy > 0 && receivingRedstoneSignal();
	}

	boolean receivingRedstoneSignal() 
	{
		return worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
	}

	public int gaugeEnergyScaled(int var1)
	{
		if (Utils.isDebug()) System.out.println("TileEntityChargingBench.gaugeEnergyScaled");
		if (this.currentEnergy <= 0)
		{
			return 0;
		}
		else
		{
			int var2 = this.currentEnergy * var1 / (this.maxEnergy - this.maxInput);

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

}
