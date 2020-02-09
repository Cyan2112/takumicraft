package com.tntmodders.takumi.tileentity;

import com.google.common.collect.Lists;
import com.tntmodders.takumi.core.TakumiBlockCore;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class TileEntityTakumiPiston extends TileEntityPiston {
    private IBlockState pistonState;
    private EnumFacing pistonFacing;
    /**
     * if this piston is extending or not
     */
    private boolean extending;
    private boolean shouldHeadBeRendered;
    private static final ThreadLocal<EnumFacing> MOVING_ENTITY = ThreadLocal.withInitial(() -> null);
    private float progress;
    /**
     * the progress in (de)extending
     */
    private float lastProgress;

    public TileEntityTakumiPiston() {
        super();
    }

    public TileEntityTakumiPiston(IBlockState pistonStateIn, EnumFacing pistonFacingIn, boolean extendingIn, boolean shouldHeadBeRenderedIn) {
        super(pistonStateIn, pistonFacingIn, extendingIn, shouldHeadBeRenderedIn);
        this.pistonState = pistonStateIn;
        this.pistonFacing = pistonFacingIn;
        this.extending = extendingIn;
        this.shouldHeadBeRendered = shouldHeadBeRenderedIn;
    }

    @Override
    public IBlockState getPistonState() {
        return this.pistonState;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public int getBlockMetadata() {
        return 0;
    }

    /**
     * Returns true if a piston is extending
     */
    @Override
    public boolean isExtending() {
        return this.extending;
    }

    @Override
    public EnumFacing getFacing() {
        return this.pistonFacing;
    }

    @Override
    public boolean shouldPistonHeadBeRendered() {
        return this.shouldHeadBeRendered;
    }

    /**
     * Get interpolated progress value (between lastProgress and progress) given the fractional time between ticks as an
     * argument
     */
    @Override
    @SideOnly(Side.CLIENT)
    public float getProgress(float ticks) {
        if (ticks > 1.0F) {
            ticks = 1.0F;
        }

        return this.lastProgress + (this.progress - this.lastProgress) * ticks;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getOffsetX(float ticks) {
        return (float) this.pistonFacing.getFrontOffsetX() * this.getExtendedProgress(this.getProgress(ticks));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getOffsetY(float ticks) {
        return (float) this.pistonFacing.getFrontOffsetY() * this.getExtendedProgress(this.getProgress(ticks));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getOffsetZ(float ticks) {
        return (float) this.pistonFacing.getFrontOffsetZ() * this.getExtendedProgress(this.getProgress(ticks));
    }

    private float getExtendedProgress(float p_184320_1_) {
        return this.extending ? p_184320_1_ - 1.0F : 1.0F - p_184320_1_;
    }

    @Override
    public AxisAlignedBB getAABB(IBlockAccess p_184321_1_, BlockPos p_184321_2_) {
        return this.getAABB(p_184321_1_, p_184321_2_, this.progress).union(this.getAABB(p_184321_1_, p_184321_2_, this.lastProgress));
    }

    @Override
    public AxisAlignedBB getAABB(IBlockAccess p_184319_1_, BlockPos p_184319_2_, float p_184319_3_) {
        p_184319_3_ = this.getExtendedProgress(p_184319_3_);
        IBlockState iblockstate = this.getCollisionRelatedBlockState();
        return iblockstate.getBoundingBox(p_184319_1_, p_184319_2_).offset(p_184319_3_ * (float) this.pistonFacing.getFrontOffsetX(), p_184319_3_ * (float) this.pistonFacing.getFrontOffsetY(), p_184319_3_ * (float) this.pistonFacing.getFrontOffsetZ());
    }

    private IBlockState getCollisionRelatedBlockState() {
        return !this.isExtending() && this.shouldPistonHeadBeRendered() ? TakumiBlockCore.CREEPER_PISTON_HEAD.getDefaultState().withProperty(BlockPistonExtension.TYPE, this.pistonState.getBlock() == TakumiBlockCore.CREEPER_STICKY_PISTON ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT).withProperty(BlockPistonExtension.FACING, this.pistonState.getValue(BlockPistonBase.FACING)) : this.pistonState;
    }

    private void moveCollidedEntities(float p_184322_1_) {
        EnumFacing enumfacing = this.extending ? this.pistonFacing : this.pistonFacing.getOpposite();
        double d0 = p_184322_1_ - this.progress;
        List<AxisAlignedBB> list = Lists.newArrayList();
        this.getCollisionRelatedBlockState().addCollisionBoxToList(this.world, BlockPos.ORIGIN, new AxisAlignedBB(BlockPos.ORIGIN), list, null, true);

        if (!list.isEmpty()) {
            AxisAlignedBB axisalignedbb = this.moveByPositionAndProgress(this.getMinMaxPiecesAABB(list));
            List<Entity> list1 = this.world.getEntitiesWithinAABBExcludingEntity(null, this.getMovementArea(axisalignedbb, enumfacing, d0).union(axisalignedbb));

            if (!list1.isEmpty()) {
                boolean flag = this.pistonState.getBlock().isStickyBlock(this.pistonState);

                for (Entity entity : list1) {
                    if (entity.getPushReaction() != EnumPushReaction.IGNORE) {
                        if (flag) {
                            switch (enumfacing.getAxis()) {
                                case X:
                                    entity.motionX = enumfacing.getFrontOffsetX();
                                    break;
                                case Y:
                                    entity.motionY = enumfacing.getFrontOffsetY();
                                    break;
                                case Z:
                                    entity.motionZ = enumfacing.getFrontOffsetZ();
                            }
                        }

                        double d1 = 0.0D;

                        for (AxisAlignedBB axisAlignedBB : list) {
                            AxisAlignedBB axisalignedbb1 = this.getMovementArea(this.moveByPositionAndProgress(axisAlignedBB), enumfacing, d0);
                            AxisAlignedBB axisalignedbb2 = entity.getEntityBoundingBox();

                            if (axisalignedbb1.intersects(axisalignedbb2)) {
                                d1 = Math.max(d1, this.getMovement(axisalignedbb1, enumfacing, axisalignedbb2));

                                if (d1 >= d0) {
                                    break;
                                }
                            }
                        }

                        if (d1 > 0.0D) {
                            d1 = Math.min(d1, d0) + 0.01D;
                            MOVING_ENTITY.set(enumfacing);
                            entity.move(MoverType.PISTON, d1 * (double) enumfacing.getFrontOffsetX(), d1 * (double) enumfacing.getFrontOffsetY(), d1 * (double) enumfacing.getFrontOffsetZ());
                            MOVING_ENTITY.set(null);

                            if (!this.extending && this.shouldHeadBeRendered) {
                                this.fixEntityWithinPistonBase(entity, enumfacing, d0);
                            }
                        }
                    }
                }
            }
        }
    }

    private AxisAlignedBB getMinMaxPiecesAABB(List<AxisAlignedBB> p_191515_1_) {
        double d0 = 0.0D;
        double d1 = 0.0D;
        double d2 = 0.0D;
        double d3 = 1.0D;
        double d4 = 1.0D;
        double d5 = 1.0D;

        for (AxisAlignedBB axisalignedbb : p_191515_1_) {
            d0 = Math.min(axisalignedbb.minX, d0);
            d1 = Math.min(axisalignedbb.minY, d1);
            d2 = Math.min(axisalignedbb.minZ, d2);
            d3 = Math.max(axisalignedbb.maxX, d3);
            d4 = Math.max(axisalignedbb.maxY, d4);
            d5 = Math.max(axisalignedbb.maxZ, d5);
        }

        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }

    private double getMovement(AxisAlignedBB p_190612_1_, EnumFacing facing, AxisAlignedBB p_190612_3_) {
        switch (facing.getAxis()) {
            case X:
                return getDeltaX(p_190612_1_, facing, p_190612_3_);
            case Y:
            default:
                return getDeltaY(p_190612_1_, facing, p_190612_3_);
            case Z:
                return getDeltaZ(p_190612_1_, facing, p_190612_3_);
        }
    }

    private AxisAlignedBB moveByPositionAndProgress(AxisAlignedBB p_190607_1_) {
        double d0 = this.getExtendedProgress(this.progress);
        return p_190607_1_.offset((double) this.pos.getX() + d0 * (double) this.pistonFacing.getFrontOffsetX(), (double) this.pos.getY() + d0 * (double) this.pistonFacing.getFrontOffsetY(), (double) this.pos.getZ() + d0 * (double) this.pistonFacing.getFrontOffsetZ());
    }

    private AxisAlignedBB getMovementArea(AxisAlignedBB p_190610_1_, EnumFacing p_190610_2_, double p_190610_3_) {
        double d0 = p_190610_3_ * (double) p_190610_2_.getAxisDirection().getOffset();
        double d1 = Math.min(d0, 0.0D);
        double d2 = Math.max(d0, 0.0D);

        switch (p_190610_2_) {
            case WEST:
                return new AxisAlignedBB(p_190610_1_.minX + d1, p_190610_1_.minY, p_190610_1_.minZ, p_190610_1_.minX + d2, p_190610_1_.maxY, p_190610_1_.maxZ);
            case EAST:
                return new AxisAlignedBB(p_190610_1_.maxX + d1, p_190610_1_.minY, p_190610_1_.minZ, p_190610_1_.maxX + d2, p_190610_1_.maxY, p_190610_1_.maxZ);
            case DOWN:
                return new AxisAlignedBB(p_190610_1_.minX, p_190610_1_.minY + d1, p_190610_1_.minZ, p_190610_1_.maxX, p_190610_1_.minY + d2, p_190610_1_.maxZ);
            case UP:
            default:
                return new AxisAlignedBB(p_190610_1_.minX, p_190610_1_.maxY + d1, p_190610_1_.minZ, p_190610_1_.maxX, p_190610_1_.maxY + d2, p_190610_1_.maxZ);
            case NORTH:
                return new AxisAlignedBB(p_190610_1_.minX, p_190610_1_.minY, p_190610_1_.minZ + d1, p_190610_1_.maxX, p_190610_1_.maxY, p_190610_1_.minZ + d2);
            case SOUTH:
                return new AxisAlignedBB(p_190610_1_.minX, p_190610_1_.minY, p_190610_1_.maxZ + d1, p_190610_1_.maxX, p_190610_1_.maxY, p_190610_1_.maxZ + d2);
        }
    }

    private void fixEntityWithinPistonBase(Entity p_190605_1_, EnumFacing p_190605_2_, double p_190605_3_) {
        AxisAlignedBB axisalignedbb = p_190605_1_.getEntityBoundingBox();
        AxisAlignedBB axisalignedbb1 = Block.FULL_BLOCK_AABB.offset(this.pos);

        if (axisalignedbb.intersects(axisalignedbb1)) {
            EnumFacing enumfacing = p_190605_2_.getOpposite();
            double d0 = this.getMovement(axisalignedbb1, enumfacing, axisalignedbb) + 0.01D;
            double d1 = this.getMovement(axisalignedbb1, enumfacing, axisalignedbb.intersect(axisalignedbb1)) + 0.01D;

            if (Math.abs(d0 - d1) < 0.01D) {
                d0 = Math.min(d0, p_190605_3_) + 0.01D;
                MOVING_ENTITY.set(p_190605_2_);
                p_190605_1_.move(MoverType.PISTON, d0 * (double) enumfacing.getFrontOffsetX(), d0 * (double) enumfacing.getFrontOffsetY(), d0 * (double) enumfacing.getFrontOffsetZ());
                MOVING_ENTITY.set(null);
            }
        }
    }

    private static double getDeltaX(AxisAlignedBB p_190611_0_, EnumFacing facing, AxisAlignedBB p_190611_2_) {
        return facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? p_190611_0_.maxX - p_190611_2_.minX : p_190611_2_.maxX - p_190611_0_.minX;
    }

    private static double getDeltaY(AxisAlignedBB p_190608_0_, EnumFacing facing, AxisAlignedBB p_190608_2_) {
        return facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? p_190608_0_.maxY - p_190608_2_.minY : p_190608_2_.maxY - p_190608_0_.minY;
    }

    private static double getDeltaZ(AxisAlignedBB p_190604_0_, EnumFacing facing, AxisAlignedBB p_190604_2_) {
        return facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? p_190604_0_.maxZ - p_190604_2_.minZ : p_190604_2_.maxZ - p_190604_0_.minZ;
    }

    /**
     * removes a piston's tile entity (and if the piston is moving, stops it)
     */
    @Override
    public void clearPistonTileEntity() {
        if (this.lastProgress < 1.0F && this.world != null) {
            this.progress = 1.0F;
            this.lastProgress = this.progress;
            this.world.removeTileEntity(this.pos);
            this.invalidate();

            if (this.world.getBlockState(this.pos).getBlock() == TakumiBlockCore.CREEPER_PISTON_EXTEND) {
                this.world.setBlockState(this.pos, this.pistonState, 3);
                this.world.neighborChanged(this.pos, this.pistonState.getBlock(), this.pos);
            }
        }
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    @Override
    public void update() {
        this.lastProgress = this.progress;

        if (this.lastProgress >= 1.0F) {
            this.world.removeTileEntity(this.pos);
            this.invalidate();

            if (this.world.getBlockState(this.pos).getBlock() == TakumiBlockCore.CREEPER_PISTON_EXTEND) {
                this.world.setBlockState(this.pos, this.pistonState, 3);
                this.world.neighborChanged(this.pos, this.pistonState.getBlock(), this.pos);
            }
        } else {
            float f = this.progress + 0.5F;
            this.moveCollidedEntities(f);
            this.progress = f;

            if (this.progress >= 1.0F) {
                this.progress = 1.0F;
            }
        }
    }

    @Override
    public void addCollissionAABBs(World p_190609_1_, BlockPos p_190609_2_, AxisAlignedBB p_190609_3_, List<AxisAlignedBB> p_190609_4_, @Nullable Entity p_190609_5_) {
        if (!this.extending && this.shouldHeadBeRendered) {
            this.pistonState.withProperty(BlockPistonBase.EXTENDED, Boolean.TRUE).addCollisionBoxToList(p_190609_1_, p_190609_2_, p_190609_3_, p_190609_4_, p_190609_5_, false);
        }

        EnumFacing enumfacing = MOVING_ENTITY.get();

        if ((double) this.progress >= 1.0D || enumfacing != (this.extending ? this.pistonFacing : this.pistonFacing.getOpposite())) {
            int i = p_190609_4_.size();
            IBlockState iblockstate;

            if (this.shouldPistonHeadBeRendered()) {
                iblockstate = TakumiBlockCore.CREEPER_PISTON_HEAD.getDefaultState().withProperty(BlockPistonExtension.FACING, this.pistonFacing).withProperty(BlockPistonExtension.SHORT, this.extending != 1.0F - this.progress < 0.25F);
            } else {
                iblockstate = this.pistonState;
            }

            float f = this.getExtendedProgress(this.progress);
            double d0 = (float) this.pistonFacing.getFrontOffsetX() * f;
            double d1 = (float) this.pistonFacing.getFrontOffsetY() * f;
            double d2 = (float) this.pistonFacing.getFrontOffsetZ() * f;
            iblockstate.addCollisionBoxToList(p_190609_1_, p_190609_2_, p_190609_3_.offset(-d0, -d1, -d2), p_190609_4_, p_190609_5_, true);

            for (int j = i; j < p_190609_4_.size(); ++j) {
                p_190609_4_.set(j, p_190609_4_.get(j).offset(d0, d1, d2));
            }
        }
    }
}
