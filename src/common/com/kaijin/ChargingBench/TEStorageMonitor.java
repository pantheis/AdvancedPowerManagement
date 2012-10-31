/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.ChargingBench;

import ic2.api.IEnergyStorage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

public class TEStorageMonitor extends TileEntity implements IInventory, ISidedInventory
{
	private ItemStack[] contents = new ItemStack[7];

	private int tickTime;
	private int tickDelay = 5;
	
	public float lowerBoundary = 0.60F;
	public float upperBoundary = 0.90F;
	
	public int lowerBoundaryBits;
	public int upperBoundaryBits;

	private boolean tileLoaded;

	public int baseTier;
	public int energyStored;
	public int energyCapacity;
	public int chargeLevel;
	
	public boolean isPowering;
	public boolean blockState;

	public int[] targetCoords = new int[3];

	public TileEntity targetTile;

	public TEStorageMonitor()
	{
		super();
	}

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	/**
	 * This will cause the block to drop anything inside it, create a new item in the
	 * world of its type, invalidate the tile entity, remove itself from the IC2
	 * EnergyNet and clear the block space (set it to air)
	 */
	private void selfDestroy()
	{
		dropContents();
		ItemStack stack = new ItemStack(ChargingBench.blockChargingBench, 1, 10);
		dropItem(stack);
		worldObj.setBlockAndMetadataWithNotify(xCoord, yCoord, zCoord, 0, 0);
		this.invalidate();
	}

	public void dropItem(ItemStack item)
	{
		EntityItem entityitem = new EntityItem(worldObj, (double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D, item);
		entityitem.delayBeforeCanPickup = 10;
		worldObj.spawnEntityInWorld(entityitem);
	}

	public void dropContents()
	{
		ItemStack item;
		int i;
		for (i = 0; i < contents.length; ++i)
		{
			item = contents[i];
			if (item != null && item.stackSize > 0) dropItem(item);
		}
	}
	
	@Override
	public int getSizeInventory()
	{
		// Only input/output slots are accessible to machines
		return 1;
	}

	@Override
	public int getStartInventorySide(ForgeDirection side)
	{
		/*
		switch (side)
		{
		case UP:
			return StorageMonitor.slotUp;
		case DOWN:
			return StorageMonitor.slotDown;
		case NORTH:
			return StorageMonitor.slotNorth;
		case SOUTH:
			return StorageMonitor.slotSouth;
		case WEST:
			return StorageMonitor.slotWest;
		case EAST:
			return StorageMonitor.slotEast;
		default:
			return StorageMonitor.slotUniversal;
		}
		*/
		return ChargingBench.smSlotUniversal;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		// Each side accesses a single slot
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return contents[i];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		if (this.contents[slot] != null)
		{
			ItemStack output;

			if (this.contents[slot].stackSize <= amount)
			{
				output = this.contents[slot];
				this.contents[slot] = null;
				this.onInventoryChanged(slot);
				return output;
			}
			else
			{
				output = this.contents[slot].splitStack(amount);

				if (this.contents[slot].stackSize == 0)
				{
					this.contents[slot] = null;
				}
				this.onInventoryChanged(slot);
				return output;
			}
		}
		else
		{
			return null;
		}
	}

	public ItemStack getStackInSlotOnClosing(int slot)
	{
		if (this.contents[slot] == null)
		{
			return null;
		}

		ItemStack stack = this.contents[slot];
		this.contents[slot] = null;
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack)
	{
		this.contents[slot] = itemstack;

		if (Utils.isDebug() && itemstack != null)
		{
			if (ChargingBench.proxy.isServer())
			{
				System.out.println("Server assigned stack tag: " + itemstack.stackTagCompound);
				if (itemstack.stackTagCompound != null) System.out.println("     " + itemstack.stackTagCompound.getTags().toString());
			}
			if (ChargingBench.proxy.isClient())
			{
				System.out.println("Client assigned stack tag: " + itemstack.stackTagCompound);
				if (itemstack.stackTagCompound != null) System.out.println("     " + itemstack.stackTagCompound.getTags().toString());
			}
		}
		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit())
		{
			itemstack.stackSize = getInventoryStackLimit();
		}
		this.onInventoryChanged(slot);
	}

