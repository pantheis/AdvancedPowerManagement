/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import ic2.api.IEnergyStorage;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

public class TEStorageMonitor extends TECommon implements IInventory, ISidedInventory
{
	private ItemStack[] contents;

	private int tickTime = 0;
	private int tickDelay = 5;
	
	public int lowerBoundary = 60;
	public int upperBoundary = 90;
	
	private boolean tileLoaded = false;

	public int energyStored = 0;
	public int energyCapacity = 0;
	public int chargeLevel = 0;
	
	public boolean isPowering = false;
	public boolean blockState = false;

	public int[] targetCoords;

	public TEStorageMonitor()
	{
		super();
		contents = new ItemStack[Info.SM_INVENTORY_SIZE];
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		if (!AdvancedPowerManagement.proxy.isClient())
		{
			super.readFromNBT(nbttagcompound);

			// State info to remember
			isPowering = nbttagcompound.getBoolean("isPowering");
			upperBoundary = nbttagcompound.getInteger("upperBoundary");
			lowerBoundary = nbttagcompound.getInteger("lowerBoundary");

			// Our inventory
			NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
			//Redundant: contents = new ItemStack[Info.SM_INVENTORY_SIZE];
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
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		if (!AdvancedPowerManagement.proxy.isClient())
		{
			super.writeToNBT(nbttagcompound);

			// State info to remember
			nbttagcompound.setBoolean("isPowering", isPowering);
			nbttagcompound.setInteger("upperBoundary", upperBoundary);
			nbttagcompound.setInteger("lowerBoundary", lowerBoundary);

			// Our inventory
			NBTTagList nbttaglist = new NBTTagList();
			for (int i = 0; i < contents.length; ++i)
			{
				if (contents[i] != null)
				{
					//if (ChargingBench.isDebugging) System.out.println("WriteNBT contents[" + i + "] stack tag: " + contents[i].stackTagCompound);
					NBTTagCompound nbttagcompound1 = new NBTTagCompound();
					nbttagcompound1.setByte("Slot", (byte)i);
					contents[i].writeToNBT(nbttagcompound1);
					nbttaglist.appendTag(nbttagcompound1);
				}
			}
			nbttagcompound.setTag("Items", nbttaglist);
		}
	}

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	@Override
	public int getGuiID()
	{
		return 4;
	}

	/**
	 * This will cause the block to drop anything inside it, create a new item in the
	 * world of its type, invalidate the tile entity, remove itself from the IC2
	 * EnergyNet and clear the block space (set it to air)
	 */
	private void selfDestroy()
	{
		dropContents();
		ItemStack stack = new ItemStack(AdvancedPowerManagement.blockAdvPwrMan, 1, 11);
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

	@Override
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
	
	public boolean isItemValid(int slot, ItemStack stack)
	{
		// Decide if the item is valid to place in a slot  
		return stack != null && stack.getItem() instanceof ItemStorageLinkCard; 
	}

	/**
	 * Runs once on tile entity load to make sure all of our internals are setup correctly
	 */
	private void onLoad()
	{
		if (!AdvancedPowerManagement.proxy.isClient())
		{
			tileLoaded = true;
			checkInventory();
			if (targetCoords != null)
			{
				TileEntity tile = null;
				if (targetCoords[3] == worldObj.provider.dimensionId)
				{
					tile = worldObj.getBlockTileEntity(targetCoords[0], targetCoords[1], targetCoords[2]);
				}

				if (tile instanceof IEnergyStorage)
				{
					energyStored = ((IEnergyStorage)tile).getStored();
					energyCapacity = ((IEnergyStorage)tile).getCapacity();
					blockState = true;
				}
				else
				{
					energyStored = 0;
					energyCapacity = 0;
					blockState = false;
				}
			}
			chargeLevel = gaugeEnergyScaled(12);

			if (energyCapacity > 0) // Avoid divide by zero and also test if the remote energy storage is valid
			{
				updateRedstone();
			}
			else if (isPowering) // If we're emitting redstone at this point, we need to shut it off
			{
				isPowering = false;
				worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, worldObj.getBlockId(xCoord, yCoord, zCoord));
			}
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	@Override
	public void updateEntity() //TODO Marked for easy access
	{
		if (AdvancedPowerManagement.proxy.isClient()) return;

		if (!tileLoaded)
		{
			onLoad();
		}

		// Delayed work
		if (tickTime > 0)
		{
			tickTime--;
		}
		else
		{
			tickTime = tickDelay;
			if (targetCoords != null)
			{
				TileEntity tile = null;
				if (targetCoords[3] == worldObj.provider.dimensionId)
				{
					tile = worldObj.getBlockTileEntity(targetCoords[0], targetCoords[1], targetCoords[2]);
				}

				if (tile instanceof IEnergyStorage)
				{
					//if (ChargingBench.isDebugging) System.out.println("updateEntity - check energy level of remote block");
					energyStored = ((IEnergyStorage)tile).getStored();
					energyCapacity = ((IEnergyStorage)tile).getCapacity();
					if (!blockState)
					{
						worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
					}
					blockState = true;
				}
				else
				{
					energyStored = 0;
					energyCapacity = 0;
					if (blockState)
					{
						worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
					}
					blockState = false;
				}
			}

			if (energyCapacity > 0) // Avoid divide by zero and also test if the remote energy storage is valid
			{
				updateRedstone();
			}
			else if (isPowering) // If we're emitting redstone at this point, we need to shut it off
			{
				isPowering = false;
				worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, worldObj.getBlockId(xCoord, yCoord, zCoord));
			}

			// Trigger this only when charge level passes where it would need to update the client texture
			int oldChargeLevel = chargeLevel;
			chargeLevel = gaugeEnergyScaled(12);
			if (oldChargeLevel != chargeLevel)
			{
				//if (ChargingBench.isDebugging) System.out.println("TE oldChargeLevel: " + oldChargeLevel + " chargeLevel: " + chargeLevel); 
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}
	}

	private void updateRedstone()
	{
		float chargePercent = ((float)energyStored * 100.0F) / (float)energyCapacity;
		if ((isPowering == false && chargePercent < lowerBoundary) || (isPowering == true && chargePercent >= upperBoundary))
		{
			if (Info.isDebugging) System.out.println("Storage Monitor toggling redstone. chargePercent:" + chargePercent);
			isPowering = !isPowering;
			worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, worldObj.getBlockId(xCoord, yCoord, zCoord));
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	private void checkInventory()
	{
		ItemStack item = getStackInSlot(Info.SM_SLOT_UNIVERSAL);
		if (item == null || !(item.getItem() instanceof ItemStorageLinkCard))
		{
			targetCoords = null;
			energyCapacity = 0;
			energyStored = 0;
			blockState = false;
		}
		else
		{
			targetCoords = ItemCardBase.getCoordinates(item);
			ItemCardBase.setCoordinates(item, targetCoords); // Make sure old cards have a dimension number
		}
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	boolean receivingRedstoneSignal()
	{
		return worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
	}

	public int gaugeEnergyScaled(int gaugeSize)
	{
		if (energyStored <= 0 || energyCapacity <= 0)
		{
			return 0;
		}

		int result = energyStored * gaugeSize / energyCapacity;
		if (result > gaugeSize) result = gaugeSize;

		return result;
	}

	//Networking stuff

	/**
	 * Packet reception by server of what button was clicked on the client's GUI.
	 * @param id = the button ID
	 */
	@Override
	public void receiveGuiButton(int id)
	{
		switch (id)
		{
		case 0:
			upperBoundary -= 10;
			if (upperBoundary < 1) upperBoundary = 1;
			if (upperBoundary < lowerBoundary) lowerBoundary = upperBoundary;
			break;
		case 1:
			upperBoundary -= 1;
			if (upperBoundary < 1) upperBoundary = 1;
			if (upperBoundary < lowerBoundary) lowerBoundary = upperBoundary;
			break;
		case 2:
			upperBoundary += 1;
			if (upperBoundary > 100) upperBoundary = 100;
			break;
		case 3:
			upperBoundary += 10;
			if (upperBoundary == 11) upperBoundary = 10;
			if (upperBoundary > 100) upperBoundary = 100;
			break;
		case 4:
			lowerBoundary -= 10;
			if (lowerBoundary < 1) lowerBoundary = 1;
			break;
		case 5:
			lowerBoundary -= 1;
			if (lowerBoundary < 1) lowerBoundary = 1;
			break;
		case 6:
			lowerBoundary += 1;
			if (lowerBoundary > 100) lowerBoundary = 100;
			if (lowerBoundary > upperBoundary) upperBoundary = lowerBoundary;
			break;
		case 7:
			lowerBoundary += 10;
			if (lowerBoundary == 11) lowerBoundary = 10;
			if (lowerBoundary > 100) lowerBoundary = 100;
			if (lowerBoundary > upperBoundary) upperBoundary = lowerBoundary;
			break;
		}
	}

	@Override
	public Packet250CustomPayload getDescriptionPacket()
	{
		return createDescPacket();
	}

	@Override
	protected void addUniqueDescriptionData(DataOutputStream data) throws IOException
	{
		data.writeInt(chargeLevel);
		data.writeBoolean(isPowering);
		data.writeBoolean(blockState);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void receiveDescriptionData(int packetID, DataInputStream stream)
	{
		final int a;
		final boolean b;
		final boolean c;
		try
		{
			a = stream.readInt();
			b = stream.readBoolean();
			c = stream.readBoolean();
		}
		catch (IOException e)
		{
			logDescPacketError(e);
			return;
		}
		chargeLevel = a;
		isPowering = b;
		blockState = c;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	// ISidedInventory

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
		return Info.SM_SLOT_UNIVERSAL;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		// Each side accesses a single slot
		return 1;
	}

	// IInventory

	@Override
	public int getSizeInventory()
	{
		// Only input/output slots are accessible to machines
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
		if (contents[slot] != null)
		{
			ItemStack output;

			if (contents[slot].stackSize <= amount)
			{
				output = contents[slot];
				contents[slot] = null;
				this.onInventoryChanged(slot);
				return output;
			}
			else
			{
				output = contents[slot].splitStack(amount);

				if (contents[slot].stackSize == 0)
				{
					contents[slot] = null;
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

	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		if (contents[slot] == null)
		{
			return null;
		}

		ItemStack stack = contents[slot];
		contents[slot] = null;
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack)
	{
		contents[slot] = itemstack;

		if (Info.isDebugging && itemstack != null)
		{
			if (AdvancedPowerManagement.proxy.isServer())
			{
				System.out.println("Server assigned stack tag: " + itemstack.stackTagCompound);
				if (itemstack.stackTagCompound != null) System.out.println("     " + itemstack.stackTagCompound.getTags().toString());
			}
			if (AdvancedPowerManagement.proxy.isClient())
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

	@Override
	public void onInventoryChanged(int slot)
	{
		this.onInventoryChanged();
	}

	@Override
	public void onInventoryChanged()
	{
		if (Info.isDebugging) System.out.println("TEStorageMonitor.onInventoryChanged");
		checkInventory();
		super.onInventoryChanged();
	}

	@Override
	public String getInvName()
	{
		return Info.KEY_BLOCK_NAMES[11] + Info.KEY_NAME_SUFFIX;
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
}
