package com.kaijin.ChargingBench;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import ic2.api.*;
import net.minecraft.src.Chunk;
import net.minecraft.src.Container;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import com.kaijin.ChargingBench.*;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

public class TEChargingBench extends TileEntity implements IEnergySink, IWrenchable, IInventory, ISidedInventory
{
	private ItemStack[] contents = new ItemStack[19];

	private boolean initialized;

	public int baseTier;

	public int powerTier; // Transformer upgrades allow charging from energy crystals and lapotrons

	// Base values
	public int baseMaxInput;
	public int baseStorage;
	//public int baseChargeRate;

	// Adjustable values that need communicating via container
	public int adjustedMaxInput;
	public int adjustedStorage;
	//public int adjustedChargeRate;

	public int currentEnergy;

	public float drainFactor;
	public float chargeFactor;

	public TEChargingBench(int i)
	{
		//base tier = what we're passed, so 1, 2 or 3
		this.baseTier = i;
		if (Utils.isDebug()) System.out.println("BaseTier: " + this.baseTier);

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

		//setup Adjusted variables to = defaults, we'll be adjusting them in entityUpdate
		//this.adjustedChargeRate = this.baseChargeRate;
		this.adjustedMaxInput = this.baseMaxInput;
		this.adjustedStorage = this.baseStorage;

		this.powerTier = this.baseTier;

		this.drainFactor = 1.0F;
		this.chargeFactor = 1.0F;
	}

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	@Override
	public boolean isAddedToEnergyNet()
	{
		return initialized;
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction)
	{
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
		dropContents();
		ItemStack stack = new ItemStack(ChargingBench.ChargingBench, 1, this.baseTier - 1);
		dropItem(stack);
		worldObj.setBlockAndMetadataWithUpdate(xCoord, yCoord, zCoord, 0, 0, true);
		this.invalidate();
	}

	public void dropItem(ItemStack item)
	{
		// All this math just to slightly alter the start location of the items? Who cares? They spray in every direction anyway. Let's speed this up a little.
		//		final double f1 = 0.7D;
		//		final double f2 = 0.3D;
		//		double dx = ((worldObj.rand.nextFloat() * f1) + f2) * 0.5D;
		//		double dy = ((worldObj.rand.nextFloat() * f1) + f2) * 0.5D;
		//		double dz = ((worldObj.rand.nextFloat() * f1) + f2) * 0.5D;
		//		EntityItem entityitem = new EntityItem(worldObj, (double)xCoord + dx, (double)yCoord + dy, (double)zCoord + dz, item);
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

	@Override
	public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int side)
	{
		return false;
	}

	@Override
	public short getFacing()
	{
		return 0;
	}

	@Override
	public void setFacing(short facing)	{}

	@Override
	public boolean wrenchCanRemove(EntityPlayer entityPlayer)
	{
		return true;
	}

	@Override
	public float getWrenchDropRate()
	{
		return 1.0F;
	}

	@Override
	public int getSizeInventory()
	{
		// Only input/output slots are accessible to machines
		return 3;
	}

