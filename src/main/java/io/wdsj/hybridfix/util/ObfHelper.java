package io.wdsj.hybridfix.util;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

/**
 * Obfuscation helper, used to get corresponding names based on environment.
 */
public class ObfHelper {
    private ObfHelper() {
    }
    private static final boolean isDeobfuscated = FMLLaunchHandler.isDeobfuscatedEnvironment();

    public static String getName(String mcpName, String srgName) {
        return isDeobfuscated ? mcpName : srgName;
    }
}
