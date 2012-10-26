package com.kaijin.ChargingBench;

import ic2.api.Items;

import java.util.List;
import java.util.Random;

import net.minecraft.src.Block;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

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
		for (int i = 0; i < 10; ++i)
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
			else if (meta == 7 || meta == 8 || meta == 9)
			{
				entityplayer.openGui(ChargingBench.instance, 2, world, x, y, z);
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
		else if (tile instanceof TEBatteryStation) //FIXME fix textures
		{
			switch (side)
			{
			case 0: // bottom
				return 0;

			case 1: // top
				return meta + 9; // 16 + meta - 7;
			default:
				//int chargeLevel = ((TEBatteryStation)tile).chargeLevel * 16;
				int working = ((TEBatteryStation)tile).doingWork ? 3 : 0;
				//return this.sideTexture + meta + chargeLevel + working;
				return meta - 5 + working;
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
			if (meta < 7)
			{
				return this.baseTexture + meta;				
			}
			else
			{
				return meta + 9; //FIXME Fix texture for Discharging Bench
			}
			

		default: // side
			if (meta < 3)
			{
				return this.sideTexture + meta;
			}
			else if (meta > 6)
			{
				return meta - 5; //FIXME Fix texture for Discharging Bench
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

		case 7:
			return new TEBatteryStation1();

		case 8:
			return new TEBatteryStation2();

		case 9:
			return new TEBatteryStation3();

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
		case 7:
		case 8:
		case 9:
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

	public int damageDropped(int meta)
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
			else if (tile instanceof TECommonBench) 
			{
				((TECommonBench)tile).dropContents();
				tile.invalidate();
			}
			else if (tile instanceof TEBatteryStation) 
			{
				((TEBatteryStation)tile).dropContents();
				tile.invalidate();
			}
		}
	}
}
