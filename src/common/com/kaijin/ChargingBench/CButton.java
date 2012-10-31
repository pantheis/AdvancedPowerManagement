package com.kaijin.ChargingBench;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

@SideOnly(Side.CLIENT)
public class CButton extends GuiButton
{
    /** Path to custom texture for button */
    protected String texture;
    
    protected int uLoc;
    protected int vLoc;
    protected int uHoverOffset;
    protected int vHoverOffset;
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
	 * @param uHoverOffset - x pixel offset for mouse-over texture
	 * @param vHoverOffset - y pixel offset for mouse-over texture
	 * @param text        - text to display on button
	 * @param color       - color for the text
	 * @param hoverColor  - color for the text while hovering
	 * @param texture     - path to texture file 
	 */
	public CButton(int id, int xLoc, int yLoc, int width, int height, int uLoc, int vLoc, int uHoverOffset, int vHoverOffset, String text, int color, int hoverColor, String texture)
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
        this.vLoc = yLoc;
        this.uHoverOffset = uHoverOffset;
        this.vHoverOffset = vHoverOffset;
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
            int textureID = mc.renderEngine.getTexture(ChargingBench.proxy.GUI3_PNG);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            mc.renderEngine.bindTexture(textureID);
            this.isHovering = xLoc >= this.xPosition && yLoc >= this.yPosition && xLoc < this.xPosition + this.width && yLoc < this.yPosition + this.height;
            int hoverState = this.getHoverState(this.field_82253_i);
            if (hoverState == 1) hoverState = 0;
            this.drawTexturedModalRect(this.xPosition, this.yPosition, this.uLoc + (hoverState * this.uHoverOffset), this.vLoc + (hoverState * this.vHoverOffset), this.width, this.height);
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
            this.drawCenteredString(fr, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, renderColor);
        }
    }
	
}