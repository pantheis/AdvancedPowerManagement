package com.kaijin.AdvPowerMan;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import cpw.mods.fml.common.FMLLog;

import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;

public abstract class TECommon extends TileEntity
{
	/**
	 * TileEntities override this to select a GUI to open on block activation
	 * @return int guiID
	 */
	public int getGuiID()
	{
		return -1;
	}

	/**
	 * TileEntites implement this to receive packet data, they are then responsible
	 * in their own code to handle the packet.
	 * @param packetID The first value from the packet, in case it's needed again
	 * @param stream The remaining unread packet data for the tile entity to handle
	 */
	public void receiveDescriptionData(int packetID, DataInputStream stream) {} // Stub for classes that need no desc data

	public void receiveGuiButton(int buttonID) {} // Stub for classes with no buttons

	public void receiveGuiControl(int controlID, int state) {} // Stub for classes with no other controls

	public void receiveGuiText(int fieldID, String text) {} // Stub for classes with no text fields

	/**
	 * Packet transmission from client to server of what button was clicked on the GUI.
	 * @param id = the button ID
	 */
	public void sendGuiButton(int id)
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);
		try
		{
			data.writeInt(0); // Packet ID for Storage Monitor GUI button clicks
			data.writeInt(xCoord);
			data.writeInt(yCoord);
			data.writeInt(zCoord);
			data.writeInt(id);
		}
		catch (IOException e)
		{
			FMLLog.getLogger().info(Info.TITLE_LOG + "Client failed to create packet. (Details: " + e.toString() + ")");
			return;
		}

		AdvancedPowerManagement.proxy.sendPacketToServer(new Packet250CustomPayload(Info.PACKET_CHANNEL, bytes.toByteArray()));
	}

	/**
	 * Does the bulk of the work of creating the description packet.
	 * Performs a callback to addUniqueDescriptionData. That method must be overridden.
	 * We're not overriding getDescriptionPacket in this class because not all of our tile entities need such packets.
	 * @return The completed Packet250.
	 */
	protected Packet250CustomPayload createDescPacket()
	{
		//if (ChargingBench.isDebugging) System.out.println("TE getAuxillaryInfoPacket()");
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);
		try
		{
			data.writeInt(0);
			data.writeInt(xCoord);
			data.writeInt(yCoord);
			data.writeInt(zCoord);
			addUniqueDescriptionData(data);
		}
		catch (IOException e)
		{
			FMLLog.getLogger().info(Info.TITLE_LOG + "Server failed to create description packet. (Details: " + e.toString() + ")");
		}

		Packet250CustomPayload packet = new Packet250CustomPayload(Info.PACKET_CHANNEL, bytes.toByteArray());
		packet.isChunkDataPacket = true;
		return packet;
	}

	/**
	 *  Tile Entities that use description packets must override this to write whatever
	 *  information they require into 'data' 
	 * @param data - Base packet data with packet ID and coordinates already written
	 */
	protected void addUniqueDescriptionData(DataOutputStream data) throws IOException
	{
		// Why can't I throw something that I WANT to be uncaught so it stops the program??
		// Piggy-backing on IOException is stupid, but hopefully this will never end up happening anyway.
		throw new IOException("This tile entity must override addUniqueDescriptionData to pass its description correctly! " + this.getClass());
	}

	protected void logDescPacketError(Exception e)
	{
		FMLLog.getLogger().info(Info.TITLE_LOG + "Client received invalid description packet. (Details: " + e.toString() + ")");
	}

	public void dropContents() {} // Stub for block destroyed event

	public void onInventoryChanged(int slot)
	{
		onInventoryChanged();
	}
}
