package com.kaijin.ChargingBench;

import ic2.api.*;
import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;

public class TEChargingBench extends TileEntity implements IEnergySource, IEnergySink, IWrenchable,
                                                           IEnergyStorage
{

	public TEChargingBench(int i)
	{
		
	}
	@Override
	public boolean emitsEnergyTo(TileEntity receiver, Direction direction) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAddedToEnergyNet() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getMaxEnergyOutput() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean demandsEnergy() {
		// TODO Auto-generated method stub
		return false;
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
		return 1;
	}
	@Override
	public int getStored() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int getCapacity() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int getOutput() {
		// TODO Auto-generated method stub
		return 0;
	}

}
