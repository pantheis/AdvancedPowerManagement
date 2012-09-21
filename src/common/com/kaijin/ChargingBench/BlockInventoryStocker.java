/* Inventory Stocker
 *  Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 *  Licensed as open source with restrictions. Please see attached LICENSE.txt.
 */

package com.kaijin.InventoryStocker;

import java.util.*;

import net.minecraft.src.Block;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.MathHelper;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

import com.kaijin.InventoryStocker.*;

import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import cpw.mods.fml.common.network.NetworkRegistry;


public class BlockInventoryStocker extends Block
{
	public BlockInventoryStocker(int i, int j, Material material)
	{
		super(i, j, material);
	}

	public String getTextureFile()
	{
		return CommonProxy.BLOCK_PNG;
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

	@SideOnly(Side.CLIENT)
	public int getBlockTexture(IBlockAccess blocks, int x, int y, int z, int i)
	{
		//TODO possibly redo this to use Forge.Direction
		TileEntity tile = blocks.getBlockTileEntity(x, y, z);
		if(tile instanceof TileEntityInventoryStocker)
		{
			//int m = blocks.getBlockMetadata(x, y, z);
			int m = ((TileEntityInventoryStocker)tile).Metainfo;
			int dir = m & 7;
			int side = Utils.lookupRotatedSide(i, dir);
			int powered = (m & 8) >> 3;

			//if (Utils.isDebug()) System.out.println("getBlockTexture - m = " + m);

			// Sides (0-5) are: Front, Back, Top, Bottom, Left, Right
			if (side == 0) // Front
			{
				int time = (int)tile.worldObj.getWorldTime();
				return 2 + powered * (((time >> 2) & 3) + 1);
			}

			int open = tile != null ? (((TileEntityInventoryStocker)tile).doorOpenOnSide(i) ? 2 : 0) : 0;

			if (side == 1) // Back
			{
				return 32 + powered + open;
			}

			return 16 + powered + open; // Top, Bottom, Left, Right
		}
		return i;
	}

	private int determineOrientation(World world, int x, int y, int z, EntityPlayer player)
	{
		if (player.rotationPitch > 45D)
		{
			return 0;
		}

		if (player.rotationPitch < -45D)
		{
			return 1;
		}

		int dir = MathHelper.floor_double((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		return dir == 0 ? 3 : (dir == 1 ? 4 : (dir == 2 ? 2 : (dir == 3 ? 5 : 0)));
	}

	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving par5EntityLiving)
	{
		int dir = determineOrientation(world, x, y, z, (EntityPlayer)par5EntityLiving);
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if(tile instanceof TileEntityInventoryStocker)
		{
			((TileEntityInventoryStocker)tile).Metainfo = dir;
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
		else if (InventoryStocker.proxy.isServer())
		{
			// Prevent GUI pop-up and handle block rotation
			if (entityplayer.isSneaking())
			{
				if (entityplayer.getCurrentEquippedItem() == null)
				{
					TileEntity tile = world.getBlockTileEntity(x, y, z);
					if(tile instanceof TileEntityInventoryStocker)
					{
						int Metainfo = ((TileEntityInventoryStocker)tile).Metainfo;
						int dir = Metainfo & 7; // Get orientation from first 3 bits of meta data
						Metainfo ^= dir; // Clear those bits
						++dir; // Rotate

						if (dir > 5)
						{
							dir = 0;    // Start over
						}

						Metainfo |= dir; // Write orientation back to meta data value

						((TileEntityInventoryStocker)tile).Metainfo = Metainfo;
						world.markBlockNeedsUpdate(x, y, z);
					}
					//prevent GUI popup when sneaking and your hand is empty
					return false;
				}
				if (Utils.isDebug()) System.out.println("Block.isServer.isSneaking");
				// Prevent GUI popup when sneaking but with something in your hand
				// This allows you to sneak place things directly on the inventory stocker
				return false;
			}

			//If we got here, we're not sneaking, time to get to work opening the GUI
			// Duplicate part of onNeighborBlockChange to ensure status is up-to-date before GUI opens
			TileEntityInventoryStocker tile = (TileEntityInventoryStocker)world.getBlockTileEntity(x, y, z);
			if (tile != null)
			{
				tile.onUpdate();
			}
			if (Utils.isDebug()) System.out.println("BlockInventoryStocker.onBlockActivated.openGUI");
			if(Utils.isDebug())
			{
				if(entityplayer instanceof EntityPlayerMP)
				{
					System.out.println("Block-EntityPlayer instance of EntityPlayerMP");
				}
				else
				{
					System.out.println("Block-EntityPlayer NOT instance of EntityPlayerMP");
				}
			}
			entityplayer.openGui(InventoryStocker.instance, 1, world, x, y, z);
			return true;
		}
		if (Utils.isDebug()) System.out.println("Block.onBlockActivated.fallthrough-should not happen?");
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		if (Utils.isDebug()) System.out.println("BlockInventoryStocker.createTileEntity");
		return new TileEntityInventoryStocker();
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
	public boolean hasTileEntity(int metadata)
	{
		return true;
	}

	/**
	 * This is called when something changes near our block, we use it to detect placement of pipes/tubes
	 * so we can update our textures. We also use it to verify the attached storage inventory hasn't been
	 * removed or changed.
	 */
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
	{
		if (Utils.isDebug()) System.out.println("BlockInventoryStocker.onNeighborBlockChange");
		TileEntityInventoryStocker tile = (TileEntityInventoryStocker)world.getBlockTileEntity(x, y, z);
		if (tile != null)
		{
			tile.onUpdate();
		}
		super.onNeighborBlockChange(world, x, y, z, blockID);
	}

	public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int par1)
	{
		preDestroyBlock(world, x, y, z);
		if (Utils.isDebug()) System.out.println("BlockInventoryStocker.onBlockDestroyedByPlayer");
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

		if (tile instanceof IInventory && !InventoryStocker.proxy.isClient())
		{
			dropItems(world, (IInventory) tile, i, j, k);
			tile.invalidate();
		}
	}
}
