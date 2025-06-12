package mod.linguardium.open2lan;


import mod.linguardium.open2lan.mixin.IntegratedServerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.event.Level;

import java.util.Optional;

import static mod.linguardium.open2lan.ServerSettingsUtil.*;

public class Open2LanScreen extends Screen {
    private static final Component ALLOW_COMMANDS_TEXT = Component.translatable("selectWorld.allowCommands");
    private static final Component GAME_MODE_TEXT = Component.translatable("selectWorld.gameMode");
    private static final Component MAX_PLAYERS_TEXT = Component.translatable("lanServer.maxPlayers");
    private static final Component ONLINE_MODE_TEXT = Component.translatable("lanServer.onlineMode");
    private static final Component ENABLE_PVP_TEXT = Component.translatable("lanServer.pvpEnabled");
    private static final Component OTHER_PLAYERS_TEXT = Component.translatable("lanServer.otherPlayers");
    private static final Component SELECT_PORT_TEXT = Component.translatable("lanServer.selectPort");
    private static final Component START_TEXT = Component.translatable("lanServer.start");
    private static final Component CONFIG_SAVED_TEXT = Component.translatable("lanServer.configSaved");
    private static final Component CONFIG_TITLE_TEXT = Component.translatable("lanServer.configTitle");
    private static final Component UNABLE_TO_OPEN_TO_LAN = Component.translatable("commands.publish.failed");
    private static final String OPEN_TO_LAN_SUCCESS_TRANSLATION_KEY = "commands.publish.started";

    private static final int DEFAULT_PORT = 25565;


    private final Screen parent;
    private final IntegratedServer server;
    private GameType gameMode = GameType.DEFAULT_MODE;
    private boolean allowCommands = false;
    private boolean onlineMode = true;
    private boolean enablePvp = true;
    private int lanPort = DEFAULT_PORT;
    private int maxPlayers = 8;

    private Component displayTitle;

    private int portTextColor;

    public Open2LanScreen(Screen parent, @NotNull Minecraft client) {
        super(Component.translatable("lanServer.title"));
        this.parent = parent;
        this.minecraft = client;
        this.server = client.getSingleplayerServer();
        this.displayTitle = getTitle();
        if (server == null) {
            Logging.log(Level.ERROR, "Open2Lan screen opened without single player running");
            client.setScreen(parent);
            return;
        }
        gameMode = server.getWorldData().getGameType();
        allowCommands = server.getWorldData().isAllowCommands();
        enablePvp = server.isPvpAllowed();
        copySettingsFromRunningServer(this.server);
    }

