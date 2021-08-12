package mod.linguardium.open2lan.mixin;

import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OpenToLanScreen.class)
public abstract class OpenToLanMixin extends Screen {
    protected OpenToLanMixin(Text title) {
        super(title);
    }

    private boolean online_mode = true;
    private ButtonWidget buttonOnlineMode;
    private Integer port = 25565;
    private TextFieldWidget portField;
    private int portTextColor = 0xFFFFFF;

    @Inject(at = @At("HEAD"), method = "init")
    private void addMyButtons(CallbackInfo ci) {
        this.buttonOnlineMode = this.addDrawableChild(new ButtonWidget(this.width / 2 + 5, 150, 150, 20, new TranslatableText("lanServerProperties.onlineMode").append(":"), (buttonWidget) -> {
            online_mode = !online_mode;
            this.buttonOnlineMode.setMessage((new TranslatableText("lanServerProperties.onlineMode")).append(": ").append(ScreenTexts.onOrOff(this.online_mode)));
        }));
        this.buttonOnlineMode.setMessage((new TranslatableText("lanServerProperties.onlineMode")).append(": ").append(ScreenTexts.onOrOff(this.online_mode)));

        this.portField = new TextFieldWidget(this.client.textRenderer, this.width / 2 - 155, 150, 150, 20, new TranslatableText("lanServerProperties.selectPort"));
        this.portField.setText(Integer.toString(port));
        this.addDrawableChild(portField);

        portField.setChangedListener((sPort) -> {
            Integer i = null;
            try {
                i = Integer.parseInt(sPort);
            } catch (NumberFormatException ignored) {
            }
            if (i != null && i < 65536 && i > 0) {
                port = i;
                portTextColor = 0xFFFFFF;
            } else {
                port = 25565;
                portTextColor = 0xFF0000;
            }
            portField.setEditableColor(portTextColor);
        });
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V"), method = "render")
    private void addPortLabel(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Text selectPortText = new TranslatableText("lanServerProperties.selectPort");
        drawCenteredText(matrices, this.textRenderer, selectPortText, this.width / 2 - 155 + (this.textRenderer.getWidth(selectPortText) / 2), 135, 16777215);
        portField.render(matrices, mouseX, mouseY, delta);
    }

    @ModifyVariable(at = @At(value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/client/util/NetworkUtils;findLocalPort()I"),
            method = "method_19851(Lnet/minecraft/client/gui/widget/ButtonWidget;)V", ordinal = 0
    )
    private int getPortValue(int i) {
        if (port > 0 && port < 65536) return port;
        return i;
    }
    // the first translatable text in bytecode is inside the successful establishment of the server.
    @Inject(at = @At(value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/client/util/NetworkUtils;findLocalPort()I", ordinal = 0),
            method = "method_19851(Lnet/minecraft/client/gui/widget/ButtonWidget;)V"
    )
    private void setOnlineMode(CallbackInfo ci) {
        this.client.getServer().setOnlineMode(online_mode);
    }

}
