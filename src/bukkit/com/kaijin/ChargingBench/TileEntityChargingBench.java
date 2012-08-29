package com.kaijin.ChargingBench;

import ic2.api.Direction;
import ic2.api.IElectricItem;
import ic2.api.IEnergySource;
import ic2.common.ElectricItem;
import ic2.common.EnergyNet;
import ic2.common.Ic2Items;
import ic2.common.TileEntityElecMachine;
import ic2.platform.Platform;
import net.minecraft.server.Container;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.ItemStack;
import net.minecraft.server.PlayerInventory;
import net.minecraft.server.TileEntity;

public class TileEntityChargingBench extends TileEntityElecMachine implements IEnergySource
{
    public int baseTier;
    public int baseMaxInput;
    public int operations;

    public TileEntityChargingBench(int var1)
    {
        super(17, 0, 4 * (int)Math.pow(2.0D, (double)(var1 * 2 + 3)), 4 * (int)Math.pow(2.0D, (double)(var1 * 2 + 3)), var1);

        if (Utils.isDebug())
        {
            System.out.println("TileEntityChargingBench");
        }

        this.fuelslot = 12;
        this.baseTier = var1;
        this.baseMaxInput = (int)Math.pow(2.0D, (double)(var1 * 2 + 3));
        this.operations = 1;
    }

    /**
     * Returns the name of the inventory.
     */
    public String getName()
    {
        if (Utils.isDebug())
        {
            System.out.println("getInvName");
        }

        return "Charging Bench";
    }

    public int gaugeEnergyScaled(int var1)
    {
        if (Utils.isDebug())
        {
            System.out.println("TileEntityChargingBench.gaugeEnergyScaled");
        }

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

    /**
     * Allows the entity to update its state. Overridden in most subclasses, e.g. the mob spawner uses this to count
     * ticks and creates a new spawn inside its implementation.
     */
    public void q_()
    {
        if (Utils.isDebug())
        {
            System.out.println("TileEntityChargingBench.updateEntity");
        }

        super.q_();

        if (Platform.isSimulating())
        {
            if (Utils.isDebug())
            {
                System.out.println("TileEntityChargingBench.updateEntity.Platform.isSimulating()");
            }

            this.setOverclockRates();
            boolean var1 = false;
            int var2;
            int var3;
            int var4;

            if (this.world.isBlockIndirectlyPowered(this.x, this.y, this.z))
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
                    this.energy -= var3 - EnergyNet.getForWorld(this.world).emitEnergyFrom(this, var3);
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
                        if (this.inventory[var2].count > 1)
                        {
                            for (var3 = 0; var3 < 12; ++var3)
                            {
                                if (this.inventory[var3] == null)
                                {
                                    this.inventory[var3] = new ItemStack(this.inventory[var2].getItem().id, 1, this.inventory[var2].getData());
                                    --this.inventory[var2].count;
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
                this.update();
            }
        }
    }

    public boolean demandsEnergy()
    {
        if (Utils.isDebug())
        {
            System.out.println("TileEntityChargingBench.demandsEnergy");
        }

        return this.world.isBlockIndirectlyPowered(this.x, this.y, this.z) ? false : this.energy <= this.maxEnergy - this.baseMaxInput;
    }

    public boolean emitsEnergyTo(TileEntity var1, Direction var2)
    {
        if (Utils.isDebug())
        {
            System.out.println("TileEntityChargingBench.emitsEnergyTo");
        }

        return true;
    }

    public int getMaxEnergyOutput()
    {
        if (Utils.isDebug())
        {
            System.out.println("TileEntityChargingBench.getMaxEnergyOutput");
        }

        return (int)Math.pow(2.0D, (double)(2 * this.baseTier + 3));
    }

    public void setOverclockRates()
    {
        if (Utils.isDebug())
        {
            System.out.println("TileEntityChargingBench.setOverclockRates");
        }

        int var1 = 0;
        int var2 = 0;
        int var3 = 0;

        for (int var4 = 13; var4 <= 16; ++var4)
        {
            ItemStack var5 = this.inventory[var4];

            if (var5 != null)
            {
                if (var5.doMaterialsMatch(Ic2Items.overclockerUpgrade))
                {
                    var1 += var5.count;
                }
                else if (var5.doMaterialsMatch(Ic2Items.transformerUpgrade))
                {
                    var2 += var5.count;
                }
                else if (var5.doMaterialsMatch(Ic2Items.energyStorageUpgrade))
                {
                    var3 += var5.count;
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
        if (Utils.isDebug())
        {
            System.out.println("TileEntityChargingBench.injectEnergy");
        }

        this.setOverclockRates();
        return super.injectEnergy(var1, var2);
    }

    public Container getGuiContainer(EntityHuman var1)
    {
        if (Utils.isDebug())
        {
            System.out.println("TileEntityChargingBench.getGuiContainer");
        }

        return new ContainerChargingBench(var1, this);
    }
    
    @Override
    public ItemStack[] getContents() {
        // TODO Auto-generated method stub
        return inventory;
    }

    @Override
    public void setMaxStackSize(int arg0) {
        // TODO Auto-generated method stub
        
    }
}
