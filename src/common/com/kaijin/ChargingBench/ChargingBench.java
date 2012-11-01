/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.ChargingBench;

import ic2.api.Items;
import net.minecraft.src.Block;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.ICraftingHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "ChargingBench", name="Charging Bench", version=CommonProxy.VERSION, dependencies = "required-after:IC2@[1.108,);required-after:Forge@[6.0.1.349,)")
@NetworkMod(clientSideRequired = true, serverSideRequired = false,
clientPacketHandlerSpec = @SidedPacketHandler(channels = {"ChargingBench"}, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = ("ChargingBench"), packetHandler = ServerPacketHandler.class))
public class ChargingBench implements ICraftingHandler
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

	public static Block blockChargingBench;
	public static Item itemBenchTools;
	public static Item itemStorageLinkCard;
	public static Item itemStorageLinkCardCreator;

	public static int blockChargingBenchID;
	public static int itemBenchToolsID;
	public static int itemStorageLinkCardID;
	public static int itemStorageLinkCardCreatorID;

	// Constants for use in multiple classes
	public static final String packetChannel = "ChargingBench"; // CHANNEL MAX 16 CHARS

	public static final int lastMetaValue = 11;

	static final int cbSlotInput = 0;
	static final int cbSlotOutput = 1;
	static final int cbSlotPowerSource = 2;
	static final int cbSlotCharging = 3;
	static final int cbSlotUpgrade = 15;

	static final int bsSlotInput = 0;
	static final int bsSlotOutput = 1;
	static final int bsSlotPowerSourceStart = 2;

	static final int smSlotUniversal = 0;

	static final int cbInventorySize = 19;
	static final int bsInventorySize = 14;
	static final int smInventorySize = 7;

	static ItemStack componentCopperCable;
	static ItemStack componentGoldCable;
	static ItemStack componentIronCable;
	static ItemStack componentBatBox;
	static ItemStack componentMFE;
	static ItemStack componentMFSU;
	static ItemStack componentCircuit;

	static ItemStack ic2overclockerUpg;
	static ItemStack ic2transformerUpg;
	static ItemStack ic2storageUpg;

	public static int ic2WrenchID;
	public static int ic2ElectricWrenchID;

	public static boolean isDebugging;

	@PreInit
	public static void preInit(FMLPreInitializationEvent event)
	{
		try
		{
			Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
			configuration.load();
			blockChargingBenchID = configuration.getBlock(modNamePacked, 2491).getInt();
			itemBenchToolsID = configuration.getItem(configuration.CATEGORY_ITEM, benchToolsName, 22499).getInt();
			itemStorageLinkCardID = configuration.getItem(configuration.CATEGORY_ITEM, "LinkCard", 22495).getInt();
			itemStorageLinkCardCreatorID = configuration.getItem(configuration.CATEGORY_ITEM, "LinkCardCreator", 22496).getInt();
			isDebugging = Boolean.parseBoolean((configuration.get(configuration.CATEGORY_GENERAL, "debug",  false).value));
			configuration.save();
		}
		catch (Exception var1)
		{
			System.out.println("[" + modNamePacked + "] Error while trying to access configuration!");
			throw new RuntimeException(var1);
		}
	}

	@Init
	public void load(FMLInitializationEvent event)
	{
		GameRegistry.registerCraftingHandler(this);

		blockChargingBench = new BlockChargingBench(blockChargingBenchID, 0, Material.ground).setHardness(0.75F).setResistance(5F).setStepSound(Block.soundStoneFootstep).setBlockName("ChargingBench").setCreativeTab(CreativeTabs.tabDecorations);
		//LanguageRegistry.addName(ChargingBench, modNameSpaced);
		GameRegistry.registerBlock(blockChargingBench, ItemChargingBench.class);

		// Charging Bench
		GameRegistry.registerTileEntity(TEChargingBench.class, "LV " + modNameSpaced); // Legacy mappings for backward compatibility - we didn't know wtf we were doing when we started this mod :)
		GameRegistry.registerTileEntity(TEChargingBench.class, "MV " + modNameSpaced); // Legacy
		GameRegistry.registerTileEntity(TEChargingBench.class, "HV " + modNameSpaced); // Legacy
		GameRegistry.registerTileEntity(TEChargingBench.class, "kaijin.chargingBench"); // Proper mapping

		LanguageRegistry.instance().addStringLocalization("blockChargingBench1.name", "LV " + modNameSpaced);
		LanguageRegistry.instance().addStringLocalization("blockChargingBench2.name", "MV " + modNameSpaced);
		LanguageRegistry.instance().addStringLocalization("blockChargingBench3.name", "HV " + modNameSpaced);

		// Battery Station
		GameRegistry.registerTileEntity(TEBatteryStation.class, "LV " + dischargerName); // Legacy mappings
		GameRegistry.registerTileEntity(TEBatteryStation.class, "MV " + dischargerName); // Legacy
		GameRegistry.registerTileEntity(TEBatteryStation.class, "HV " + dischargerName); // Legacy
		GameRegistry.registerTileEntity(TEBatteryStation.class, "kaijin.batteryStation"); // Proper mapping

		LanguageRegistry.instance().addStringLocalization("blockBatteryStation1.name", "LV " + dischargerName);
		LanguageRegistry.instance().addStringLocalization("blockBatteryStation2.name", "MV " + dischargerName);
		LanguageRegistry.instance().addStringLocalization("blockBatteryStation3.name", "HV " + dischargerName);

		// Storage Monitor
		GameRegistry.registerTileEntity(TEStorageMonitor.class, "kaijin.storageMonitor");

		LanguageRegistry.instance().addStringLocalization("blockStorageMonitor.name", "Storage Monitor");

		// Emitter
		GameRegistry.registerTileEntity(TEEmitter.class, "LV " + emitterName); // Legacy mappings
		GameRegistry.registerTileEntity(TEEmitter.class, "MV " + emitterName); // Legacy
		GameRegistry.registerTileEntity(TEEmitter.class, "HV " + emitterName); // Legacy
		GameRegistry.registerTileEntity(TEEmitter.class, "EV " + emitterName); // Legacy
		GameRegistry.registerTileEntity(TEEmitter.class, "kaijin.emitter"); // Proper mapping
		GameRegistry.registerTileEntity(TEAdvEmitter.class, "kaijin.advEmitter"); // Proper mapping
		

		LanguageRegistry.instance().addStringLocalization("blockEmitterBlock1.name", "LV " + emitterName);
		LanguageRegistry.instance().addStringLocalization("blockEmitterBlock2.name", "MV " + emitterName);
		LanguageRegistry.instance().addStringLocalization("blockEmitterBlock3.name", "HV " + emitterName);
		LanguageRegistry.instance().addStringLocalization("blockEmitterBlock4.name", "EV " + emitterName);
		LanguageRegistry.instance().addStringLocalization("blockEmitterBlock4.name", "EV " + emitterName);
		LanguageRegistry.instance().addStringLocalization("blockEmitterAdjustable.name", "Adjustable " + emitterName);

		// Items
		itemBenchTools = new ItemBenchTools(itemBenchToolsID).setItemName(toolkitName);
		LanguageRegistry.instance().addStringLocalization("item.benchTools.toolkit.name", toolkitName);
		LanguageRegistry.instance().addStringLocalization("item.benchTools.LV-kit.name", "LV " + componentsName);
		LanguageRegistry.instance().addStringLocalization("item.benchTools.MV-kit.name", "MV " + componentsName);
		LanguageRegistry.instance().addStringLocalization("item.benchTools.HV-kit.name", "HV " + componentsName);

		itemStorageLinkCard = new ItemStorageLinkCard(itemStorageLinkCardID).setItemName("Energy-Link Card");
		LanguageRegistry.addName(itemStorageLinkCard, "Energy-Link Card");
		
		itemStorageLinkCardCreator = new ItemStorageLinkCardCreator(itemStorageLinkCardCreatorID).setItemName("Link-Card Creator");
		LanguageRegistry.addName(itemStorageLinkCardCreator, "Link-Card Creator");

		NetworkRegistry.instance().registerGuiHandler(this.instance, proxy);
		proxy.load();
		if (proxy.isServer())
		{
			FMLLog.getLogger().info (modNameSpaced + " " + CommonProxy.VERSION + " loaded.");
		}
		if (isDebugging)
		{
			FMLLog.getLogger().info(modNameSpaced + " debugging enabled.");
		}

		// For returning charging benches and deconstructing them
		componentCopperCable = Items.getItem("insulatedCopperCableItem").copy();
		componentCopperCable.stackSize = 3;
		componentGoldCable = Items.getItem("doubleInsulatedGoldCableItem").copy();
		componentGoldCable.stackSize = 3;
		componentIronCable = Items.getItem("trippleInsulatedIronCableItem").copy();
		componentIronCable.stackSize = 3;
		componentBatBox = Items.getItem("batBox").copy();
		componentMFE = Items.getItem("mfeUnit").copy();
		componentMFSU = Items.getItem("mfsUnit").copy();
		componentCircuit = Items.getItem("electronicCircuit").copy();

		// For internal reference to verify items can be placed in inventory.
		ic2overclockerUpg = Items.getItem("overclockerUpgrade").copy();
		ic2transformerUpg = Items.getItem("transformerUpgrade").copy();
		ic2storageUpg = Items.getItem("energyStorageUpgrade").copy();

		ic2WrenchID = Items.getItem("wrench").itemID;
		ic2ElectricWrenchID = Items.getItem("electricWrench").itemID;
}

	@PostInit
	public void modsLoaded(FMLPostInitializationEvent event)
	{
		//if (ChargingBench.isDebugging) System.out.println("ChargingBench.modsLoaded");

		// Charging Bench recipes
		GameRegistry.addRecipe(new ItemStack(blockChargingBench, 1, 0), new Object[] {"UUU", "WCW", "WBW", 'U', Items.getItem("insulatedCopperCableItem"), 'W', Block.planks, 'C', Items.getItem("electronicCircuit"), 'B', Items.getItem("batBox")});
		GameRegistry.addRecipe(new ItemStack(blockChargingBench, 1, 1), new Object[] {"UUU", "WCW", "WBW", 'U', Items.getItem("doubleInsulatedGoldCableItem"), 'W', Block.planks, 'C', Items.getItem("electronicCircuit"), 'B', Items.getItem("mfeUnit")});
		GameRegistry.addRecipe(new ItemStack(blockChargingBench, 1, 2), new Object[] {"UUU", "WCW", "WBW", 'U', Items.getItem("trippleInsulatedIronCableItem"), 'W', Block.planks, 'C', Items.getItem("electronicCircuit"), 'B', Items.getItem("mfsUnit")});

		// Battery Station recipes
		GameRegistry.addRecipe(new ItemStack(blockChargingBench, 1,  8), new Object[] {"UUU", "WCW", "WBW", 'U', Items.getItem("insulatedCopperCableItem"), 'W', Block.planks, 'C', Items.getItem("electronicCircuit"), 'B', Items.getItem("lvTransformer")});
		GameRegistry.addRecipe(new ItemStack(blockChargingBench, 1,  9), new Object[] {"UUU", "WCW", "WBW", 'U', Items.getItem("doubleInsulatedGoldCableItem"), 'W', Block.planks, 'C', Items.getItem("electronicCircuit"), 'B', Items.getItem("mvTransformer")});
		GameRegistry.addRecipe(new ItemStack(blockChargingBench, 1, 10), new Object[] {"UUU", "WCW", "WBW", 'U', Items.getItem("trippleInsulatedIronCableItem"), 'W', Block.planks, 'C', Items.getItem("electronicCircuit"), 'B', Items.getItem("hvTransformer")});

		// Storage Monitor recipe
		GameRegistry.addRecipe(new ItemStack(blockChargingBench, 1, 11), new Object[] {"WUW", "GCG", "WRW", 'W', Block.planks, 'U', Items.getItem("goldCableItem"), 'G', Block.glass, 'C', Items.getItem("electronicCircuit"), 'R', Item.redstone});

		// Link Card Creator recipe
		GameRegistry.addRecipe(new ItemStack(itemStorageLinkCardCreator, 1, 0), new Object[] {"U  ", " C ", "  V", 'U', Items.getItem("insulatedCopperCableItem"), 'C', Items.getItem("electronicCircuit"), 'V', Item.paper});

		// Bench Toolkit recipe
		GameRegistry.addRecipe(new ItemStack(itemBenchTools, 1, 0), new Object[] {" I ", "S S", 'I', Item.ingotIron, 'S', Item.stick});

		// LV, MV, HV Charging Bench Components recipes
		GameRegistry.addShapelessRecipe(new ItemStack(itemBenchTools, 1, 1), new ItemStack(itemBenchTools, 1, 0), new ItemStack(blockChargingBench, 1, 0));
		GameRegistry.addShapelessRecipe(new ItemStack(itemBenchTools, 1, 2), new ItemStack(itemBenchTools, 1, 0), new ItemStack(blockChargingBench, 1, 1));
		GameRegistry.addShapelessRecipe(new ItemStack(itemBenchTools, 1, 3), new ItemStack(itemBenchTools, 1, 0), new ItemStack(blockChargingBench, 1, 2));

		// LV, MV, HV Charging Bench reassembly recipes
		GameRegistry.addShapelessRecipe(new ItemStack(blockChargingBench, 1, 0), new ItemStack(itemBenchTools, 1, 0), new ItemStack(itemBenchTools, 1, 1));
		GameRegistry.addShapelessRecipe(new ItemStack(blockChargingBench, 1, 1), new ItemStack(itemBenchTools, 1, 0), new ItemStack(itemBenchTools, 1, 2));
		GameRegistry.addShapelessRecipe(new ItemStack(blockChargingBench, 1, 2), new ItemStack(itemBenchTools, 1, 0), new ItemStack(itemBenchTools, 1, 3));
	}

	// ICraftingHandler

	@Override
	public void onCrafting(EntityPlayer player, ItemStack item, IInventory craftMatrix)
	{
		int max = craftMatrix.getSizeInventory();
		for (int i=0; i < max; i++)
		{        	
			ItemStack stack = craftMatrix.getStackInSlot(i);
			if (stack != null && stack.getItem() == itemBenchTools && stack.getItemDamage() == 0)
			{				
				stack.stackSize++;
			}
		}
	}

	@Override
	public void onSmelting(EntityPlayer player, ItemStack item) {}
}
