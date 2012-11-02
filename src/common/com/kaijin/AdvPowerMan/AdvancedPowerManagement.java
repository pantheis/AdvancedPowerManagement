/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

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

@Mod(modid = Info.TITLE_PACKED, name=Info.TITLE, version=Info.VERSION, dependencies = Info.DEPENDENCIES)
@NetworkMod(clientSideRequired = true, serverSideRequired = false,
clientPacketHandlerSpec = @SidedPacketHandler(channels = {Info.PACKET_CHANNEL}, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = (Info.PACKET_CHANNEL), packetHandler = ServerPacketHandler.class))
public class AdvancedPowerManagement implements ICraftingHandler
{
	@SidedProxy(clientSide = Info.CLIENT_PROXY, serverSide = Info.SERVER_PROXY)
	public static CommonProxy proxy; //This object will be populated with the class that you choose for the environment

	@Instance(Info.TITLE_PACKED)
	public static AdvancedPowerManagement instance; //The instance of the mod that will be defined, populated, and callable

	public static Block blockAdvPwrMan;
	public static Item itemBenchTools;
	public static Item itemStorageLinkCard;
	public static Item itemStorageLinkCardCreator;

	public static int blockIDAdvPwrMan;
	public static int itemIDBenchTools;
	public static int itemIDStorageLinkCard;
	public static int itemIDStorageLinkCardCreator;

	@PreInit
	public static void preInit(FMLPreInitializationEvent event)
	{
		try
		{
			Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
			configuration.load();
			blockIDAdvPwrMan = configuration.getBlock("ChargingBench", 2491).getInt();
			itemIDBenchTools = configuration.getItem(configuration.CATEGORY_ITEM, "BenchTools", 22499).getInt();
			itemIDStorageLinkCard = configuration.getItem(configuration.CATEGORY_ITEM, "LinkCard", 22495).getInt();
			itemIDStorageLinkCardCreator = configuration.getItem(configuration.CATEGORY_ITEM, "LinkCardCreator", 22496).getInt();
			Info.isDebugging = Boolean.parseBoolean((configuration.get(configuration.CATEGORY_GENERAL, "debug",  false).value));
			configuration.save();
		}
		catch (Exception var1)
		{
			System.out.println("[" + Info.TITLE + "] Error while trying to access configuration!");
			throw new RuntimeException(var1);
		}
	}

	@Init
	public void load(FMLInitializationEvent event)
	{
		GameRegistry.registerCraftingHandler(this);

		blockAdvPwrMan = new BlockAdvPwrMan(blockIDAdvPwrMan, 0, Material.wood).setHardness(0.75F).setResistance(5F).setStepSound(Block.soundStoneFootstep).setBlockName("AdvPwrMan").setCreativeTab(CreativeTabs.tabDecorations);
		GameRegistry.registerBlock(blockAdvPwrMan, ItemBlockAdvPwrMan.class);

		// Charging Benches
		GameRegistry.registerTileEntity(TEChargingBench.class, "LV " + Info.CHARGER_NAME); // Legacy mappings for backward compatibility - we didn't know wtf we were doing when we started this mod :)
		GameRegistry.registerTileEntity(TEChargingBench.class, "MV " + Info.CHARGER_NAME); // Legacy
		GameRegistry.registerTileEntity(TEChargingBench.class, "HV " + Info.CHARGER_NAME); // Legacy
		GameRegistry.registerTileEntity(TEChargingBench.class, "kaijin.chargingBench"); // Proper mapping

		// Battery Stations
		GameRegistry.registerTileEntity(TEBatteryStation.class, "LV " + Info.DISCHARGER_NAME); // Legacy mappings
		GameRegistry.registerTileEntity(TEBatteryStation.class, "MV " + Info.DISCHARGER_NAME); // Legacy
		GameRegistry.registerTileEntity(TEBatteryStation.class, "HV " + Info.DISCHARGER_NAME); // Legacy
		GameRegistry.registerTileEntity(TEBatteryStation.class, "kaijin.batteryStation"); // Proper mapping

		// Storage Monitor
		GameRegistry.registerTileEntity(TEStorageMonitor.class, "kaijin.storageMonitor");

		// Emitters
		GameRegistry.registerTileEntity(TEEmitter.class, "LV " + Info.EMITTER_NAME); // Legacy mappings
		GameRegistry.registerTileEntity(TEEmitter.class, "MV " + Info.EMITTER_NAME); // Legacy
		GameRegistry.registerTileEntity(TEEmitter.class, "HV " + Info.EMITTER_NAME); // Legacy
		GameRegistry.registerTileEntity(TEEmitter.class, "EV " + Info.EMITTER_NAME); // Legacy
		GameRegistry.registerTileEntity(TEEmitter.class, "kaijin.emitter"); // Proper mapping
		GameRegistry.registerTileEntity(TEAdvEmitter.class, "kaijin.advEmitter");

		// Items
		itemBenchTools = new ItemBenchTools(itemIDBenchTools).setItemName(Info.TOOLKIT_NAME);

		itemStorageLinkCard = new ItemStorageLinkCard(itemIDStorageLinkCard).setItemName(Info.LINK_CARD_NAME);
		
		itemStorageLinkCardCreator = new ItemStorageLinkCardCreator(itemIDStorageLinkCardCreator).setItemName(Info.LINK_CREATOR_NAME);

		Info.registerTranslations();

		NetworkRegistry.instance().registerGuiHandler(this.instance, proxy);
		proxy.load();
		if (proxy.isServer())
		{
			FMLLog.getLogger().info (Info.TITLE + " " + Info.VERSION + " loaded.");
		}
		if (Info.isDebugging)
		{
			FMLLog.getLogger().info(Info.TITLE + " debugging enabled.");
		}

		// For returning charging benches and deconstructing them
		Info.componentCopperCable = Items.getItem("insulatedCopperCableItem").copy();
		Info.componentCopperCable.stackSize = 3;
		Info.componentGoldCable = Items.getItem("doubleInsulatedGoldCableItem").copy();
		Info.componentGoldCable.stackSize = 3;
		Info.componentIronCable = Items.getItem("trippleInsulatedIronCableItem").copy();
		Info.componentIronCable.stackSize = 3;
		Info.componentBatBox = Items.getItem("batBox").copy();
		Info.componentMFE = Items.getItem("mfeUnit").copy();
		Info.componentMFSU = Items.getItem("mfsUnit").copy();
		Info.componentCircuit = Items.getItem("electronicCircuit").copy();

		// For internal reference to verify items can be placed in inventory.
		Info.ic2overclockerUpg = Items.getItem("overclockerUpgrade").copy();
		Info.ic2transformerUpg = Items.getItem("transformerUpgrade").copy();
		Info.ic2storageUpg = Items.getItem("energyStorageUpgrade").copy();

		Info.ic2WrenchID = Items.getItem("wrench").itemID;
		Info.ic2ElectricWrenchID = Items.getItem("electricWrench").itemID;
}

