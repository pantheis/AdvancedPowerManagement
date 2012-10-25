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
import net.minecraft.src.Item;
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
	private int tickTime;
	private int tickDelay = 10;

	public int baseTier;

	// Base values
	public int baseMaxOutput;
	public int currentEnergy;

	//For outside texture display
	public boolean doingWork;

	public TEBatteryStation(int i)
	{
		contents = new ItemStack[14];

		//base tier = what we're passed, so 1, 2 or 3
		this.baseTier = i;
		this.powerTier = i;
		if (Utils.isDebug()) System.out.println("BaseTier: " + this.baseTier);

		//Max Input math = 32 for tier 1, 128 for tier 2, 512 for tier 3
		this.baseMaxOutput = (int)Math.pow(2.0D, (double)(2 * this.baseTier + 3));
		if (Utils.isDebug()) System.out.println("BaseMaxOutput: " + this.baseMaxOutput);
	}

	// IC2 API functions
	public boolean isAddedToEnergyNet()
	{
		return initialized;
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, Direction direction)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int getMaxEnergyOutput()
	{
		// TODO Auto-generated method stub
		return this.baseMaxOutput;
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
	@Override
	protected void selfDestroy()
	{
		dropContents();
		ItemStack stack = new ItemStack(ChargingBench.ChargingBench, 1, this.baseTier - 1);
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
			if (slot == ChargingBench.BSslotOutput) return true; // GUI won't allow placement of items here, but if the bench or an external machine does, it should at least let it sit there as long as it's an electrical item.
			if (item.canProvideEnergy() && item.getTier() <= this.powerTier)
			{
				if ((slot >= ChargingBench.BSslotPowerSourceStart && slot < ChargingBench.BSslotPowerSourceStart + 12) || slot == ChargingBench.BSslotInput) return true;
			}
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

		boolean lastWorkState = doingWork;
		doingWork = false;

		// Work done every tick
		if(!receivingRedstoneSignal())
		{
			drainPowerSource();
			emitEnergy();
		}
		moveOutputItems();
		acceptInputItems();

		// Trigger this only when charge level passes where it would need to update the client texture
		int oldChargeLevel = chargeLevel;
		//this.chargeLevel = gaugeEnergyScaled(12);
		if (oldChargeLevel != chargeLevel || lastWorkState != doingWork)
		{
			//if (Utils.isDebug()) System.out.println("TE oldChargeLevel: " + oldChargeLevel + " chargeLevel: " + this.chargeLevel); 
			worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
		}
	}

	private void emitEnergy()
	{
//		if (Utils.isDebug()) System.out.println("preEmit-currentEnergy: " + currentEnergy);
		int surplus = EnergyNet.getForWorld(worldObj).emitEnergyFrom(this, currentEnergy);
		currentEnergy = surplus;
//		if (Utils.isDebug()) System.out.println("postEmit-currentEnergy: " + currentEnergy);
	}

	//TODO test this
	private void drainPowerSource()
	{
		for (int i = ChargingBench.BSslotPowerSourceStart; i < ChargingBench.BSslotPowerSourceStart + 12; i++)
		{
//			if (Utils.isDebug()) System.out.println("currentEnergy: " + currentEnergy + " baseMaxOutput: " + baseMaxOutput);
			if (currentEnergy >= baseMaxOutput) return;

			ItemStack stack = this.contents[i];
			if (stack != null && stack.getItem() instanceof IElectricItem && stack.stackSize == 1)
			{
				IElectricItem item = (IElectricItem)(stack.getItem());
				if (item.getTier() <= this.powerTier && item.canProvideEnergy())
				{
					int emptyItemID = item.getEmptyItemId();
					int chargedItemID = item.getChargedItemId();

					if (stack.itemID == chargedItemID)
					{
						int transferLimit = item.getTransferLimit();
						int amountNeeded = baseMaxOutput - currentEnergy;
						if (transferLimit == 0) transferLimit = this.baseMaxOutput;
						if (transferLimit > amountNeeded) transferLimit = amountNeeded;

						int chargeReturned = ElectricItem.discharge(stack, transferLimit, powerTier, false, false);
						// Add the energy we received to our current energy level
						if (Utils.isDebug()) System.out.println("transferLimit:" + transferLimit + " amountNeeded:" + amountNeeded + " chargeReturned:" + chargeReturned);
						if (chargeReturned > 0)
						{
							this.currentEnergy += chargeReturned;
							this.doingWork = true;
						}

						// Workaround for buggy IC2 API .discharge that automatically switches stack to emptyItemID but leaves a stackTagCompound on it, so it can't be stacked with never-used empties  
						if (chargedItemID != emptyItemID && (chargeReturned < transferLimit || ElectricItem.discharge(stack, 1, powerTier, false, true) == 0))
						{
							if (Utils.isDebug()) System.out.println("Switching to emptyItemID: " + emptyItemID + " from stack.itemID: " + stack.itemID + " - chargedItemID: " + chargedItemID);
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

		ItemStack outputStack = contents[ChargingBench.BSslotOutput];
		if (outputStack == null || (outputStack.isStackable() && outputStack.stackSize < outputStack.getMaxStackSize()))
		{
			// Output slot could receive item(s). Try to find something to move there.
			for (int slot = ChargingBench.BSslotPowerSourceStart; slot < ChargingBench.BSslotPowerSourceStart + 12; ++slot)
			{
				ItemStack currentStack = contents[slot];
				if (currentStack != null && currentStack.getItem() instanceof IElectricItem)
				{
					IElectricItem powerSource = (IElectricItem)(currentStack.getItem());
					if (powerSource.getTier() <= this.powerTier) // && powerSource.canProvideEnergy()
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
									contents[ChargingBench.BSslotOutput] = currentStack;
									contents[slot] = null;
								}
								else
								{
									// We already know the stack isn't full yet
									contents[ChargingBench.BSslotOutput].stackSize++;
									contents[slot].stackSize--;
									if (contents[slot].stackSize < 1) contents[slot] = null;
								}
								this.onInventoryChanged();
								break;
							}
						}
						else if (outputStack == null)
						{
							boolean empty = ElectricItem.discharge(currentStack, 1, powerTier, true, true) == 0;
							if (empty)
							{
								// Pick Me
								contents[ChargingBench.BSslotOutput] = currentStack;
								contents[slot] = null;
								this.onInventoryChanged();
								break;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Check to see if there are any items in the input slot. If so, check to see if there are any
	 * free discharging slots. If so, move one from the input slot to a free discharging slot.
	 */
	private void acceptInputItems()
	{
		ItemStack stack = contents[ChargingBench.BSslotInput];
		if (stack == null || !(stack.getItem() instanceof IElectricItem)) return;
		
		IElectricItem item = (IElectricItem)stack.getItem();
		if (item.canProvideEnergy())
		{
			// Input slot contains a power source. If possible, move one of it into the discharging area.
			for (int slot = ChargingBench.BSslotPowerSourceStart; slot < ChargingBench.BSslotPowerSourceStart + 12; ++slot)
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

	private void rejectInvalidInput()
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

	//Networking stuff
	@Override
	public Packet250CustomPayload getDescriptionPacket()
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

	// ISidedInventory

	@Override
	public int getStartInventorySide(ForgeDirection side)
	{
		switch (side)
		{
		case UP:
		case DOWN:
			return ChargingBench.BSslotInput;
		default:
			return ChargingBench.BSslotOutput;
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
		return "BatteryStation";
	}

	@Override
	public void onInventoryChanged(int slot)
	{
		if (slot == ChargingBench.BSslotInput || slot == ChargingBench.BSslotOutput)
		{
			rejectInvalidInput();
		}
		super.onInventoryChanged();
	}
}