	@Override
	public int getStartInventorySide(ForgeDirection side)
	{
		switch (side)
		{
		case UP:
			return ChargingBench.slotInput;
		case DOWN:
			return ChargingBench.slotOutput;
		default:
			return ChargingBench.slotPowerSource;
		}
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

		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit())
		{
			itemstack.stackSize = getInventoryStackLimit();
		}
		this.onInventoryChanged(slot);
	}

	public void doUpgradeEffects()
	{
		// Count our upgrades
		ItemStack stack;
		int ocCount = 0;
		int tfCount = 0;
		int esCount = 0;
		for (int i = ChargingBench.slotUpgrade; i < ChargingBench.slotUpgrade + 4; ++i)
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

		// Cap upgrades at sane quantities that won't result in negative energy storage and such.
		if (ocCount > 64) ocCount = 64;
		if (esCount > 204) esCount = 204;
		if (tfCount > 3) tfCount = 3;

		// Recompute upgrade effects
		this.chargeFactor = (float)Math.pow(1.3F, ocCount); // 30% more power transferred to an item per overclocker, exponential.
		this.drainFactor = (float)Math.pow(1.5F, ocCount); // 50% more power drained per overclocker, exponential. Yes, you waste power, that's how OCs work.

		this.powerTier = this.baseTier + tfCount; // Allows better energy storage items to be plugged into the battery slot of lower tier benches.
		if (this.powerTier > 3) this.powerTier = 3;

		this.adjustedStorage = this.baseStorage * (esCount + 10) / 10; // 10% additional storage per upgrade.
		if (this.currentEnergy > this.adjustedStorage) this.currentEnergy = this.adjustedStorage; // If storage has decreased, lose any excess energy.

		this.adjustedMaxInput = (int)Math.pow(2.0D, (double)(2 * (this.baseTier + tfCount) + 3));
		if (this.adjustedMaxInput > 2048) this.adjustedMaxInput = 2048; // You can feed EV in with 1-4 TF upgrades, if you so desire.
	}

	public void onInventoryChanged(int slot)
	{
		//TODO Start processing inventory updates here
		if (slot >= ChargingBench.slotCharging && slot < ChargingBench.slotCharging + 12)
		{
			// Initialize item charging? Make sure it's not fully charged already? Not sure

		}
		else if (slot >= ChargingBench.slotUpgrade && slot < ChargingBench.slotUpgrade + 4)
		{
			// One of the upgrade slots was touched, so we need to recalculate.
			doUpgradeEffects();
		}
		else if (slot == ChargingBench.slotPowerSource)
		{
			// Perhaps eject the item if it's not valid?

		}
		else if (slot == ChargingBench.slotInput)
		{
			// Try to move it into the charging area, if it's valid
			// Perhaps eject the item if it's not valid?

		}
		else if (slot == ChargingBench.slotOutput)
		{
			// Nothing to do here? If some machine stuffs something here, the player needs to redesign it - or they're testing sided access.
			// Perhaps eject the item if it's not even electrical?
		}
		super.onInventoryChanged();
	}

	public boolean isItemValid(int slot, ItemStack stack)
	{
		// Decide if the item is a valid IC2 electrical item
		if (stack != null && stack.getItem() instanceof IElectricItem)
		{
			IElectricItem item = (IElectricItem)(stack.getItem());
			// Is the item appropriate for this slot?
			if (slot == ChargingBench.slotPowerSource && item.canProvideEnergy() && item.getTier() <= this.powerTier) return true;
			if (slot >= ChargingBench.slotCharging && slot < ChargingBench.slotCharging + 12 && item.getTier() <= baseTier) return true;
			if (slot >= ChargingBench.slotUpgrade && slot < ChargingBench.slotUpgrade + 4 && (stack.isItemEqual(ChargingBench.ic2overclockerUpg) || stack.isItemEqual(ChargingBench.ic2transformerUpg) || stack.isItemEqual(ChargingBench.ic2storageUpg))) return true;
			if (slot == ChargingBench.slotInput && item.getTier() <= baseTier) return true;
			if (slot == ChargingBench.slotOutput) return true; // GUI won't allow placement of items here, but if the bench or an external machine does, it should at least let it sit there as long as it's an electrical item.
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
			//baseChargeRate = nbttagcompound.getInteger("energyUsedPerTick");

			NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
			NBTTagList nbttagextras = nbttagcompound.getTagList("remoteSnapshot");

			contents = new ItemStack[ChargingBench.inventorySize];

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
			//nbttagcompound.setInteger("energyUsedPerTick", baseChargeRate);
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
		if (ChargingBench.proxy.isClient()) return;

		if (!initialized && worldObj != null)
		{
			EnergyNet.getForWorld(worldObj).addTileEntity(this);
			initialized = true;
		}

		if (isActive())
		{
			//redstone activation stuff here, if any
			//			if (Utils.isDebug()) System.out.println("updateEntity.CurrentEergy: " + this.currentEnergy);
			//			if (currentEnergy < 0) currentEnergy = 0;
		}

		// Work done every tick
		drainPowerSource();
		chargeItems();
		moveOutputItems();
		acceptInputItems();
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

		ItemStack stack = contents[ChargingBench.slotPowerSource];
		if (stack != null && stack.getItem() instanceof IElectricItem && this.currentEnergy < this.adjustedStorage)
		{
			IElectricItem powerSource = (IElectricItem)(stack.getItem());

			if (powerSource.getTier() <= this.powerTier && powerSource.canProvideEnergy())
			{
				int itemTransferLimit = powerSource.getTransferLimit();
				int energyNeeded = this.adjustedStorage - this.currentEnergy;

				// Test if the amount of energy we have room for is greater than what the item can transfer per tick.
				if (energyNeeded > itemTransferLimit)
				{
					// If so, request the max it can transfer per tick.
					chargeReturned = ElectricItem.discharge(stack, itemTransferLimit, powerTier, false, false);
				}
				else // If we need less than it can transfer per tick, request only what we have room for so we don't waste power.
				{
					chargeReturned = ElectricItem.discharge(stack, energyNeeded, powerTier, false, false);
				}
			}

			if (chargeReturned == 0)
			{
				int emptyItemID = powerSource.getEmptyItemId();
				int chargedItemID = powerSource.getChargedItemId();
				if (emptyItemID != chargedItemID)
				{
					ItemStack newStack = new ItemStack(emptyItemID, 1, 0);
					setInventorySlotContents(ChargingBench.slotPowerSource, newStack);
				}
			}
		}

		// Add the energy we received to our current energy level,
		this.currentEnergy += chargeReturned;
		// and make sure that we didn't go over. If we somehow did, drop the excess.
		if (this.currentEnergy > this.adjustedStorage) this.currentEnergy = this.adjustedStorage;
	}

	/**
	 * First, check the output slot to see if it's empty. If so, look to see if there are any fully 
	 * charged items in the main inventory. Move the first fully charged item to the output slot.
	 */
	private void moveOutputItems()
	{
		// TODO Auto-generated method stub
		ItemStack stack = contents[ChargingBench.slotOutput];
		if (stack == null)
		{
			// Output slot is empty. Try to find a fully charged item to move there.
			for (int slot = ChargingBench.slotCharging; slot < ChargingBench.slotCharging + 12; ++slot)
			{
				ItemStack currentStack = contents[slot];
				if (currentStack != null && currentStack.getItem() instanceof IElectricItem)
				{
					// Test if the item is fully charged (cannot accept any more power).
					if (ElectricItem.charge(currentStack, 1, baseTier, false, true) == 0)
					{
						contents[ChargingBench.slotOutput] = currentStack;
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
		ItemStack stack = contents[ChargingBench.slotInput];
		if (stack != null && stack.getItem() instanceof IElectricItem)
		{
			// Input slot contains something electrical. If possible, move one of it into the charging area.
			IElectricItem item = (IElectricItem)(stack.getItem());
			for (int slot = ChargingBench.slotCharging; slot < ChargingBench.slotCharging + 12; ++slot)
			{
				if (contents[slot] == null)
				{
					// Grab one unit from input and move it to the selected slot.
					contents[slot] = decrStackSize(ChargingBench.slotInput, 1);
					break;
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
		// TODO Auto-generated method stub
		int chargeTransferred = 0;
		ItemStack newStack;

		for(int i = 0; i < 12; i++)
		{
			ItemStack stack = this.getStackInSlot(ChargingBench.slotCharging + i);
			if (stack != null && stack.getItem() instanceof IElectricItem)
			{
				IElectricItem item = (IElectricItem)(stack.getItem());
				int emptyItemID = item.getEmptyItemId();
				int chargedItemID = item.getChargedItemId();
				int currentItemID = stack.itemID; 
				int maxItemCharge = item.getMaxCharge();
				int itemTransferLimit = item.getTransferLimit();
				if (itemTransferLimit == 0) itemTransferLimit = this.baseMaxInput;
				int itemTier = item.getTier();
				int adjustedTransferLimit = (int)Math.ceil(this.chargeFactor * itemTransferLimit);

				if (itemTier <= this.baseTier)
				{
					int amountNeeded = ElectricItem.charge(stack, adjustedTransferLimit, powerTier, true, true);
					int adjustedEnergyUse = (int)Math.ceil((this.drainFactor / this.chargeFactor) * amountNeeded);
					if(adjustedEnergyUse <= this.currentEnergy)
					{
						if (currentItemID != chargedItemID)
						{
							newStack = new ItemStack(chargedItemID, 1, 0);
							setInventorySlotContents(ChargingBench.slotCharging + i, newStack);
						}
						chargeTransferred = ElectricItem.charge(stack, adjustedTransferLimit, powerTier, true, false);
					}
					adjustedEnergyUse = (int)Math.ceil((this.drainFactor / this.chargeFactor) * chargeTransferred);
					this.currentEnergy -= adjustedEnergyUse;
					if(this.currentEnergy < 0) this.currentEnergy = 0;
				}
			}
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

	public int gaugeEnergyScaled(int gaugeSize)
	{
		if (this.currentEnergy <= 0)
		{
			return 0;
		}
		else
		{
			int result = currentEnergy * gaugeSize / adjustedStorage;

			if (result > gaugeSize)
			{
				result = gaugeSize;
			}

			return result;
		}
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
}
