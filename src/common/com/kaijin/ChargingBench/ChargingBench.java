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

@Mod(modid = "ChargingBench", name="Charging Bench", version=Utils.VERSION, dependencies = "required-after:IC2@[1.106,);required-after:Forge@[4.1.1.251,)")
@NetworkMod(clientSideRequired = true, serverSideRequired = false,
clientPacketHandlerSpec = @SidedPacketHandler(channels = {"ChargingBench"}, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = ("ChargingBench"), packetHandler = ServerPacketHandler.class))
public class ChargingBench
{
	public static final String modNamePacked = "ChargingBench";
	public static final String modNameSpaced = "Charging Bench";
	public static final String emitterName = "Emitter";
	public static final String dischargerName = "Battery Station";

	public static final String benchToolsName = "BenchTools";
	public static final String toolkitName = "Charging Bench Toolkit";
	public static final String componentsName = modNameSpaced + " Components";

	@SidedProxy(clientSide = "com.kaijin.ChargingBench.ClientProxy", serverSide = "com.kaijin.ChargingBench.CommonProxy")
	public static CommonProxy proxy; //This object will be populated with the class that you choose for the environment

	@Instance(modNamePacked)
	public static ChargingBench instance; //The instance of the mod that will be defined, populated, and callable

	static int ChargingBenchBlockID;

	static int ItemBenchToolsID;

	// Constants for use in multiple classes
	static final int CBslotInput = 0;
	static final int CBslotOutput = 1;
	static final int CBslotPowerSource = 2;
	static final int CBslotCharging = 3;
	static final int CBslotUpgrade = 15;

	static final int BSslotInput = 0;
	static final int BSslotOutput = 1;
	static final int BSslotPowerSource = 2;
	
