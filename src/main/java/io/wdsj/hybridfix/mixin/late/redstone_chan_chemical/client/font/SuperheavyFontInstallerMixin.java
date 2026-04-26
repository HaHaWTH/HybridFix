package io.wdsj.hybridfix.mixin.late.redstone_chan_chemical.client.font;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "com.chinaex123.redstone_chemical_elements.client.font.SuperheavyFontInstaller", remap = false)
public abstract class SuperheavyFontInstallerMixin {
    /**
     * @author Creeam
     * @reason Breaks RGB rendering
     */
    @Overwrite
    public static void installIfNeeded() {
    }
}
