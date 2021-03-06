package com.tntmodders.takumi.entity.mobs;

import com.tntmodders.takumi.entity.EntityTakumiAbstractCreeper;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ExplosionEvent.Detonate;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EntityReturnCreeper extends EntityTakumiAbstractCreeper {

    public EntityReturnCreeper(World worldIn) {
        super(worldIn);
    }

    @Nullable
    @Override
    protected Item getDropItem() {
        return Items.BED;
    }

    @Override
    public float getBlockPathWeight(BlockPos pos) {
        return this.world.getBlockState(pos).getBlock().isBed(this.world.getBlockState(pos), this.world, pos, null) ?
                10 : super.getBlockPathWeight(pos);
    }

    @Override
    public boolean takumiExplodeEvent(Detonate event) {
        if (!this.world.isRemote) {
            List<EntityPlayer> list = new ArrayList<>();
            event.getAffectedEntities().forEach(entity -> {
                if (entity instanceof EntityPlayer) {
                    BlockPos pos = ((EntityPlayer) entity).getBedLocation(this.world.provider.getDimension());
                    if (pos != null &&
                            this.world.getBlockState(pos).getBlock().isBed(this.world.getBlockState(pos), this.world,
                                    pos, entity)) {
                        ((EntityPlayer) entity).attemptTeleport(pos.getX() + 0.5, pos.getY() + 0.75, pos.getZ() + 0.5);
                        //TakumiCraftCore.LOGGER.info(entity);
                        list.add(((EntityPlayer) entity));
                    }
                }
            });
            event.getAffectedEntities().removeAll(list);

        }
        return true;
    }

    @Override
    public int getPrimaryColor() {
        return 0x003300;
    }

    @Override
    public void onDeath(DamageSource source) {
        if (!this.world.isRemote) {
            this.dropItem(Items.ENDER_PEARL, 1);
        }
        super.onDeath(source);
    }

    @Override
    public void takumiExplode() {
        if (!this.world.isRemote) {
            this.world.createExplosion(this, this.posX, this.posY, this.posZ, this.getPowered() ? 8 : 5, false);
        }
    }

    @Override
    public EnumTakumiRank takumiRank() {
        return EnumTakumiRank.MID;
    }

    @Override
    public EnumTakumiType takumiType() {
        return EnumTakumiType.NORMAL_M;
    }

    @Override
    public int getExplosionPower() {
        return 0;
    }

    @Override
    public int getSecondaryColor() {
        return 0xff8888;
    }

    @Override
    public boolean isCustomSpawn() {
        return false;
    }

    @Override
    public String getRegisterName() {
        return "returncreeper";
    }

    @Override
    public int getRegisterID() {
        return 266;
    }

    @Override
    public void additionalSpawn() {
        EntityRegistry.addSpawn(this.getClass(), 1, 2, 2, EnumCreatureType.MONSTER,
                Biomes.SKY);
    }
}