	static final int CBinventorySize = 19;
	static final int BSinventorySize = 14;

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
			ItemBenchToolsID = configuration.getOrCreateIntProperty(benchToolsName, configuration.CATEGORY_ITEM, 22499).getInt();
			ChargingBenchBlockID = configuration.getOrCreateBlockIdProperty(modNamePacked, 2491).getInt();
			isDebugging = Boolean.parseBoolean((configuration.getOrCreateBooleanProperty("debug", configuration.CATEGORY_GENERAL, false).value));
			configuration.save();
		}
		catch (Exception var1)
		{
			System.out.println("[" + modNamePacked + "] Error while trying to access configuration!");
			throw new RuntimeException(var1);
		}
	}

	public static Block ChargingBench;
	public static Item ItemBenchTools;

	@Init
	public void load(FMLInitializationEvent event)
	{
		ChargingBench = new BlockChargingBench(ChargingBenchBlockID, 0, Material.ground).setHardness(0.75F).setResistance(5F).setStepSound(Block.soundStoneFootstep).setBlockName("ChargingBench").setCreativeTab(CreativeTabs.tabDeco);
		//LanguageRegistry.addName(ChargingBench, modNameSpaced);
		GameRegistry.registerBlock(ChargingBench, ItemChargingBench.class);

		//Charging Bench blocks
		GameRegistry.registerTileEntity(TEChargingBench1.class, "LV " + modNameSpaced);
		GameRegistry.registerTileEntity(TEChargingBench2.class, "MV " + modNameSpaced);
		GameRegistry.registerTileEntity(TEChargingBench3.class, "HV " + modNameSpaced);

		LanguageRegistry.instance().addStringLocalization("blockChargingBench1.name", "LV " + modNameSpaced);
		LanguageRegistry.instance().addStringLocalization("blockChargingBench2.name", "MV " + modNameSpaced);
		LanguageRegistry.instance().addStringLocalization("blockChargingBench3.name", "HV " + modNameSpaced);

		//Discharging Bench blocks
		GameRegistry.registerTileEntity(TEBatteryStation1.class, "LV " + dischargerName);
		GameRegistry.registerTileEntity(TEBatteryStation2.class, "MV " + dischargerName);
		GameRegistry.registerTileEntity(TEBatteryStation3.class, "HV " + dischargerName);

		LanguageRegistry.instance().addStringLocalization("blockBatteryStation1.name", "LV " + dischargerName);
		LanguageRegistry.instance().addStringLocalization("blockBatteryStation2.name", "MV " + dischargerName);
		LanguageRegistry.instance().addStringLocalization("blockBatteryStation3.name", "HV " + dischargerName);

		//Emitter Blocks, emit 32, 128 or 512 EU/T
		GameRegistry.registerTileEntity(TEEmitter1.class, "LV " + emitterName);
		GameRegistry.registerTileEntity(TEEmitter2.class, "MV " + emitterName);
		GameRegistry.registerTileEntity(TEEmitter3.class, "HV " + emitterName);
		GameRegistry.registerTileEntity(TEEmitter4.class, "EV " + emitterName);

		LanguageRegistry.instance().addStringLocalization("blockEmitterBlock1.name", "LV " + emitterName);
		LanguageRegistry.instance().addStringLocalization("blockEmitterBlock2.name", "MV " + emitterName);
		LanguageRegistry.instance().addStringLocalization("blockEmitterBlock3.name", "HV " + emitterName);
		LanguageRegistry.instance().addStringLocalization("blockEmitterBlock4.name", "EV " + emitterName);

		ItemBenchTools = new ItemBenchTools(ItemBenchToolsID).setItemName(toolkitName);
		LanguageRegistry.instance().addStringLocalization("benchTools.toolkit.name", toolkitName);
		LanguageRegistry.instance().addStringLocalization("benchTools.LV-kit.name", "LV " + componentsName);
		LanguageRegistry.instance().addStringLocalization("benchTools.MV-kit.name", "MV " + componentsName);
		LanguageRegistry.instance().addStringLocalization("benchTools.HV-kit.name", "HV " + componentsName);

		NetworkRegistry.instance().registerGuiHandler(this.instance, proxy);
		proxy.load();
		if (proxy.isServer())
		{
			FMLLog.getLogger().info (modNameSpaced + " " + Utils.VERSION + " loaded.");
		}
		if (isDebugging)
		{
			FMLLog.getLogger().info(modNameSpaced + " debugging enabled.");
		}

		// For internal reference to verify items can be placed in inventory.
		ic2overclockerUpg = Items.getItem("overclockerUpgrade").copy();
		ic2transformerUpg = Items.getItem("transformerUpgrade").copy();
		ic2storageUpg = Items.getItem("energyStorageUpgrade").copy();

		//TODO Remove this code when updating to MC 1.4, IC2 fixes this in future versions
		// Also adding them to the creative inventory, since current IC2 version doesn't.
		ic2overclockerUpg.getItem().setTabToDisplayOn(CreativeTabs.tabMisc);
		ic2transformerUpg.getItem().setTabToDisplayOn(CreativeTabs.tabMisc);
		ic2storageUpg.getItem().setTabToDisplayOn(CreativeTabs.tabMisc);
	}

	@PostInit
	public void modsLoaded(FMLPostInitializationEvent event)
	{
		if (Utils.isDebug()) System.out.println("ChargingBench.modsLoaded");

		// New and improved recipes for a new and improved ChargingBench
		GameRegistry.addRecipe(new ItemStack(ChargingBench, 1, 0), new Object[] {"UUU", "WCW", "WBW", 'U', Items.getItem("insulatedCopperCableItem"), 'W', Block.planks, 'C', Items.getItem("electronicCircuit"), 'B', Items.getItem("batBox")});
		GameRegistry.addRecipe(new ItemStack(ChargingBench, 1, 1), new Object[] {"UUU", "WCW", "WBW", 'U', Items.getItem("doubleInsulatedGoldCableItem"), 'W', Block.planks, 'C', Items.getItem("electronicCircuit"), 'B', Items.getItem("mfeUnit")});
		GameRegistry.addRecipe(new ItemStack(ChargingBench, 1, 2), new Object[] {"UUU", "WCW", "WBW", 'U', Items.getItem("trippleInsulatedIronCableItem"), 'W', Block.planks, 'C', Items.getItem("electronicCircuit"), 'B', Items.getItem("mfsUnit")});

		// Discharging Bench recipes
		GameRegistry.addRecipe(new ItemStack(ChargingBench, 1, 7), new Object[] {"UUU", "WCW", "WBW", 'U', Items.getItem("insulatedCopperCableItem"), 'W', Block.planks, 'C', Items.getItem("electronicCircuit"), 'B', Items.getItem("lvTransformer")});
		GameRegistry.addRecipe(new ItemStack(ChargingBench, 1, 8), new Object[] {"UUU", "WCW", "WBW", 'U', Items.getItem("doubleInsulatedGoldCableItem"), 'W', Block.planks, 'C', Items.getItem("electronicCircuit"), 'B', Items.getItem("mvTransformer")});
		GameRegistry.addRecipe(new ItemStack(ChargingBench, 1, 9), new Object[] {"UUU", "WCW", "WBW", 'U', Items.getItem("trippleInsulatedIronCableItem"), 'W', Block.planks, 'C', Items.getItem("electronicCircuit"), 'B', Items.getItem("hvTransformer")});

		// Bench Toolkit recipe
		GameRegistry.addRecipe(new ItemStack(ItemBenchTools, 1, 0), new Object[] {" I ", "S S", 'I', Item.ingotIron, 'S', Item.stick});
	}
}
