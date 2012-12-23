/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/

package com.kaijin.AdvPowerMan;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class Utils
{
	public boolean isClient()
	{
		return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
	}

	public boolean isServer()
	{
		return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER;
	}

	/**
	 * 
	 * @param fr    - Font Renderer handle
	 * @param text  - Text to display
	 * @param xLoc  - x location
	 * @param yLoc  - y location
	 * @param color - Color
	 */
	@SideOnly(Side.CLIENT)
	public static void drawCenteredText(FontRenderer fr, String text, int xLoc, int yLoc, int color)
	{
		fr.drawString(text, xLoc - fr.getStringWidth(text) / 2, yLoc, color);
	}

	/**
	 * 
	 * @param fr    - Font Renderer handle
	 * @param text  - Text to display
	 * @param xLoc  - x location
	 * @param yLoc  - y location
	 * @param color - Color
	 */
	@SideOnly(Side.CLIENT)
	public static void drawRightAlignedText(FontRenderer fr, String text, int xLoc, int yLoc, int color)
	{
		fr.drawString(text, xLoc - fr.getStringWidth(text), yLoc, color);
	}

	/**
	 * 
	 * @param fr    - Font Renderer handle
	 * @param text  - Text to display
	 * @param xLoc  - x location
	 * @param yLoc  - y location
	 * @param color - Color
	 */
	@SideOnly(Side.CLIENT)
	public static void drawLeftAlignedText(FontRenderer fr, String text, int xLoc, int yLoc, int color)
	{
		fr.drawString(text, xLoc, yLoc, color);
	}

	private static final int MASKR = 0xFF0000;
	private static final int MASKG = 0x00FF00;
	private static final int MASKB = 0x0000FF;

	/**
	 * Individually multiply R, G, B color components by scalar value to dim or brighten the color.
	 * Does not check for overflow. Beware when using values over 1.0F.
	 * @param color - original color
	 * @param brightnessFactor - should be positive and <> 1.0F
	 * @return - modified color
	 */
	public static int multiplyColorComponents(int color, float brightnessFactor)
	{
		return ((int)(brightnessFactor * (color & MASKR)) & MASKR)
			 | ((int)(brightnessFactor * (color & MASKG)) & MASKG)
			 | ((int)(brightnessFactor * (color & MASKB)) & MASKB);
	}

	public static int interpolateColors(int a, int b, float lerp)
	{
		final int MASK1 = 0xff00ff; 
		final int MASK2 = 0x00ff00; 

		int f2 = (int)(256 * lerp);
		int f1 = 256 - f2;

		return ((((( a & MASK1 ) * f1 ) + ( ( b & MASK1 ) * f2 )) >> 8 ) & MASK1 ) 
			 | ((((( a & MASK2 ) * f1 ) + ( ( b & MASK2 ) * f2 )) >> 8 ) & MASK2 );
	}

	public static final int GUIBACKGROUNDCOLOR = 0xC6C6C6;

	public static int overlayColors(int base, int over)
	{
		final float rDiff = 1F - ((float)(base & MASKR) / MASKR);
		final float gDiff = 1F - ((float)(base & MASKG) / MASKG);
		final float bDiff = 1F - ((float)(base & MASKB) / MASKB);

		final int r2 = (over & MASKR);
		final int g2 = (over & MASKG);
		final int b2 = (over & MASKB);

		return base + ((int)(rDiff * r2) & MASKR) + ((int)(gDiff * g2) & MASKG) + ((int)(bDiff * b2) & MASKB);
	}

	private static final int oX[] = {0, -1, 0, 1};
	private static final int oY[] = {-1, 0, 1, 0};

	/**
	 * Draws right-aligned text with a 'glow' surrounding it. 
	 * @param fr    - Font Renderer handle
	 * @param text  - Text to display
	 * @param xLoc  - x location (upper right corner)
	 * @param yLoc  - y location (upper right corner)
	 * @param color - Main Color
	 * @param glowColor - Surrounding Color
	 */
	@SideOnly(Side.CLIENT)
	public static void drawRightAlignedGlowingText(FontRenderer fr, String text, int xLoc, int yLoc, int color, int glowColor)
	{
		drawGlowingText(fr, text, xLoc - fr.getStringWidth(text), yLoc, color, glowColor);
	}

	/**
	 * Draws centered text with a 'glow' surrounding it. 
	 * @param fr    - Font Renderer handle
	 * @param text  - Text to display
	 * @param xLoc  - x location (top center)
	 * @param yLoc  - y location (top center)
	 * @param color - Main Color
	 * @param glowColor - Surrounding Color
	 */
	@SideOnly(Side.CLIENT)
	public static void drawCenteredGlowingText(FontRenderer fr, String text, int xLoc, int yLoc, int color, int glowColor)
	{
		drawGlowingText(fr, text, xLoc - fr.getStringWidth(text) / 2, yLoc, color, glowColor);
	}

	/**
	 * Draws left-aligned text with a 'glow' surrounding it. 
	 * @param fr    - Font Renderer handle
	 * @param text  - Text to display
	 * @param xLoc  - x location (upper left corner)
	 * @param yLoc  - y location (upper left corner)
	 * @param color - Main Color
	 * @param glowColor - Surrounding Color
	 */
	@SideOnly(Side.CLIENT)
	public static void drawGlowingText(FontRenderer fr, String text, int xLoc, int yLoc, int color, int glowColor)
	{
		for (int i = 0; i < 4; i++)
		{
			fr.drawString(text, xLoc + oX[i], yLoc + oY[i], glowColor);
		}
		fr.drawString(text, xLoc, yLoc, color);
	}

	/*
	 * Convert desired side to actual side based on orientation of block
	 * I  Meta
	 *    D U N S W E     0 1 2 3 4 5
	 * 0  F K T T T T   0 0 1 2 2 2 2
	 * 1  K F B B B B   1 1 0 3 3 3 3
	 * 2  T B F K L R   2 2 3 0 1 5 4
	 * 3  B T K F R L   3 3 2 1 0 4 5
	 * 4  L L L R F K   4 5 5 5 4 0 1
	 * 5  R R R L K F   5 4 4 4 5 1 0
	 *
	 */
	public static int lookupRotatedSide(int side, int orientation)
	{
		final int table[][] =
			{
				{0, 1, 2, 2, 2, 2},
				{1, 0, 3, 3, 3, 3},
				{2, 3, 0, 1, 5, 4},
				{3, 2, 1, 0, 4, 5},
				{5, 5, 5, 4, 0, 1},
				{4, 4, 4, 5, 1, 0}
			};
		return table[side][orientation];
	}

	public static NBTTagCompound getOrCreateStackTag(ItemStack itemStack)
	{
		if (itemStack != null)
		{
			NBTTagCompound tag = itemStack.getTagCompound();
			if (tag == null)
			{
				tag = new NBTTagCompound();
				itemStack.setTagCompound(tag);
			}
			return tag;
		}
		return null;
	}
}
