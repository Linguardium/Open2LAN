package mod.linguardium.open2lan.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin {
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;isLocal()Z"), method = "acceptPlayer")
    private boolean onlineModeInLocal(ClientConnection clientConnection){
        return false;
    }
}
