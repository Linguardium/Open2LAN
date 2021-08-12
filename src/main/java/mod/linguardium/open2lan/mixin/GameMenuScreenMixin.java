package mod.linguardium.open2lan.mixin;

import me.shedaniel.autoconfig.AutoConfig;
import mod.linguardium.open2lan.LanServerPropertiesScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;isIntegratedServerRunning()Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void lanServerProperties(CallbackInfo ci, String s, ButtonWidget openToLanButton) {
        ButtonWidget lanServerPropertiesButton = this.addDrawableChild(new ButtonWidget(this.width / 2 + 4, this.height / 4 + 96 + -16, 98, 20, new TranslatableText("menu.lanServerProperties"),
                (button) -> this.client.setScreen(new LanServerPropertiesScreen(this, client).build())
        ));

        if (this.client != null) {
            boolean isLanServer = this.client.isIntegratedServerRunning() && this.client.getServer().isRemote();
            openToLanButton.visible = !isLanServer;
            lanServerPropertiesButton.visible = isLanServer;
        }
    }
}
