package mod.linguardium.open2lan.mixin;

import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OpenToLanScreen.class)
public abstract class OpenToLanMixin  extends Screen {
    protected OpenToLanMixin(Text title) {
        super(title);
    }


    private boolean online_mode = true;
    private ButtonWidget buttonOnlineMode;
    private Integer port = 25564;
    private TextFieldWidget portField;
    private int portTextColor = 0xFFFFFF;

    @Shadow
    abstract void updateButtonText();

    @Inject(at=@At("HEAD"),method="init")
    private void addMyButtons(CallbackInfo ci) {
        this.buttonOnlineMode = this.addButton(new ButtonWidget(this.width / 2 + 5, 150, 150, 20, new TranslatableText("lanServer.onlineMode"), (buttonWidget) -> {
            online_mode=!online_mode;
            this.updateButtonText();
        }));
        this.portField=new TextFieldWidget(this.client.textRenderer,this.width / 2 - 155,150,150,20,new TranslatableText("lanServer.selectPort"));
        this.portField.setText(Integer.toString(port));
        this.addChild(portField);

        portField.setChangedListener((sPort)->{
           Integer i = null;
           try {
               i=Integer.parseInt(sPort);
           } catch (NumberFormatException ignored) {}
           if (i != null && i < 65536 && i > 0) {
               port = i;
               portTextColor = 0xFFFFFF;
           }else{
               port = 25564;
               portTextColor = 0xFF0000;
           }

        });
    }
    @Inject(at=@At("TAIL"),method="updateButtonText")
    private void updateMyButtons(CallbackInfo ci) {
        this.buttonOnlineMode.setMessage((new TranslatableText("lanServer.onlineMode")).append(" ").append(ScreenTexts.getToggleText(this.online_mode)));
        portField.setEditableColor(portTextColor);
    }
    @Inject(at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V"),method="render")
    private void addPortLabel(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Text selectPortText = new TranslatableText("lanServer.selectPort");
        this.drawCenteredText(matrices, this.textRenderer, selectPortText, this.width / 2-155+(this.textRenderer.getWidth(selectPortText)/2), 135, 16777215);
        portField.render(matrices,mouseX,mouseY,delta);
    }
    @Redirect(at=@At(value="INVOKE",target="Lnet/minecraft/client/util/NetworkUtils;findLocalPort()I"),method="method_19851(Lnet/minecraft/client/gui/widget/ButtonWidget;)V")
    private int getPortValue() {
        if (port > 0 && port < 65536) {
            return port;
        }
        return NetworkUtils.findLocalPort();
    }
    // the first translatable text in bytecode is inside the successful establishment of the server.
    @Inject(at=@At(value="INVOKE",target="Lnet/minecraft/server/integrated/IntegratedServer;openToLan(Lnet/minecraft/world/GameMode;ZI)Z", ordinal=0),method="method_19851(Lnet/minecraft/client/gui/widget/ButtonWidget;)V")
    private void setOnlineMode(CallbackInfo ci) {
        this.client.getServer().setOnlineMode(online_mode);
    }

}
