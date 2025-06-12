package mod.linguardium.open2lan;

import com.google.common.base.Predicates;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import mod.linguardium.open2lan.mixin.IntegratedServerAccessor;
import mod.linguardium.open2lan.mixin.PlayerListSettings;
import mod.linguardium.open2lan.mixin.PublishCommandAccessor;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

import java.util.function.Predicate;

public class PublishCommandExtension {
    public static final SimpleCommandExceptionType WRONG_SERVER_TYPE = new SimpleCommandExceptionType(new LiteralMessage("Command only works on client"));
    public static void extendPublishCommand(CommandDispatcher<CommandSourceStack> dispatcher, CommandNode<CommandSourceStack> rootNode, CommandNode<CommandSourceStack> publishCommand) {
        Predicate<CommandSourceStack> existingPredicate = publishCommand.getRequirement();
        Predicate<CommandSourceStack> isNotRunningServerExtension = (context) -> !context.getServer().isPublished();
        Predicate<CommandSourceStack> isSinglePlayerHostPredicate = (context) -> {
            if (context.isPlayer()) {
                Player player = context.getPlayer();
                if (player != null && player.getGameProfile().equals(context.getServer().getSingleplayerProfile())) return true;
            }
            return false;
        };
        if (existingPredicate == null) {
            existingPredicate = Predicates.alwaysTrue();
        }
        // allow publishing even when cheats are off if in single player and the host
        publishCommand = replaceRequirement(rootNode, publishCommand, existingPredicate.or(isSinglePlayerHostPredicate));

        addModifyServerSettingsCommand(rootNode, publishCommand);

        CommandNode<CommandSourceStack> noPVPNode = Commands.literal("disablePVP").executes(PublishCommandExtension::executeExtendedPublishCommandNoPvP).build();
        CommandNode<CommandSourceStack> offlineModeNode = Commands.argument("offlineMode", BoolArgumentType.bool()).executes(PublishCommandExtension::executeExtendedPublishCommand).build();
        CommandNode<CommandSourceStack> maxPlayersNode = Commands.argument("maxPlayers", IntegerArgumentType.integer(1,128)).executes(PublishCommandExtension::executeExtendedPublishCommand).build();
        noPVPNode.addChild(offlineModeNode);
        noPVPNode.addChild(maxPlayersNode);
        offlineModeNode.addChild(maxPlayersNode);
        publishCommand.addChild(maxPlayersNode);

        CommandNode<CommandSourceStack> allowCheatsNode = publishCommand.getChild("allowCommands");
        if (allowCheatsNode == null) return;
        CommandNode<CommandSourceStack> setGameModeNode = allowCheatsNode.getChild("gamemode");
        if (setGameModeNode == null) return;
        setGameModeNode.addChild(offlineModeNode);
        setGameModeNode.addChild(noPVPNode);

        CommandNode<CommandSourceStack> setPortNode = setGameModeNode.getChild("port");
        if (setPortNode == null) return;
        // only allow port setting when its not already running
        setPortNode = replaceRequirement(setGameModeNode, setPortNode, existingPredicate.and(isNotRunningServerExtension));

        // add our commands to the end anywhere it can be
        setPortNode.addChild(maxPlayersNode);
        setPortNode.addChild(noPVPNode);
        setPortNode.addChild(offlineModeNode);

    }
    private static CommandNode<CommandSourceStack> replaceRequirement(CommandNode<CommandSourceStack> parentNode, CommandNode<CommandSourceStack> node, Predicate<CommandSourceStack> predicate) {
        parentNode.getChildren().remove(node);
        CommandNode<CommandSourceStack> newNode = node.createBuilder()
                .requires(predicate).build();
        node.getChildren().forEach(newNode::addChild);
        parentNode.addChild(newNode);
        return newNode;
    }
    private static int togglePVP(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        boolean isPvpAllowed = !context.getSource().getServer().isPvpAllowed();
        context.getSource().getServer().setPvpAllowed(isPvpAllowed);
        context.getSource().sendSuccess(()->Component.translatable((isPvpAllowed?"options.on.composed":"options.off.composed"), Component.translatable("mco.configure.world.pvp")),true);
        return 1;
    }
    private static int toggleCheats(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        boolean areCheatsAllowed = !context.getSource().getServer().getPlayerList().isAllowCommandsForAllPlayers();
        context.getSource().getServer().getPlayerList().setAllowCommandsForAllPlayers(areCheatsAllowed);
        if (context.getSource().getServer() instanceof IntegratedServer server) {
            ServerSettingsUtil.sendUpdatesToPlayers((IntegratedServer & IntegratedServerAccessor) server);
        }
        context.getSource().sendSuccess(()->Component.translatable((areCheatsAllowed?"options.on.composed":"options.off.composed"), Component.translatable("selectWorld.allowCommands")),true);
        return 1;
    }
    private static int toggleOnlineMode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        boolean onlineMode = !context.getSource().getServer().usesAuthentication();
        context.getSource().getServer().setUsesAuthentication(onlineMode);
        context.getSource().sendSuccess(()->Component.translatable((onlineMode?"options.on.composed":"options.off.composed"), Component.translatable("lanServer.onlineMode")),true);
        return 1;
    }
    private static int setMaxPlayers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int maxPlayers = getOptionalArgument(context, "playerCount", Integer.class, context.getSource().getServer().getMaxPlayers());
        ((PlayerListSettings)context.getSource().getServer().getPlayerList()).setMaxPlayers(maxPlayers);
        context.getSource().sendSuccess(()->Component.translatable("lanServer.maxPlayers").append(Component.literal(" "+String.valueOf(maxPlayers))), true);
        return maxPlayers;
    }

    private static void addModifyServerSettingsCommand(CommandNode<CommandSourceStack> root, CommandNode<CommandSourceStack> base) {
        CommandNode<CommandSourceStack> togglePVPNode = Commands.literal("togglePVP").executes(PublishCommandExtension::togglePVP).build();

        CommandNode<CommandSourceStack> setMaxPlayersNode = Commands.literal("setMaxPlayers").build();
        CommandNode<CommandSourceStack> setMaxPlayersArgumentNode = Commands.argument("playerCount", IntegerArgumentType.integer(1,128)).executes(PublishCommandExtension::setMaxPlayers).build();
        setMaxPlayersNode.addChild(setMaxPlayersArgumentNode);

        CommandNode<CommandSourceStack> toggleOnlineMode = Commands.literal("toggleOnlineMode").executes(PublishCommandExtension::toggleOnlineMode).build();

        CommandNode<CommandSourceStack> toggleCheats = Commands.literal("toggleCheats").executes(PublishCommandExtension::toggleCheats).build();

        CommandNode<CommandSourceStack> modifyServerSettings = Commands.literal("modifyServer").requires(base.getRequirement()).build();
        modifyServerSettings.addChild(toggleCheats);
        modifyServerSettings.addChild(togglePVPNode);
        modifyServerSettings.addChild(setMaxPlayersNode);
        modifyServerSettings.addChild(toggleOnlineMode);
        root.addChild(modifyServerSettings);

    }
    private static int executeExtendedPublishCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return executeExtendedPublishCommand(context, true);
    }

    public static int executeExtendedPublishCommandNoPvP(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return executeExtendedPublishCommand(context, false);
    }
    public static <T extends IntegratedServer & IntegratedServerAccessor> int executeExtendedPublishCommand(CommandContext<CommandSourceStack> context, boolean pvp) throws CommandSyntaxException {
        boolean allowCheats = getOptionalArgument(context, "allowCommands", Boolean.class, false);
        boolean offlineMode = getOptionalArgument(context, "offlineMode", Boolean.class, false);
        GameType gameMode = getOptionalArgument(context, "gamemode", GameType.class, GameType.DEFAULT_MODE);
        int maxPlayers = getOptionalArgument(context, "maxPlayers", Integer.class, 8);
        CommandSourceStack source = context.getSource();
        int port = -1;
        if (!(source.getServer() instanceof IntegratedServer integratedServer)) throw WRONG_SERVER_TYPE.create();
        if (!integratedServer.isPublished()){
            port = getOptionalArgument(context, "port", Integer.class, HttpUtil.isPortAvailable(25565) ? 25565 : HttpUtil.getAvailablePort());
            PublishCommandAccessor.executeOpen2Lan(context.getSource(), port, allowCheats,gameMode);
        } else {
            ServerSettingsUtil.updateRunningServerSettings((T)integratedServer, gameMode, allowCheats);
        }
        ServerSettingsUtil.finalizeRunningServerSettings(integratedServer, !offlineMode, pvp, maxPlayers);
        return 1;
    }

    private static <T> T getOptionalArgument(CommandContext<?> context, String argumentName, Class<T> argumentClass, T defaultValue) {
        try {
            return context.getArgument(argumentName, argumentClass);
        }catch(IllegalArgumentException e) {
            return defaultValue;
        }
    }

}
