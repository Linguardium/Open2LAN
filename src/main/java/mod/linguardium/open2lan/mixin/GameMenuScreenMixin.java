package mod.linguardium.open2lan.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mod.linguardium.open2lan.Open2LanScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.Optional;
import java.util.function.Supplier;

@Mixin(PauseScreen.class)
public class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Component title) {
        super(title);
    }

    @Unique
    private static final Component OPEN_TO_LAN_TEXT = Component.translatable("menu.shareToLan");
    @Unique
    private static final Component LAN_CONFIG_TEXT = Component.translatable("menu.lanConfig");

    /* find button creation between from MinecraftClient.isIntegratedServerRunning method to MinecraftClient.isInSingleplayer method */
    @WrapOperation(
            method = "createPauseMenu",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/PauseScreen;openScreenButton(Lnet/minecraft/network/chat/Component;Ljava/util/function/Supplier;)Lnet/minecraft/client/gui/components/Button;"
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/Minecraft;hasSingleplayerServer()Z"
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/Minecraft;isLocalServer()Z"
                    )
            )
    )
    private Button substitudeOpen2LanButton(PauseScreen instance, Component component, Supplier<Screen> supplier, Operation<Button> buttonFactory) {
        if (this.minecraft == null || !this.minecraft.hasSingleplayerServer()) return buttonFactory.call(instance, component, supplier);
        Button openButton = buttonFactory.call(instance, OPEN_TO_LAN_TEXT,  (Supplier<Screen>)() -> new Open2LanScreen(this, this.minecraft));
        boolean isServerRunning = Optional.ofNullable(this.minecraft)
                .map(client-> {
                    if (client.hasSingleplayerServer()) return client.getSingleplayerServer();
                    return null;
                })
                .map(IntegratedServer::isPublished)
                .orElse(false);
        if (isServerRunning) {
            openButton.setMessage(LAN_CONFIG_TEXT);
        }
        return openButton;
    }
}
