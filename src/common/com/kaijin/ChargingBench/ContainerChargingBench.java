package com.kaijin.ChargingBench;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ICrafting;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;
import cpw.mods.fml.common.network.Player;

public class ContainerChargingBench extends Container
{
	private final int benchShiftClickRange = 17;
	private final int playerInventoryStartSlot = 19;

	public TEChargingBench tileentity;
	public int currentEnergy;
	public int adjustedStorage;
	public short adjustedChargeRate;
	public short adjustedMaxInput;

	public ContainerChargingBench(InventoryPlayer player, TEChargingBench tile)
	{
		if (Utils.isDebug()) System.out.println("ContainerChargingBench");
		this.tileentity = tile;
		this.currentEnergy = 0;
		this.adjustedMaxInput = 0;
		this.adjustedStorage = 0;
		this.adjustedChargeRate = 0;

		int topOffset = 32; // Got tired of forgetting to manually alter ALL of the constants. (This won't affect the energy bar!)

		int xCol;
		int yRow;

		// Input charging slots
		for (yRow = 0; yRow < 4; ++yRow) // 4 rows high
		{
			for (xCol = 0; xCol < 3; ++xCol) // 3 columns across
			{
				this.addSlotToContainer(new SlotChargeable(tile, 3 * yRow + xCol, 52 + xCol * 18, topOffset + yRow * 18, tileentity.baseTier + 1)); // 52, 32 is upper left input slot 
			}
		}

		// Upgrade slots (Overclocker, storage)
		this.addSlotToContainer(new SlotMachineUpgrade(tile, 12, 152, topOffset));
		this.addSlotToContainer(new SlotMachineUpgrade(tile, 13, 152, topOffset + 18));
		this.addSlotToContainer(new SlotMachineUpgrade(tile, 14, 152, topOffset + 36));
		this.addSlotToContainer(new SlotMachineUpgrade(tile, 15, 152, topOffset + 54));

		// Input Slot
		this.addSlotToContainer(new SlotInput(tile, 17, 130, topOffset, tileentity.baseTier + 1));

		// Output slot
		this.addSlotToContainer(new SlotOutput(tile, 18, 130, topOffset + 54));

		// Power source slot
		this.addSlotToContainer(new SlotPowerSource(tile, 16, 8, topOffset + 54, tileentity.baseTier + 1));

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
		// if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateCraftingResults");
		super.updateCraftingResults();

		for (int var1 = 0; var1 < this.crafters.size(); ++var1)
		{
			ICrafting var2 = (ICrafting)this.crafters.get(var1);

			if (this.currentEnergy != this.tileentity.currentEnergy)
			{
				var2.updateCraftingInventoryInfo(this, 0, this.tileentity.currentEnergy & 65535);
				var2.updateCraftingInventoryInfo(this, 1, this.tileentity.currentEnergy >>> 16);
			}

			if (this.adjustedMaxInput != this.tileentity.adjustedMaxInput)
			{
				var2.updateCraftingInventoryInfo(this, 2, this.tileentity.adjustedMaxInput);
			}

			if (this.adjustedStorage != this.tileentity.adjustedStorage)
			{
				var2.updateCraftingInventoryInfo(this, 3, this.tileentity.adjustedStorage & 65535);
				var2.updateCraftingInventoryInfo(this, 4, this.tileentity.adjustedStorage >>> 16);
			}

			if (this.adjustedChargeRate != this.tileentity.adjustedChargeRate)
			{
				var2.updateCraftingInventoryInfo(this, 5, this.tileentity.adjustedChargeRate);
			}
		}
		this.currentEnergy = this.tileentity.currentEnergy;
		this.adjustedStorage = this.tileentity.adjustedStorage;
		this.adjustedMaxInput = (short)this.tileentity.adjustedMaxInput;
		this.adjustedChargeRate = (short)this.tileentity.adjustedChargeRate;
	}

	@Override
	public void updateProgressBar(int var1, int var2)
	{
		super.updateProgressBar(var1, var2);

		if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar");
		switch (var1)
		{
		case 0:
			if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar.case0");
			this.tileentity.currentEnergy = this.tileentity.currentEnergy & -65536 | var2;
			break;

		case 1:
			if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar.case1");
			this.tileentity.currentEnergy = this.tileentity.currentEnergy & 65535 | var2 << 16;
			break;

		case 2:
			if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar.case2");
			this.tileentity.adjustedMaxInput = var2;

		case 3:
			if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar.case3");
			this.tileentity.adjustedStorage = this.tileentity.adjustedStorage & -65536 | var2;
			break;

		case 4:
			if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar.case4");
			this.tileentity.adjustedStorage = this.tileentity.adjustedStorage & 65535 | var2 << 16;
			break;

		case 5:
			if (Utils.isDebug()) System.out.println("ContainerChargingBench.updateProgressBar.case5");
			this.tileentity.adjustedChargeRate = var2;
		}
	}

    /**
     * merges provided ItemStack with the first avaliable one in the container/player inventory
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
	public ItemStack transferStackInSlot(int slotID)
	{
		ItemStack original = null;
		Slot slotclicked = (Slot)this.inventorySlots.get(slotID);

		if (slotclicked != null && slotclicked.getHasStack())
		{
			ItemStack sourceStack = slotclicked.getStack();
			original = sourceStack.copy();

			if (slotID < playerInventoryStartSlot)
			{
				if (!this.mergeItemStack(sourceStack, playerInventoryStartSlot, this.inventorySlots.size(), true))
				{
					return null;
				}
			}
			else if (!this.mergeItemStack(sourceStack, 0, benchShiftClickRange, false))
			{
				return null;
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
	public ItemStack slotClick(int slotID, int button, boolean shiftclick, EntityPlayer par4EntityPlayer)
    {
        ItemStack result = null;

        if (Utils.isDebug() && ChargingBench.proxy.isServer()) System.out.println("ContainerChargingBench.slotClick(slotID=" + slotID + ", button=" + button + ", shift=" + shiftclick + ");");

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
                            result = clickedStack.copy();
                        }

                        int quantity;

                        if (clickedStack == null)
                        { // There's nothing in the slot, place the held item there if possible
                            if (mouseStack != null && slot.isItemValid(mouseStack))
                            {
                                quantity = button == 0 ? mouseStack.stackSize : 1;

                                if (quantity > slot.getSlotStackLimit())
                                {
                                    quantity = slot.getSlotStackLimit();
                                }

                                slot.putStack(mouseStack.splitStack(quantity));

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
