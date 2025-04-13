package dev.imanity.antixray.sdk;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.config.Settings;

@SuppressWarnings("unused")
public class AntiXraySDK {

    private static AntiXrayAdapter ADAPTER;

    public static AntiXrayAdapter getAdapter() {
        return ADAPTER;
    }

    public static void setAdapter(AntiXrayAdapter adapter) {
        if (!Settings.rayTraceAntiXraySDK) {
            HybridFix.LOGGER.warn("AntiXray SDK integration is disabled, RaytraceAntiXray won't work properly. Please enable rayTraceAntiXraySDK in HybridFix config.");
        }
        ADAPTER = adapter;
    }
}