/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/

package com.kaijin.AdvPowerMan;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CButton extends GuiButton
{
	/** Path to custom texture for button */
	protected String texture;

	protected int uLoc;
	protected int vLoc;
	protected int uHoverLoc;
	protected int vHoverLoc;
	protected int color;
	protected int hoverColor;
	protected boolean isHovering;

	/** CButton will assume the texture size is equal to the width and height of the button
	 * 
	 * @param id          - ID of button
	 * @param xLoc        - x location of button on screen
	 * @param yLoc        - y location of button on screen
	 * @param width       - width of button
	 * @param height      - height of button
	 * @param uLoc        - x location of start of texture in texture file
	 * @param vLoc        - y location of start of texture in texture file
	 * @param uHoverLoc   - x location of start of texture for mouse over in texture file
	 * @param vHoverLoc   - x location of start of texture for mouse over in texture file
	 * @param text        - text to display on button
	 * @param color       - color for the text
	 * @param hoverColor  - color for the text while hovering
	 * @param texture     - path to texture file 
	 */
	public CButton(int id, int xLoc, int yLoc, int width, int height, int uLoc, int vLoc, int uHoverLoc, int vHoverLoc, String text, int color, int hoverColor, String texture)
	{
		super(id, xLoc, yLoc, width, height, text);
		this.enabled = true;
		this.drawButton = true;
		this.id = id;
		this.xPosition = xLoc;
		this.yPosition = yLoc;
		this.width = width;
		this.height = height;
		this.uLoc = uLoc;
		this.vLoc = vLoc;
		this.uHoverLoc = uHoverLoc;
		this.vHoverLoc = vHoverLoc;
		this.displayString = text;
		this.color = color;
		this.hoverColor = hoverColor;
		this.texture = texture;
	}

	/**
	 * Draws this button to the screen.
	 */
	@Override
	public void drawButton(Minecraft mc, int xLoc, int yLoc)
    {
		if (drawButton)
		{
			FontRenderer fr = mc.fontRenderer;

			if (texture != null)
			{
				int textureID = mc.renderEngine.getTexture(texture);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				mc.renderEngine.bindTexture(textureID);
			}

			isHovering = xLoc >= xPosition && yLoc >= yPosition && xLoc < xPosition + width && yLoc < yPosition + height;

			int hoverState = this.getHoverState(isHovering);
			if (hoverState == 2)
			{
				this.drawTexturedModalRect(xPosition, yPosition, uHoverLoc, vHoverLoc, width, height);
			}
			else
			{
				this.drawTexturedModalRect(xPosition, yPosition, uLoc, vLoc, width, height);
			}

			int defaultColor = color;
			int renderColor = defaultColor;
			
			if (!enabled)
			{
			    renderColor = -6250336;
			}
			else if (isHovering)
			{
			    renderColor = hoverColor;
			}

			fr.drawString(displayString, xPosition + (width - fr.getStringWidth(displayString)) / 2, yPosition + (height - 7) / 2, renderColor);
		}
    }
}
