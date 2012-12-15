/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

public class BlockAdvPwrMan extends Block
{
	//base texture index
	private final int baseTexture = 16;
	private final int sideTexture = 32;

	public BlockAdvPwrMan(int i, int j, Material material)
	{
		super(i, j, material);
	}

    @SideOnly(Side.CLIENT)
    @Override
	public void getSubBlocks(int blockID, CreativeTabs creativetabs, List list)
	{
		for (int i = 0; i <= Info.LAST_META_VALUE; ++i)
		{
			if (i >= 3 && i <= 6) continue; // Don't add legacy emitters to creative inventory
			list.add(new ItemStack(blockID, 1, i));
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int par6, float par7, float par8, float par9)
	{
		//int currentEquippedItemID = 0; //TODO We're not currently responding to wrenches

		//if (entityplayer.getCurrentEquippedItem() != null)
		//{
		//	currentEquippedItemID = entityplayer.getCurrentEquippedItem().itemID;
		//}

		//if (entityplayer.isSneaking() || currentEquippedItemID == Info.ic2WrenchID || currentEquippedItemID == Info.ic2ElectricWrenchID)
		if (entityplayer.isSneaking())
		{
			// Prevent GUI popup when sneaking - this allows you to place things directly on blocks
			return false;
		}

		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (tile instanceof TECommon)
		{
			final int id = ((TECommon)tile).getGuiID();
			if (id < 1) return false;
			if (AdvancedPowerManagement.proxy.isServer())
			{
				entityplayer.openGui(AdvancedPowerManagement.instance, id, world, x, y, z);
			}
		}

		return true;
	}

	public String getTextureFile()
	{
		return Info.BLOCK_PNG;
	}

	//Textures in the world
	@SideOnly(Side.CLIENT)
	public int getBlockTexture(IBlockAccess blocks, int x, int y, int z, int side)
	{
		final int meta = blocks.getBlockMetadata(x, y, z);
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
				final int chargeLevel = ((TEChargingBench)tile).chargeLevel * 16;
				final int working = ((TEChargingBench)tile).doingWork ? 3 : 0;
				return sideTexture + meta + chargeLevel + working;
			}
		}
		else if (tile instanceof TEAdvEmitter)
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
				final int working = ((TEBatteryStation)tile).doingWork ? 3 : 0;
				return meta - 6 + working; // = 2 through 7
			}
		}
		else if (tile instanceof TEStorageMonitor)
		{
			switch (side)
			{
			case 0: // bottom
				return 15;

			case 1: // top
				return 14;

			default:
				if (((TEStorageMonitor)tile).blockState)
				{
					final int chargeLevel = ((TEStorageMonitor)tile).chargeLevel * 16;
					final int isPowering = ((TEStorageMonitor)tile).isPowering ? 1 : 0;
					return 30 + isPowering + chargeLevel;
				}
				else return 238;
			}
		}

		//If we're here, something is wrong
		return 0;
	}

	//Textures in your inventory
	@Override
	public int getBlockTextureFromSideAndMetadata(int i, int meta)
	{
		switch (i)
		{
		case 0: // bottom
			return meta < 11 ? 0 : 15;

		case 1: // top
			if (meta < 8) // CB or emitter tops
			{
				return baseTexture + meta;				
			}
			else if (meta < 11) // Battery Station top
			{
				return meta + 8;
			}
			else
			{
				return 14;
			}

		default: // side
			if (meta < 3) // Charging Bench
			{
				return sideTexture + meta;
			}
			else if (meta < 8) // Emitters
			{
				return baseTexture + meta;
			}
			else if (meta < 11) // Battery Station
			{
				return meta - 6;
			}
			else
			{
				return 238; 
			}
		}
	}

	@Override
	public boolean isProvidingWeakPower(IBlockAccess block, int x, int y, int z, int side)
	{
		TileEntity tile = block.getBlockTileEntity(x, y, z);
		return tile instanceof TEStorageMonitor && ((TEStorageMonitor)tile).isPowering;
	}

	@Override
	public boolean isProvidingStrongPower(IBlockAccess block, int x, int y, int z, int side)
	{
		return false;
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
	public boolean isBlockNormalCube(World world, int x, int y, int z)
	{
		return false;
	}

	@Override
	public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		//if (ChargingBench.isDebugging) System.out.println("BlockAdvPwrMan.createTileEntity");
		switch (metadata)
		{
		case 0:
			return new TEChargingBench(1);

		case 1:
			return new TEChargingBench(2);

		case 2:
			return new TEChargingBench(3);

		case 3:
			return new TEAdvEmitter(1); // Update old emitter tier 1

		case 4:
			return new TEAdvEmitter(2); // Update old emitter tier 2

		case 5:
			return new TEAdvEmitter(3); // Update old emitter tier 3

		case 6:
			return new TEAdvEmitter(4); // Update old emitter tier 4

		case 7:
			return new TEAdvEmitter();
			
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
		return metadata >= 0 && metadata <= Info.LAST_META_VALUE;
	}

	public int idDropped(int var1, Random var2, int var3)
	{
		//if (ChargingBench.isDebugging) System.out.println("BlockAdvPwrMan.idDropped");
		return blockID;
	}

	public int damageDropped(int meta)
	{
		//if (ChargingBench.isDebugging) System.out.println("BlockAdvPwrMan.damageDropped");
		return meta;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int meta)
	{
		preDestroyBlock(world, x, y, z);
	}

	public static void preDestroyBlock(World world, int i, int j, int k)
	{
		if (!AdvancedPowerManagement.proxy.isClient())
		{
			TileEntity tile = world.getBlockTileEntity(i, j, k);
			if (tile == null) return;
			try
			{
				((TECommon)tile).dropContents();
			}
			catch (ClassCastException e)
			{
				FMLLog.getLogger().warning(Info.TITLE_LOG + "Attempted to destroy APM block with non-APM tile entity at: " + i + ", " + j + ", " + k);
			}
			tile.invalidate();
		}
	}
}
