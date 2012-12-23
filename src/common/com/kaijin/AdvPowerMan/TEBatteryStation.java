/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import ic2.api.Direction;
import ic2.api.ElectricItem;
import ic2.api.energy.EnergyNet;
import ic2.api.IElectricItem;
import ic2.api.energy.tile.IEnergySource;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

public class TEBatteryStation extends TECommonBench implements IEnergySource, IInventory, ISidedInventory
{
	public int opMode;

	// Base values
	public int packetSize;
	public int currentEnergy = 0;

	private boolean invChanged = false;
	private boolean hasEnoughItems = false;

	//For outside texture display
	public boolean doingWork;

	private int energyOut = 0;
	public MovingAverage outputTracker = new MovingAverage(10);

	public TEBatteryStation() // Default constructor used only when loading tile entity from world save
	{
		super();
		// Do nothing else; Creating the inventory array and loading previous values will be handled in NBT read method momentarily. 
	}

	public TEBatteryStation(int i) // Constructor used when placing a new tile entity, to set up correct parameters
	{
		super();
		contents = new ItemStack[14];

		//base tier = what we're passed, so 1, 2 or 3
		baseTier = i;
		opMode = 1;
		initializeValues();
	}

	private void initializeValues()
	{
		powerTier = baseTier;
		//Output math = 32 for tier 1, 128 for tier 2, 512 for tier 3
		packetSize = (int)Math.pow(2.0D, (double)(2 * baseTier + 3));
		
	}

	// IC2 API functions

	public boolean isAddedToEnergyNet()
	{
		return initialized;
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, Direction direction)
	{
		return true;
	}

	@Override
	public int getMaxEnergyOutput()
	{
		return packetSize;
	}

