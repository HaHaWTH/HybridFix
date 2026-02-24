package io.wdsj.hybridfix.mixin.late.voxelmap.residence_ext;

import com.mamiyaotaru.voxelmap.Map;
import com.mamiyaotaru.voxelmap.util.GLShim;
import io.wdsj.hybridfix.handler.voxelmap.SerializedResidence;
import io.wdsj.hybridfix.handler.voxelmap.VoxelMapResidenceStorage;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(value = Map.class, remap = false)
public abstract class VoxelMapMixin {
    @Shadow private double zoomScaleAdjusted;
    @Shadow private int lastImageX;
    @Shadow private int lastImageZ;

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
        Collection<SerializedResidence> areas = VoxelMapResidenceStorage.INSTANCE.areas.values();
        if (areas.isEmpty()) return;

        GLShim.glDisable(GL11.GL_TEXTURE_2D);
        GLShim.glDisable(GL11.GL_DEPTH_TEST);

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

            GL11.glLineWidth(1.0f);
            buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(renderX1, renderZ2, 0).color(r, g, b, 255).endVertex();
            buffer.pos(renderX2, renderZ2, 0).color(r, g, b, 255).endVertex();
            buffer.pos(renderX2, renderZ1, 0).color(r, g, b, 255).endVertex();
            buffer.pos(renderX1, renderZ1, 0).color(r, g, b, 255).endVertex();
            tessellator.draw();
        }

        GLShim.glEnable(GL11.GL_DEPTH_TEST);
        GLShim.glEnable(GL11.GL_TEXTURE_2D);
        GLShim.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }
}