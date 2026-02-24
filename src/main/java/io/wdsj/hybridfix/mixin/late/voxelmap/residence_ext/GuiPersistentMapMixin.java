package io.wdsj.hybridfix.mixin.late.voxelmap.residence_ext;

import com.mamiyaotaru.voxelmap.persistent.GuiPersistentMap;
import com.mamiyaotaru.voxelmap.util.GLShim;
import io.wdsj.hybridfix.handler.voxelmap.SerializedResidence;
import io.wdsj.hybridfix.handler.voxelmap.VoxelMapResidenceStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
    // @formatter:on

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
            float rx1 = res.minX * mapToGui;
            float rz1 = res.minZ * mapToGui;
            float rx2 = (res.maxX + 1) * mapToGui;
            float rz2 = (res.maxZ + 1) * mapToGui;

            int c = res.colorHash;
            int r = (c >> 16) & 0xFF;
            int g = (c >> 8) & 0xFF;
            int b = c & 0xFF;
            int a = (c >> 24) & 0xFF; // 0x4D ≈ 30%

            buffer.pos(rx1, rz1, 0).color(r, g, b, a).endVertex();
            buffer.pos(rx1, rz2, 0).color(r, g, b, a).endVertex();
            buffer.pos(rx2, rz2, 0).color(r, g, b, a).endVertex();
            buffer.pos(rx2, rz1, 0).color(r, g, b, a).endVertex();
        }
        tessellator.draw();

        GL11.glLineWidth(2.0f);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (SerializedResidence res : areas) {
            float rx1 = res.minX * mapToGui;
            float rz1 = res.minZ * mapToGui;
            float rx2 = (res.maxX + 1) * mapToGui;
            float rz2 = (res.maxZ + 1) * mapToGui;

            int c = res.colorHash;
            int r = (c >> 16) & 0xFF;
            int g = (c >> 8) & 0xFF;
            int b = c & 0xFF;

            buffer.pos(rx1, rz2, 0).color(r, g, b, 255).endVertex();
            buffer.pos(rx2, rz2, 0).color(r, g, b, 255).endVertex();
            buffer.pos(rx2, rz2, 0).color(r, g, b, 255).endVertex();
            buffer.pos(rx2, rz1, 0).color(r, g, b, 255).endVertex();
            buffer.pos(rx2, rz1, 0).color(r, g, b, 255).endVertex();
            buffer.pos(rx1, rz1, 0).color(r, g, b, 255).endVertex();
            buffer.pos(rx1, rz1, 0).color(r, g, b, 255).endVertex();
            buffer.pos(rx1, rz2, 0).color(r, g, b, 255).endVertex();
        }
        tessellator.draw();
        GL11.glLineWidth(1.0f);

        GlStateManager.enableTexture2D();
        float fontScale = 2.0f / scScale;
        FontRenderer fr = mc.fontRenderer;

        GLShim.glPushMatrix();
        GLShim.glScalef(fontScale, fontScale, 1.0f);

        for (SerializedResidence res : areas) {
            float cx = (res.minX + res.maxX + 1) / 2.0f;
            float cz = (res.minZ + res.maxZ + 1) / 2.0f;

            float resScreenW = (res.maxX + 1 - res.minX) * mapToGui / fontScale;
            int nameW = fr.getStringWidth(res.name);

            if (resScreenW >= nameW + 4) {
                float textX = cx * mapToGui / fontScale - nameW / 2.0f;
                float textZ = cz * mapToGui / fontScale - fr.FONT_HEIGHT / 2.0f;

                if (oldNorth) {
                    float px = cx * mapToGui / fontScale;
                    float pz = cz * mapToGui / fontScale;
                    GLShim.glPushMatrix();
                    GLShim.glTranslatef(px, pz, 0);
                    GLShim.glRotatef(-90, 0, 0, 1);
                    GLShim.glTranslatef(-px, -pz, 0);
                }

                fr.drawStringWithShadow(res.name, textX, textZ, 0xFFFFFF);

                if (oldNorth) {
                    GLShim.glPopMatrix();
                }
            }
        }

        GLShim.glPopMatrix();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }
}