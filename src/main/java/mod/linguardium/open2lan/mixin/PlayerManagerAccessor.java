package mod.linguardium.open2lan.mixin;

import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerManager.class)
public interface PlayerManagerAccessor {
    @Mutable @Accessor("maxPlayers")
    void setMaxPlayers(int maxPlayers);
}
