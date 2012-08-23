package com.kaijin.ChargingBench;

import net.minecraft.src.Container;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.ic2.api.*;
import net.minecraft.src.ic2.api.ElectricItem;
import net.minecraft.src.ic2.api.EnergyNet;
import net.minecraft.src.ic2.common.*;
import net.minecraft.src.ic2.platform.*;

public class TileEntityChargingBench extends TileEntityElecMachine implements IEnergySource 
{
    public int baseTier;
    public int baseMaxInput;
    public int operations;

    public TileEntityChargingBench(int var1)
    {
        super(17, 0, 4 * (int)Math.pow(2.0D, (double)(var1 * 2 + 3)), 4 * (int)Math.pow(2.0D, (double)(var1 * 2 + 3)), var1);
        this.fuelslot = 12;
        this.baseTier = var1;
        this.baseMaxInput = (int)Math.pow(2.0D, (double)(var1 * 2 + 3));
        this.operations = 1;
    }

    public String getInvName()
    {
        return "Charging Bench";
    }

    public int gaugeEnergyScaled(int var1)
    {
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

    public void updateEntity()
    {
        super.updateEntity();

        if (Platform.isSimulating())
        {
            this.setOverclockRates();
            boolean var1 = false;
            int var2;
            int var3;
            int var4;

            if (this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord))
            {
                if (this.energy < this.maxEnergy)
                {
                    var1 = this.provideEnergy();

                    for (var2 = 0; var2 < 12; ++var2)
                    {
                        for (var3 = 0; var3 < this.operations; ++var3)
                        {
                            if (this.inventory[var2] != null && this.inventory[var2].getItem() instanceof IElectricItem && ((IElectricItem)this.inventory[var2].getItem()).canProvideEnergy())
                            {
                                var4 = this.maxInput;

                                if (this.maxEnergy - this.energy < var4)
                                {
                                    var4 = this.maxEnergy - this.energy;
                                }

                                int var5 = ElectricItem.discharge(this.inventory[var2], var4, this.tier, false, false);
                                this.energy += var5;

                                if (var5 > 0)
                                {
                                    var1 = true;
                                }

                                if (var5 == 0)
                                {
                                    break;
                                }
                            }
                        }
                    }
                }

                if (this.energy > 0)
                {
                    var2 = this.getMaxEnergyOutput();
                    var3 = this.energy > var2 ? var2 : this.energy;
                    this.energy -= var3 - EnergyNet.getForWorld(this.worldObj).emitEnergyFrom(this, var3);
                }
            }
            else
            {
                if (this.energy < this.maxEnergy)
                {
                    var1 = this.provideEnergy();
                }

                for (var2 = 0; var2 < 12 && this.energy > 0; ++var2)
                {
                    if (this.inventory[var2] != null && this.inventory[var2].getItem() instanceof IElectricItem)
                    {
                        if (this.inventory[var2].stackSize > 1)
                        {
                            for (var3 = 0; var3 < 12; ++var3)
                            {
                                if (this.inventory[var3] == null)
                                {
                                    this.inventory[var3] = new ItemStack(this.inventory[var2].getItem().shiftedIndex, 1, this.inventory[var2].getItemDamage());
                                    --this.inventory[var2].stackSize;
                                    var1 = true;
                                    break;
                                }
                            }
                        }

                        for (var3 = 0; var3 < this.operations; ++var3)
                        {
                            var4 = ElectricItem.charge(this.inventory[var2], this.energy, this.tier, false, false);
                            this.energy -= var4;

                            if (var4 > 0)
                            {
                                var1 = true;
                            }

                            if (var4 == 0)
                            {
                                break;
                            }
                        }
                    }
                }
            }

            if (var1)
            {
                this.onInventoryChanged();
            }
        }
    }

    public boolean demandsEnergy()
    {
        return this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord) ? false : this.energy <= this.maxEnergy - this.baseMaxInput;
    }

    public Container getGuiContainer(InventoryPlayer var1)
    {
        return new ContainerChargingBench(var1, this);
    }

    public boolean emitsEnergyTo(TileEntity var1, Direction var2)
    {
        return true;
    }

    public int getMaxEnergyOutput()
    {
        return (int)Math.pow(2.0D, (double)(2 * this.baseTier + 3));
    }

    public void setOverclockRates()
    {
        int var1 = 0;
        int var2 = 0;
        int var3 = 0;

        for (int var4 = 13; var4 <= 16; ++var4)
        {
            ItemStack var5 = this.inventory[var4];

            if (var5 != null)
            {
                if (var5.isItemEqual(Ic2Items.overclockerUpgrade))
                {
                    var1 += var5.stackSize;
                }
                else if (var5.isItemEqual(Ic2Items.transformerUpgrade))
                {
                    var2 += var5.stackSize;
                }
                else if (var5.isItemEqual(Ic2Items.energyStorageUpgrade))
                {
                    var3 += var5.stackSize;
                }
            }
        }

        if (var1 > 16)
        {
            var1 = 16;
        }

        if (var2 > 10)
        {
            var2 = 10;
        }

        this.tier = var2 + this.baseTier;
        this.operations = (int)Math.round(1.0D / Math.pow(0.7D, (double)var1));
        this.maxInput = (int)Math.pow(2.0D, (double)(this.tier * 2 + 3));
        this.maxEnergy = var3 * 10000 + 4 * this.getMaxEnergyOutput();
    }

    public int injectEnergy(Direction var1, int var2)
    {
        this.setOverclockRates();
        return super.injectEnergy(var1, var2);
    }

	@Override
	public boolean isAddedToEnergyNet() {
		// TODO Auto-generated method stub
		return false;
	}
}
