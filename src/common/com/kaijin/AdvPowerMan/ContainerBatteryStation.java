/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.inventory.Container;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.Slot;

public class ContainerBatteryStation extends Container
{
	private static final int shiftClickRange = 13;
	private static final int playerInventoryStartSlot = 14;

	public TEBatteryStation tileentity;
	public int opMode;
	public int average;
	public int itemsEnergyTotal;

	public ContainerBatteryStation(InventoryPlayer player, TEBatteryStation tile)
	{
		tileentity = tile;
		opMode = -1;
		average = -1;
		itemsEnergyTotal = -1;

		final int topOffset = 24; // Got tired of forgetting to manually alter ALL of the constants. (This won't affect the energy bar!)

		int xCol;
		int yRow;

		// Discharging slots, in reverse order
		for (yRow = 3; yRow >= 0; yRow--) // 4 rows high
		{
			for (xCol = 2; xCol >= 0; xCol--) // 3 columns across
			{
				this.addSlotToContainer(new SlotDrainable(tile, Info.BS_SLOT_POWER_START + 11 - xCol - 3 * yRow, 62 + xCol * 18, topOffset + yRow * 18, tile.powerTier)); // 52, 32 is upper left input slot 
			}
		}

		// Input Slot
		this.addSlotToContainer(new SlotPowerSource(tile, Info.BS_SLOT_INPUT, 17, topOffset, tile.powerTier));

		// Output slot
		this.addSlotToContainer(new SlotOutput(tile, Info.BS_SLOT_OUTPUT, 143, topOffset + 54));

		// Player inventory
		for (yRow = 0; yRow < 3; ++yRow)
		{
			for (xCol = 0; xCol < 9; ++xCol)
			{
				this.addSlotToContainer(new Slot(player, xCol + yRow * 9 + 9, 8 + xCol * 18, topOffset + 76 + yRow * 18));
			}
		}

		// Player hot bar
		for (yRow = 0; yRow < 9; ++yRow)
		{
			this.addSlotToContainer(new Slot(player, yRow, 8 + yRow * 18, topOffset + 134));
		}
	}

