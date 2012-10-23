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

public class TEBatteryStation extends TECommonBench implements IEnergySource, IInventory, ISidedInventory
{
	private ItemStack[] contents = new ItemStack[19];

	private int tickTime;
	private int tickDelay = 10;

	public int baseTier;

	// Base values
	public int baseMaxOutput;

	//For outside texture display
	public boolean doingWork;

	public TEBatteryStation(int i)
	{
		//base tier = what we're passed, so 1, 2 or 3
		this.baseTier = i;
		if (Utils.isDebug()) System.out.println("BaseTier: " + this.baseTier);

		//Max Input math = 32 for tier 1, 128 for tier 2, 512 for tier 3
		this.baseMaxOutput = (int)Math.pow(2.0D, (double)(2 * this.baseTier + 3));
		if (Utils.isDebug()) System.out.println("BaseMaxOutput: " + this.baseMaxOutput);
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, Direction direction) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getMaxEnergyOutput() {
		// TODO Auto-generated method stub
		return this.baseMaxOutput;
	}
	
	@Override
	public boolean canUpdate()
	{
		return true;
	}

	// IC2 API functions
	public boolean isAddedToEnergyNet()
	{
		return initialized;
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

	@Override
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
		return 3;
	}

	@Override
	public int getStartInventorySide(ForgeDirection side)
	{
		switch (side)
		{
		case UP:
			return ChargingBench.BSslotInput;
		case DOWN:
			return ChargingBench.BSslotOutput;
		default:
			return ChargingBench.BSslotPowerSource;
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
		if (slot == ChargingBench.BSslotInput || slot == ChargingBench.BSslotOutput)
		{
			// Move item from input to output if not valid. (Wrong tier or not electric item.)
			if (contents[ChargingBench.BSslotInput] != null && contents[ChargingBench.BSslotOutput] == null)
			{
				if (!isItemValid(ChargingBench.BSslotInput, contents[ChargingBench.BSslotInput]))
				{
					contents[ChargingBench.BSslotOutput] = contents[ChargingBench.BSslotInput];
					contents[ChargingBench.BSslotInput] = null;
				}
			}
		}
		else if (slot >= ChargingBench.BSslotPowerSource && slot < ChargingBench.BSslotPowerSource + 12)
		{
			// Make sure it's not fully charged already? Not sure, full items will be output in updateEntity

		}
		super.onInventoryChanged();
	}

	public void onInventoryChanged()
	{
		// We're not sure what called this or what slot was altered, so make sure the upgrade effects are correct just in case and then pass the call on.
		super.onInventoryChanged();
	}

	public boolean isItemValid(int slot, ItemStack stack)
	{
		// Decide if the item is a valid IC2 electrical item
		if (stack != null && stack.getItem() instanceof IElectricItem)
		{
			IElectricItem item = (IElectricItem)(stack.getItem());
			// Is the item appropriate for this slot?
			if (slot == ChargingBench.BSslotPowerSource && slot < ChargingBench.BSslotPowerSource + 12 && item.canProvideEnergy() && item.getTier() <= this.powerTier) return true;
			if (slot == ChargingBench.BSslotInput && item.getTier() <= baseTier) return true;
			if (slot == ChargingBench.BSslotOutput) return true; // GUI won't allow placement of items here, but if the bench or an external machine does, it should at least let it sit there as long as it's an electrical item.
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

			NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
			contents = new ItemStack[ChargingBench.BSinventorySize];

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
					//if (Utils.isDebug()) System.out.println("WriteNBT contents[" + i + "] stack tag: " + contents[i].stackTagCompound);
					NBTTagCompound nbttagcompound1 = new NBTTagCompound();
					nbttagcompound1.setByte("Slot", (byte)i);
					contents[i].writeToNBT(nbttagcompound1);
					nbttaglist.appendTag(nbttagcompound1);
				}
			}
			nbttagcompound.setTag("Items", nbttaglist);

			//write extra NBT stuff here
			//if (Utils.isDebug()) System.out.println("WriteNBT.CurrentEergy: " + this.currentEnergy);
			nbttagcompound.setInteger("baseTier", baseTier);
		}
	}


	@Override
	public String getInvName()
	{
		return "BatteryStation";
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
		//drainPowerSource();
		//emitEnergy();
		moveOutputItems();
		acceptInputItems();

		// Trigger this only when charge level passes where it would need to update the client texture
		int oldChargeLevel = this.chargeLevel;
		//this.chargeLevel = gaugeEnergyScaled(12);
		if (oldChargeLevel != this.chargeLevel || lastWorkState != this.doingWork)
		{
			//if (Utils.isDebug()) System.out.println("TE oldChargeLevel: " + oldChargeLevel + " chargeLevel: " + this.chargeLevel); 
			worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
		}
	}

	//FIXME
	/**
	 * Fix javadoc
	 * @return 
	 */
