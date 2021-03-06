package com.tntmodders.takumi.client.render;
/*
import com.tntmodders.takumi.entity.item.EntityAttackBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelCreeper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderAttackBlock<T extends EntityAttackBlock> extends RenderLiving<T> {
    private static final ResourceLocation LIGHTNING_TEXTURE =
            new ResourceLocation("textures/entity/creeper/creeper_armor.png");

    public RenderAttackBlock(RenderManager rendermanagerIn) {
        super(rendermanagerIn, new ModelCreeper(), 0f);
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        boolean flag = entity.isInvisible();
        GlStateManager.depthMask(!flag);
        this.bindTexture(LIGHTNING_TEXTURE);
        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        float f = (float) entity.ticksExisted + partialTicks;
        GlStateManager.translate(f * 0.01F, f * 0.01F, 0.0F);
        GlStateManager.matrixMode(5888);
        GlStateManager.enableBlend();
        float f1 = 0.5F;
        GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        this.getMainModel().setModelAttributes(this.getMainModel());
        Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(180, 1, 0, 0);
        GlStateManager.translate(0, -1.5, 0);
        this.getMainModel().render(entity, entity.limbSwing, entity.limbSwingAmount, entity.ticksExisted,
                entity.rotationYaw, entity.rotationPitch, 0.0625f);
        Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(flag);
        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return LIGHTNING_TEXTURE;
    }
}*/
