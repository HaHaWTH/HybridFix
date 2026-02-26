package io.wdsj.hybridfix.mixin.late.voxelmap.residence_ext;

import com.mamiyaotaru.voxelmap.persistent.GuiPersistentMap;
import com.mamiyaotaru.voxelmap.util.GLShim;
import io.wdsj.hybridfix.handler.voxelmap.SerializedResidence;
import io.wdsj.hybridfix.handler.voxelmap.VMResidenceRenderHelper;
import io.wdsj.hybridfix.handler.voxelmap.VoxelMapResidenceStorage;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
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

@Mixin(GuiPersistentMap.class)
public abstract class GuiPersistentMapMixin {
    // @formatter:off
    @Shadow(remap = false) private float mapToGui;
    @Shadow(remap = false) private float scScale;
    @Shadow(remap = false) private int top;
    @Shadow(remap = false) private int bottom;
    @Shadow(remap = false) private boolean oldNorth;
    @Shadow(remap = false) private Minecraft mc;

    @Unique private final Long2IntOpenHashMap hybridfix$posCount  = new Long2IntOpenHashMap();
    @Unique private final Long2IntOpenHashMap hybridfix$posOffset = new Long2IntOpenHashMap();
    // @formatter:on

    @Unique
    private static long hybridfix$posKey(float cx, float cz) {
        return ((long) Float.floatToIntBits(cx) << 32)
                | (Float.floatToIntBits(cz) & 0xFFFFFFFFL);
    }

    @Inject(
            method = "drawScreen",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mamiyaotaru/voxelmap/util/GLShim;glEnable(I)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER,
                    remap = false
            )
    )
    private void hybridfix$drawResidencesOnWorldMap(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        Collection<SerializedResidence> areas = VoxelMapResidenceStorage.INSTANCE.getAllResidences();
        if (areas.isEmpty()) return;

        double playerY = mc.player != null ? mc.player.posY : 64.0D;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int pixelTop = (int) (top * scScale);
        int pixelBottom = (int) (bottom * scScale);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(0, mc.displayHeight - pixelBottom, mc.displayWidth, pixelBottom - pixelTop);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (SerializedResidence res : areas) {
            float yAlpha = VMResidenceRenderHelper.yAlpha(playerY, res);
            if (yAlpha < VMResidenceRenderHelper.ALPHA_CUTOFF) continue;
            float rx1 = res.minX * mapToGui;
            float rz1 = res.minZ * mapToGui;
            float rx2 = (res.maxX + 1) * mapToGui;
            float rz2 = (res.maxZ + 1) * mapToGui;

            int c = res.colorHash;
            int r = (c >> 16) & 0xFF;
            int g = (c >> 8) & 0xFF;
            int b = c & 0xFF;
            int a = (int) (((c >> 24) & 0xFF) * yAlpha);
            buffer.pos(rx1, rz1, 0).color(r, g, b, a).endVertex();
            buffer.pos(rx1, rz2, 0).color(r, g, b, a).endVertex();
            buffer.pos(rx2, rz2, 0).color(r, g, b, a).endVertex();
            buffer.pos(rx2, rz1, 0).color(r, g, b, a).endVertex();
        }
        tessellator.draw();

        GL11.glLineWidth(2.0f);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (SerializedResidence res : areas) {
            float yAlpha = VMResidenceRenderHelper.yAlpha(playerY, res);
            if (yAlpha < VMResidenceRenderHelper.ALPHA_CUTOFF) continue;
            int oa = (int) (255 * yAlpha);
            float rx1 = res.minX * mapToGui;
            float rz1 = res.minZ * mapToGui;
            float rx2 = (res.maxX + 1) * mapToGui;
            float rz2 = (res.maxZ + 1) * mapToGui;

            int c = res.colorHash;
            int r = (c >> 16) & 0xFF;
            int g = (c >> 8) & 0xFF;
            int b = c & 0xFF;

            buffer.pos(rx1, rz2, 0).color(r, g, b, oa).endVertex();
            buffer.pos(rx2, rz2, 0).color(r, g, b, oa).endVertex();
            buffer.pos(rx2, rz2, 0).color(r, g, b, oa).endVertex();
            buffer.pos(rx2, rz1, 0).color(r, g, b, oa).endVertex();
            buffer.pos(rx2, rz1, 0).color(r, g, b, oa).endVertex();
            buffer.pos(rx1, rz1, 0).color(r, g, b, oa).endVertex();
            buffer.pos(rx1, rz1, 0).color(r, g, b, oa).endVertex();
            buffer.pos(rx1, rz2, 0).color(r, g, b, oa).endVertex();
        }
        tessellator.draw();
        GL11.glLineWidth(1.0f);

        GlStateManager.enableTexture2D();
        float fontScale = 2.0f / scScale;
        FontRenderer fr = mc.fontRenderer;
        hybridfix$posCount.clear();
        hybridfix$posOffset.clear();
        for (SerializedResidence res : areas) {
            if (VMResidenceRenderHelper.yAlpha(playerY, res) < VMResidenceRenderHelper.ALPHA_CUTOFF) continue;
            float cx = (res.minX + res.maxX + 1) / 2.0f;
            float cz = (res.minZ + res.maxZ + 1) / 2.0f;
            hybridfix$posCount.addTo(hybridfix$posKey(cx, cz), 1);
        }

        GLShim.glPushMatrix();
        GLShim.glScalef(fontScale, fontScale, 1.0f);

        for (SerializedResidence res : areas) {
            float yAlpha = VMResidenceRenderHelper.yAlpha(playerY, res);
            if (yAlpha < VMResidenceRenderHelper.ALPHA_CUTOFF) continue;
            float cx = (res.minX + res.maxX + 1) / 2.0f;
            float cz = (res.minZ + res.maxZ + 1) / 2.0f;
            long key = hybridfix$posKey(cx, cz);

            int total = hybridfix$posCount.get(key);
            int index = hybridfix$posOffset.getOrDefault(key, 0);
            hybridfix$posOffset.put(key, index + 1);

            String label;
            if (total > 1) {
                String full = res.name + " \u00a77(Y:" + res.minY + "~" + res.maxY + ")";
                float resScreenW = (res.maxX + 1 - res.minX) * mapToGui / fontScale;
                label = (fr.getStringWidth(full) + 4 <= resScreenW) ? full : res.name;
            } else {
                label = res.name;
            }
            float resScreenW = (res.maxX + 1 - res.minX) * mapToGui / fontScale;
            int labelW = fr.getStringWidth(label);
            if (resScreenW < labelW + 4) continue;

            float lineH = fr.FONT_HEIGHT + 2;
            float groupOffset = (index - (total - 1) / 2.0f) * lineH;

            float textX = cx * mapToGui / fontScale - labelW / 2.0f;
            float textZ = cz * mapToGui / fontScale - fr.FONT_HEIGHT / 2.0f + groupOffset;

            int textAlpha = Math.max(VMResidenceRenderHelper.MIN_TEXT_ALPHA, (int) (255 * yAlpha));
            int color = (textAlpha << 24) | 0xFFFFFF;

            if (oldNorth) {
                float px = cx * mapToGui / fontScale;
                float pz = cz * mapToGui / fontScale;
                GLShim.glPushMatrix();
                GLShim.glTranslatef(px, pz, 0);
                GLShim.glRotatef(-90, 0, 0, 1);
                GLShim.glTranslatef(-px, -pz, 0);
            }

            fr.drawStringWithShadow(label, textX, textZ, color);

            if (oldNorth) {
                GLShim.glPopMatrix();
            }
        }

        GLShim.glPopMatrix();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }
}