/*	private void drainPowerSource()
	{
		int chargeReturned = 0;

		ItemStack stack = getStackInSlot(ChargingBench.BSslotPowerSource);
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
					setInventorySlotContents(ChargingBench.BSslotPowerSource, new ItemStack(emptyItemID, 1, 0));
					//ItemStack newStack = new ItemStack(emptyItemID, 1, 0);
					//contents[ChargingBench.slotPowerSource] = newStack;
				}
			}
		}
	}
*/
	/**
	 * Look through all of the items in our main inventory and determine the current charge level,
	 * maximum charge level and maximum base charge rate for each item. Increase maximum charge
	 * rate for each item based on overclockers as appropriate, then, starting with the first slot
	 * in the main inventory, transfer one tick worth of energy from our internal storage to the
	 * item. Continue doing this for all items in the inventory until we reach the end of the main
	 * inventory or run out of internal EU storage.
	 */
/*	private void chargeItems()
	{
		for (int i = ChargingBench.BSslotPowerSource; i < ChargingBench.BSslotPowerSource + 12; i++)
		{
			ItemStack stack = this.contents[i];
			if (stack != null && stack.getItem() instanceof IElectricItem && stack.stackSize == 1)
			{
				IElectricItem item = (IElectricItem)(stack.getItem());
				if (item.getTier() <= this.baseTier)
				{
					int itemTransferLimit = item.getTransferLimit();
					if (itemTransferLimit == 0) itemTransferLimit = this.baseMaxOutput;
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
					if(adjustedEnergyUse <= this.currentEnergy && adjustedEnergyUse > 0)
					{
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
*/
	/**
	 * First, check the output slot to see if it's empty. If so, look to see if there are any fully 
	 * charged items in the main inventory. Move the first fully charged item to the output slot.
	 */
	private void moveOutputItems()
	{
		ItemStack stack = contents[ChargingBench.BSslotOutput];
		if (stack == null)
		{
			// Output slot is empty. Try to find a fully charged item to move there.
			for (int slot = ChargingBench.BSslotPowerSource; slot < ChargingBench.BSslotPowerSource + 12; ++slot)
			{
				ItemStack currentStack = contents[slot];
				if (currentStack != null && currentStack.getItem() instanceof IElectricItem)
				{
					// Test if the item is fully charged (cannot accept any more power).
					if (ElectricItem.charge(currentStack.copy(), 1, baseTier, false, true) == 0)
					{
						contents[ChargingBench.BSslotOutput] = currentStack;
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
		ItemStack stack = contents[ChargingBench.BSslotInput];
		if (stack != null && stack.getItem() instanceof IElectricItem)
		{
			// Input slot contains something electrical. If possible, move one of it into the charging area.
			IElectricItem item = (IElectricItem)(stack.getItem());
			for (int slot = ChargingBench.BSslotPowerSource; slot < ChargingBench.BSslotPowerSource + 12; ++slot)
			{
				if (contents[slot] == null)
				{
					// Grab one unit from input and move it to the selected slot.
					contents[slot] = decrStackSize(ChargingBench.BSslotInput, 1);
					break;
				}
			}
		}
	}
	
	//Networking stuff
	@Override
	public Packet250CustomPayload getAuxillaryInfoPacket()
	{
		//if (Utils.isDebug()) System.out.println("TE getAuxillaryInfoPacket()");
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);
		try
		{
			data.writeInt(1);
			data.writeInt(this.xCoord);
			data.writeInt(this.yCoord);
			data.writeInt(this.zCoord);
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
}
