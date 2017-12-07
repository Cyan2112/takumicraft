package com.tntmodders.takumi.block;

import com.tntmodders.takumi.TakumiCraftCore;
import com.tntmodders.takumi.core.TakumiEnchantmentCore;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockTakumiGunOre extends Block {
    
    public BlockTakumiGunOre() {
        super(Material.ROCK);
        this.setRegistryName(TakumiCraftCore.MODID, "gunore");
        this.setCreativeTab(TakumiCraftCore.TAB_CREEPER);
        this.setUnlocalizedName("gunore");
        this.setHardness(5f);
        this.setResistance(0f);
        this.setHarvestLevel("pickaxe", 1);
    }
    
    @Override
    public int quantityDropped(Random random) {
        return random.nextInt(1) * random.nextInt(5);
    }
    
    @Override
    public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn) {
        if (!worldIn.isRemote) {
            this.explode(worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
    }
    
    public void explode(World world, int x, int y, int z) {
        world.createExplosion(null, x + 0.5, y + 0.5, z + 0.5, getPower(), true);
    }
    
    float getPower() {
        return 1.5f;
    }
    
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.GUNPOWDER;
    }
    
    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        // ドロップする経験値の設定。(他の鉱石の数値はBlockOre参照。ここではダイヤと同じ)
        Random rand = new Random();
        return rand.nextInt(5) + 3;
    }
    
    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (!worldIn.isRemote && !(player.getHeldItemMainhand() != null && EnchantmentHelper
                .getEnchantments(player.getHeldItemMainhand()).containsKey(TakumiEnchantmentCore
                        .MINESWEEPER) && (player.getHeldItemMainhand().getStrVsBlock(state) > 1.0f || this
                .getHarvestTool(state) == null))) {
            this.explode(worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
    }
    
    // 幸運でドロップする量の設定。(幸運で掘った時にドロップする量をランダムにできる)
    @Override
    public int quantityDroppedWithBonus(int level, Random random) {
        if (level > 0 && Item.getItemFromBlock(this) != this.getItemDropped(this.getDefaultState(), random, level)) {
            int j = random.nextInt(level + 2) - 1;
            if (j < 0) {
                j = 0;
            }
            return this.quantityDropped(random) * (j + 1);
        } else {
            return this.quantityDropped(random);
        }
    }
    
    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        Random rand = new Random();
        return rand.nextInt(5) + 3;
    }
    
    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }
}
