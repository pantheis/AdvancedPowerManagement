package com.kaijin.ChargingBench;

import net.minecraft.src.ItemStack;

public class Info
{
	// Mod Info
	public static final String VERSION = "@VERSION@";
	public static final String BUILD_NUMBER = "@BUILD_NUMBER@";
	public static final String DEPENDENCIES = "required-after:IC2@[1.108,);required-after:Forge@[6.0.1.349,)";
	public static final String CLIENT_PROXY = "com.kaijin.ChargingBench.ClientProxy";
	public static final String SERVER_PROXY = "com.kaijin.ChargingBench.CommonProxy";
	public static final String PACKET_CHANNEL = "kaijinAdvPwrMan"; // CHANNEL MAX 16 CHARS
	public static final String TITLE_PACKED = "AdvancedPowerManagement";
	public static final String TITLE = "Advanced Power Management";

	// Textures
	public static final String BLOCK_PNG   = "/com/kaijin/ChargingBench/textures/AdvPowerManBlocks.png";
	public static final String ITEM_PNG    = "/com/kaijin/ChargingBench/textures/AdvPowerManItems.png";
	public static final String GUI1_PNG    = "/com/kaijin/ChargingBench/textures/GUIChargingBench.png";
	public static final String GUI2_PNG    = "/com/kaijin/ChargingBench/textures/GUIBatteryStation.png";
	public static final String GUI3_PNG    = "/com/kaijin/ChargingBench/textures/GUIStorageMonitor.png";
	public static final String GUI4_PNG    = "/com/kaijin/ChargingBench/textures/GUIAdvEmitter.png";

	// Blocks
	public static final String CHARGER_NAME = "Charging Bench";
	public static final String EMITTER_NAME = "Emitter";
	public static final String DISCHARGER_NAME = "Battery Station";
	public static final String MONITOR_NAME = "Storage Monitor";

	// Items
	public static final String TOOLKIT_NAME = CHARGER_NAME + " Toolkit";
	public static final String COMPONENTS_NAME = CHARGER_NAME + " Components";

	public static final String LINK_CARD_NAME = "Energy Link Card";
	public static final String LINK_CREATOR_NAME = "Energy Link Card (Blank)";

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
}
