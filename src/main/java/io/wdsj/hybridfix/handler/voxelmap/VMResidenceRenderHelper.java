package io.wdsj.hybridfix.handler.voxelmap;

public final class VMResidenceRenderHelper {
    public static final double MAX_Y_FADE = 64.0;
    public static final int MIN_TEXT_ALPHA = 70;
    public static final float ALPHA_CUTOFF = 0.02f;

    public static float yAlpha(double playerY, SerializedResidence res) {
        if (playerY >= res.minY && playerY <= res.maxY + 1) return 1.0f;
        double dist = playerY < res.minY
                ? (res.minY - playerY)
                : (playerY - res.maxY - 1);
        return Math.max(0.0f, 1.0f - (float) (dist / MAX_Y_FADE));
    }

    private VMResidenceRenderHelper() {
    }
}