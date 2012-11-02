/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan;

import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.src.ItemStack;

public class Info
{
	// Mod Info
	public static final String VERSION = "@VERSION@";
	//public static final String BUILD_NUMBER = "@BUILD_NUMBER@";
	public static final String DEPENDENCIES = "required-after:IC2@[1.108,);required-after:Forge@[6.0.1.349,)";
	public static final String CLIENT_PROXY = "com.kaijin.AdvPowerMan.ClientProxy";
	public static final String SERVER_PROXY = "com.kaijin.AdvPowerMan.CommonProxy";
	public static final String PACKET_CHANNEL = "kaijinAdvPwrMan"; // CHANNEL MAX 16 CHARS
	public static final String TITLE_PACKED = "AdvancedPowerManagement";
	public static final String TITLE = "Advanced Power Management";

	// Textures
	public static final String TEX_BASE    = "/com/kaijin/AdvPowerMan/textures/";
	public static final String BLOCK_PNG   = TEX_BASE + "AdvPowerManBlocks.png";
	public static final String ITEM_PNG    = TEX_BASE + "AdvPowerManItems.png";
	public static final String GUI1_PNG    = TEX_BASE + "GUIChargingBench.png";
	public static final String GUI2_PNG    = TEX_BASE + "GUIBatteryStation.png";
	public static final String GUI3_PNG    = TEX_BASE + "GUIStorageMonitor.png";
	public static final String GUI4_PNG    = TEX_BASE + "GUIAdvEmitter.png";

	public static final String[] KEY_BLOCK_NAMES = new String[] {"blockChargingBench1", "blockChargingBench2", "blockChargingBench3",
		"blockEmitterBlock1", "blockEmitterBlock2", "blockEmitterBlock3", "blockEmitterBlock4", "blockEmitterAdjustable",
		"blockBatteryStation1", "blockBatteryStation2", "blockBatteryStation3", "blockStorageMonitor"};
	public static final String KEY_NAME_SUFFIX = ".name";

	// Blocks
	public static final String CHARGER_NAME = "Charging Bench";
	public static final String DISCHARGER_NAME = "Battery Station";
	public static final String MONITOR_NAME = "Storage Monitor";
	public static final String EMITTER_NAME = "Emitter";
	public static final String ADV_EMITTER_NAME = "Adjustable Emitter";

	// Items
	public static final String TOOLKIT_NAME = CHARGER_NAME + " Toolkit";
	public static final String COMPONENTS_NAME = CHARGER_NAME + " Components";

	public static final String LINK_CARD_NAME = "Energy Link Card";
	public static final String LINK_CREATOR_NAME = "Energy Link Card (Blank)";

	// GUI strings
	public static final String KEY_TITLE = "AdvPwrMan.title";
	public static final String KEY_EU = "AdvPwrMan.misc.EU";
	public static final String KEY_EMITTER_PACKET = "AdvPwrMan.emitter.packet";
	public static final String KEY_EMITTER_OUTPUT = "AdvPwrMan.emitter.output";
	public static final String KEY_MONITOR_INVALID = "AdvPwrMan.monitor.invalid";
	public static final String KEY_MONITOR_UPPER = "AdvPwrMan.monitor.upper";
	public static final String KEY_MONITOR_LOWER = "AdvPwrMan.monitor.lower";


	// Other constants for use in multiple classes
	public static final int LAST_META_VALUE = 11;

	public static final int CB_SLOT_INPUT = 0;
	public static final int CB_SLOT_OUTPUT = 1;
	public static final int CB_SLOT_POWER_SOURCE = 2;
	public static final int CB_SLOT_CHARGING = 3;
	public static final int CB_SLOT_UPGRADE = 15;

	public static final int BS_SLOT_INPUT = 0;
	public static final int BS_SLOT_OUTPUT = 1;
	public static final int BS_SLOT_POWER_START = 2;

	public static final int SM_SLOT_UNIVERSAL = 0;

	public static final int CB_INVENTORY_SIZE = 19;
	public static final int BS_INVENTORY_SIZE = 14;
	public static final int SM_INVENTORY_SIZE = 1;

