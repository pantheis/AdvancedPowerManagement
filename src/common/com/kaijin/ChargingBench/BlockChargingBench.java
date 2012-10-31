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
	private final int baseTexture = 16;
	private final int sideTexture = 32;

	public BlockChargingBench(int i, int j, Material material)
	{
		super(i, j, material);
	}

	public void getSubBlocks(int blockID, CreativeTabs creativetabs, List list)
	{
		for (int i = 0; i <= ChargingBench.lastMetaValue; ++i)
		{
			if (i == 7) continue; //TODO Adjustable Emitter goes here, take this line out once it's added.
			list.add(new ItemStack(blockID, 1, i));
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int par6, float par7, float par8, float par9)
	{
		int currentEquippedItemID = 0;

		if (world.isRemote)
		{
			if (entityplayer.getCurrentEquippedItem() != null)
			{
				currentEquippedItemID = entityplayer.getCurrentEquippedItem().itemID;
			}

			if (entityplayer.isSneaking() || currentEquippedItemID == ChargingBench.ic2WrenchID || currentEquippedItemID == ChargingBench.ic2ElectricWrenchID)
			{
				// Prevent GUI popup when sneaking - this allows you to sneak place things directly on the charging bench
				//if (Utils.isDebug()) System.out.println("Block.world.isRemote.isSneaking");
				return false;
			}
		}
		else if (ChargingBench.proxy.isServer())
		{
			if (entityplayer.getCurrentEquippedItem() != null)
			{
				currentEquippedItemID = entityplayer.getCurrentEquippedItem().itemID;
			}

			if (entityplayer.isSneaking() || currentEquippedItemID == ChargingBench.ic2WrenchID || currentEquippedItemID == ChargingBench.ic2ElectricWrenchID)
			{
				// Prevent GUI popup when sneaking
				// This allows you to sneak place things directly on the charging bench
				return false;
			}
			//if (Utils.isDebug()) System.out.println("BlockChargingBench.BlockActivated");
			int meta = world.getBlockMetadata(x, y, z);
			if (meta >= 0 && meta <= 2)
			{
				entityplayer.openGui(ChargingBench.instance, 1, world, x, y, z);
				return true;
			}
			else if (meta >= 8 && meta <= 10)
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
		if (tile instanceof TEChargingBench) // TODO What's faster, TE instanceof tests or block metadata comparisons? We probably want to switch. 
		{
			switch (side)
			{
			case 0: // bottom
				return 0;

			case 1: // top
				return baseTexture + meta;

			default:
				int chargeLevel = ((TEChargingBench)tile).chargeLevel * 16;
				int working = ((TEChargingBench)tile).doingWork ? 3 : 0;
				return sideTexture + meta + chargeLevel + working;
			}
		}
		else if (tile instanceof TEEmitter)
		{
			switch (side)
			{
			case 0: // bottom
				return 0;

			default:
				return baseTexture + meta;
			}
		}
		else if (tile instanceof TEBatteryStation)
		{
			switch (side)
			{
			case 0: // bottom
				return 0;

			case 1: // top
				return meta + 8; // 16 + meta - 8 = 16 through 18

			default:
				int working = ((TEBatteryStation)tile).doingWork ? 3 : 0;
				return meta - 6 + working; // = 2 through 7
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
			if (meta < 8) // CB or emitter tops
			{
				return baseTexture + meta;				
			}
			else // Battery Station top
			{
				return meta + 8;
			}

		default: // side
			if (meta < 3) // Charging Bench
			{
				return sideTexture + meta;
			}
			else if (meta < 7) // Emitters
			{
				return baseTexture + meta;
			}
			else if (meta == 7) //TODO Adjustable Emitter goes here. Can it share the previous calculation?
			{
				return baseTexture + meta;
			}
			else if (meta < 11) // Battery Station
			{
				return meta - 5;
			}
			else //TODO Storage Monitor
			{
				return 0; 
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
		//if (Utils.isDebug()) System.out.println("BlockChargingBench.createTileEntity");
		switch (metadata)
		{
		case 0:
			return new TEChargingBench(1);

		case 1:
			return new TEChargingBench(2);

		case 2:
			return new TEChargingBench(3);

		case 3:
			return new TEEmitter(1);

		case 4:
			return new TEEmitter(2);

		case 5:
			return new TEEmitter(3);

		case 6:
			return new TEEmitter(4);

		case 7:
			return null; //TODO TEEmitterAdjustable goes here.
		case 8:
			return new TEBatteryStation(1);

		case 9:
			return new TEBatteryStation(2);

		case 10:
			return new TEBatteryStation(3);

		case 11:
			return new TEStorageMonitor();

		default:
			return null;
		}
	}

	@Override
	public boolean hasTileEntity(int metadata)
	{
		return metadata >= 0 && metadata <= ChargingBench.lastMetaValue;
	}

	public int idDropped(int var1, Random var2, int var3)
	{
		//if (Utils.isDebug()) System.out.println("BlockChargingBench.idDropped");
		return blockID;
	}

	public int damageDropped(int meta)
	{
		//if (Utils.isDebug()) System.out.println("BlockChargingBench.damageDropped");
		return meta;
	}

	@Override
	public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int par1)
	{
		preDestroyBlock(world, x, y, z);
		//if (Utils.isDebug()) System.out.println("BlockChargingBench.onBlockDestroyedByPlayer");
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
