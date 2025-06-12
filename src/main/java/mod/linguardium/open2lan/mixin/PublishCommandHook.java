package mod.linguardium.open2lan.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.PublishCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static mod.linguardium.open2lan.PublishCommandExtension.extendPublishCommand;

@Mixin(PublishCommand.class)
public class PublishCommandHook {
    @WrapOperation(method="register",at= @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;register(Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;)Lcom/mojang/brigadier/tree/LiteralCommandNode;", remap=false))
    private static LiteralCommandNode<CommandSourceStack> hookPublishCommandRegistration(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> rootNodeBuilder, Operation<LiteralCommandNode<CommandSourceStack>> registration) {
        LiteralCommandNode<CommandSourceStack> publishCommandNode = registration.call(dispatcher, rootNodeBuilder);
        extendPublishCommand(dispatcher,dispatcher.getRoot(), publishCommandNode);
        return publishCommandNode;
    }
    /*
        commandDispatcher.register(
        (
            (
                /publish
                Commands.literal("publish").requires((commandSourceStack) -> {
                    return commandSourceStack.hasPermission(4);
                })
             ).executes((commandContext) -> {
                    return publish((CommandSourceStack)commandContext.getSource(), HttpUtil.getAvailablePort(), false, (GameType)null);
                })
        /publish allowCommands

          ).then(
            (
                Commands
                .argument("allowCommands", BoolArgumentType.bool())
                .executes((commandContext) -> {
                    return publish(commandContext.getSource(), HttpUtil.getAvailablePort(), BoolArgumentType.getBool(commandContext, "allowCommands"), (GameType)null);
                })
        /publish allowCommands gameMode

             ).then(
                (
                    Commands
                    .argument("gamemode", GameModeArgument.gameMode())
                    .executes((commandContext) -> {
                        return publish((CommandSourceStack)commandContext.getSource(), HttpUtil.getAvailablePort(), BoolArgumentType.getBool(commandContext, "allowCommands"), GameModeArgument.getGameMode(commandContext, "gamemode"));
                    })
                 ).then(Commands.argument("port", IntegerArgumentType.integer(0, 65535)).executes((commandContext) -> {
            return publish((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "port"), BoolArgumentType.getBool(commandContext, "allowCommands"), GameModeArgument.getGameMode(commandContext, "gamemode"));
        })))));
     */
}
