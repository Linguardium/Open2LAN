package mod.linguardium.open2lan;

import mod.linguardium.open2lan.mixin.IntegratedServerAccessor;
import mod.linguardium.open2lan.mixin.PlayerListSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.Commands;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ServerSettingsUtil {
    public static <T extends IntegratedServer & IntegratedServerAccessor> void sendUpdatesToPlayers(T integratedServer) {
        Optional.ofNullable(integratedServer.getClient())
                .map(mc->mc.player)
                .ifPresent(host -> {
                    int hostPermissions = integratedServer.getProfilePermissions(host.getGameProfile());
                    host.setPermissionLevel(hostPermissions);
                });
        Commands commandManager = integratedServer.getCommands();
        integratedServer.getPlayerList().getPlayers().forEach(commandManager::sendCommands);
    }
    public static boolean isOpenToLan(@NotNull Minecraft client) {
        IntegratedServer integratedServer = client.getSingleplayerServer();
        return isSinglePlayerRunning(client) && integratedServer != null && integratedServer.isPublished();
    }
    public static boolean isSinglePlayerRunning(@NotNull Minecraft client) {
         return client.hasSingleplayerServer();
    }
    public static boolean validMaxPlayers(Integer playerCount) {
        return playerCount != null && playerCount <= 128 && playerCount > 1;
    }
    public static boolean validPort(Integer port) {
        return port != null && port > 1024 && port < 65536;
    }
    public static int getPortOrRandom(int port) {
        if (!validPort(port) || !HttpUtil.isPortAvailable(port)) {
            return HttpUtil.getAvailablePort();
        }
        return port;
    }
    public static <T extends IntegratedServer & IntegratedServerAccessor> Integer openServerToLan(Minecraft client, T integratedServer, int lanPort, GameType gameMode, boolean allowCommands) {
        lanPort = getPortOrRandom(lanPort);
        if (integratedServer.publishServer(gameMode, allowCommands, lanPort)) {
            return lanPort;
        }
        return null;
    }
    public static <T extends IntegratedServer & IntegratedServerAccessor> void updateRunningServerSettings(T integratedServer, GameType gameMode, boolean allowCommands) {
        if (integratedServer.getClient() == null) return;
        integratedServer.setForcedGameMode(gameMode);
        integratedServer.getPlayerList().setAllowCommandsForAllPlayers(allowCommands);
        sendUpdatesToPlayers(integratedServer);
    }
    public static void finalizeRunningServerSettings(IntegratedServer integratedServer, boolean onlineMode, boolean enablePvp, int maxPlayers) {
        PlayerListSettings playerlistSettings = (PlayerListSettings) integratedServer.getPlayerList();
        integratedServer.setUsesAuthentication(onlineMode);
        integratedServer.setPvpAllowed(enablePvp);
        playerlistSettings.setMaxPlayers(maxPlayers);

    }
}
