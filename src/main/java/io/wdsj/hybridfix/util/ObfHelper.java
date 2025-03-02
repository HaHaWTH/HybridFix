package io.wdsj.hybridfix.util;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

public class ObfHelper {
    private ObfHelper() {
    }
    private static final boolean isDeobfuscated = FMLLaunchHandler.isDeobfuscatedEnvironment();

    public static String getName(String mcpName, String srgName) {
        return isDeobfuscated ? mcpName : srgName;
    }
}
