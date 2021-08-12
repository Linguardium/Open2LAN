package mod.linguardium.open2lan.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "getOfflinePlayerUuid", at = @At("HEAD"), cancellable = true)
    private static void offlinePlayerUUIDremap(String nickname, CallbackInfoReturnable<UUID> cir) {
        //TODO

    }


}
