package mod.linguardium.open2lan.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net/minecraft/server/network/ServerLoginPacketListenerImpl$1")
public class ServerLoginNetworkHandlerThreadMixin {
    @WrapOperation(method="run",at= @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isSingleplayer()Z"))
    private boolean singlePlayerNotOpenInLan(MinecraftServer server, Operation<Boolean> original) {
        boolean isSinglePlayer = original.call(server);
        if (!(server instanceof IntegratedServer integratedServer)) return isSinglePlayer;
        if (integratedServer.isPublished()) return !integratedServer.usesAuthentication();
        return isSinglePlayer;
    }
}