	// Some global variables
	public static boolean isDebugging;

	public static int ic2WrenchID;
	public static int ic2ElectricWrenchID;

	// For returning charging benches and deconstructing them
	public static ItemStack componentCopperCable;
	public static ItemStack componentGoldCable;
	public static ItemStack componentIronCable;
	public static ItemStack componentBatBox;
	public static ItemStack componentMFE;
	public static ItemStack componentMFSU;
	public static ItemStack componentCircuit;

	// For internal reference to verify items can be placed in inventory.
	public static ItemStack ic2overclockerUpg;
	public static ItemStack ic2transformerUpg;
	public static ItemStack ic2storageUpg;

	public static void registerTranslations()
	{
		LanguageRegistry lang = LanguageRegistry.instance();

		lang.addStringLocalization(KEY_BLOCK_NAMES[0] + KEY_NAME_SUFFIX, "LV " + Info.CHARGER_NAME);
		lang.addStringLocalization(KEY_BLOCK_NAMES[1] + KEY_NAME_SUFFIX, "MV " + Info.CHARGER_NAME);
		lang.addStringLocalization(KEY_BLOCK_NAMES[2] + KEY_NAME_SUFFIX, "HV " + Info.CHARGER_NAME);

		lang.addStringLocalization(KEY_BLOCK_NAMES[3] + KEY_NAME_SUFFIX, "LV " + Info.EMITTER_NAME);
		lang.addStringLocalization(KEY_BLOCK_NAMES[4] + KEY_NAME_SUFFIX, "MV " + Info.EMITTER_NAME);
		lang.addStringLocalization(KEY_BLOCK_NAMES[5] + KEY_NAME_SUFFIX, "HV " + Info.EMITTER_NAME);
		lang.addStringLocalization(KEY_BLOCK_NAMES[6] + KEY_NAME_SUFFIX, "EV " + Info.EMITTER_NAME);
		lang.addStringLocalization(KEY_BLOCK_NAMES[7] + KEY_NAME_SUFFIX, "Adjustable " + Info.EMITTER_NAME);

		lang.addStringLocalization(KEY_BLOCK_NAMES[8] + KEY_NAME_SUFFIX, "LV " + Info.DISCHARGER_NAME);
		lang.addStringLocalization(KEY_BLOCK_NAMES[9] + KEY_NAME_SUFFIX, "MV " + Info.DISCHARGER_NAME);
		lang.addStringLocalization(KEY_BLOCK_NAMES[10] + KEY_NAME_SUFFIX, "HV " + Info.DISCHARGER_NAME);

		lang.addStringLocalization(KEY_BLOCK_NAMES[11] + KEY_NAME_SUFFIX, Info.MONITOR_NAME);

		lang.addStringLocalization("item.benchTools.toolkit.name", Info.TOOLKIT_NAME);
		lang.addStringLocalization("item.benchTools.LV-kit.name", "LV " + Info.COMPONENTS_NAME);
		lang.addStringLocalization("item.benchTools.MV-kit.name", "MV " + Info.COMPONENTS_NAME);
		lang.addStringLocalization("item.benchTools.HV-kit.name", "HV " + Info.COMPONENTS_NAME);

		LanguageRegistry.addName(AdvancedPowerManagement.itemStorageLinkCard, Info.LINK_CARD_NAME);
		LanguageRegistry.addName(AdvancedPowerManagement.itemStorageLinkCardCreator, Info.LINK_CREATOR_NAME);

		lang.addStringLocalization(KEY_TITLE, TITLE);

		lang.addStringLocalization(KEY_EU, "EU");

		// GUI strings
		lang.addStringLocalization(KEY_EMITTER_PACKET, "Packet size (Voltage)");
		lang.addStringLocalization(KEY_EMITTER_OUTPUT, "Output / Tick (Max 64 Packets)");
		lang.addStringLocalization(KEY_MONITOR_INVALID, "No Valid Link");
		lang.addStringLocalization(KEY_MONITOR_UPPER, "Upper Threshold (Off)");
		lang.addStringLocalization(KEY_MONITOR_LOWER, "Lower Threshold (On)");
	}
}