	// End IC2 API

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	@Override
	public int getGuiID()
	{
		return 3;
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

	public boolean isItemValid(int slot, ItemStack stack)
	{
		// Decide if the item is a valid IC2 electrical item
		if (stack != null && stack.getItem() instanceof IElectricItem)
		{
			IElectricItem item = (IElectricItem)(stack.getItem());
			// Is the item appropriate for this slot?
			if (slot == Info.BS_SLOT_OUTPUT) return true; // GUI won't allow placement of items here, but if the bench or an external machine does, it should at least let it sit there as long as it's an electrical item.
			if (item.canProvideEnergy() && item.getTier() <= powerTier)
			{
				if ((slot >= Info.BS_SLOT_POWER_START && slot < Info.BS_SLOT_POWER_START + 12) || slot == Info.BS_SLOT_INPUT) return true;
			}
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

			if (Info.isDebugging) System.out.println("BS ID: " + nbttagcompound.getString("id"));

			baseTier = nbttagcompound.getInteger("baseTier");
			opMode = nbttagcompound.getInteger("opMode");
			currentEnergy = nbttagcompound.getInteger("currentEnergy");

			// Our inventory
			contents = new ItemStack[Info.BS_INVENTORY_SIZE];
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
			initializeValues();
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
			nbttagcompound.setInteger("opMode", opMode);
			nbttagcompound.setInteger("currentEnergy", currentEnergy);

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
		if (AdvancedPowerManagement.proxy.isClient()) return;

		if (!initialized && worldObj != null)
		{
			EnergyNet.getForWorld(worldObj).addTileEntity(this);
			initialized = true;
		}

		boolean lastWorkState = doingWork;
		doingWork = false;
		invChanged = false;
		hasEnoughItems = true;

		if (!receivingRedstoneSignal())
		{
			// Work done only when not redstone powered 
			drainPowerSource();
			energyOut = emitEnergy();
		}
		else
		{
			energyOut = 0;
		}
		// Work done every tick
		moveOutputItems();
		repositionItems();
		acceptInputItems();
		outputTracker.tick(energyOut);

		if (invChanged)
		{
			this.onInventoryChanged(); // This doesn't need to be called multiple times, so it gets flagged to happen here if needed.
		}

		// Trigger this only when it would need to update the client texture
		if (lastWorkState != doingWork)
		{
			//if (ChargingBench.isDebugging) System.out.println("TE oldChargeLevel: " + oldChargeLevel + " chargeLevel: " + chargeLevel); 
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	private int emitEnergy()
	{
		//if (ChargingBench.isDebugging) System.out.println("preEmit-currentEnergy: " + currentEnergy);
		if (currentEnergy >= packetSize)
		{
			final int surplus = EnergyNet.getForWorld(worldObj).emitEnergyFrom(this, packetSize);
			if (surplus < packetSize)
			{
				final int sent = packetSize - surplus;
				currentEnergy -= sent;
				doingWork = true;
				return sent; // For average tracker
			}
		}
		return 0;
	}

	private void drainPowerSource()
	{
		hasEnoughItems = false;
		for (int i = Info.BS_SLOT_POWER_START; i < Info.BS_SLOT_POWER_START + 12; i++)
		{
			//if (ChargingBench.isDebugging) System.out.println("currentEnergy: " + currentEnergy + " baseMaxOutput: " + baseMaxOutput);
			if (currentEnergy >= packetSize)
			{
				hasEnoughItems = true;
				break;
			}

			ItemStack stack = contents[i];
			if (stack != null && stack.getItem() instanceof IElectricItem && stack.stackSize == 1)
			{
				IElectricItem item = (IElectricItem)(stack.getItem());
				if (item.getTier() <= powerTier && item.canProvideEnergy())
				{
					int emptyItemID = item.getEmptyItemId();
					int chargedItemID = item.getChargedItemId();

					if (stack.itemID == chargedItemID)
					{
						int transferLimit = item.getTransferLimit();
						//int amountNeeded = baseMaxOutput - currentEnergy;
						if (transferLimit == 0) transferLimit = packetSize;
						//if (transferLimit > amountNeeded) transferLimit = amountNeeded;

						int chargeReturned = ElectricItem.discharge(stack, transferLimit, powerTier, false, false);
						if (chargeReturned > 0)
						{
							// Add the energy we received to our current energy level
							currentEnergy += chargeReturned;
							doingWork = true;
						}

						// Workaround for buggy IC2 API .discharge that automatically switches stack to emptyItemID but leaves a stackTagCompound on it, so it can't be stacked with never-used empties  
						if (chargedItemID != emptyItemID && (chargeReturned < transferLimit || ElectricItem.discharge(stack, 1, powerTier, false, true) == 0))
						{
							//if (ChargingBench.isDebugging) System.out.println("Switching to emptyItemID: " + emptyItemID + " from stack.itemID: " + stack.itemID + " - chargedItemID: " + chargedItemID);
							setInventorySlotContents(i, new ItemStack(emptyItemID, 1, 0));
						}
					}
				}
			}
		}
	}

	/**
	 * First, check the output slot to see if it's empty. If so, look to see if there are any fully 
	 * DIScharged items in the main inventory. Move the first empty item to the output slot.
	 * If output slot contains stackable empties, check for matching empties to add to that stack.
	 */
	private void moveOutputItems()
	{
		rejectInvalidInput();

		ItemStack outputStack = contents[Info.BS_SLOT_OUTPUT];
		if (outputStack == null || (outputStack.isStackable() && outputStack.stackSize < outputStack.getMaxStackSize()))
		{
			// Output slot could receive item(s). Try to find something to move there.
			for (int slot = 0; slot < contents.length; ++slot)
			{
				if (slot == Info.BS_SLOT_OUTPUT) continue;

				ItemStack currentStack = contents[slot];
				if (currentStack != null && currentStack.getItem() instanceof IElectricItem)
				{
					IElectricItem powerSource = (IElectricItem)(currentStack.getItem());
					if (powerSource.getTier() <= powerTier) // && powerSource.canProvideEnergy()
					{
						int emptyItemID = powerSource.getEmptyItemId();
						int chargedItemID = powerSource.getChargedItemId();
						if (emptyItemID != chargedItemID)
						{
							if (currentStack.itemID == emptyItemID)
							{
								// Pick Me
								if (outputStack == null)
								{
									contents[Info.BS_SLOT_OUTPUT] = currentStack;
									contents[slot] = null;
								}
								else
								{
									// We already know the stack isn't full yet
									contents[Info.BS_SLOT_OUTPUT].stackSize++;
									contents[slot].stackSize--;
									if (contents[slot].stackSize < 1) contents[slot] = null;
								}
								invChanged = true;
								break;
							}
						}
						else if (outputStack == null)
						{
							boolean empty = ElectricItem.discharge(currentStack, 1, powerTier, true, true) == 0;
							if (empty)
							{
								// Pick Me
								contents[Info.BS_SLOT_OUTPUT] = currentStack;
								contents[slot] = null;
								invChanged = true;
								break;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Adjust positions of items in inventory to preserve FIFO order where possible.
	 */
	private void repositionItems()
	{
		final int lastIndex = Info.BS_SLOT_POWER_START + 11;
		int vacancy = Info.BS_SLOT_POWER_START;
		while (vacancy < lastIndex && contents[vacancy] != null)
		{
			vacancy++;
		}
		int hunt = vacancy + 1;
		while (vacancy < lastIndex && hunt <= lastIndex) // Mix of < and <= is not an error: Avoids needing +1 or -1 added to something.
		{
			if (contents[vacancy] == null && contents[hunt] != null)
			{
				contents[vacancy] = contents[hunt];
				contents[hunt] = null;
				invChanged = true;
				vacancy++;
			}
			hunt++;
		}
	}

	/**
	 * Check to see if there are any items in the input slot. If so, check to see if there are any
	 * free discharging slots. If so, move one from the input slot to a free discharging slot.
	 */
	private void acceptInputItems()
	{
		//System.out.println("aII: opMode " + opMode);
		ItemStack stack = contents[Info.BS_SLOT_INPUT];
		if (stack == null || !(stack.getItem() instanceof IElectricItem) || (opMode == 1 && hasEnoughItems)) return;

		IElectricItem item = (IElectricItem)stack.getItem();
		if (item.canProvideEnergy())
		{
			// Input slot contains a power source. If possible, move one of it into the discharging area.
			for (int slot = Info.BS_SLOT_POWER_START; slot < Info.BS_SLOT_POWER_START + 12; ++slot)
			{
				if (contents[slot] == null)
				{
					// Grab one unit from input and move it to the selected slot.
					contents[slot] = decrStackSize(Info.BS_SLOT_INPUT, 1);
					break;
				}
			}
		}
	}

	private void rejectInvalidInput()
	{
		// Move item from input to output if not valid. (Wrong tier or not electric item.)
		if (contents[Info.BS_SLOT_INPUT] != null && contents[Info.BS_SLOT_OUTPUT] == null)
		{
			if (!isItemValid(Info.BS_SLOT_INPUT, contents[Info.BS_SLOT_INPUT]))
			{
				contents[Info.BS_SLOT_OUTPUT] = contents[Info.BS_SLOT_INPUT];
				contents[Info.BS_SLOT_INPUT] = null;
				invChanged = true;
			}
		}
	}

	// Add up amount of energy stored in items in all slots except output and return that value
	public int getTotalEnergy()
	{
		int energySum = 0;
		for (int i = 0; i < Info.BS_SLOT_POWER_START + 12; i++)
		{
			if (i == Info.BS_SLOT_OUTPUT) continue;

			final ItemStack stack = contents[i];
			if (stack != null && stack.getItem() instanceof IElectricItem && stack.stackSize == 1)
			{
				final IElectricItem item = (IElectricItem)(stack.getItem());
				if (item.getTier() <= powerTier && item.canProvideEnergy() && stack.itemID == item.getChargedItemId())
				{
					final int chargeReturned = ElectricItem.discharge(stack, Integer.MAX_VALUE, powerTier, true, true);
					if (chargeReturned > 0)
					{
						// Add the energy we received to our current energy level
						energySum += chargeReturned;
					}
				}
			}
		}
		return energySum;
	}

	//Networking stuff

	@Override
	public Packet250CustomPayload getDescriptionPacket()
	{
		return createDescPacket();
	}

	@Override
	protected void addUniqueDescriptionData(DataOutputStream data) throws IOException
	{
		data.writeBoolean(doingWork);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void receiveDescriptionData(int packetID, DataInputStream stream)
	{
		final boolean b;
		try
		{
			b = stream.readBoolean();
		}
		catch (IOException e)
		{
			logDescPacketError(e);
			return;
		}
		doingWork = b;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public void receiveGuiButton(int buttonID)
	{
		if (buttonID == 0)
		{
			opMode ^= 1;
		}
	}

	// ISidedInventory

	@Override
	public int getStartInventorySide(ForgeDirection side)
	{
		switch (side)
		{
		case UP:
		case DOWN:
			return Info.BS_SLOT_INPUT;
		default:
			return Info.BS_SLOT_OUTPUT;
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
		return 2;
	}

	@Override
	public String getInvName()
	{
		switch (baseTier)
		{
		case 1:
			return Info.KEY_BLOCK_NAMES[8] + Info.KEY_NAME_SUFFIX;
		case 2:
			return Info.KEY_BLOCK_NAMES[9] + Info.KEY_NAME_SUFFIX;
		case 3:
			return Info.KEY_BLOCK_NAMES[10] + Info.KEY_NAME_SUFFIX;
		}
		return "";
	}

	@Override
	public void onInventoryChanged(int slot)
	{
		if (slot == Info.BS_SLOT_INPUT || slot == Info.BS_SLOT_OUTPUT)
		{
			rejectInvalidInput();
		}
		super.onInventoryChanged();
	}
}