	@PostInit
	public void modsLoaded(FMLPostInitializationEvent event)
	{
		//if (ChargingBench.isDebugging) System.out.println("ChargingBench.modsLoaded");

		// Charging Bench recipes
		GameRegistry.addRecipe(new ItemStack(blockAdvPwrMan, 1, 0), new Object[] {"UUU", "WCW", "WBW", 'U', Items.getItem("insulatedCopperCableItem"), 'W', Block.planks, 'C', Items.getItem("electronicCircuit"), 'B', Items.getItem("batBox")});
		GameRegistry.addRecipe(new ItemStack(blockAdvPwrMan, 1, 1), new Object[] {"UUU", "WCW", "WBW", 'U', Items.getItem("doubleInsulatedGoldCableItem"), 'W', Block.planks, 'C', Items.getItem("electronicCircuit"), 'B', Items.getItem("mfeUnit")});
		GameRegistry.addRecipe(new ItemStack(blockAdvPwrMan, 1, 2), new Object[] {"UUU", "WCW", "WBW", 'U', Items.getItem("trippleInsulatedIronCableItem"), 'W', Block.planks, 'C', Items.getItem("electronicCircuit"), 'B', Items.getItem("mfsUnit")});

		// Battery Station recipes
		GameRegistry.addRecipe(new ItemStack(blockAdvPwrMan, 1,  8), new Object[] {"UUU", "WCW", "WBW", 'U', Items.getItem("insulatedCopperCableItem"), 'W', Block.planks, 'C', Items.getItem("electronicCircuit"), 'B', Items.getItem("lvTransformer")});
		GameRegistry.addRecipe(new ItemStack(blockAdvPwrMan, 1,  9), new Object[] {"UUU", "WCW", "WBW", 'U', Items.getItem("doubleInsulatedGoldCableItem"), 'W', Block.planks, 'C', Items.getItem("electronicCircuit"), 'B', Items.getItem("mvTransformer")});
		GameRegistry.addRecipe(new ItemStack(blockAdvPwrMan, 1, 10), new Object[] {"UUU", "WCW", "WBW", 'U', Items.getItem("trippleInsulatedIronCableItem"), 'W', Block.planks, 'C', Items.getItem("electronicCircuit"), 'B', Items.getItem("hvTransformer")});

		// Storage Monitor recipe
		GameRegistry.addRecipe(new ItemStack(blockAdvPwrMan, 1, 11), new Object[] {"WUW", "GCG", "WRW", 'W', Block.planks, 'U', Items.getItem("goldCableItem"), 'G', Block.glass, 'C', Items.getItem("electronicCircuit"), 'R', Item.redstone});

		// Link Card Creator recipe
		GameRegistry.addRecipe(new ItemStack(itemStorageLinkCardCreator, 1, 0), new Object[] {"U  ", " C ", "  V", 'U', Items.getItem("insulatedCopperCableItem"), 'C', Items.getItem("electronicCircuit"), 'V', Item.paper});

		// Bench Toolkit recipe
		GameRegistry.addRecipe(new ItemStack(itemBenchTools, 1, 0), new Object[] {" I ", "S S", 'I', Item.ingotIron, 'S', Item.stick});

		// LV, MV, HV Charging Bench Components recipes
		GameRegistry.addShapelessRecipe(new ItemStack(itemBenchTools, 1, 1), new ItemStack(itemBenchTools, 1, 0), new ItemStack(blockAdvPwrMan, 1, 0));
		GameRegistry.addShapelessRecipe(new ItemStack(itemBenchTools, 1, 2), new ItemStack(itemBenchTools, 1, 0), new ItemStack(blockAdvPwrMan, 1, 1));
		GameRegistry.addShapelessRecipe(new ItemStack(itemBenchTools, 1, 3), new ItemStack(itemBenchTools, 1, 0), new ItemStack(blockAdvPwrMan, 1, 2));

		// LV, MV, HV Charging Bench reassembly recipes
		GameRegistry.addShapelessRecipe(new ItemStack(blockAdvPwrMan, 1, 0), new ItemStack(itemBenchTools, 1, 0), new ItemStack(itemBenchTools, 1, 1));
		GameRegistry.addShapelessRecipe(new ItemStack(blockAdvPwrMan, 1, 1), new ItemStack(itemBenchTools, 1, 0), new ItemStack(itemBenchTools, 1, 2));
		GameRegistry.addShapelessRecipe(new ItemStack(blockAdvPwrMan, 1, 2), new ItemStack(itemBenchTools, 1, 0), new ItemStack(itemBenchTools, 1, 3));
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
