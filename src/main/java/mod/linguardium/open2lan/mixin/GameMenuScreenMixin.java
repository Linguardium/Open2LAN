package mod.linguardium.open2lan.mixin;

import mod.linguardium.open2lan.Open2LanScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.function.Supplier;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    private ButtonWidget createButton(Text text, Supplier<Screen> screenSupplier) {
        return null;
    }

    ;
    private static final Text OPEN_TO_LAN_TEXT = Text.translatable("menu.shareToLan");
    private static final Text LAN_CONFIG_TEXT = Text.translatable("menu.lanConfig");

    /* Redirect GridWidget.Adder.add method in slice from MinecraftClient.isIntegratedServerRunning method to MinecraftClient.isInSingleplayer method */
    @Redirect(
            method = "initWidgets",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/widget/GridWidget$Adder;add(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;"
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/MinecraftClient;isIntegratedServerRunning()Z"
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/MinecraftClient;isInSingleplayer()Z"
                    )
            )
    )
    private Widget redirectAdderAdd(GridWidget.Adder adder, Widget oldButton) {
        if (!client.isIntegratedServerRunning()) return adder.add(oldButton);

        ButtonWidget button = createButton(OPEN_TO_LAN_TEXT, () -> new Open2LanScreen(this, client));
        if (client != null && client.isIntegratedServerRunning() && client.getServer().isRemote()) {
            button.setMessage(LAN_CONFIG_TEXT);
        }
        return adder.add(button);
    }
}