	@Override
	public void updateCraftingResults()
	{
		// if (ChargingBench.isDebugging) System.out.println("ContainerChargingBench.updateCraftingResults");
		super.updateCraftingResults();

		final int syncAvg = (int)(tileentity.outputTracker.getAverage() * 100);
		final int energy = tileentity.getTotalEnergy();
		for (int crafterIndex = 0; crafterIndex < crafters.size(); ++crafterIndex)
		{
			ICrafting crafter = (ICrafting)this.crafters.get(crafterIndex);

			if (average != syncAvg)
			{
				crafter.sendProgressBarUpdate(this, 0, syncAvg & 65535);
				crafter.sendProgressBarUpdate(this, 1, syncAvg >>> 16);
			}

			if (itemsEnergyTotal != energy)
			{
				crafter.sendProgressBarUpdate(this, 2, energy & 65535);
				crafter.sendProgressBarUpdate(this, 3, energy >>> 16);
			}

			if (opMode != tileentity.opMode)
			{
				crafter.sendProgressBarUpdate(this, 4, tileentity.opMode);
			}
		}
		opMode = tileentity.opMode;
		average = syncAvg;
		itemsEnergyTotal = energy;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void updateProgressBar(int param, int value)
	{
		super.updateProgressBar(param, value);

		switch (param)
		{
		case 0:
			average = average & -65536 | value;
			break;

		case 1:
			average = average & 65535 | (value << 16);
			break;

		case 2:
			itemsEnergyTotal = itemsEnergyTotal & -65536 | value;
			break;

		case 3:
			itemsEnergyTotal = itemsEnergyTotal & 65535 | (value << 16);
			break;

		case 4:
			this.opMode = value;
			break;

		default:
			System.out.println("ContainerBatteryStation.updateProgressBar - Warning: default case!");
		}
	}

	/**
	 * Merges provided ItemStack with the first available one in the container/player inventory
	 */
	@Override
	protected boolean mergeItemStack(ItemStack stack, int startSlot, int endSlot, boolean reverseOrder)
	{
		boolean result = false;
		int slotID = startSlot;

		if (reverseOrder)
		{
			slotID = endSlot - 1;
		}

		Slot currentSlot;
		ItemStack currentStack;

		if (stack.isStackable())
		{
			while (stack.stackSize > 0 && (!reverseOrder && slotID < endSlot || reverseOrder && slotID >= startSlot))
			{
				currentSlot = (Slot)inventorySlots.get(slotID);
				currentStack = currentSlot.getStack();

				if (currentStack != null && currentStack.itemID == stack.itemID
						&& (!stack.getHasSubtypes() || stack.getItemDamage() == currentStack.getItemDamage())
						&& ItemStack.areItemStackTagsEqual(stack, currentStack)
						&& currentSlot.isItemValid(stack))
				{
					int limit = Math.min(stack.getMaxStackSize(), currentSlot.getSlotStackLimit());
					int sum = currentStack.stackSize + stack.stackSize;
					if (sum <= limit)
					{
						stack.stackSize = 0;
						currentStack.stackSize = sum;
						currentSlot.onSlotChanged();
						result = true;
					}
					else if (currentStack.stackSize < limit)
					{
						int diff = limit - currentStack.stackSize;
						stack.stackSize -= diff;
						currentStack.stackSize = limit;
						currentSlot.onSlotChanged();
						result = true;
					}
				}

				if (reverseOrder)
				{
					--slotID;
				}
				else
				{
					++slotID;
				}
			}
		}

		if (stack.stackSize > 0)
		{
			if (reverseOrder)
			{
				slotID = endSlot - 1;
			}
			else
			{
				slotID = startSlot;
			}

			while (!reverseOrder && slotID < endSlot || reverseOrder && slotID >= startSlot)
			{
				currentSlot = (Slot)inventorySlots.get(slotID);
				currentStack = currentSlot.getStack();

				if (currentStack == null && currentSlot.isItemValid(stack))
				{
					int limit = currentSlot.getSlotStackLimit();
					if (stack.stackSize <= limit)
					{
						currentSlot.putStack(stack.copy());
						currentSlot.onSlotChanged();
						stack.stackSize = 0;
						result = true;
						break;
					}
					else
					{
						currentSlot.putStack(stack.splitStack(limit));
						currentSlot.onSlotChanged();
						result = true;
					}
				}

				if (reverseOrder)
				{
					--slotID;
				}
				else
				{
					++slotID;
				}
			}
		}

		return result;
	}

	/**
	 * transferStackInSlot with a new signature, not yet mapped to the proper method name
	 */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer p, int slotID)
	{
		ItemStack original = null;
		Slot slotclicked = (Slot)inventorySlots.get(slotID);

		if (slotclicked != null && slotclicked.getHasStack())
		{
			ItemStack sourceStack = slotclicked.getStack();
			original = sourceStack.copy();

			if (slotID < playerInventoryStartSlot)
			{
				// Move stuff to the player's inventory
				if (!this.mergeItemStack(sourceStack, playerInventoryStartSlot, inventorySlots.size(), true))
				{
					return null;
				}
			}
			else
			{
				// Move stuff to the battery station's inventory
				if (!this.mergeItemStack(sourceStack, 0, shiftClickRange, false))
				{
					return null;
				}
			}

			if (sourceStack.stackSize == 0)
			{
				slotclicked.putStack((ItemStack)null);
			}
			else
			{
				slotclicked.onSlotChanged();
			}
		}
		return original;
	}

