package com.tntmodders.takumi.item;

import com.tntmodders.takumi.TakumiCraftCore;
import com.tntmodders.takumi.core.TakumiItemCore;
import com.tntmodders.takumi.entity.item.EntityTakumiArrow;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemTakumiArrow extends ItemArrow {

    public final int power;
    public final int pierce;
    public final boolean destroy;

    public ItemTakumiArrow(String name, int powerIn, int pierceIn, boolean destroyIn) {
        super();
        this.power = powerIn;
        this.pierce = pierceIn;
        this.destroy = destroyIn;
        this.setRegistryName(TakumiCraftCore.MODID, "takumiarrow_" + name);
        this.setCreativeTab(TakumiCraftCore.TAB_CREEPER);
        this.setUnlocalizedName("takumiarrow_" + name);
    }

    @Override
    public EntityArrow createArrow(World worldIn, ItemStack stack, EntityLivingBase shooter) {
        if (shooter.getActiveItemStack().getItem() == Items.BOW) {
            return ((ItemArrow) Items.ARROW).createArrow(worldIn, stack, shooter);
        }
        if (stack.getItem() == TakumiItemCore.TAKUMI_ARROW_SAN) {
            return new EntityTakumiArrow(worldIn, new ItemStack(this), shooter, EntityTakumiArrow.EnumArrowType.SHOT);
        }
        if (stack.getItem() == TakumiItemCore.TAKUMI_ARROW_KAN) {
            return new EntityTakumiArrow(worldIn, new ItemStack(this), shooter, EntityTakumiArrow.EnumArrowType.PIERCE);
        }
        return new EntityTakumiArrow(worldIn, new ItemStack(this), shooter);
    }
}
