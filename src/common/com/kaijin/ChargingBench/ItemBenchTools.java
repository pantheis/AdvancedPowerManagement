package com.kaijin.ChargingBench;

import java.util.List;

import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MathHelper;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

public class ItemBenchTools extends Item
{
	public static final String[] benchToolsNames = new String[] {"toolkit", "LV-kit", "MV-kit", "HV-kit"};

	public ItemBenchTools(int par1)
	{
		super(par1);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setMaxStackSize(1);
        this.setCreativeTab(CreativeTabs.tabMisc);
	}

	@Override
	public String getTextureFile()
	{
		return ChargingBench.proxy.ITEM_PNG;
	}

	/**
     * Gets an icon index based on an item's damage value
     */
    @SideOnly(Side.CLIENT)
    public int getIconFromDamage(int par1)
    {
        return MathHelper.clamp_int(par1, 0, 3);
    }

	@Override
	public boolean isRepairable()
	{
		return false;
	}

    /**
     * If this returns true, after a recipe involving this item is crafted the container item will be added to the
     * player's inventory instead of remaining in the crafting grid.
     */
/*	public boolean doesContainerItemLeaveCraftingGrid(ItemStack par1ItemStack)
	{
		return false;
	}

	public ItemStack getContainerItemStack(ItemStack itemStack)
	{
		if (itemStack.getItemDamage() == 0) return new ItemStack(ChargingBench.ItemBenchTools, 1, 0);
		return null;
	}

	public Item getContainerItem()
	{
		return this;
	}

	public boolean hasContainerItem()
	{
		return false;
	}
*/
	public String getItemNameIS(ItemStack par1ItemStack)
	{
		int meta = MathHelper.clamp_int(par1ItemStack.getItemDamage(), 0, 3);
		return "item.benchTools." + benchToolsNames[meta];
	}
	
	protected void generateItemStack(ItemStack stack, EntityPlayer player)
	{
		EntityItem entityitem = player.dropPlayerItemWithRandomChoice(stack, false);
		entityitem.delayBeforeCanPickup = 0;
	}
	
	/**
	 * This is called when the item is used, before the block is activated.
	 * @param stack The Item Stack
	 * @param player The Player that used the item
	 * @param world The Current World
	 * @param x Target X Position
	 * @param y Target Y Position
	 * @param z Target Z Position
	 * @param side The side of the target hit
	 * @return Return true to prevent any further processing.
	 */
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) 
	{
		if (ChargingBench.proxy.isClient()) return false;
	
		// Test if the target is a charging bench and the item is a component kit. If so, do the upgrade and return true.
		if (world.getBlockId(x, y, z) != ChargingBench.ChargingBenchBlockID || stack.getItemDamage() < 1 || stack.getItemDamage() > 3 || player == null)
		{
	        return false;
		}
	
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (!(tile instanceof TEChargingBench))
		{
			return false;
		}
	
		int recoveredTier = ((TEChargingBench)tile).swapBenchComponents(stack.getItemDamage());
		generateItemStack(new ItemStack(ChargingBench.ItemBenchTools, 1, recoveredTier), player);
		stack.stackSize--;
		return true;
	}
	
	/**
	 * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
	 */
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		if (player.isSneaking() && stack.getItemDamage() > 0 && stack.getItemDamage() < 4)
		{
			switch (stack.getItemDamage())
			{
			case 1:
				generateItemStack(ChargingBench.componentCopperCable.copy(), player);
				generateItemStack(ChargingBench.componentBatBox.copy(), player);
				break;
			case 2:
				generateItemStack(ChargingBench.componentGoldCable.copy(), player);
				generateItemStack(ChargingBench.componentMFE.copy(), player);
				break;
			case 3:
				generateItemStack(ChargingBench.componentIronCable.copy(), player);
				generateItemStack(ChargingBench.componentMFSU.copy(), player);
				break;
			}
			generateItemStack(ChargingBench.componentCircuit.copy(), player);
			stack.stackSize--;
		}
		return stack;
	}
	
	/**
	 * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
	 */
	@SideOnly(Side.CLIENT)
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for (int meta = 0; meta < 4; ++meta)
		{
			par3List.add(new ItemStack(par1, 1, meta));
		}
	}

}
