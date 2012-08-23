package net.minecraft.src;


import java.io.File;

import com.kaijin.ChargingBench.BlockChargingBench;
import com.kaijin.ChargingBench.GuiChargingBench;
import com.kaijin.ChargingBench.ItemChargingBench;
import com.kaijin.ChargingBench.TileEntityChargingBench;
import com.kaijin.ChargingBench.TileEntityChargingBench1;
import com.kaijin.ChargingBench.TileEntityChargingBench2;
import com.kaijin.ChargingBench.TileEntityChargingBench3;

import net.minecraft.src.forge.*;
import net.minecraft.src.*;
import net.minecraft.src.ic2.api.*;

public class mod_IC2_ChargingBench extends NetworkMod
{
    public static Configuration config;
    public static int idBlockChargingBench;
    public static Block blockChargingBench;
    public static int guiIdChargingBench;

    public void load()
    {
        blockChargingBench = new BlockChargingBench(idBlockChargingBench);
        ModLoader.registerBlock(blockChargingBench, ItemChargingBench.class);
        ModLoader.registerTileEntity(TileEntityChargingBench1.class, "Charging Bench Mk1");
        ModLoader.registerTileEntity(TileEntityChargingBench2.class, "Charging Bench Mk2");
        ModLoader.registerTileEntity(TileEntityChargingBench3.class, "Charging Bench Mk3");
        ModLoader.addRecipe(new ItemStack(blockChargingBench, 1, 0), new Object[] {"UUU", "W W", "WWW", 'U', Items.getItem("copperCableItem"), 'W', Block.planks});
        ModLoader.addRecipe(new ItemStack(blockChargingBench, 1, 1), new Object[] {"UUU", "WCW", "WWW", 'U', Items.getItem("goldCableItem"), 'W', Block.planks, 'C', Items.getItem("electronicCircuit")});
        ModLoader.addRecipe(new ItemStack(blockChargingBench, 1, 2), new Object[] {"UUU", "WCW", "WWW", 'U', Items.getItem("ironCableItem"), 'W', Block.planks, 'C', Items.getItem("advancedCircuit")});
        ModLoader.addLocalization("blockChargingBench1.name", "Charging Bench Mk1");
        ModLoader.addLocalization("blockChargingBench2.name", "Charging Bench Mk2");
        ModLoader.addLocalization("blockChargingBench3.name", "Charging Bench Mk3");
        MinecraftForgeClient.preloadTexture("/ic2/sprites/ChargingBench.png");
        ModLoaderMp.registerGUI(this, mod_IC2_ChargingBench.guiIdChargingBench);

    }

    public String getVersion()
    {
        return "1.90-1";
    }
    


    public static boolean launchGUI(EntityPlayer var0, TileEntity var1)
    {
    	ModLoader.openGUI(var0, new GuiChargingBench(var0.inventory, (TileEntityChargingBench)var1));
    	return true;
    }

    public GuiScreen handleGUI(int var1)
    {
    	EntityPlayerSP var2 = ModLoader.getMinecraftInstance().thePlayer;
    	return var2 == null ? null : new GuiChargingBench(var2.inventory, new TileEntityChargingBench(0));
    }


    static
    {
        try
        {
            config = new Configuration(new File(Platform.getMinecraftDir() + "/config/IC2ChargingBench.cfg"));
            config.load();
            idBlockChargingBench = Integer.valueOf(config.getOrCreateIntProperty("blockChargingBench", "block", 189).value).intValue();
            guiIdChargingBench = Integer.valueOf(config.getOrCreateIntProperty("guiIdChargingBench", "general", 110).value).intValue();
            config.save();
        }
        catch (Exception var1)
        {
            System.out.println("[ChargingBench] Error while trying to access configuration!");
            throw new RuntimeException(var1);
        }
    }
}
