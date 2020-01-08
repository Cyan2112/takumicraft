package com.tntmodders.takumi.entity.mobs;

import com.tntmodders.takumi.client.render.RenderSkeletonCreeper;
import com.tntmodders.takumi.core.TakumiItemCore;
import com.tntmodders.takumi.entity.EntityTakumiAbstractCreeper;
import com.tntmodders.takumi.entity.ai.EntityAIAttackRangedTakumiBow;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.event.world.ExplosionEvent.Detonate;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Calendar;

public class EntitySkeletonCreeper extends EntityTakumiAbstractCreeper implements IRangedAttackMob {

    private static final DataParameter<Boolean> SWINGING_ARMS =
            EntityDataManager.createKey(EntitySkeletonCreeper.class, DataSerializers.BOOLEAN);
    private final EntityAIAttackRangedTakumiBow<EntitySkeletonCreeper> aiArrowAttack =
            new EntityAIAttackRangedTakumiBow<>(this, 1.0D, 20, 15.0F);
    private final EntityAIAttackMelee aiAttackOnCollide = new EntityAIAttackMelee(this, 1.2D, false) {
        @Override
        public void startExecuting() {
            super.startExecuting();
            EntitySkeletonCreeper.this.setSwingingArms(true);
        }

        @Override
        public void resetTask() {
            super.resetTask();
            EntitySkeletonCreeper.this.setSwingingArms(false);
        }
    };

