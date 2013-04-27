/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockAdvPwrMan extends BlockContainer
{
	static final String[] tierPrefix = {"LV", "MV", "HV"};  

	//TODO Register GUI slot overlay icons!

	protected Icon benchBottom;
	protected Icon smTop;
	protected Icon smBottom;
	protected Icon smInvalid;
	protected Icon emitter;
	protected Icon[] benchTop;
	protected Icon[][][] cbSides;
	protected Icon[][] bsSides; 
	protected Icon[][] smSides;

	public BlockAdvPwrMan(int i, Material material)
	{
		super(i, material);
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

	@Override
	public void registerIcons(IconRegister iconRegister)
	{
		cbSides = new Icon[3][2][13];
		bsSides = new Icon[3][2];
		smSides = new Icon[2][13];
		benchTop = new Icon[3];
		benchBottom = iconRegister.registerIcon("AdvancedPowerManagement:BenchBottom");
		smTop = iconRegister.registerIcon("AdvancedPowerManagement:StorageMonitorTop");
		smBottom = iconRegister.registerIcon("AdvancedPowerManagement:StorageMonitorBottom");
		smInvalid = iconRegister.registerIcon("AdvancedPowerManagement:StorageMonitorInvalid");
		emitter = iconRegister.registerIcon("AdvancedPowerManagement:Emitter");
		for (int i = 0; i < 13; i++)
		{
			String temp = Integer.toString(i);
			for (int j = 0; j < 3; j++)
			{
				cbSides[j][0][i] = iconRegister.registerIcon("AdvancedPowerManagement:" + tierPrefix[j] + "ChargingBenchOff" + temp);
				cbSides[j][1][i] = iconRegister.registerIcon("AdvancedPowerManagement:" + tierPrefix[j] + "ChargingBenchOn" + temp);
			}
			smSides[0][i] = iconRegister.registerIcon("AdvancedPowerManagement:StorageMonitorOff" + temp);
			smSides[1][i] = iconRegister.registerIcon("AdvancedPowerManagement:StorageMonitorOn" + temp);
		}
		for (int j = 0; j < 3; j++)
		{
			benchTop[j] = iconRegister.registerIcon("AdvancedPowerManagement:" + tierPrefix[j] + "ChargingBenchTop");
			bsSides[j][0] = iconRegister.registerIcon("AdvancedPowerManagement:" + tierPrefix[j] + "BatteryStationOff");
			bsSides[j][1] = iconRegister.registerIcon("AdvancedPowerManagement:" + tierPrefix[j] + "BatteryStationOn");
		}
	}

	//Textures in the world
	@SideOnly(Side.CLIENT)
	public Icon getBlockTexture(IBlockAccess blocks, int x, int y, int z, int side)
	{
		final int meta = blocks.getBlockMetadata(x, y, z);
		TileEntity tile = blocks.getBlockTileEntity(x, y, z);
		if (tile instanceof TEChargingBench) 
		{
			switch (side)
			{
			case 0: // bottom
				return benchBottom;

			case 1: // top
				return benchTop[meta];

			default:
				return cbSides[meta][((TEChargingBench)tile).doingWork ? 1 : 0][((TEChargingBench)tile).chargeLevel];
			}
		}
		else if (tile instanceof TEAdvEmitter)
		{
			return emitter;
		}
		else if (tile instanceof TEBatteryStation)
		{
			switch (side)
			{
			case 0: // bottom
				return benchBottom;

			case 1: // top
				return benchTop[meta - 8];

			default:
				return bsSides[meta - 8][((TEBatteryStation)tile).doingWork ? 1 : 0];
			}
		}
		else if (tile instanceof TEStorageMonitor)
		{
			switch (side)
			{
			case 0: // bottom
				return smBottom;

			case 1: // top
				return smTop;

			default:
				if (((TEStorageMonitor)tile).blockState)
				{
					return smSides[((TEStorageMonitor)tile).isPowering ? 1 : 0][((TEStorageMonitor)tile).chargeLevel];
				}
				else return smInvalid;
			}
		}

		//If we're here, something is wrong
		return benchBottom;
	}

	//Textures in your inventory
	@Override
	public Icon getIcon(int side, int meta)
	{
		if (meta >= 3 && meta < 8)
		{
			return emitter;
		}
		switch (side)
		{
		case 0: // bottom
			return meta == 11 ? smBottom : benchBottom;

		case 1: // top
			if (meta < 3) // CB tops
			{
				return benchTop[meta];				
			}
			else if (meta < 11) // Battery Station top
			{
				return benchTop[meta - 8];
			}
			else
			{
				return smTop;
			}

		default: // side
			if (meta < 3) // Charging Bench
			{
				return cbSides[meta][0][0];
			}
			else if (meta < 11) // Battery Station
			{
				return cbSides[meta - 8][0][0];
			}
			else
			{
				return smInvalid;
			}
		}
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess block, int x, int y, int z, int side)
	{
		TileEntity tile = block.getBlockTileEntity(x, y, z);
		return tile instanceof TEStorageMonitor && ((TEStorageMonitor)tile).isPowering ? 1 : 0; // TODO Verify this works properly
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess block, int x, int y, int z, int side)
	{
		return 0;
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
	public TileEntity createNewTileEntity(World world)
	{
		return null;
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
