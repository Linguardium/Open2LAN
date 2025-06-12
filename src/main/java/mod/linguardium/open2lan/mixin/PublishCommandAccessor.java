package mod.linguardium.open2lan.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.PublishCommand;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PublishCommand.class)
public interface PublishCommandAccessor {
    @Invoker("publish")
    static int executeOpen2Lan(CommandSourceStack commandSourceStack, int port, boolean allowCheats, @Nullable GameType gameType) throws CommandSyntaxException { throw new IllegalStateException("Implemented by Mixin"); }

}
