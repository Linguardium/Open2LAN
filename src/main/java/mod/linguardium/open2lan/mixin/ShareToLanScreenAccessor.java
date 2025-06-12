package mod.linguardium.open2lan.mixin;

import net.minecraft.client.gui.screens.ShareToLanScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShareToLanScreen.class)
public interface ShareToLanScreenAccessor {
    @Accessor("PORT_LOWER_BOUND")
    static int minPort() { throw new IllegalStateException("Implemented via Mixin"); }
    @Accessor("PORT_HIGHER_BOUND")
    static int maxPort() { throw new IllegalStateException("Implemented via Mixin"); }

}