	public void onInventoryChanged(int slot)
	{
		if (Utils.isDebug()) System.out.println("TE.onInventoryChanged.slot.checkInventory()");
		checkInventory();
		super.onInventoryChanged();
	}

	@Override
	public void onInventoryChanged()
	{
		// We're not sure what called this or what slot was altered, so make sure the upgrade effects are correct just in case and then pass the call on.
		if (Utils.isDebug()) System.out.println("TE.onInventoryChanged.checkInventory()");
		super.onInventoryChanged();
	}

	public boolean isItemValid(int slot, ItemStack stack)
	{
		// Decide if the item is a valid IC2 electrical item
		if (stack != null && stack.getItem() instanceof ItemStorageLinkCard)
		{
			return true;
		}
		return false; 
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		if(!ChargingBench.proxy.isClient())
		{
			super.readFromNBT(nbttagcompound);

			// Read extra NBT stuff here
			baseTier = nbttagcompound.getInteger("baseTier");
			isPowering = nbttagcompound.getBoolean("isPowering");
			NBTTagList nbttaglist = nbttagcompound.getTagList("Items");

			contents = new ItemStack[ChargingBench.smInventorySize];

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

			// We can calculate these, no need to save/load them.
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
					//if (Utils.isDebug()) System.out.println("WriteNBT contents[" + i + "] stack tag: " + contents[i].stackTagCompound);
					NBTTagCompound nbttagcompound1 = new NBTTagCompound();
					nbttagcompound1.setByte("Slot", (byte)i);
					contents[i].writeToNBT(nbttagcompound1);
					nbttaglist.appendTag(nbttagcompound1);
				}
			}
			nbttagcompound.setTag("Items", nbttaglist);

