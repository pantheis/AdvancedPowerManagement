package com.kaijin.ChargingBench;

import java.util.List;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

import net.minecraft.src.*;

public class ItemBenchTools extends Item
{
	public static final String[] benchToolsNames = new String[] {"toolkit", "LV-kit", "MV-kit", "HV-kit"};

	public ItemBenchTools(int par1)
	{
		super(par1);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setTabToDisplayOn(CreativeTabs.tabMisc);
        //this.setItemName("benchTools");
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

/*	public String getItemName()
	{
		return "benchTools";
	}
*/
	public String getItemNameIS(ItemStack par1ItemStack)
    {
        int meta = MathHelper.clamp_int(par1ItemStack.getItemDamage(), 0, 3);
        return "benchTools." + benchToolsNames[meta];
    }

    public boolean tryPlaceIntoWorld(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10)
    {
        if (!par2EntityPlayer.canPlayerEdit(par4, par5, par6))
        {
            return false;
        }

        // do stuff

        return true;
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
