package com.kaijin.ChargingBench;

import ic2.api.Direction;
import ic2.api.ElectricItem;
import ic2.api.EnergyNet;
import ic2.api.IElectricItem;
import ic2.api.IEnergySink;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

public class TEChargingBench extends TECommonBench implements IEnergySink, IInventory, ISidedInventory
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

	public TEChargingBench(int i)
	{
		this.contents = new ItemStack[19];

		//base tier = what we're passed, so 1, 2 or 3
		this.baseTier = i;
		initializeBaseValues();

		//setup Adjusted variables to = defaults, we'll be adjusting them in entityUpdate
		this.adjustedMaxInput = this.baseMaxInput;
		this.adjustedStorage = this.baseStorage;

		this.powerTier = this.baseTier;

		this.drainFactor = 1.0F;
		this.chargeFactor = 1.0F;
	}

	protected void initializeBaseValues()
	{
		if (Utils.isDebug()) System.out.println("Initializing - BaseTier: " + this.baseTier);

		//Max Input math = 32 for tier 1, 128 for tier 2, 512 for tier 3
		this.baseMaxInput = (int)Math.pow(2.0D, (double)(2 * this.baseTier + 3));
		if (Utils.isDebug()) System.out.println("BaseMaxInput: " + this.baseMaxInput);

		switch(baseTier)
		{
		case 1:
			this.baseStorage = 40000;
			break;
		case 2:
			this.baseStorage = 600000;
			break;
		case 3:
			this.baseStorage = 10000000;
			break;
		default:
			this.baseStorage = 0;
		}
		if (Utils.isDebug()) System.out.println("BaseStorage: " + this.baseStorage);
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
		this.chargeLevel = gaugeEnergyScaled(12);
		worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
		return oldTier;
	}

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction)
	{
		return true;
	}

	public boolean demandsEnergy()
	{
		return (this.currentEnergy < this.adjustedStorage && !receivingRedstoneSignal());
	}

	public int injectEnergy(Direction directionFrom, int supply)
	{
		int surplus = 0;
		if (ChargingBench.proxy.isServer())
		{
			// if supply is greater than the max we can take per tick
			if(supply > adjustedMaxInput)
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

	/**
	 * This will cause the block to drop anything inside it, create a new item in the
	 * world of its type, invalidate the tile entity, remove itself from the IC2
	 * EnergyNet and clear the block space (set it to air)
	 */
	@Override
	protected void selfDestroy()
	{
		dropContents();
		ItemStack stack = new ItemStack(ChargingBench.ChargingBench, 1, this.baseTier - 1);
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
		for (int i = ChargingBench.CBslotUpgrade; i < ChargingBench.CBslotUpgrade + 4; ++i)
		{
			stack = this.contents[i];
			if (stack != null)
			{
				if (stack.isItemEqual(ChargingBench.ic2overclockerUpg))
				{
					ocCount += stack.stackSize;
				}
				else if (stack.isItemEqual(ChargingBench.ic2storageUpg))
				{
					esCount += stack.stackSize;
				}
				else if (stack.isItemEqual(ChargingBench.ic2transformerUpg))
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
		this.chargeFactor = (float)Math.pow(1.3F, ocCount); // 30% more power transferred to an item per overclocker, exponential.
		this.drainFactor = (float)Math.pow(1.5F, ocCount); // 50% more power drained per overclocker, exponential. Yes, you waste power, that's how OCs work.

		// Transformers:
		this.powerTier = this.baseTier + tfCount; // Allows better energy storage items to be plugged into the battery slot of lower tier benches.
		if (this.powerTier > 3) this.powerTier = 3;

		this.adjustedMaxInput = (int)Math.pow(2.0D, (double)(2 * (this.baseTier + tfCount) + 3));
		if (this.adjustedMaxInput > 2048) this.adjustedMaxInput = 2048; // You can feed EV in with 1-4 TF upgrades, if you so desire.

		// Energy Storage:
		switch (this.baseTier)
		{
		case 1:
			this.adjustedStorage = this.baseStorage + esCount * 10000; // LV: 25% additional storage per upgrade (10,000).
			break;
		case 2:
			this.adjustedStorage = this.baseStorage + esCount * 60000; // MV: 10% additional storage per upgrade (60,000).
			break;
		case 3:
			this.adjustedStorage = this.baseStorage + esCount * 500000; // HV: 5% additional storage per upgrade (500,000).
			break;
		default:
			this.adjustedStorage = this.baseStorage; // This shouldn't ever happen, but just in case, it shouldn't crash it - storage upgrades just won't work.
		}
		if (this.currentEnergy > this.adjustedStorage) this.currentEnergy = this.adjustedStorage; // If storage has decreased, lose any excess energy.
	}

	public boolean isItemValid(int slot, ItemStack stack)
	{
		// Decide if the item is a valid IC2 electrical item
		if (stack != null && stack.getItem() instanceof IElectricItem)
		{
			IElectricItem item = (IElectricItem)(stack.getItem());
			// Is the item appropriate for this slot?
			if (slot == ChargingBench.CBslotPowerSource && item.canProvideEnergy() && item.getTier() <= this.powerTier) return true;
			if (slot >= ChargingBench.CBslotCharging && slot < ChargingBench.CBslotCharging + 12 && item.getTier() <= baseTier) return true;
			if (slot >= ChargingBench.CBslotUpgrade && slot < ChargingBench.CBslotUpgrade + 4 && (stack.isItemEqual(ChargingBench.ic2overclockerUpg) || stack.isItemEqual(ChargingBench.ic2transformerUpg) || stack.isItemEqual(ChargingBench.ic2storageUpg))) return true;
			if (slot == ChargingBench.CBslotInput && item.getTier() <= baseTier) return true;
			if (slot == ChargingBench.CBslotOutput) return true; // GUI won't allow placement of items here, but if the bench or an external machine does, it should at least let it sit there as long as it's an electrical item.
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
			currentEnergy = nbttagcompound.getInteger("currentEnergy");
			if (Utils.isDebug()) System.out.println("ReadNBT.CurrentEergy: " + this.currentEnergy);
			baseMaxInput = nbttagcompound.getInteger("maxInput");
			baseStorage = nbttagcompound.getInteger("baseStorage");
			baseTier = nbttagcompound.getInteger("baseTier");

			NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
			contents = new ItemStack[ChargingBench.CBinventorySize];

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
			doUpgradeEffects();
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
			nbttagcompound.setInteger("currentEnergy", currentEnergy);
			//if (Utils.isDebug()) System.out.println("WriteNBT.CurrentEergy: " + this.currentEnergy);
			nbttagcompound.setInteger("maxInput", baseMaxInput);
			nbttagcompound.setInteger("baseStorage", baseStorage);
			nbttagcompound.setInteger("baseTier", baseTier);
		}
	}

	@Override
	public void updateEntity() //TODO Marked for easy access
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

		boolean lastWorkState = this.doingWork;
		this.doingWork = false;

		// Work done every tick
		drainPowerSource();
		chargeItems();
		moveOutputItems();
		acceptInputItems();

		// Trigger this only when charge level passes where it would need to update the client texture
		int oldChargeLevel = this.chargeLevel;
		this.chargeLevel = gaugeEnergyScaled(12);
		if (oldChargeLevel != this.chargeLevel || lastWorkState != this.doingWork)
		{
			//if (Utils.isDebug()) System.out.println("TE oldChargeLevel: " + oldChargeLevel + " chargeLevel: " + this.chargeLevel); 
			worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
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

		ItemStack stack = getStackInSlot(ChargingBench.CBslotPowerSource);
		if (stack != null && stack.getItem() instanceof IElectricItem && this.currentEnergy < this.adjustedStorage)
		{
			IElectricItem powerSource = (IElectricItem)(stack.getItem());

			int emptyItemID = powerSource.getEmptyItemId();
			int chargedItemID = powerSource.getChargedItemId();

			if (stack.itemID == chargedItemID)
			{
				if (powerSource.getTier() <= this.powerTier && powerSource.canProvideEnergy())
				{
					int itemTransferLimit = powerSource.getTransferLimit();
					int energyNeeded = this.adjustedStorage - this.currentEnergy;

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
						this.currentEnergy += chargeReturned;
						if (chargeReturned > 0) this.doingWork = true;
						// and make sure that we didn't go over. If we somehow did, drop the excess.
						if (this.currentEnergy > this.adjustedStorage) this.currentEnergy = this.adjustedStorage;
					}
				}

				// Workaround for buggy IC2 API .discharge that automatically switches stack to emptyItemID but leaves a stackTagCompound on it, so it can't be stacked with never-used empties  
				if (chargedItemID != emptyItemID && ElectricItem.discharge(stack, 1, powerTier, false, true) == 0)
				{
					if (Utils.isDebug()) System.out.println("Switching to emptyItemID: " + emptyItemID + " from stack.itemID: " + stack.itemID + " - chargedItemID: " + chargedItemID);
					setInventorySlotContents(ChargingBench.CBslotPowerSource, new ItemStack(emptyItemID, 1, 0));
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
		for (int i = ChargingBench.CBslotCharging; i < ChargingBench.CBslotCharging + 12; i++)
		{
			ItemStack stack = this.contents[i];
			if (this.currentEnergy > 0 && stack != null && stack.getItem() instanceof IElectricItem && stack.stackSize == 1)
			{
				IElectricItem item = (IElectricItem)(stack.getItem());
				if (item.getTier() <= this.baseTier)
				{
					int itemTransferLimit = item.getTransferLimit();
					if (itemTransferLimit == 0) itemTransferLimit = this.baseMaxInput;
					int adjustedTransferLimit = (int)Math.ceil(this.chargeFactor * itemTransferLimit);

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

					int adjustedEnergyUse = (int)Math.ceil((this.drainFactor / this.chargeFactor) * amountNeeded);
					if (adjustedEnergyUse > 0)
					{
						if (adjustedEnergyUse > this.currentEnergy)
						{
							// Allow that last trickle of energy to be transferred out of the bench 
							adjustedTransferLimit = (adjustedTransferLimit * this.currentEnergy) / adjustedEnergyUse;
							adjustedEnergyUse = this.currentEnergy;
						}
						// We don't need to do this with the current API, it's switching the ItemID for us. Just make sure we don't try to charge stacked batteries, as mentioned above!
						//int chargedItemID = item.getChargedItemId();
						//if (stack.itemID != chargedItemID)
						//{
						//	setInventorySlotContents(i, new ItemStack(chargedItemID, 1, 0));
						//}
						ElectricItem.charge(this.contents[i], adjustedTransferLimit, powerTier, true, false);
						this.currentEnergy -= adjustedEnergyUse;
						if (this.currentEnergy < 0) this.currentEnergy = 0;
						this.doingWork = true;
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
		ItemStack stack = contents[ChargingBench.CBslotOutput];
		if (stack == null)
		{
			// Output slot is empty. Try to find a fully charged item to move there.
			for (int slot = ChargingBench.CBslotCharging; slot < ChargingBench.CBslotCharging + 12; ++slot)
			{
				ItemStack currentStack = contents[slot];
				if (currentStack != null && currentStack.getItem() instanceof IElectricItem)
				{
					// Test if the item is fully charged (cannot accept any more power).
					if (ElectricItem.charge(currentStack.copy(), 1, baseTier, false, true) == 0)
					{
						contents[ChargingBench.CBslotOutput] = currentStack;
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
		ItemStack stack = contents[ChargingBench.CBslotInput];
		if (stack != null && stack.getItem() instanceof IElectricItem)
		{
			// Input slot contains something electrical. If possible, move one of it into the charging area.
			IElectricItem item = (IElectricItem)(stack.getItem());
			for (int slot = ChargingBench.CBslotCharging; slot < ChargingBench.CBslotCharging + 12; ++slot)
			{
				if (contents[slot] == null)
				{
					// Grab one unit from input and move it to the selected slot.
					contents[slot] = decrStackSize(ChargingBench.CBslotInput, 1);
					break;
				}
			}
		}
	}

	public int gaugeEnergyScaled(int gaugeSize)
	{
		if (this.currentEnergy <= 0)
		{
			return 0;
		}

		int result = this.currentEnergy * gaugeSize / this.adjustedStorage;
		if (result > gaugeSize) result = gaugeSize;

		return result;
	}

	//Networking stuff
	@Override
	public Packet250CustomPayload getDescriptionPacket()
	{
		//if (Utils.isDebug()) System.out.println("TE getAuxillaryInfoPacket()");
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);
		try
		{
			data.writeInt(0);
			data.writeInt(this.xCoord);
			data.writeInt(this.yCoord);
			data.writeInt(this.zCoord);
			data.writeInt(this.chargeLevel);
			data.writeBoolean(this.doingWork);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "ChargingBench"; // CHANNEL MAX 16 CHARS
		packet.data = bytes.toByteArray();
		packet.length = packet.data.length;
		return packet;
	}

	// ISidedInventory

	@Override
	public int getStartInventorySide(ForgeDirection side)
	{
		switch (side)
		{
		case UP:
			return ChargingBench.CBslotInput;
		case DOWN:
			return ChargingBench.CBslotOutput;
		default:
			return ChargingBench.CBslotPowerSource;
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
		return "ChargingBench";
	}

	@Override
	public void onInventoryChanged(int slot)
	{
		if (slot == ChargingBench.CBslotInput || slot == ChargingBench.CBslotOutput)
		{
			// Move item from input to output if not valid. (Wrong tier or not electric item.)
			if (contents[ChargingBench.CBslotInput] != null && contents[ChargingBench.CBslotOutput] == null)
			{
				if (!isItemValid(ChargingBench.CBslotInput, contents[ChargingBench.CBslotInput]))
				{
					contents[ChargingBench.CBslotOutput] = contents[ChargingBench.CBslotInput];
					contents[ChargingBench.CBslotInput] = null;
				}
			}
		}
		else if (slot >= ChargingBench.CBslotUpgrade && slot < ChargingBench.CBslotUpgrade + 4)
		{
			// One of the upgrade slots was touched, so we need to recalculate.
			doUpgradeEffects();
		}
		else if (slot >= ChargingBench.CBslotCharging && slot < ChargingBench.CBslotCharging + 12)
		{
			// Make sure it's not fully charged already? Not sure, full items will be output in updateEntity

		}
		else if (slot == ChargingBench.CBslotPowerSource)
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
