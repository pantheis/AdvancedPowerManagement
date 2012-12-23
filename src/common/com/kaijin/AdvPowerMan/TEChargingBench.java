/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import ic2.api.Direction;
import ic2.api.ElectricItem;
import ic2.api.energy.EnergyNet;
import ic2.api.IElectricItem;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.IEnergyStorage;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

public class TEChargingBench extends TECommonBench implements IEnergySink, IEnergyStorage, IInventory, ISidedInventory
{
	// Base values
	public int baseMaxInput;
	public int baseStorage;

	// Adjustable values that need communicating via container
	public int adjustedMaxInput;
	public int adjustedStorage;

	public int currentEnergy;
	//For outside texture display
	protected int chargeLevel;

	public float drainFactor;
	public float chargeFactor;

	public TEChargingBench() // Default constructor used only when loading tile entity from world save
	{
		super();
		// Do nothing else; Creating the inventory array and loading previous values will be handled in NBT read method momentarily.
	}
	
	public TEChargingBench(int i) // Constructor used when placing a new tile entity, to set up correct parameters
	{
		super();
		contents = new ItemStack[19];

		//base tier = what we're passed, so 1, 2 or 3
		baseTier = i;
		initializeBaseValues();

		//setup Adjusted variables to = defaults, we'll be adjusting them in entityUpdate
		adjustedMaxInput = baseMaxInput;
		adjustedStorage = baseStorage;

		powerTier = baseTier;

		drainFactor = 1.0F;
		chargeFactor = 1.0F;
	}

	protected void initializeBaseValues()
	{
		//if (ChargingBench.isDebugging) System.out.println("Initializing - BaseTier: " + baseTier);

		//Max Input math = 32 for tier 1, 128 for tier 2, 512 for tier 3
		baseMaxInput = (int)Math.pow(2.0D, (double)(2 * baseTier + 3));
		//if (ChargingBench.isDebugging) System.out.println("BaseMaxInput: " + baseMaxInput);

		switch(baseTier)
		{
		case 1:
			baseStorage = 40000;
			break;
		case 2:
			baseStorage = 600000;
			break;
		case 3:
			baseStorage = 10000000;
			break;
		default:
			baseStorage = 0;
		}
		//if (ChargingBench.isDebugging) System.out.println("BaseStorage: " + baseStorage);
	}

