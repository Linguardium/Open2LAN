package mod.linguardium.open2lan.mixin;

import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerList.class)
public interface PlayerListSettings {
    @Mutable @Accessor("maxPlayers")
    void setMaxPlayers(int maxPlayers);
}
