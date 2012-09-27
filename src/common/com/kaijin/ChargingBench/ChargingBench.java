package com.kaijin.ChargingBench;

import java.io.File;

import net.minecraft.src.Block;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.Entity;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;

import com.kaijin.ChargingBench.*;

import ic2.api.*;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "ChargingBench", name="Charging Bench", version="1.3.2.a1", dependencies = "required-after:IC2@[1.106,);required-after:Forge@[4.1.1.251,)")
@NetworkMod(clientSideRequired = true, serverSideRequired = false,
clientPacketHandlerSpec = @SidedPacketHandler(channels = {"ChargingBench"}, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = ("ChargingBench"), packetHandler = ServerPacketHandler.class))
public class ChargingBench
{
	@SidedProxy(clientSide = "com.kaijin.ChargingBench.ClientProxy", serverSide = "com.kaijin.ChargingBench.CommonProxy")
	public static CommonProxy proxy; //This object will be populated with the class that you choose for the environment

	@Instance("ChargingBench")
	public static ChargingBench instance; //The instance of the mod that will be defined, populated, and callable

	static int ChargingBenchBlockID;

	// Constants for use in multiple classes
	static final int slotInput = 0;
	static final int slotOutput = 1;
	static final int slotPowerSource = 2;
	static final int slotCharging = 3;
	static final int slotUpgrade = 15;
	
	static final int inventorySize = 19;

	static ItemStack ic2overclockerUpg;
	static ItemStack ic2transformerUpg;
	static ItemStack ic2storageUpg;

	public static boolean isDebugging;

	@PreInit
	public static void preInit(FMLPreInitializationEvent event)
	{
		try
		{

			Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
			configuration.load();
			ChargingBenchBlockID = configuration.getOrCreateBlockIdProperty("ChargingBench", 2491).getInt();
			isDebugging = Boolean.parseBoolean((configuration.getOrCreateBooleanProperty("debug", configuration.CATEGORY_GENERAL, false).value));
			configuration.save();
		}
		catch (Exception var1)
		{
			System.out.println("[ChargingBench] Error while trying to access configuration!");
			throw new RuntimeException(var1);
		}

	}

	public static Block ChargingBench;

	@Init
	public void load(FMLInitializationEvent event)
	{
		ChargingBench = new BlockChargingBench(ChargingBenchBlockID, 0, Material.ground).setHardness(0.75F).setResistance(5F).setStepSound(Block.soundStoneFootstep).setBlockName("ChargingBench").setCreativeTab(CreativeTabs.tabDeco);
		LanguageRegistry.addName(ChargingBench, "Charging Bench");
		GameRegistry.registerBlock(ChargingBench, ItemChargingBench.class);

		GameRegistry.registerTileEntity(TEChargingBench1.class, "LV Charging Bench");
		GameRegistry.registerTileEntity(TEChargingBench2.class, "MV Charging Bench");
		GameRegistry.registerTileEntity(TEChargingBench3.class, "HV Charging Bench");
		
		//Test Blocks, emit 32, 128 or 512 EU/T
		GameRegistry.registerTileEntity(TEEmitter1.class, "LV Emitter");
		GameRegistry.registerTileEntity(TEEmitter2.class, "MV Emitter");
		GameRegistry.registerTileEntity(TEEmitter3.class, "HV Emitter");
		GameRegistry.registerTileEntity(TEEmitter4.class, "EV Emitter");

		LanguageRegistry.instance().addStringLocalization("blockChargingBench1.name", "LV Charging Bench");
		LanguageRegistry.instance().addStringLocalization("blockChargingBench2.name", "MV Charging Bench");
		LanguageRegistry.instance().addStringLocalization("blockChargingBench3.name", "HV Charging Bench");
		
		LanguageRegistry.instance().addStringLocalization("blockEmitterBlock1.name", "LV Emitter");
		LanguageRegistry.instance().addStringLocalization("blockEmitterBlock2.name", "MV Emitter");
		LanguageRegistry.instance().addStringLocalization("blockEmitterBlock3.name", "HV Emitter");
		LanguageRegistry.instance().addStringLocalization("blockEmitterBlock4.name", "EV Emitter");

		NetworkRegistry.instance().registerGuiHandler(this.instance, proxy);
		proxy.load();
		if (proxy.isServer())
		{
			FMLLog.getLogger().info ("ChargingBench loaded.");
		}
		if (isDebugging)
		{
			FMLLog.getLogger().info("ChargingBench debugging enabled.");
		}

		// For internal reference to verify items can be placed in inventory.
		ic2overclockerUpg = Items.getItem("overclockerUpgrade").copy();
		ic2transformerUpg = Items.getItem("transformerUpgrade").copy();
		ic2storageUpg = Items.getItem("energyStorageUpgrade").copy();

		// Also adding them to the creative inventory, since current IC2 version doesn't.
		ic2overclockerUpg.getItem().setTabToDisplayOn(CreativeTabs.tabMisc);
		ic2transformerUpg.getItem().setTabToDisplayOn(CreativeTabs.tabMisc);
		ic2storageUpg.getItem().setTabToDisplayOn(CreativeTabs.tabMisc);

	}

	@PostInit
	public void modsLoaded(FMLPostInitializationEvent event)
	{
		if (Utils.isDebug()) System.out.println("ChargingBench.modsLoaded");

		//new and improved recipes for a new and improved ChargingBench
		GameRegistry.addRecipe(new ItemStack(ChargingBench, 1, 0), new Object[] {"UUU", "WCW", "WWW", 'U', Items.getItem("copperCableItem"), 'W', Block.planks, 'C', Items.getItem("batBox")});
		GameRegistry.addRecipe(new ItemStack(ChargingBench, 1, 1), new Object[] {"UUU", "WCW", "WWW", 'U', Items.getItem("goldCableItem"), 'W', Block.planks, 'C', Items.getItem("mfeUnit")});
		GameRegistry.addRecipe(new ItemStack(ChargingBench, 1, 2), new Object[] {"UUU", "WCW", "WWW", 'U', Items.getItem("ironCableItem"), 'W', Block.planks, 'C', Items.getItem("mfsUnit")});
	}
}
