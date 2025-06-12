package mod.linguardium.open2lan.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(IntegratedServer.class)
public interface IntegratedServerAccessor {
    @Accessor("publishedGameType")
    void setForcedGameMode(GameType gameMode);
    @Accessor("minecraft")
    Minecraft getClient();
}
