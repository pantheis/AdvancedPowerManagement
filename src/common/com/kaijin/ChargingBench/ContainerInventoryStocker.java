/* Inventory Stocker
 *  Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 *  Licensed as open source with restrictions. Please see attached LICENSE.txt.
 */

package com.kaijin.InventoryStocker;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import com.kaijin.GenericMod.InventoryStocker;
import com.kaijin.GenericMod.TileEntityInventoryStocker;
import com.kaijin.GenericMod.Utils;
import com.kaijin.InventoryStocker.*;
import cpw.mods.fml.common.network.Player;
import net.minecraft.src.*;

public class ContainerInventoryStocker extends Container
{
	private TileEntityInventoryStocker tile;
	private List<EntityPlayerMP> guiPlayerList = new ArrayList<EntityPlayerMP>();

	public ContainerInventoryStocker(InventoryPlayer playerinventory, TileEntityInventoryStocker inventorystockerinventory)
	{
		this.tile = inventorystockerinventory;
		int xCol;
		int yRow;

		for (yRow = 0; yRow < 3; ++yRow)
		{
			for (xCol = 0; xCol < 3; ++xCol)
			{
				this.addSlotToContainer(new Slot(inventorystockerinventory, xCol + 3 * yRow, 8 + xCol * 18, 18 + yRow * 18));
			}
		}

		for (yRow = 0; yRow < 3; ++yRow)
		{
			for (xCol = 0; xCol < 3; ++xCol)
			{
				this.addSlotToContainer(new Slot(inventorystockerinventory, 9 + xCol + 3 * yRow, 116 + xCol * 18, 18 + yRow * 18));
			}
		}

		for (yRow = 0; yRow < 3; ++yRow)
		{
			for (xCol = 0; xCol < 9; ++xCol)
			{
				this.addSlotToContainer(new Slot(playerinventory, xCol + yRow * 9 + 9, 8 + xCol * 18, 86 + yRow * 18));
			}
		}

		for (xCol = 0; xCol < 9; ++xCol)
		{
			this.addSlotToContainer(new Slot(playerinventory, xCol, 8 + xCol * 18, 144));
		}
	}

	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return this.tile.isUseableByPlayer(entityplayer);
	}

	public ItemStack transferStackInSlot(int par1)
	{
		ItemStack var2 = null;
		Slot var3 = (Slot)this.inventorySlots.get(par1);

		if (var3 != null && var3.getHasStack())
		{
			ItemStack var4 = var3.getStack();
			var2 = var4.copy();

			if (par1 < 18)
			{
				if (!this.mergeItemStack(var4, 18, this.inventorySlots.size(), true))
				{
					return null;
				}
			}
			else if (!this.mergeItemStack(var4, 0, 18, false))
			{
				return null;
			}

			if (var4.stackSize == 0)
			{
				var3.putStack((ItemStack)null);
			}
			else
			{
				var3.onSlotChanged();
			}
		}

		return var2;
	}

	@Override
	public void addCraftingToCrafters(ICrafting par1ICrafting)
	{
		super.addCraftingToCrafters(par1ICrafting);
		if (Utils.isDebug())
		{
			System.out.println("gui.addCraftingToCrafters");
			String n = ((EntityPlayerMP)par1ICrafting).username;
			System.out.println("container.addCraftingToCrafters.server: " + n);
		}
		guiPlayerList.add(((EntityPlayerMP)par1ICrafting));
		tile.sendSnapshotStateClient((EntityPlayerMP)(par1ICrafting));
		tile.entityOpenList(guiPlayerList);
	}
	/**
	 * Callback for when the crafting gui is closed.
	 */
	@Override
	public void onCraftGuiClosed(EntityPlayer par1EntityPlayer)
	{
		super.onCraftGuiClosed(par1EntityPlayer);
		if (Utils.isDebug()) System.out.println("gui.onCraftGuiClosed-client+server");
		if (InventoryStocker.proxy.isServer())
		{
			if (Utils.isDebug()) System.out.println("gui.onCraftGuiClosed-SERVER");
			if (guiPlayerList.contains(((EntityPlayerMP)par1EntityPlayer)))
			{
				if (Utils.isDebug())
				{
					System.out.println("gui.addCraftingToCrafters");
					String n = ((EntityPlayerMP)par1EntityPlayer).username;
					System.out.println("gui.onCraftGuiClosed.RemoveNameFromList: " + n);
				}
				guiPlayerList.remove(((EntityPlayerMP)par1EntityPlayer));
				tile.entityOpenList(guiPlayerList);
			}
		}
	}
}
