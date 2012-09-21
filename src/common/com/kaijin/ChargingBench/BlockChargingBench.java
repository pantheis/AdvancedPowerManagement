package com.kaijin.ChargingBench;

import java.util.*;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import net.minecraft.src.*;
import com.kaijin.ChargingBench.*;

public class BlockChargingBench extends Block
{
	public BlockChargingBench(int i, int j, Material material)
	{
		super(i, j, material);
	}

	public void getSubBlocks(int blockID, CreativeTabs creativetabs, List list)
	{
		for (int i = 0; i < 3; ++i)
		{
			list.add(new ItemStack(blockID, 1, i));
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int par6, float par7, float par8, float par9)
	{
		if(world.isRemote)
		{
			// Prevent GUI pop-up and handle block rotation
			if (entityplayer.isSneaking())
			{
				if (Utils.isDebug()) System.out.println("Block.world.isRemote.isSneaking");
				// Prevent GUI popup when sneaking
				// This allows you to sneak place things directly on the inventory stocker
				return false;
			}
		}
		else if (ChargingBench.proxy.isServer())
		{
			// Prevent GUI pop-up and handle block rotation
			if (entityplayer.isSneaking())
			{
				// Prevent GUI popup when sneaking
				// This allows you to sneak place things directly on the inventory stocker
				return false;
			}
			if (Utils.isDebug()) System.out.println("BlockChargingBench.BlockActivated");
			entityplayer.openGui(ChargingBench.instance, 1, world, x, y, z);
			return true;
		}
		return true;
	}

	public String getTextureFile()
	{
		return ChargingBench.proxy.BLOCK_PNG;
	}

	@SideOnly(Side.CLIENT)
	public int getBlockTexture(IBlockAccess blocks, int x, int y, int z, int i)
	{
		return i;
	}

	public int getBlockTextureFromSide(int i)
	{
		switch (i)
		{
		case 0: // Bottom
			return 16;

		case 1: // Top
			return 0;

		case 2: // North
			return 16;

		case 3: // South
			return 16;

		default: // 4-5 West-East
			return 16;
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

		default:
			return null;
		}
	}

	@Override
	public boolean hasTileEntity(int metadata)
	{
		return true;
	}

	public int idDropped(int var1, Random var2, int var3)
	{
		if (Utils.isDebug()) System.out.println("BlockChargingBench.idDropped");
		return this.blockID;
	}

	protected int damageDropped(int var1)
	{
		if (Utils.isDebug()) System.out.println("BlockChargingBench.damageDropped");
		return var1;
	}
	public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int par1)
	{
		preDestroyBlock(world, x, y, z);
		if (Utils.isDebug()) System.out.println("BlockChargingBench.onBlockDestroyedByPlayer");
		super.onBlockDestroyedByPlayer(world, x, y, z, par1);
	}

	public static void dropItems(World world, ItemStack stack, int i, int j, int k)
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
				dropItems(world, inventory.getStackInSlot(l).copy(), i, j, k);
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
	}
}
