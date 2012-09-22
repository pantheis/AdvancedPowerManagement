package com.kaijin.ChargingBench;

import java.util.*;

import ic2.api.*;
import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import com.kaijin.ChargingBench.*;

public class TEChargingBench extends TileEntity implements IEnergySource, IEnergySink, IWrenchable,
IEnergyStorage, IInventory, ISidedInventory,
INetworkDataProvider, INetworkUpdateListener
{
	private ItemStack[] contents = new ItemStack[this.getSizeInventory()];
	private boolean initialized;

	public int energy;
	public int maxInput;
	public int baseMaxInput;
	public int maxEnergy;
	public int baseTier;

	public TEChargingBench(int i)
	{
		this.baseMaxInput = (int)Math.pow(2.0D, (double)(i * 2 + 3));
		this.baseTier = i;
	}



	@Override
	public boolean canUpdate()
	{
		return true;
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, Direction direction) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAddedToEnergyNet() {
		// TODO Auto-generated method stub
		return initialized;
	}

	@Override
	public int getMaxEnergyOutput() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean demandsEnergy() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int injectEnergy(Direction directionFrom, int amount) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int side) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public short getFacing() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setFacing(short facing) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean wrenchCanRemove(EntityPlayer entityPlayer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float getWrenchDropRate() {
		// TODO Auto-generated method stub
		return 1.0F;
	}

	@Override
	public int getStored() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCapacity() {
		// TODO Auto-generated method stub
		return 102400;
	}

	@Override
	public int getOutput() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSizeInventory() {
		// TODO Auto-generated method stub
		return 17;
	}

	@Override
	public int getStartInventorySide(ForgeDirection side) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		// TODO Auto-generated method stub
		return contents[i];
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2) {
		// TODO Auto-generated method stub
		if (this.contents[var1] != null)
		{
			ItemStack var3;

			if (this.contents[var1].stackSize <= var2)
			{
				var3 = this.contents[var1];
				this.contents[var1] = null;
				this.onInventoryChanged();
				return var3;
			}
			else
			{
				var3 = this.contents[var1].splitStack(var2);

				if (this.contents[var1].stackSize == 0)
				{
					this.contents[var1] = null;
				}

				this.onInventoryChanged();
				return var3;
			}
		}
		else
		{
			return null;
		}
	}

	public ItemStack getStackInSlotOnClosing(int var1)
	{
		if (this.contents[var1] == null)
		{
			return null;
		}

		ItemStack stack = this.contents[var1];
		this.contents[var1] = null;
		return stack;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		this.contents[i] = itemstack;

		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit())
		{
			itemstack.stackSize = getInventoryStackLimit();
		}
		this.onInventoryChanged();
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		//TODO
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		//TODO
	}


	@Override
	public String getInvName() {
		// TODO Auto-generated method stub
		return "ChargingBench";
	}

	@Override
	public int getInventoryStackLimit() {
		// TODO Auto-generated method stub
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this)
		{
			return false;
		}

		return entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64D;
	}

	@Override
	public void openChest() {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeChest() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateEntity()
	{
		if (!initialized && worldObj != null)
		{
			if (worldObj.isRemote)
			{
				NetworkHelper.requestInitialData(this);
			} else
			{
				EnergyNet.getForWorld(worldObj).addTileEntity(this);
			}
			initialized = true;
		}
	}

	public int gaugeEnergyScaled(int var1)
	{
		if (Utils.isDebug()) System.out.println("TileEntityChargingBench.gaugeEnergyScaled");
		if (this.energy <= 0)
		{
			return 0;
		}
		else
		{
			int var2 = this.energy * var1 / (this.maxEnergy - this.baseMaxInput);

			if (var2 > var1)
			{
				var2 = var1;
			}

			return var2;
		}
	}

	@Override
	public void onNetworkUpdate(String field) {
		// TODO Auto-generated method stub

	}

	private static List<String> fields=Arrays.asList(new String[0]);
	@Override
	public List<String> getNetworkedFields() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void invalidate()
	{
		if (worldObj!=null && initialized)
		{
			EnergyNet.getForWorld(worldObj).removeTileEntity(this);
		}
		super.invalidate();
	}

}
