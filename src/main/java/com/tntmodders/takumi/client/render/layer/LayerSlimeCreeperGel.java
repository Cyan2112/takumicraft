package com.tntmodders.takumi.client.render.layer;

import com.tntmodders.takumi.client.render.RenderSlimeCreeper;
import com.tntmodders.takumi.entity.mobs.EntitySlimeCreeper;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelSlime;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;


public class LayerSlimeCreeperGel implements LayerRenderer<EntitySlimeCreeper> {

    private final RenderSlimeCreeper slimeRenderer;
    private final ModelBase slimeModel = new ModelSlime(0);

    public LayerSlimeCreeperGel(RenderSlimeCreeper slimeRendererIn) {
        this.slimeRenderer = slimeRendererIn;
    }

    @Override
    public void doRenderLayer(EntitySlimeCreeper entitylivingbaseIn, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (!entitylivingbaseIn.isInvisible()) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableNormalize();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            this.slimeModel.setModelAttributes(this.slimeRenderer.getMainModel());
            this.slimeModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch,
                    scale);
            GlStateManager.disableBlend();
            GlStateManager.disableNormalize();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}