package com.tntmodders.takumi.block;

import com.tntmodders.takumi.TakumiCraftCore;
import net.minecraft.block.BlockPane;
import net.minecraft.block.material.Material;

public class BlockTakumiIronBars extends BlockPane {

    public BlockTakumiIronBars(Material material, String s, float hardness, String tool) {
        super(Material.IRON,true);
        this.setRegistryName(TakumiCraftCore.MODID, s);
        this.setCreativeTab(TakumiCraftCore.TAB_CREEPER);
        this.setUnlocalizedName(s);
        this.setHardness(hardness);
        this.setResistance(10000000f);
        if (tool != null) {
            this.setHarvestLevel(tool, 2);
        }
    }
}
