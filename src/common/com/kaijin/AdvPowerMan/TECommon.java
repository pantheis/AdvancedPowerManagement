package com.kaijin.AdvPowerMan;

import java.io.DataInputStream;

import net.minecraft.src.TileEntity;

abstract public class TECommon extends TileEntity
{
	/**
	 * TileEntites implement this to receive packet data, they are then responsible
	 * in their own code to handle the packet.
	 * @param packetID
	 * @param stream
	 */
	public abstract void receiveDescriptionData(int packetID, DataInputStream stream);
}