    protected void init() {
        if (this.minecraft == null) return;
        // TODO: extend existing screen?
        StringWidget titleLabel = new StringWidget(displayTitle, this.font);
        titleLabel.setPosition(width / 2 , Math.min(40, height / 4 - 30));
        addRenderableWidget(titleLabel);

        StringWidget otherPlayers = new StringWidget(OTHER_PLAYERS_TEXT, this.font);
        otherPlayers.setPosition(width / 2 - (this.font.width(OTHER_PLAYERS_TEXT) / 2), Math.max(55, height / 4 - 5));
        addRenderableWidget(otherPlayers);

        StringWidget portLabel = new StringWidget(SELECT_PORT_TEXT, this.font);
        portLabel.setPosition(width / 2 - 150 + (this.font.width(SELECT_PORT_TEXT) / 2), height / 4 + 32);
        addRenderableWidget(portLabel);

        StringWidget maxPlayersLabel = new StringWidget(MAX_PLAYERS_TEXT, this.font);
        maxPlayersLabel.setPosition(width / 2 + 7 + (this.font.width(MAX_PLAYERS_TEXT) / 2), height / 4 + 32);
        addRenderableWidget(maxPlayersLabel);

        // GAMEMODE
        CycleButton<GameType> gamemodeButton = CycleButton.builder(GameType::getShortDisplayName).withValues(GameType.values()).withInitialValue(gameMode)
                .create(width / 2 - 155, height / 4 + 8, 150, 20, GAME_MODE_TEXT, (button, gameMode) -> this.gameMode = gameMode);
        addRenderableWidget(gamemodeButton);

        // ALLOW COMMANDS
        CycleButton<Boolean> allowCommandsButton = CycleButton.onOffBuilder(allowCommands)
                .create(this.width / 2 + 5, height / 4 + 8, 150, 20, ALLOW_COMMANDS_TEXT, (button, allowCommands) -> this.allowCommands = allowCommands);
        addRenderableWidget(allowCommandsButton);

        // PORT
        EditBox portField = new EditBox(this.font, width / 2 - 155 + 1, height / 4 + 45, 148, 20, SELECT_PORT_TEXT);
        portField.insertText(Integer.toString(lanPort));
        portField.setMaxLength(6);
        portField.setResponder((sPort) -> {

            if (sPort.isBlank()) {
                this.lanPort=DEFAULT_PORT;
                return;
            }
            Integer i = null;
            try {
                i = Integer.parseInt(sPort);
            } catch (NumberFormatException ignored) {
            }
            if (validPort(i)) {
                lanPort = i;
                portTextColor = 0xFFFFFF;
            } else {
                lanPort = 25565;
                portTextColor = 0xFF0000;
            }
            portField.setTextColor(portTextColor);
        });
        addRenderableWidget(portField);

        // MAX PLAYERS
        EditBox maxPlayersField = new EditBox(this.font, width / 2 + 5 + 1, height / 4 + 45, 148, 20, MAX_PLAYERS_TEXT);
        maxPlayersField.insertText(Integer.toString(maxPlayers));
        maxPlayersField.setMaxLength(3);
        maxPlayersField.setResponder(value->this.onMaxPlayersChange(value, maxPlayersField));
        addRenderableWidget(maxPlayersField);

        // ONLINE MODE
        CycleButton<Boolean> onlineModeButton = CycleButton.onOffBuilder(onlineMode)
                .create(width / 2 - 155, height / 4 + 69, 150, 20, ONLINE_MODE_TEXT, (button, onlineMode) -> this.onlineMode = onlineMode);
        addRenderableWidget(onlineModeButton);

        // ENABLE PVP
        CycleButton<Boolean> enablePvpButton = CycleButton.onOffBuilder(enablePvp)
                .create(width / 2 + 5, height / 4 + 69, 150, 20, ENABLE_PVP_TEXT, (button, enablePvp) -> this.enablePvp = enablePvp);
        addRenderableWidget(enablePvpButton);

        // DONE
        Button doneButton = Button.builder(START_TEXT, this::onDonePress).bounds(width / 2 - 155, height / 4 + 104, 150, 20).build();
        
        this.addRenderableWidget(doneButton);

        // CANCEL
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, this::onCancel).bounds(this.width / 2 + 5, height / 4 + 104, 150, 20).build());

        // UPDATE PAGE
        if (isOpenToLan(this.minecraft)) {
            doneButton.setMessage(CommonComponents.GUI_DONE);
            portField.setEditable(false);
            displayTitle = CONFIG_TITLE_TEXT;
        }
    }

    private <T extends IntegratedServer & IntegratedServerAccessor> void onDonePress(Button doneButton) {
        Minecraft client = this.minecraft;
        if (client == null) return;
        MinecraftServer server = client.getSingleplayerServer();
        if (!(server instanceof IntegratedServer && server instanceof IntegratedServerAccessor)) return;
        @SuppressWarnings("unchecked") T integratedServer = (T) server;
        if (!isSinglePlayerRunning(client)) {
            this.clearScreen();
            return;
        }
        if (isOpenToLan(client)) {
            this.minecraft.setScreen(parent);
            updateRunningServerSettings(integratedServer, this.gameMode, this.allowCommands);
            addLocalChatMessage(CONFIG_SAVED_TEXT);
        } else {
            this.clearScreen();
            openServerToLan(integratedServer);
        }
        finalizeRunningServerSettings(integratedServer, this.onlineMode, this.enablePvp, this.maxPlayers);

    }

    private <T extends IntegratedServer & IntegratedServerAccessor> void openServerToLan(T integratedServer) {
        Integer hostedPort = ServerSettingsUtil.openServerToLan(integratedServer.getClient(), integratedServer, lanPort, this.gameMode, this.allowCommands);
        if (hostedPort != null) {
            this.addLocalChatMessage(Component.translatable(OPEN_TO_LAN_SUCCESS_TRANSLATION_KEY, hostedPort));
        }else {
            this.addLocalChatMessage(UNABLE_TO_OPEN_TO_LAN);
        }
        integratedServer.getClient().updateTitle();
    }

    private void clearScreen() {
        if (this.minecraft == null) return;
        this.minecraft.setScreen(null);
    }

    private void copySettingsFromRunningServer(IntegratedServer server) {
        gameMode = server.getForcedGameType();
        allowCommands = server.getPlayerList().isAllowCommandsForAllPlayers();
        onlineMode = server.usesAuthentication();
        enablePvp = server.isPvpAllowed();
        lanPort = server.getPort();
        if (!validPort(lanPort)) lanPort = DEFAULT_PORT;
        maxPlayers = server.getMaxPlayers();
    }

    private void addLocalChatMessage(Component text) {
        Optional.ofNullable(this.minecraft)
                .map(mc->{
                    mc.getNarrator().sayNow(text);
                    return mc.gui;
                })
                .map(Gui::getChat)
                .ifPresent(chat->chat.addMessage(text));
    }
    private void onCancel(Button cancelButton) {
        if (this.minecraft == null) return;
        this.minecraft.setScreen(this.parent);
    }

    private void onMaxPlayersChange(String sMaxPlayers, EditBox maxPlayersField) {
        Integer i = null;
        try {
            i = Integer.parseInt(sMaxPlayers);
        } catch (NumberFormatException ignored) {
        }
        int maxPlayerTextColor = 0xFFFFFF;
        if (validMaxPlayers(i)) {
            this.maxPlayers = i;
        } else {
            this.maxPlayers = 8;
            maxPlayerTextColor = 0xFF0000;
        }
        maxPlayersField.setTextColor(maxPlayerTextColor);
    }

}