	/**
	 * Called to upgrade (or downgrade) a charging bench to a certain tier.
	 * @param newTier The tier to replace the charging bench with, based on the component item used
	 * @return the original tier of the charging bench, for creating the correct component item
	 */
	public int swapBenchComponents(int newTier)
	{
		int oldTier = baseTier;
		baseTier = newTier;
		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, newTier - 1);
		initializeBaseValues();
		doUpgradeEffects();
		chargeLevel = gaugeEnergyScaled(12);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		return oldTier;
	}

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	// IC2 API stuff

	// IEnergySink
	
	@Override
	public void setStored(int energy)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public int addEnergy(int amount)
	{
		// TODO Auto-generated method stub
		// Returning our current energy value always, we do not implement this function
		return currentEnergy;
	}

	@Override
	public boolean isTeleporterCompatible(Direction side)
	{
		return false;
	}

	@Override
	public int getMaxSafeInput() {
		// TODO Auto-generated method stub
		return adjustedMaxInput;
	}
	
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction)
	{
		return true;
	}

	@Override
	public int demandsEnergy()
	{
//		return (currentEnergy < adjustedStorage && !receivingRedstoneSignal());
		if(!receivingRedstoneSignal())
		{
			return adjustedStorage - currentEnergy;
		}
		return 0;
	}

	@Override
	public int injectEnergy(Direction directionFrom, int supply)
	{
		int surplus = 0;
		if (AdvancedPowerManagement.proxy.isServer())
		{
			// if supply is greater than the max we can take per tick
			if (supply > adjustedMaxInput)
			{
				//If the supplied EU is over the baseMaxInput, we're getting
				//supplied higher than acceptable current. Pop ourselves off
				//into the world and return all but 1 EU, or if the supply
				//somehow was 1EU, return zero to keep IC2 from spitting out 
				//massive errors in the log
				selfDestroy();
				if (supply <= 1)
					return 0;
				else
					return supply - 1;
			}
			else
			{
				currentEnergy += supply;
				// check if our current energy level is now over the max energy level
				if (currentEnergy > adjustedStorage)
				{
					//if so, our surplus to return is equal to that amount over
					surplus = currentEnergy - adjustedStorage;
					//and set our current energy level TO our max energy level
					currentEnergy = adjustedStorage;
				}
				//surplus may be zero or greater here
			}
		}
		return surplus;
	}

	// IEnergyStorage

	/**
	 * Get the amount of energy currently stored in the block.
	 * 
	 * @return Energy stored in the block
	 */
	@Override
	public int getStored()
	{
		return currentEnergy;
	}
	
	/**
	 * Get the maximum amount of energy the block can store.
	 * 
	 * @return Maximum energy stored
	 */
	@Override
	public int getCapacity()
	{
		return adjustedStorage;
	}
	
	/**
	 * Get the block's energy output.
	 * 
	 * @return Energy output in EU/t
	 */
	@Override
	public int getOutput()
	{
		return 0;
	}

	// End IC2 API

	@Override
	public int getGuiID()
	{
		return 1;
	}

	/**
	 * This will cause the block to drop anything inside it, create a new item in the
	 * world of its type, invalidate the tile entity, remove itself from the IC2
	 * EnergyNet and clear the block space (set it to air)
	 */
	@Override
	protected void selfDestroy()
	{
		dropContents();
		ItemStack stack = new ItemStack(AdvancedPowerManagement.blockAdvPwrMan, 1, baseTier - 1);
		dropItem(stack);
		worldObj.setBlockAndMetadataWithNotify(xCoord, yCoord, zCoord, 0, 0);
		this.invalidate();
	}

	public void doUpgradeEffects()
	{
		// Count our upgrades
		ItemStack stack;
		int ocCount = 0;
		int tfCount = 0;
		int esCount = 0;
		for (int i = Info.CB_SLOT_UPGRADE; i < Info.CB_SLOT_UPGRADE + 4; ++i)
		{
			stack = contents[i];
			if (stack != null)
			{
				if (stack.isItemEqual(Info.ic2overclockerUpg))
				{
					ocCount += stack.stackSize;
				}
				else if (stack.isItemEqual(Info.ic2storageUpg))
				{
					esCount += stack.stackSize;
				}
				else if (stack.isItemEqual(Info.ic2transformerUpg))
				{
					tfCount += stack.stackSize;
				}
			}
		}

		// Cap upgrades at sane quantities that won't result in negative energy storage from integer overflows and such.
		if (ocCount > 64) ocCount = 64;
		if (esCount > 64) esCount = 64;
		if (tfCount > 3) tfCount = 3;

		// Overclockers:
		chargeFactor = (float)Math.pow(1.3F, ocCount); // 30% more power transferred to an item per overclocker, exponential.
		drainFactor = (float)Math.pow(1.5F, ocCount); // 50% more power drained per overclocker, exponential. Yes, you waste power, that's how OCs work.

		// Transformers:
		powerTier = baseTier + tfCount; // Allows better energy storage items to be plugged into the battery slot of lower tier benches.
		if (powerTier > 3) powerTier = 3;

		adjustedMaxInput = (int)Math.pow(2.0D, (double)(2 * (baseTier + tfCount) + 3));
		if (adjustedMaxInput > 2048) adjustedMaxInput = 2048; // You can feed EV in with 1-4 TF upgrades, if you so desire.

		// Energy Storage:
		switch (baseTier)
		{
		case 1:
			adjustedStorage = baseStorage + esCount * 10000; // LV: 25% additional storage per upgrade (10,000).
			break;
		case 2:
			adjustedStorage = baseStorage + esCount * 60000; // MV: 10% additional storage per upgrade (60,000).
			break;
		case 3:
			adjustedStorage = baseStorage + esCount * 500000; // HV: 5% additional storage per upgrade (500,000).
			break;
		default:
			adjustedStorage = baseStorage; // This shouldn't ever happen, but just in case, it shouldn't crash it - storage upgrades just won't work.
		}
		if (currentEnergy > adjustedStorage) currentEnergy = adjustedStorage; // If storage has decreased, lose any excess energy.
	}

	public boolean isItemValid(int slot, ItemStack stack)
	{
		// Decide if the item is a valid IC2 electrical item
		if (stack != null && stack.getItem() instanceof IElectricItem)
		{
			IElectricItem item = (IElectricItem)(stack.getItem());
			// Is the item appropriate for this slot?
			if (slot == Info.CB_SLOT_POWER_SOURCE && item.canProvideEnergy() && item.getTier() <= powerTier) return true;
			if (slot >= Info.CB_SLOT_CHARGING && slot < Info.CB_SLOT_CHARGING + 12 && item.getTier() <= baseTier) return true;
			if (slot >= Info.CB_SLOT_UPGRADE && slot < Info.CB_SLOT_UPGRADE + 4 && (stack.isItemEqual(Info.ic2overclockerUpg) || stack.isItemEqual(Info.ic2transformerUpg) || stack.isItemEqual(Info.ic2storageUpg))) return true;
			if (slot == Info.CB_SLOT_INPUT && item.getTier() <= baseTier) return true;
			if (slot == Info.CB_SLOT_OUTPUT) return true; // GUI won't allow placement of items here, but if the bench or an external machine does, it should at least let it sit there as long as it's an electrical item.
		}
		return false; 
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

			if (Info.isDebugging) System.out.println("CB ID: " + nbttagcompound.getString("id"));

			baseTier = nbttagcompound.getInteger("baseTier");
			currentEnergy = nbttagcompound.getInteger("currentEnergy");
			//if (ChargingBench.isDebugging) System.out.println("ReadNBT.CurrentEergy: " + currentEnergy);

			// Our inventory
			contents = new ItemStack[Info.CB_INVENTORY_SIZE];
			NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
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
			initializeBaseValues();
			doUpgradeEffects();
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

			nbttagcompound.setInteger("baseTier", baseTier);
			nbttagcompound.setInteger("currentEnergy", currentEnergy);
			//if (ChargingBench.isDebugging) System.out.println("WriteNBT.CurrentEergy: " + currentEnergy);

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
	public void updateEntity() //TODO Marked for easy access
	{
		if (AdvancedPowerManagement.proxy.isClient())
		{
			return;
		}

		if (!initialized && worldObj != null)
		{
			EnergyNet.getForWorld(worldObj).addTileEntity(this);
			initialized = true;
		}

		boolean lastWorkState = doingWork;
		doingWork = false;

		// Work done every tick
		drainPowerSource();
		chargeItems();
		moveOutputItems();
		acceptInputItems();

		// Trigger this only when charge level passes where it would need to update the client texture
		int oldChargeLevel = chargeLevel;
		chargeLevel = gaugeEnergyScaled(12);
		if (oldChargeLevel != chargeLevel || lastWorkState != doingWork)
		{
			//if (ChargingBench.isDebugging) System.out.println("TE oldChargeLevel: " + oldChargeLevel + " chargeLevel: " + chargeLevel); 
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	/**
	 * Looks in the power item slot to see if it can pull in EU from a valid item in that slot.
	 * If so, pull in as much EU as the item allows to be transferred per tick up to the maximum
	 * energy transfer rate based on our tier, limited also by the maximum energy storage capacity.
	 * ie. do not pull in more than we have room for
	 * @return 
	 */
	private void drainPowerSource()
	{
		int chargeReturned = 0;

		ItemStack stack = getStackInSlot(Info.CB_SLOT_POWER_SOURCE);
		if (stack != null && stack.getItem() instanceof IElectricItem && currentEnergy < adjustedStorage)
		{
			IElectricItem powerSource = (IElectricItem)(stack.getItem());

			int emptyItemID = powerSource.getEmptyItemId();
			int chargedItemID = powerSource.getChargedItemId();

			if (stack.itemID == chargedItemID)
			{
				if (powerSource.getTier() <= powerTier && powerSource.canProvideEnergy())
				{
					int itemTransferLimit = powerSource.getTransferLimit();
					int energyNeeded = adjustedStorage - currentEnergy;

					// Test if the amount of energy we have room for is greater than what the item can transfer per tick.
					if (energyNeeded > itemTransferLimit)
					{
						// If so, request the max it can transfer per tick.
						energyNeeded = itemTransferLimit;
						// If we need less than it can transfer per tick, request only what we have room for so we don't waste power.
					}

					if (energyNeeded > 0)
					{
						chargeReturned = ElectricItem.discharge(stack, energyNeeded, powerTier, false, false);
						// Add the energy we received to our current energy level,
						currentEnergy += chargeReturned;
						if (chargeReturned > 0) doingWork = true;
						// and make sure that we didn't go over. If we somehow did, drop the excess.
						if (currentEnergy > adjustedStorage) currentEnergy = adjustedStorage;
					}
				}

				// Workaround for buggy IC2 API .discharge that automatically switches stack to emptyItemID but leaves a stackTagCompound on it, so it can't be stacked with never-used empties  
				if (chargedItemID != emptyItemID && ElectricItem.discharge(stack, 1, powerTier, false, true) == 0)
				{
					//if (ChargingBench.isDebugging) System.out.println("Switching to emptyItemID: " + emptyItemID + " from stack.itemID: " + stack.itemID + " - chargedItemID: " + chargedItemID);
					setInventorySlotContents(Info.CB_SLOT_POWER_SOURCE, new ItemStack(emptyItemID, 1, 0));
					//ItemStack newStack = new ItemStack(emptyItemID, 1, 0);
					//contents[ChargingBench.slotPowerSource] = newStack;
				}
			}
		}
	}

	/**
	 * Look through all of the items in our main inventory and determine the current charge level,
	 * maximum charge level and maximum base charge rate for each item. Increase maximum charge
	 * rate for each item based on overclockers as appropriate, then, starting with the first slot
	 * in the main inventory, transfer one tick worth of energy from our internal storage to the
	 * item. Continue doing this for all items in the inventory until we reach the end of the main
	 * inventory or run out of internal EU storage.
	 */
	private void chargeItems()
	{
		for (int i = Info.CB_SLOT_CHARGING; i < Info.CB_SLOT_CHARGING + 12; i++)
		{
			ItemStack stack = contents[i];
			if (currentEnergy > 0 && stack != null && stack.getItem() instanceof IElectricItem && stack.stackSize == 1)
			{
				IElectricItem item = (IElectricItem)(stack.getItem());
				if (item.getTier() <= baseTier)
				{
					int itemTransferLimit = item.getTransferLimit();
					if (itemTransferLimit == 0) itemTransferLimit = baseMaxInput;
					int adjustedTransferLimit = (int)Math.ceil(chargeFactor * itemTransferLimit);

					int amountNeeded;
					if (item.getChargedItemId() != item.getEmptyItemId() || stack.isStackable())
					{
						// Running stack.copy() on every item every tick would be a horrible thing for performance, but the workaround is needed
						// for ElectricItem.charge adding stackTagCompounds for charge level to EmptyItemID batteries even when run in simulate mode.
						// Limiting its use by what is hopefully a broad enough test to catch all cases where it's necessary in order to avoid problems.
						// Using it for any item types listed as stackable and for any items where the charged and empty item IDs differ.
						amountNeeded = ElectricItem.charge(stack.copy(), adjustedTransferLimit, powerTier, true, true);
					}
					else
					{
						amountNeeded = ElectricItem.charge(stack, adjustedTransferLimit, powerTier, true, true);
					}

					int adjustedEnergyUse = (int)Math.ceil((drainFactor / chargeFactor) * amountNeeded);
					if (adjustedEnergyUse > 0)
					{
						if (adjustedEnergyUse > currentEnergy)
						{
							// Allow that last trickle of energy to be transferred out of the bench 
							adjustedTransferLimit = (adjustedTransferLimit * currentEnergy) / adjustedEnergyUse;
							adjustedEnergyUse = currentEnergy;
						}
						// We don't need to do this with the current API, it's switching the ItemID for us. Just make sure we don't try to charge stacked batteries, as mentioned above!
						//int chargedItemID = item.getChargedItemId();
						//if (stack.itemID != chargedItemID)
						//{
						//	setInventorySlotContents(i, new ItemStack(chargedItemID, 1, 0));
						//}
						ElectricItem.charge(contents[i], adjustedTransferLimit, powerTier, true, false);
						currentEnergy -= adjustedEnergyUse;
						if (currentEnergy < 0) currentEnergy = 0;
						doingWork = true;
					}
				}
			}
		}
	}

	/**
	 * First, check the output slot to see if it's empty. If so, look to see if there are any fully 
	 * charged items in the main inventory. Move the first fully charged item to the output slot.
	 */
	private void moveOutputItems()
	{
		ItemStack stack = contents[Info.CB_SLOT_OUTPUT];
		if (stack == null)
		{
			// Output slot is empty. Try to find a fully charged item to move there.
			for (int slot = Info.CB_SLOT_CHARGING; slot < Info.CB_SLOT_CHARGING + 12; ++slot)
			{
				ItemStack currentStack = contents[slot];
				if (currentStack != null && currentStack.getItem() instanceof IElectricItem)
				{
					// Test if the item is fully charged (cannot accept any more power).
					if (ElectricItem.charge(currentStack.copy(), 1, baseTier, false, true) == 0)
					{
						contents[Info.CB_SLOT_OUTPUT] = currentStack;
						contents[slot] = null;
						this.onInventoryChanged();
						break;
					}
				}
			}
		}
	}

	/**
	 * Check to see if there are any items in the input slot. If so, check to see if there are any
	 * free charging slots. If so, move one from the input slot to a free charging slot. Do not
	 * move more than one, if the stack contains more.
	 */
	private void acceptInputItems()
	{
		ItemStack stack = contents[Info.CB_SLOT_INPUT];
		if (stack != null && stack.getItem() instanceof IElectricItem)
		{
			// Input slot contains something electrical. If possible, move one of it into the charging area.
			IElectricItem item = (IElectricItem)(stack.getItem());
			for (int slot = Info.CB_SLOT_CHARGING; slot < Info.CB_SLOT_CHARGING + 12; ++slot)
			{
				if (contents[slot] == null)
				{
					// Grab one unit from input and move it to the selected slot.
					contents[slot] = decrStackSize(Info.CB_SLOT_INPUT, 1);
					break;
				}
			}
		}
	}

	public int gaugeEnergyScaled(int gaugeSize)
	{
		if (currentEnergy <= 0)
		{
			return 0;
		}

		int result = currentEnergy * gaugeSize / adjustedStorage;
		if (result > gaugeSize) result = gaugeSize;

		return result;
	}

	//Networking stuff

	@SideOnly(Side.CLIENT)
	@Override
	public void receiveDescriptionData(int packetID, DataInputStream stream)
	{
		final int a;
		final boolean b;
		try
		{
			a = stream.readInt();
			b = stream.readBoolean();
		}
		catch (IOException e)
		{
			logDescPacketError(e);
			return;
		}
		chargeLevel = a;
		doingWork = b;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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
		data.writeBoolean(doingWork);
	}

	// ISidedInventory

	@Override
	public int getStartInventorySide(ForgeDirection side)
	{
		switch (side)
		{
		case UP:
			return Info.CB_SLOT_INPUT;
		case DOWN:
			return Info.CB_SLOT_OUTPUT;
		default:
			return Info.CB_SLOT_POWER_SOURCE;
		}
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
		return 3;
	}

	@Override
	public String getInvName()
	{
		switch (baseTier)
		{
		case 1:
			return Info.KEY_BLOCK_NAMES[0] + Info.KEY_NAME_SUFFIX;
		case 2:
			return Info.KEY_BLOCK_NAMES[1] + Info.KEY_NAME_SUFFIX;
		case 3:
			return Info.KEY_BLOCK_NAMES[2] + Info.KEY_NAME_SUFFIX;
		}
		return "";
	}

	@Override
	public void onInventoryChanged(int slot)
	{
		if (slot == Info.CB_SLOT_INPUT || slot == Info.CB_SLOT_OUTPUT)
		{
			// Move item from input to output if not valid. (Wrong tier or not electric item.)
			if (contents[Info.CB_SLOT_INPUT] != null && contents[Info.CB_SLOT_OUTPUT] == null)
			{
				if (!isItemValid(Info.CB_SLOT_INPUT, contents[Info.CB_SLOT_INPUT]))
				{
					contents[Info.CB_SLOT_OUTPUT] = contents[Info.CB_SLOT_INPUT];
					contents[Info.CB_SLOT_INPUT] = null;
				}
			}
		}
		else if (slot >= Info.CB_SLOT_UPGRADE && slot < Info.CB_SLOT_UPGRADE + 4)
		{
			// One of the upgrade slots was touched, so we need to recalculate.
			doUpgradeEffects();
		}
		else if (slot >= Info.CB_SLOT_CHARGING && slot < Info.CB_SLOT_CHARGING + 12)
		{
			// Make sure it's not fully charged already? Not sure, full items will be output in updateEntity

		}
		else if (slot == Info.CB_SLOT_POWER_SOURCE)
		{
			// Perhaps eject the item if it's not valid? No, just leave it alone. 
			// If machinery added it the player can figure out the problem by trying to remove and replace it and realizing it won't fit.
		}
		super.onInventoryChanged();
	}

	@Override
	public void onInventoryChanged()
	{
		// We're not sure what called this or what slot was altered, so make sure the upgrade effects are correct just in case and then pass the call on.
		doUpgradeEffects();
		super.onInventoryChanged();
	}
}
