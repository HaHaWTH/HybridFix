package io.wdsj.hybridfix.mixin.late.voxelmap.residence_ext;

import com.mamiyaotaru.voxelmap.Map;
import com.mamiyaotaru.voxelmap.util.GLShim;
import com.mamiyaotaru.voxelmap.util.I18nUtils;
import io.wdsj.hybridfix.handler.voxelmap.SerializedResidence;
import io.wdsj.hybridfix.handler.voxelmap.VoxelMapResidenceStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(value = Map.class, remap = false)
public abstract class VoxelMapMixin {
    // @formatter:off
    @Shadow private double zoomScaleAdjusted;
    @Shadow private int lastImageX;
    @Shadow private int lastImageZ;
    @Shadow private Minecraft game;
    @Shadow private FontRenderer fontRenderer;
    @Shadow private boolean fullscreenMap;
    @Shadow protected abstract int chkLen(String paramStr);
    @Shadow private int scHeight;
    @Shadow private int ztimer;
    @Shadow private int scWidth;
    @Shadow protected abstract void write(String text, float x, float y, int color);
    // @formatter:on

    @Inject(method = "onTickInGame", at = @At("HEAD"))
    private void hybridfix$updateResidenceTracker(CallbackInfo ci) {
        if (this.game.player != null && this.game.gameSettings != null) {
            int vd = this.game.gameSettings.renderDistanceChunks;
            VoxelMapResidenceStorage.INSTANCE.updatePlayerPos(this.game.player.posX, this.game.player.posZ, vd);
        }
    }

    @Unique
    private static final Object[] hybridfix$CACHE = new Object[2];
    @Inject(method = "showCoords", at = @At("RETURN"))
    private void hybridfix$displayCurrentResName(int x, int y, CallbackInfo ci) {
        if (!this.fullscreenMap || this.game.player == null) return;

        SerializedResidence current = hybridfix$findCurrentRes();
        if (current == null) return;

        hybridfix$CACHE[0] = current.name;
        hybridfix$CACHE[1] = current.owner;
        String name = I18nUtils.getString("hybridfix.voxelmap_ext.current_residence", hybridfix$CACHE);
        hybridfix$CACHE[0] = null;
        hybridfix$CACHE[1] = null;
        float yOff = (ztimer > 0) ? 25.0F : 15.0F;
        this.write(name, (float)(this.scWidth / 2 - this.chkLen(name) / 2), yOff, 0xFFFFFF);
    }

    @Unique
    private SerializedResidence hybridfix$findCurrentRes() {
        double px = this.game.player.posX;
        double py = this.game.player.posY;
        double pz = this.game.player.posZ;
        for (SerializedResidence res : VoxelMapResidenceStorage.INSTANCE.getActiveResidences()) {
            if (px >= res.minX && px <= res.maxX + 1 && py >= res.minY && py <= res.maxY + 1 && pz >= res.minZ && pz <= res.maxZ + 1)
                return res;
        }
        return null;
    }

    @Inject(
            method = "renderMap",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mamiyaotaru/voxelmap/util/GLUtils;drawPost()V",
                    ordinal = 3,
                    shift = At.Shift.AFTER
            )
    )
    private void hybridfix$drawResOutlineWithNativeMask(int x, int y, int scScale, CallbackInfo ci) {
        Collection<SerializedResidence> areas = VoxelMapResidenceStorage.INSTANCE.getActiveResidences();
        if (areas.isEmpty()) return;

        GLShim.glDisable(GL11.GL_TEXTURE_2D);
        GLShim.glDisable(GL11.GL_DEPTH_TEST);

        float dynamicLineWidth = (float) (2.0 / zoomScaleAdjusted);
        dynamicLineWidth = Math.max(1.0f, Math.min(dynamicLineWidth, 4.0f));
        GL11.glLineWidth(dynamicLineWidth);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (SerializedResidence res : areas) {
            double x1 = (res.minX - lastImageX) / zoomScaleAdjusted;
            double z1 = (res.minZ - lastImageZ) / zoomScaleAdjusted;
            double x2 = (res.maxX + 1 - lastImageX) / zoomScaleAdjusted;
            double z2 = (res.maxZ + 1 - lastImageZ) / zoomScaleAdjusted;

            double renderX1 = x + x1;
            double renderZ1 = y + z1;
            double renderX2 = x + x2;
            double renderZ2 = y + z2;

            int c = res.colorHash;
            int r = c >> 16 & 255;
            int g = c >> 8 & 255;
            int b = c & 255;

            buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(renderX1, renderZ2, 0).color(r, g, b, 255).endVertex();
            buffer.pos(renderX2, renderZ2, 0).color(r, g, b, 255).endVertex();
            buffer.pos(renderX2, renderZ1, 0).color(r, g, b, 255).endVertex();
            buffer.pos(renderX1, renderZ1, 0).color(r, g, b, 255).endVertex();
            tessellator.draw();
        }

        GL11.glLineWidth(1.0f);
        GLShim.glEnable(GL11.GL_DEPTH_TEST);
        GLShim.glEnable(GL11.GL_TEXTURE_2D);
        GLShim.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }
}