    public EntitySkeletonCreeper(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 1.99F);
        this.setCombatTask();
    }

    /**
     * sets this entity's combat AI.
     */
    public void setCombatTask() {
        if (this.world != null && !this.world.isRemote) {
            this.tasks.removeTask(this.aiAttackOnCollide);
            this.tasks.removeTask(this.aiArrowAttack);
            ItemStack itemstack = this.getHeldItemMainhand();

            if (itemstack.getItem() == TakumiItemCore.TAKUMI_BOW) {
                int i = 40;

                if (this.world.getDifficulty() != EnumDifficulty.HARD) {
                    i = 60;
                }

                this.aiArrowAttack.setAttackCooldown(i);
                this.tasks.addTask(1, this.aiArrowAttack);
            } else {
                this.tasks.addTask(1, this.aiAttackOnCollide);
            }
        }
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAICreeperSwell(this));
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIRestrictSun(this));
        this.tasks.addTask(3, new EntityAIFleeSun(this, 1.0D));
        this.tasks.addTask(3, new EntityAIAvoidEntity<>(this, EntityWolf.class, 6.0F, 1.0D, 1.2D));
        this.tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(6, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityIronGolem.class, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SWINGING_ARMS, Boolean.FALSE);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setCombatTask();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ENTITY_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_SKELETON_DEATH;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LootTableList.ENTITIES_SKELETON;
    }

    @Override
    public void additionalSpawn() {
        EntityRegistry.addSpawn(this.getClass(), this.takumiRank().getSpawnWeight() * 2, 1, 5, EnumCreatureType.MONSTER,
                Biomes.HELL);
    }

    @Override
    public boolean takumiExplodeEvent(Detonate event) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Object getRender(RenderManager manager) {
        return new RenderSkeletonCreeper<>(manager);
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (!this.world.isRemote) {
            if (this.rand.nextBoolean()) {
                this.entityDropItem(this.getHeldItem(EnumHand.MAIN_HAND), 0);
            }
            if (this.getHeldItemMainhand().getItem() == TakumiItemCore.TAKUMI_BOW) {
                this.dropItem(TakumiItemCore.TAKUMI_ARROW_HA,
                        this.rand.nextBoolean() ? this.rand.nextInt(5) : this.rand.nextInt(1));
            }
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, Block blockIn) {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }

    /**
     * Returns the Y Offset of this entity.
     */
    @Override
    public double getYOffset() {
        return -0.6D;
    }

    @Override
    public float getEyeHeight() {
        return 1.74F;
    }

    protected SoundEvent getStepSound() {
        return SoundEvents.ENTITY_SKELETON_STEP;
    }

    /**
     * Get this Entity's EnumCreatureAttribute
     */
    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }

    /**
     * Handles updating while riding another entity
     */
    @Override
    public void updateRidden() {
        super.updateRidden();

        if (this.getRidingEntity() instanceof EntityCreature) {
            EntityCreature entitycreature = (EntityCreature) this.getRidingEntity();
            this.renderYawOffset = entitycreature.renderYawOffset;
        }
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    @Override
    public void onLivingUpdate() {
        if (this.world.isDaytime() && !this.world.isRemote) {
            float f = this.getBrightness();
            BlockPos blockpos = this.getRidingEntity() instanceof EntityBoat ?
                    new BlockPos(this.posX, (double) Math.round(this.posY), this.posZ).up() :
                    new BlockPos(this.posX, (double) Math.round(this.posY), this.posZ);

            if (f > 0.5F && this.rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.world.canSeeSky(blockpos)) {
                boolean flag = true;
                ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

                if (!itemstack.isEmpty()) {
                    if (itemstack.isItemStackDamageable()) {
                        itemstack.setItemDamage(itemstack.getItemDamage() + this.rand.nextInt(2));

                        if (itemstack.getItemDamage() >= itemstack.getMaxDamage()) {
                            this.renderBrokenItemStack(itemstack);
                            this.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStack.EMPTY);
                        }
                    }

                    flag = false;
                }

                if (flag) {
                    this.setFire(8);
                }
            }
        }

        super.onLivingUpdate();
    }

    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack, boolean flg) {
        super.setItemStackToSlot(slotIn, stack);
        if (flg && !this.world.isRemote && slotIn == EntityEquipmentSlot.MAINHAND) {
            this.setCombatTask();
        }
    }

    /**
     * Attack the specified entity using a ranged attack.
     */
    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        EntityArrow entityarrow = this.getArrow(distanceFactor);
        double d0 = target.posX - this.posX;
        double d1 = target.getEntityBoundingBox().minY + (double) (target.height / 3.0F) - entityarrow.posY;
        double d2 = target.posZ - this.posZ;
        double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
        entityarrow.setThrowableHeading(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F,
                (float) (14 - this.world.getDifficulty().getDifficultyId() * 4));
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        this.world.spawnEntity(entityarrow);
    }

    protected EntityArrow getArrow(float v) {
        return TakumiItemCore.TAKUMI_ARROW_HA.createArrow(this.world, new ItemStack(TakumiItemCore.TAKUMI_ARROW_HA),
                this);
    }

    @SideOnly(Side.CLIENT)
    public boolean isSwingingArms() {
        return this.dataManager.get(SWINGING_ARMS);
    }

    @Override
    public void setSwingingArms(boolean swingingArms) {
        this.dataManager.set(SWINGING_ARMS, swingingArms);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_SKELETON_AMBIENT;
    }

    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {
        this.setItemStackToSlot(slotIn, stack, true);
    }

    /**
     * Gives armor or weapon for entity based on given DifficultyInstance
     */
    @Override
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        super.setEquipmentBasedOnDifficulty(difficulty);
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(TakumiItemCore.TAKUMI_BOW));
    }

    /**
     * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
     * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory
     */
    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,
            @Nullable
                    IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        this.setEquipmentBasedOnDifficulty(difficulty);
        this.setEnchantmentBasedOnDifficulty(difficulty);
        this.setCombatTask();
        this.setCanPickUpLoot(this.rand.nextFloat() < 0.55F * difficulty.getClampedAdditionalDifficulty());

        if (this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty()) {
            Calendar calendar = this.world.getCurrentDate();

            if (calendar.get(Calendar.MONTH) + 1 == 10 && calendar.get(Calendar.DATE) == 31 &&
                    this.rand.nextFloat() < 0.25F) {
                this.setItemStackToSlot(EntityEquipmentSlot.HEAD,
                        new ItemStack(this.rand.nextFloat() < 0.1F ? Blocks.LIT_PUMPKIN : Blocks.PUMPKIN));
                this.inventoryArmorDropChances[EntityEquipmentSlot.HEAD.getIndex()] = 0.0F;
            }
        }

        return livingdata;
    }

    @Override
    public void takumiExplode() {
    }

    @Override
    public EnumTakumiRank takumiRank() {
        return EnumTakumiRank.LOW;
    }

    @Override
    public EnumTakumiType takumiType() {
        return EnumTakumiType.NORMAL;
    }

    @Override
    public int getExplosionPower() {
        return 3;
    }

    @Override
    public int getSecondaryColor() {
        return 7846775;
    }

    @Override
    public boolean isCustomSpawn() {
        return false;
    }

    @Override
    public String getRegisterName() {
        return "skeletoncreeper";
    }

    @Override
    public int getRegisterID() {
        return 14;
    }
}
