/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import java.io.File;
import java.util.logging.Level;
import ic2.api.Items;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.block.material.Material;
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

	public static int blockIDAdvPwrMan = 2491;
	public static int itemIDBenchTools = 22499;
	public static int itemIDStorageLinkCard = 22495;
	public static int itemIDStorageLinkCardCreator = 22496;

	@PreInit
	public static void preInit(FMLPreInitializationEvent event)
	{
		Info.isDebugging = false;
		try
		{
			Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
			configuration.load();

			// Check for legacy configuration file
			File oldFile = new File(event.getModConfigurationDirectory(), "ChargingBench.cfg");
			boolean migrate = oldFile.exists();
			if (migrate)
			{
				FMLLog.getLogger().info(Info.TITLE_LOG + "Discovered old config file: " + oldFile + " - Attempting to migrate block and item IDs.");
				if (configuration.hasKey("block", "AdvPowerManBlock"))
				{
					FMLLog.getLogger().info(Info.TITLE_LOG + "New config file already contains settings. Skipping migration.");
				}
				else
				{
					Configuration oldconfig = new Configuration(oldFile);
					oldconfig.load();
					blockIDAdvPwrMan = oldconfig.get(configuration.CATEGORY_BLOCK, "ChargingBench", blockIDAdvPwrMan).getInt();
					Info.isDebugging = Boolean.parseBoolean((oldconfig.get(configuration.CATEGORY_GENERAL, "debug",  Info.isDebugging).value));
					oldconfig.save();
					boolean success = oldFile.delete();
					if (success)
					{
						FMLLog.getLogger().info(Info.TITLE_LOG + "Done with old config file.");
					}
					else
					{
						FMLLog.getLogger().warning(Info.TITLE_LOG + "Could not delete old configuration file: " + oldFile + " - Requesting delete on exit.");
						oldFile.deleteOnExit();
					}
				}
			}

			// Read or create config file properties, reusing any block and item IDs discovered in old file, if it was present
			blockIDAdvPwrMan = configuration.getBlock("AdvPowerManBlock", blockIDAdvPwrMan).getInt();
			itemIDBenchTools = configuration.getItem(configuration.CATEGORY_ITEM, "BenchTools", itemIDBenchTools).getInt();
			itemIDStorageLinkCard = configuration.getItem(configuration.CATEGORY_ITEM, "LinkCard", itemIDStorageLinkCard).getInt();
			itemIDStorageLinkCardCreator = configuration.getItem(configuration.CATEGORY_ITEM, "LinkCardCreator", itemIDStorageLinkCardCreator).getInt();
			Info.isDebugging = Boolean.parseBoolean((configuration.get(configuration.CATEGORY_GENERAL, "debug",  Info.isDebugging).value));
			configuration.save();
			if (migrate)
			{
				FMLLog.getLogger().info(Info.TITLE_LOG + "Successfully migrated settings to new config file.");
			}
		}
		catch (Exception e)
		{
			FMLLog.getLogger().log(Level.SEVERE, Info.TITLE_LOG + "Error while trying to access configuration!", e);
			throw new RuntimeException(e);
		}
	}

	@Init
	public void load(FMLInitializationEvent event)
	{
		FMLLog.getLogger().fine(Info.TITLE_LOG + "Loading.");
		GameRegistry.registerCraftingHandler(this);

		blockAdvPwrMan = new BlockAdvPwrMan(blockIDAdvPwrMan, 0, Material.ground).setHardness(0.75F).setResistance(5F).setStepSound(Block.soundStoneFootstep).setBlockName("AdvPwrMan").setCreativeTab(CreativeTabs.tabDecorations);
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
		GameRegistry.registerTileEntity(TEAdvEmitter.class, "LV " + Info.EMITTER_NAME); // Legacy mappings
		GameRegistry.registerTileEntity(TEAdvEmitter.class, "MV " + Info.EMITTER_NAME); // Legacy
		GameRegistry.registerTileEntity(TEAdvEmitter.class, "HV " + Info.EMITTER_NAME); // Legacy
		GameRegistry.registerTileEntity(TEAdvEmitter.class, "EV " + Info.EMITTER_NAME); // Legacy
		GameRegistry.registerTileEntity(TEAdvEmitter.class, "kaijin.emitter"); // Now legacy as well
		GameRegistry.registerTileEntity(TEAdvEmitter.class, "kaijin.advEmitter");

		// Items
		itemBenchTools = new ItemBenchTools(itemIDBenchTools).setItemName(Info.TOOLKIT_NAME);

		itemStorageLinkCard = new ItemStorageLinkCard(itemIDStorageLinkCard).setItemName(Info.LINK_CARD_NAME);
		
		itemStorageLinkCardCreator = new ItemStorageLinkCardCreator(itemIDStorageLinkCardCreator).setItemName(Info.LINK_CREATOR_NAME);

		Info.registerTranslations();

		NetworkRegistry.instance().registerGuiHandler(this.instance, proxy);
		proxy.load();

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

		if (proxy.isServer())
		{
			FMLLog.getLogger().info(Info.TITLE_LOG + Info.TITLE + " " + Info.VERSION + " loaded.");
		}

		if (Info.isDebugging)
		{
			FMLLog.getLogger().info(Info.TITLE_LOG + "Debugging enabled.");
		}

		FMLLog.getLogger().fine(Info.TITLE_LOG + "Done loading.");
}

	@PostInit
	public void modsLoaded(FMLPostInitializationEvent event)
	{
		FMLLog.getLogger().fine(Info.TITLE_LOG + "Adding crafting recipes.");

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