			//write extra NBT stuff here
			nbttagcompound.setInteger("baseTier", baseTier);
			nbttagcompound.setBoolean("isPowering", isPowering);
		}
	}


	@Override
	public String getInvName()
	{
		return "StorageMonitor";
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

	/**
	 * Runs once on tile entity load to make sure all of our internals are setup correctly
	 */
	private void onLoad()
	{
		if (!ChargingBench.proxy.isClient())
		{
			tileLoaded = true;
			checkInventory();
			if (targetCoords != null)
			{
				TileEntity tile = worldObj.getBlockTileEntity(targetCoords[0], targetCoords[1], targetCoords[2]);
				if (tile instanceof IEnergyStorage)
				{
					//				if (Utils.isDebug()) System.out.println("updateEntity - check energy level of remote block");
					this.energyStored = ((IEnergyStorage)tile).getStored();
					this.energyCapacity = ((IEnergyStorage)tile).getCapacity();
					this.blockState = true;
				}
				else
				{
					this.energyStored = -1;
					this.energyCapacity = -1;
					this.blockState = false;
				}
			}
			int oldChargeLevel = this.chargeLevel;
			this.chargeLevel = gaugeEnergyScaled(12);
			lowerBoundaryBits = (int)(this.lowerBoundary * 100.0F); 
			upperBoundaryBits = (int)(this.upperBoundary * 100.0F);

			if(this.energyCapacity > 0) // Avoid divide by zero and also test if the remote energy storage is valid
			{
				updateRedstone();
			}
			else if(isPowering) // If we're emitting redstone at this point, we need to shut it off
			{
				this.isPowering = false;
				worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, worldObj.getBlockId(xCoord, yCoord, zCoord));
			}
			worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
		}
	}

	@Override
	public void updateEntity() //TODO Marked for easy access
	{
		if (ChargingBench.proxy.isClient())
		{
			return;
		}

		if (!tileLoaded)
		{
			onLoad();
		}

		// Work done every tick

		// Delayed work
		if (tickTime == 0)
		{
			tickTime = tickDelay;
			if (targetCoords != null)
			{
				TileEntity tile = worldObj.getBlockTileEntity(targetCoords[0], targetCoords[1], targetCoords[2]);
				if (tile instanceof IEnergyStorage)
				{
					// if (Utils.isDebug()) System.out.println("updateEntity - check energy level of remote block");
					this.energyStored = ((IEnergyStorage)tile).getStored();
					this.energyCapacity = ((IEnergyStorage)tile).getCapacity();
					blockState = true;
				}
				else
				{
					this.energyStored = -1;
					this.energyCapacity = -1;
					if (blockState)
					{
						worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
					}
					this.blockState = false;
				}
			}

			// Trigger this only when charge level passes where it would need to update the client texture
			int oldChargeLevel = this.chargeLevel;
			this.chargeLevel = gaugeEnergyScaled(12);
			
			if(this.energyCapacity > 0) // Avoid divide by zero and also test if the remote energy storage is valid
			{
				updateRedstone();
			}
			else if(isPowering) // If we're emitting redstone at this point, we need to shut it off
			{
				this.isPowering = false;
				worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, worldObj.getBlockId(xCoord, yCoord, zCoord));
			}

			if (oldChargeLevel != this.chargeLevel)
			{
				//if (Utils.isDebug()) System.out.println("TE oldChargeLevel: " + oldChargeLevel + " chargeLevel: " + this.chargeLevel); 
				worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
			}
		}
		else if (tickTime > 0)
		{
			tickTime--;
		}
	}

	private void updateRedstone()
	{
		// TODO Auto-generated method stub
		float chargePercent = 0.00F;
		chargePercent = (float)this.energyStored / this.energyCapacity;
		if (Utils.isDebug()) System.out.println("chargePercent:" + chargePercent);
		if (chargePercent < this.lowerBoundary && this.isPowering == false)
		{
			this.isPowering = true;
			worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, worldObj.getBlockId(xCoord, yCoord, zCoord));
			worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
			
		}
		if (chargePercent > this.upperBoundary && this.isPowering == true)
		{
			this.isPowering = false;
			worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, worldObj.getBlockId(xCoord, yCoord, zCoord));
			worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
		}
	}

	private void checkInventory()//FIXME correct constant block updates
	{
		ItemStack item = getStackInSlot(ChargingBench.smSlotUniversal);
		if (item == null || !(item.getItem() instanceof ItemStorageLinkCard))
		{
			targetCoords = null;
			this.energyCapacity = -1;
			this.energyStored = -1;
			blockState = false;
		}
		else
		{
			targetCoords = ItemCardBase.getCoordinates(item);
		}
		worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
	}

	boolean receivingRedstoneSignal()
	{
		return worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
	}

	public int gaugeEnergyScaled(int gaugeSize)
	{
		if (this.energyStored <= 0 || this.energyCapacity <= 0)
		{
			return 0;
		}

		int result = this.energyStored * gaugeSize / this.energyCapacity;
		if (result > gaugeSize) result = gaugeSize;

		return result;
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
	}

	//Networking stuff

	public void receiveDescriptionData(int charge, boolean power, boolean state)
	{
		chargeLevel = charge;
		isPowering = power;
		blockState = state;
		worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
	}
	
	@Override
	public Packet250CustomPayload getDescriptionPacket()
	{
		if (Utils.isDebug()) System.out.println("TE getAuxillaryInfoPacket()");
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);
		try
		{
			data.writeInt(0);
			data.writeInt(this.xCoord);
			data.writeInt(this.yCoord);
			data.writeInt(this.zCoord);
			data.writeInt(this.chargeLevel);
			data.writeBoolean(this.isPowering);
			data.writeBoolean(this.blockState);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = ChargingBench.packetChannel; // CHANNEL MAX 16 CHARS
		packet.data = bytes.toByteArray();
		packet.length = packet.data.length;
		return packet;
	}
}
