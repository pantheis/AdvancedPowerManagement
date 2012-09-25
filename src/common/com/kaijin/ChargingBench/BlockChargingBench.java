package com.kaijin.ChargingBench;

import java.util.*;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import net.minecraft.src.*;
import com.kaijin.ChargingBench.*;
import ic2.api.*;

public class BlockChargingBench extends Block
{
	public BlockChargingBench(int i, int j, Material material)
	{
		super(i, j, material);
	}

	public void getSubBlocks(int blockID, CreativeTabs creativetabs, List list)
	{
		for (int i = 0; i < 7; ++i)
		{
			list.add(new ItemStack(blockID, 1, i));
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int par6, float par7, float par8, float par9)
	{
		int currentEquippedItemID = 0;
		ItemStack wrench = Items.getItem("wrench");
		ItemStack electricWrench = Items.getItem("electricWrench");

		if(world.isRemote)
		{
			// Prevent GUI pop-up
			//NPE catch, do not try to get the name of a null item
			if (entityplayer.getCurrentEquippedItem() != null)
			{
				currentEquippedItemID = entityplayer.getCurrentEquippedItem().itemID;
			}

			if (entityplayer.isSneaking() || currentEquippedItemID == wrench.itemID || currentEquippedItemID == electricWrench.itemID)
			{
				if (Utils.isDebug()) System.out.println("Block.world.isRemote.isSneaking");
				// Prevent GUI popup when sneaking
				// This allows you to sneak place things directly on the inventory stocker
				return false;
			}
		}

		else if (ChargingBench.proxy.isServer())
		{
			// Prevent GUI pop-up
			//NPE catch, do not try to get the name of a null item
			if (entityplayer.getCurrentEquippedItem() != null)
			{
				currentEquippedItemID = entityplayer.getCurrentEquippedItem().itemID;
			}

			if (entityplayer.isSneaking() || currentEquippedItemID == wrench.itemID || currentEquippedItemID == electricWrench.itemID)
			{
				// Prevent GUI popup when sneaking
				// This allows you to sneak place things directly on the inventory stocker
				return false;
			}
			if (Utils.isDebug()) System.out.println("BlockChargingBench.BlockActivated");
			int meta = world.getBlockMetadata(x, y, z);
			if (meta == 0 || meta == 1 || meta == 2)
			{
				entityplayer.openGui(ChargingBench.instance, 1, world, x, y, z);
				return true;
			}
			else
			{
				return false;
			}
		}
		return true;
	}

	public String getTextureFile()
	{
		return ChargingBench.proxy.BLOCK_PNG;
	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int i, int meta)
	{
		switch (i)
		{
		case 0: // bottom
			return 0;

		case 1: // top
			switch (meta)
			{
			case 0: // mark 1
				return 16;

			case 1: // mark 2
				return 17;

			case 2: // mark 3
				return 18;

			case 3: // emitter 1
				return 16;

			case 4: // emitter 2
				return 17;

			case 5: // emitter 3
				return 18;
				
			case 6: // emitter 4
				return 0;

			default: // something wrong?
				return 0;
			}

		default: // the rest of the sides
			switch (meta)
			{
			case 0: // mark 1
				return 32;

			case 1: // mark 2
				return 33;

			case 2: // mark 3
				return 34;

			case 3: // emitter 1
				return 16;

			case 4: // emitter 2
				return 17;

			case 5: // emitter 3
				return 18;
				
			case 6: // emitter 4
				return 0;
			}
			return 0; // if we're here, something is wrong
		}
	}

	@Override
	public boolean canProvidePower()
	{
		return false; // Old means of causing visual RedPower wire connections.
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, int X, int Y, int Z, int direction)
	{
		return true; // Will appear to connect to RedPower wires and such.
		// Currently still causes redstone dust to appear to connect in some cases where it shouldn't; Not our fault.
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		if (Utils.isDebug()) System.out.println("BlockChargingBench.createTileEntity");
		switch (metadata)
		{
		case 0:
			return new TEChargingBench1();

		case 1:
			return new TEChargingBench2();

		case 2:
			return new TEChargingBench3();

		case 3:
			return new TEEmitter1();

		case 4:
			return new TEEmitter2();

		case 5:
			return new TEEmitter3();
			
		case 6:
			return new TEEmitter4();

		default:
			return null;
		}
	}

	@Override
	public boolean hasTileEntity(int metadata)
	{
		switch (metadata)
		{
		case 0:
			return true;

		case 1:
			return true;

		case 2:
			return true;

		case 3:
			return true;

		case 4:
			return true;

		case 5:
			return true;
			
		case 6:
			return true;

		default:
			return false;
		}
	}

	public int idDropped(int var1, Random var2, int var3)
	{
		if (Utils.isDebug()) System.out.println("BlockChargingBench.idDropped");
		return this.blockID;
	}

	protected int damageDropped(int meta)
	{
		if (Utils.isDebug()) System.out.println("BlockChargingBench.damageDropped");
		return meta;
	}

	@Override
	public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int par1)
	{
		FMLLog.getLogger().info("block.onBlockDestroyedByPlayer");
		preDestroyBlock(world, x, y, z);
		if (Utils.isDebug()) System.out.println("BlockChargingBench.onBlockDestroyedByPlayer");
		super.onBlockDestroyedByPlayer(world, x, y, z, par1);
	}

	public static void dropItem(World world, ItemStack stack, int i, int j, int k)
	{
		float f1 = 0.7F;
		double d = (double)(world.rand.nextFloat() * f1) + (double)(1.0F - f1) * 0.5D;
		double d1 = (double)(world.rand.nextFloat() * f1) + (double)(1.0F - f1) * 0.5D;
		double d2 = (double)(world.rand.nextFloat() * f1) + (double)(1.0F - f1) * 0.5D;
		EntityItem entityitem = new EntityItem(world, (double) i + d,
				(double) j + d1, (double) k + d2, stack);
		entityitem.delayBeforeCanPickup = 10;
		world.spawnEntityInWorld(entityitem);
	}

	public static void dropItems(World world, IInventory inventory, int i, int j, int k)
	{
		for (int l = 0; l < inventory.getSizeInventory(); ++l)
		{
			ItemStack items = inventory.getStackInSlot(l);

			if (items != null && items.stackSize > 0)
			{
				dropItem(world, inventory.getStackInSlot(l).copy(), i, j, k);
			}
		}
	}

	public static void preDestroyBlock(World world, int i, int j, int k)
	{
		TileEntity tile = world.getBlockTileEntity(i, j, k);

		if (tile instanceof IInventory && !ChargingBench.proxy.isClient())
		{
			dropItems(world, (IInventory) tile, i, j, k);
			tile.invalidate();
		}
		if (tile instanceof TEEmitter && !ChargingBench.proxy.isClient())
		{
			tile.invalidate();
		}
	}
}
