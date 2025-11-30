package io.wdsj.hybridfix.mixin.fix.forge.ping_status;

import io.wdsj.hybridfix.config.Settings;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.status.server.SPacketServerInfo;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.bukkit.ChatColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FMLCommonHandler.class)
public class FMLCommonHandlerMixin {
    @Unique
    private static final ServerStatusResponse hybridFix$startingResponse = new ServerStatusResponse();
    static {
        if (Settings.startUpMOTDSettings.enable) {
            hybridFix$startingResponse.setServerDescription(new TextComponentString(hybridFix$translateAlternateColorCodes('&', Settings.startUpMOTDSettings.messageOfTheDay)));
            hybridFix$startingResponse.setPlayers(new ServerStatusResponse.Players(0, 0));
            hybridFix$startingResponse.setVersion(new ServerStatusResponse.Version("1.12.2", 340));
        }
    }
    @Inject(
            method = "handleServerHandshake",
            at = @At(
                    value = "INVOKE_STRING",
                    target = "Lnet/minecraft/util/text/TextComponentString;<init>(Ljava/lang/String;)V", args = "ldc=Server is still starting! Please wait before reconnecting."
            ),
            cancellable = true,
            require = 0
    )
    public void handleServerHandshake(C00Handshake packet, NetworkManager manager, CallbackInfoReturnable<Boolean> cir) {
        if (packet.getRequestedState() == EnumConnectionState.STATUS) {
            if (Settings.startUpMOTDSettings.enable) manager.sendPacket(new SPacketServerInfo(hybridFix$startingResponse));
            manager.closeChannel(new TextComponentString("Server is still starting! Please wait before reconnecting."));
            cir.setReturnValue(false);
        }
    }

    @Unique
    private static String hybridFix$translateAlternateColorCodes(char altColorChar, String textToTranslate) { // copied to avoid classloading issues
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {
                b[i] = ChatColor.COLOR_CHAR;
                b[i+1] = Character.toLowerCase(b[i+1]);
            }
        }
        return new String(b);
    }
}
