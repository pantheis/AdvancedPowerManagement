package com.kaijin.ChargingBench;

import ic2.api.IElectricItem;
import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ICrafting;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemArmor;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;
import cpw.mods.fml.common.network.Player;

public class ContainerBatteryStation extends Container
{
	private final int benchShiftClickRange = 17;
	private final int playerInventoryStartSlot = 14;
	private final int playerArmorStartSlot = 55;

	public TEBatteryStation tileentity;
	public int currentEnergy;
	public int adjustedStorage;
	//public short adjustedChargeRate;
	public short adjustedMaxInput;

	public ContainerBatteryStation(InventoryPlayer player, TEBatteryStation tile)
	{
		if (Utils.isDebug()) System.out.println("ContainerBatteryStation");
		this.tileentity = tile;
		this.currentEnergy = -1;
		this.adjustedMaxInput = -1;
		this.adjustedStorage = -1;

		final int topOffset = 32; // Got tired of forgetting to manually alter ALL of the constants. (This won't affect the energy bar!)

		int xCol;
		int yRow;

		// Input charging slots
		for (yRow = 0; yRow < 4; ++yRow) // 4 rows high
		{
			for (xCol = 0; xCol < 3; ++xCol) // 3 columns across
			{
				this.addSlotToContainer(new SlotPowerSource(tile, ChargingBench.BSslotPowerSourceStart + xCol + 3 * yRow, 52 + xCol * 18, topOffset + yRow * 18)); // 52, 32 is upper left input slot 
			}
		}

		// Input Slot
		this.addSlotToContainer(new SlotPowerSource(tile, ChargingBench.BSslotInput, 130, topOffset));

		// Output slot
		this.addSlotToContainer(new SlotOutput(tile, ChargingBench.BSslotOutput, 130, topOffset + 54));

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

	//The following is not needed, but being left for now for reference
/*
	@Override
	public void updateCraftingResults()
	{
		// if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateCraftingResults");
		super.updateCraftingResults();

		for (int crafterIndex = 0; crafterIndex < this.crafters.size(); ++crafterIndex)
		{
			ICrafting crafter = (ICrafting)this.crafters.get(crafterIndex);

			if (this.currentEnergy != this.tileentity.currentEnergy)
			{
				crafter.updateCraftingInventoryInfo(this, 0, this.tileentity.currentEnergy & 65535);
				crafter.updateCraftingInventoryInfo(this, 1, this.tileentity.currentEnergy >>> 16);
			}

			if (this.adjustedMaxInput != this.tileentity.adjustedMaxInput)
			{
				crafter.updateCraftingInventoryInfo(this, 2, this.tileentity.adjustedMaxInput);
			}

			if (this.adjustedStorage != this.tileentity.adjustedStorage)
			{
				crafter.updateCraftingInventoryInfo(this, 3, this.tileentity.adjustedStorage & 65535);
				crafter.updateCraftingInventoryInfo(this, 4, this.tileentity.adjustedStorage >>> 16);
			}

			//if (this.adjustedChargeRate != this.tileentity.adjustedChargeRate)
			//{
			//	crafter.updateCraftingInventoryInfo(this, 5, this.tileentity.adjustedChargeRate);
			//}
		}
		this.currentEnergy = this.tileentity.currentEnergy;
		this.adjustedStorage = this.tileentity.adjustedStorage;
		this.adjustedMaxInput = (short)this.tileentity.adjustedMaxInput;
		//this.adjustedChargeRate = (short)this.tileentity.adjustedChargeRate;
	}
	
	@Override
	public void updateProgressBar(int param, int value)
	{
		super.updateProgressBar(param, value);

		switch (param)
		{
		case 0:
			//if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar case 0 tileentity.currentEnergy = " + (this.tileentity.currentEnergy & -65536) + " | " + value);
			this.tileentity.currentEnergy = this.tileentity.currentEnergy & -65536 | value;
			break;

		case 1:
			//if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar case 1 tileentity.currentEnergy = " + (this.tileentity.currentEnergy & 65535) + " | " + (value << 16));
			this.tileentity.currentEnergy = this.tileentity.currentEnergy & 65535 | (value << 16);
			break;

		case 2:
			//if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar case 2 tileentity.adjustedMaxInput = " + value);
			this.tileentity.adjustedMaxInput = value;
			break;

		case 3:
			//if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar case 3 tileentity.adjustedStorage = " + (this.tileentity.adjustedStorage & -65536) + " | " + value);
			this.tileentity.adjustedStorage = this.tileentity.adjustedStorage & -65536 | value;
			break;

		case 4:
			//if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar case 4 tileentity.adjustedStorage = " + (this.tileentity.adjustedStorage & 65535) + " | " + (value << 16));
			this.tileentity.adjustedStorage = this.tileentity.adjustedStorage & 65535 | (value << 16);
			break;

		case 5:
			//if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar case 5 tileentity.adjustedChargeRate = " + value);
			//this.tileentity.adjustedChargeRate = value;
			break;

		default:
			System.out.println("ContainerDischargingBench.updateProgressBar - Warning: default case!");
		}
	}

*/
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
				currentSlot = (Slot)this.inventorySlots.get(slotID);
				currentStack = currentSlot.getStack();

				if (currentStack != null && currentStack.itemID == stack.itemID
						&& (!stack.getHasSubtypes() || stack.getItemDamage() == currentStack.getItemDamage())
						&& ItemStack.func_77970_a(stack, currentStack) // func_77970_a = areItemStackTagCompoundsEqual
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
				currentSlot = (Slot)this.inventorySlots.get(slotID);
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

	@Override
	public ItemStack transferStackInSlot(int slotID) //FIXME needs updating for BatteryStation
	{
		ItemStack original = null;
		Slot slotclicked = (Slot)this.inventorySlots.get(slotID);

		if (slotclicked != null && slotclicked.getHasStack())
		{
			ItemStack sourceStack = slotclicked.getStack();
			original = sourceStack.copy();

			// Charging Bench Slots
			if (slotID < playerInventoryStartSlot)
			{
				// Look for electric armor to move into armor equipped slots from inside our charging bench
				if (original.getItem() instanceof ItemArmor && original.getItem() instanceof IElectricItem && !((Slot)this.inventorySlots.get(55 + ((ItemArmor)original.getItem()).armorType)).getHasStack())
				{
					int armorType = 55 + ((ItemArmor)original.getItem()).armorType;
					if (!this.mergeItemStack(sourceStack, armorType, armorType + 1, false))
					{
						return null;
					}
				}
				// If there wasn't room, or it isn't armor, toss it into the player inventory
				else if (!this.mergeItemStack(sourceStack, playerInventoryStartSlot, this.inventorySlots.size(), false)) // False to not use the stupid reverse order item placement
				{
					return null;
				}
			}
			else if (slotID >= playerArmorStartSlot && slotID < playerArmorStartSlot + 4)
			{
				// Player Armor Slots
				if ((original.getItem() instanceof ItemArmor) && !(original.getItem() instanceof IElectricItem))
				{
					// Move regular armor from armor slots into main inventory
					if (!this.mergeItemStack(sourceStack, playerInventoryStartSlot, this.inventorySlots.size(), false)) // False to not use the stupid reverse order item placement
					{
						return null;
					}	
				}
				else if (!this.mergeItemStack(sourceStack, 0, benchShiftClickRange, false))
				{
					// Put electrical armor items from armor slots into bench
					// if that fails, try to put them into our main inventory instead
					if (!this.mergeItemStack(sourceStack, playerInventoryStartSlot, this.inventorySlots.size(), false)) // False to not use the stupid reverse order item placement)
					{
						return null;
					}
				}
			}
			else if ((original.getItem() instanceof ItemArmor) && !(original.getItem() instanceof IElectricItem) && !((Slot)this.inventorySlots.get(55 + ((ItemArmor)original.getItem()).armorType)).getHasStack())
			{
				// Move regular armor from main inventory into armor slots
				int armorType = 55 + ((ItemArmor)original.getItem()).armorType;
				if (!this.mergeItemStack(sourceStack, armorType, armorType + 1, false))
				{
					return null;
				}
			}
			else
			{
				// Move stuff from anywhere not caught above to our charging bench inventory
				if (!this.mergeItemStack(sourceStack, 0, benchShiftClickRange, false))
				{
					if (original.getItem() instanceof ItemArmor && original.getItem() instanceof IElectricItem && !((Slot)this.inventorySlots.get(55 + ((ItemArmor)original.getItem()).armorType)).getHasStack())
					{
						// Move electric armor from main inventory into armor slots
						int armorType = 55 + ((ItemArmor)original.getItem()).armorType;
						if (!this.mergeItemStack(sourceStack, armorType, armorType + 1, false))
						{
							return null;
						}
					}
					else
					{
						return null;
					}
				}
			}


			/*
			 * FIXME Need to fix having electric armor not move from player inventory into
			 * open armor slots when charging bench is completely full. Currently electric
			 * armor will only move from player inventory into charging bench. It needs to
			 * move to the charging bench if the bench has room, then move to open armor slots
			 * if the armor slots have room, then do nothing.
			 */

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
	public ItemStack slotClick(int slotID, int button, boolean shiftclick, EntityPlayer par4EntityPlayer)
	{
		ItemStack result = null;

		if (Utils.isDebug() && ChargingBench.proxy.isServer()) System.out.println("ContainerBatteryStation.slotClick(slotID=" + slotID + ", button=" + button + ", shift=" + shiftclick + ");");

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
				else if (shiftclick)
				{
					ItemStack original = this.transferStackInSlot(slotID);

					// For crafting and other situations where a new stack could appear in the slot after each click; may be useful for output slot
					if (original != null)
					{
						int originalID = original.itemID;
						result = original.copy();
						Slot slot = (Slot)this.inventorySlots.get(slotID);

						if (slot != null && slot.getStack() != null && slot.getStack().itemID == originalID)
						{
							this.retrySlotClick(slotID, button, shiftclick, par4EntityPlayer);
						}
					}
				}
				else
				{
					if (slotID < 0)
					{
						return null;
					}

					Slot slot = (Slot)this.inventorySlots.get(slotID);

					if (slot != null)
					{
						ItemStack clickedStack = slot.getStack();
						ItemStack mouseStack = invPlayer.getItemStack();

						if (clickedStack != null)
						{
							if (Utils.isDebug()) System.out.println("Clicked stack tag: " + clickedStack.stackTagCompound + " / Item ID: " + clickedStack.itemID);
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

							slot.onPickupFromSlot(invPlayer.getItemStack());
						}
						else if (slot.isItemValid(mouseStack))
						{ // Both the mouse and the slot contain items, run this code if the item can be placed here 
							if (clickedStack.itemID == mouseStack.itemID && (!clickedStack.getHasSubtypes() || clickedStack.getItemDamage() == mouseStack.getItemDamage()) && ItemStack.func_77970_a(clickedStack, mouseStack))
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
						else if (clickedStack.itemID == mouseStack.itemID && mouseStack.getMaxStackSize() > 1 && (!clickedStack.getHasSubtypes() || clickedStack.getItemDamage() == mouseStack.getItemDamage()) && ItemStack.func_77970_a(clickedStack, mouseStack))
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

								slot.onPickupFromSlot(invPlayer.getItemStack());
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
		// if (Utils.isDebug()) System.out.println("ContainerChargingBench.canInteractWith");
		return this.tileentity.isUseableByPlayer(var1);
	}
}
