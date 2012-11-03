package com.kaijin.AdvPowerMan;

import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

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
        this.displayString = text;
        this.color = color;
        this.texture = texture;
        this.uLoc = uLoc;
        this.vLoc = vLoc;
        this.uHoverLoc = uHoverLoc;
        this.vHoverLoc = vHoverLoc;
        this.hoverColor = hoverColor;
	}
	
    /**
     * Draws this button to the screen.
     */
	@Override
    public void drawButton(Minecraft mc, int xLoc, int yLoc)
    {
        if (this.drawButton)
        {
            FontRenderer fr = mc.fontRenderer;
            int textureID = mc.renderEngine.getTexture(Info.GUI3_PNG);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            mc.renderEngine.bindTexture(textureID);
            this.isHovering = xLoc >= this.xPosition && yLoc >= this.yPosition && xLoc < this.xPosition + this.width && yLoc < this.yPosition + this.height;
            int hoverState = this.getHoverState(isHovering);
            if (hoverState == 2)
            {
            	this.drawTexturedModalRect(this.xPosition, this.yPosition, this.uHoverLoc, this.vHoverLoc, this.width, this.height);
            }
            else
            {
            	this.drawTexturedModalRect(this.xPosition, this.yPosition, this.uLoc, this.vLoc, this.width, this.height);
            }
            this.mouseDragged(mc, xLoc, yLoc);
            int defaultColor = this.color;
            int renderColor = defaultColor;
            
            if (!this.enabled)
            {
                renderColor = -6250336;
            }
            else if (this.isHovering)
            {
                renderColor = this.hoverColor;
            }
            fr.drawString(displayString, xPosition + width / 2 - (fr.getStringWidth(displayString) / 2), yPosition + (height - 7) / 2, renderColor);
            //this.drawCenteredString(fr, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, renderColor);
        }
    }
	
}