	@Override
	public ItemStack slotClick(int slotID, int button, int shiftclick, EntityPlayer par4EntityPlayer)
	{
		ItemStack result = null;

		//if (ChargingBench.isDebugging && ChargingBench.proxy.isServer()) System.out.println("ContainerBatteryStation.slotClick(slotID=" + slotID + ", button=" + button + ", shift=" + shiftclick + ");");

		if (button > 1)
		{
			return null;
		}
		else
		{
			if (button == 0 || button == 1)
			{
				InventoryPlayer invPlayer = par4EntityPlayer.inventory;

				if (slotID == -999) // Dropping items outside GUI, identical to vanilla behavior
				{
					if (invPlayer.getItemStack() != null && slotID == -999)
					{
						if (button == 0)
						{
							par4EntityPlayer.dropPlayerItem(invPlayer.getItemStack());
							invPlayer.setItemStack((ItemStack)null);
						}

						if (button == 1)
						{
							par4EntityPlayer.dropPlayerItem(invPlayer.getItemStack().splitStack(1));

							if (invPlayer.getItemStack().stackSize == 0)
							{
								invPlayer.setItemStack((ItemStack)null);
							}
						}
					}
				}
				else if (shiftclick == 1)
				{
					ItemStack original = this.transferStackInSlot(par4EntityPlayer, slotID);

					// For crafting and other situations where a new stack could appear in the slot after each click; may be useful for output slot
					if (original != null)
					{
						int originalID = original.itemID;
						result = original.copy();
						Slot slot = (Slot)inventorySlots.get(slotID);

						if (slot != null && slot.getStack() != null && slot.getStack().itemID == originalID)
						{
							this.retrySlotClick(slotID, button, true, par4EntityPlayer);
						}
					}
				}
				else
				{
					if (slotID < 0)
					{
						return null;
					}

					Slot slot = (Slot)inventorySlots.get(slotID);

					if (slot != null)
					{
						ItemStack clickedStack = slot.getStack();
						ItemStack mouseStack = invPlayer.getItemStack();

						if (clickedStack != null)
						{
							//if (ChargingBench.isDebugging) System.out.println("Clicked stack tag: " + clickedStack.stackTagCompound + " / Item ID: " + clickedStack.itemID);
							result = clickedStack.copy();
						}

						int quantity;

						if (clickedStack == null)
						{ // There's nothing in the slot, place the held item there if possible
							if (mouseStack != null && slot.isItemValid(mouseStack))
							{
								quantity = button == 0 ? mouseStack.stackSize : 1;
								if (quantity > slot.getSlotStackLimit()) quantity = slot.getSlotStackLimit();

								ItemStack temp = mouseStack.splitStack(quantity); 
								slot.putStack(temp);

								if (mouseStack.stackSize == 0)
								{
									invPlayer.setItemStack((ItemStack)null);
								}
							}
						}
						else if (mouseStack == null)
						{ // Pick up what's in the slot
							quantity = button == 0 ? clickedStack.stackSize : (clickedStack.stackSize + 1) / 2;
							ItemStack remainder = slot.decrStackSize(quantity);
							invPlayer.setItemStack(remainder);

							if (clickedStack.stackSize == 0)
							{
								slot.putStack((ItemStack)null);
							}

							slot.onPickupFromSlot(par4EntityPlayer, invPlayer.getItemStack());
						}
						else if (slot.isItemValid(mouseStack))
						{ // Both the mouse and the slot contain items, run this code if the item can be placed here 
							if (clickedStack.itemID == mouseStack.itemID && (!clickedStack.getHasSubtypes() || clickedStack.getItemDamage() == mouseStack.getItemDamage()) && ItemStack.areItemStackTagsEqual(clickedStack, mouseStack))
							{
								quantity = button == 0 ? mouseStack.stackSize : 1;

								if (quantity > slot.getSlotStackLimit() - clickedStack.stackSize)
								{
									quantity = slot.getSlotStackLimit() - clickedStack.stackSize;
								}

								if (quantity > mouseStack.getMaxStackSize() - clickedStack.stackSize)
								{
									quantity = mouseStack.getMaxStackSize() - clickedStack.stackSize;
								}

								mouseStack.splitStack(quantity);

								if (mouseStack.stackSize == 0)
								{
									invPlayer.setItemStack((ItemStack)null);
								}

								clickedStack.stackSize += quantity;
							}
							else if (mouseStack.stackSize <= slot.getSlotStackLimit())
							{ // Exchange the items since they don't match
								slot.putStack(mouseStack);
								invPlayer.setItemStack(clickedStack);
							}
						}
						else if (clickedStack.itemID == mouseStack.itemID && mouseStack.getMaxStackSize() > 1 && (!clickedStack.getHasSubtypes() || clickedStack.getItemDamage() == mouseStack.getItemDamage()) && ItemStack.areItemStackTagsEqual(clickedStack, mouseStack))
						{ // Both the mouse and the slot contain items, run this code if they match
							quantity = clickedStack.stackSize;

							if (quantity > 0 && quantity + mouseStack.stackSize <= mouseStack.getMaxStackSize())
							{
								mouseStack.stackSize += quantity;
								clickedStack = slot.decrStackSize(quantity);

								if (clickedStack.stackSize == 0)
								{
									slot.putStack((ItemStack)null);
								}

								slot.onPickupFromSlot(par4EntityPlayer, invPlayer.getItemStack());
							}
						}

						slot.onSlotChanged();

					}
				}
			}

			return result;
		}
	}

	public boolean canInteractWith(EntityPlayer var1)
	{
		// if (ChargingBench.isDebugging) System.out.println("ContainerChargingBench.canInteractWith");
		return tileentity.isUseableByPlayer(var1);
	}
}
