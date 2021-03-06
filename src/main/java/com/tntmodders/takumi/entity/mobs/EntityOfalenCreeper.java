package com.tntmodders.takumi.entity.mobs;

import com.tntmodders.asm.TakumiASMNameMap;
import com.tntmodders.takumi.core.TakumiItemCore;
import com.tntmodders.takumi.entity.EntityTakumiAbstractCreeper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.lang.reflect.Field;

public class EntityOfalenCreeper extends EntityTakumiAbstractCreeper {

    private int ofalenSinceIgnited;

    public EntityOfalenCreeper(World worldIn) {
        super(worldIn);
        try {
            Field field = TakumiASMNameMap.getField(EntityCreeper.class, "fuseTime");
            field.setAccessible(true);
            field.set(this, 50);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(50);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1D);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        int i = this.getCreeperState();
        this.ofalenSinceIgnited += i;
        if (this.ofalenSinceIgnited < 0) {
            this.ofalenSinceIgnited = 0;
        }
        if (!this.world.isRemote && this.ofalenSinceIgnited % 50 == 1) {
            this.world.createExplosion(this, this.posX, this.posY, this.posZ, 6f, false);
        }
    }

    @Override
    public void takumiExplode() {
        if (!this.world.isRemote) {
            for (int x = -10; x <= 10; x++) {
                for (int y = -10; y <= 10; y++) {
                    for (int z = -10; z <= 10; z++) {
                        if (x * x + y * y + z * z > 98 && x * x + y * y + z * z < 102) {
                            this.world.createExplosion(this, this.posX + x, this.posY + y, this.posZ + z,
                                    this.getPowered() ? 5 : 2.5f, true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public EnumTakumiRank takumiRank() {
        return EnumTakumiRank.HIGH;
    }

    @Override
    public EnumTakumiType takumiType() {
        return EnumTakumiType.GROUND_MD;
    }

    @Override
    public int getExplosionPower() {
        return 4;
    }

    @Override
    public int getSecondaryColor() {
        return 0xdd0000;
    }

    @Override
    public boolean isCustomSpawn() {
        return true;
    }

    @Override
    public String getRegisterName() {
        return "ofalencreeper";
    }

    @Override
    public int getRegisterID() {
        return 402;
    }

    @Override
    public int getPrimaryColor() {
        return 0x0000dd;
    }

    @Override
    public void onDeath(DamageSource source) {
        if (!this.world.isRemote) {
            this.dropItem(TakumiItemCore.TAKUMI_OFALEN, this.rand.nextBoolean() ? 2 : 1 + this.rand.nextInt(2));
        }
        super.onDeath(source);
    }

    @Override
    public float getExplosionResistance(Explosion explosionIn, World worldIn, BlockPos pos, IBlockState blockStateIn) {
        return blockStateIn.getBlockHardness(worldIn, pos) == -1 ? 10000000f : 1f;
    }
}
