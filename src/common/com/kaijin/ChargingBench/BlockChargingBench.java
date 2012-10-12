package com.kaijin.ChargingBench;

import java.util.*;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import net.minecraft.src.*;

import com.kaijin.ChargingBench.*;
import ic2.api.*;

public class BlockChargingBench extends Block
{
	//base texture index
	private int baseTexture = 16;
	private int sideTexture = 32;

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
				// This allows you to sneak place things directly on the charging bench
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
				// This allows you to sneak place things directly on the charging bench
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

	//Textures in the world
	@SideOnly(Side.CLIENT)
	public int getBlockTexture(IBlockAccess blocks, int x, int y, int z, int side)
	{
		int meta = blocks.getBlockMetadata(x, y, z);
		TileEntity tile = blocks.getBlockTileEntity(x, y, z);
		if (tile instanceof TEChargingBench)
		{
			switch (side)
			{
			case 0: // bottom
				return 0;

			case 1: // top
				return this.baseTexture + meta;
			default:
				int chargeLevel = ((TEChargingBench)tile).chargeLevel * 16;
				int working = ((TEChargingBench)tile).doingWork ? 3 : 0;
				return this.sideTexture + meta + chargeLevel + working;
			}
		}
		else if (tile instanceof TEEmitter)
		{
			switch (side)
			{
			case 0: // bottom
				return 0;

			default:
				return this.baseTexture + meta;
			}
		}
		//If we're here, something is wrong
		return side;
	}

	//Textures in your inventory
	@Override
	public int getBlockTextureFromSideAndMetadata(int i, int meta)
	{
		switch (i)
		{
		case 0: // bottom
			return 0;

		case 1: // top
			return this.baseTexture + meta;

		default:
			if (meta < 3)
			{
				return this.sideTexture + meta;
			}
			else
			{
				return this.baseTexture + meta;
			}
		}
	}

	@Override
	public boolean canProvidePower()
	{
		return false; // Old means of causing visual RedPower wire connections.
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int direction)
	{
		if(world.getBlockMetadata(x, y, z) < 3)
		{
			return false;
		}
		return true;
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
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
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
		preDestroyBlock(world, x, y, z);
		if (Utils.isDebug()) System.out.println("BlockChargingBench.onBlockDestroyedByPlayer");
		super.onBlockDestroyedByPlayer(world, x, y, z, par1);
	}

	public static void preDestroyBlock(World world, int i, int j, int k)
	{
		if (!ChargingBench.proxy.isClient())
		{
			TileEntity tile = world.getBlockTileEntity(i, j, k);

			if (tile instanceof TEEmitter)
			{
				tile.invalidate();
			}
			else if (tile instanceof TEChargingBench) 
			{
				((TEChargingBench)tile).dropContents();
				tile.invalidate();
			}
		}
	}
}
