package mod.linguardium.open2lan;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import mod.linguardium.open2lan.mixin.IntegratedServerAccessor;
import mod.linguardium.open2lan.mixin.PlayerManagerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.GameMode;

public class LanServerPropertiesScreen {
    private final ConfigBuilder builder;
    private final MinecraftClient client;
    private final MinecraftServer server;

    public LanServerPropertiesScreen(Screen parent, MinecraftClient client) {
        this.builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(new TranslatableText("lanServerProperties.title"));
        this.client = client;
        this.server = client.getServer();

        ConfigCategory lanServerProperties = builder.getOrCreateCategory(new TranslatableText("lanServerProperties.title"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        lanServerProperties.addEntry(entryBuilder.startEnumSelector(new TranslatableText("selectWorld.gameMode"), GameMode.class, server.getForcedGameMode())
                .setDefaultValue(server.getSaveProperties().getGameMode())
                .setSaveConsumer(newValue -> {
                    ((IntegratedServerAccessor) server).setForcedGameMode(newValue);
                })
                .build()
        );

        lanServerProperties.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("selectWorld.allowCommands"), server.getPlayerManager().areCheatsAllowed())
                .setDefaultValue(server.getSaveProperties().areCommandsAllowed())
                .setSaveConsumer(newValue -> {
                    server.getPlayerManager().setCheatsAllowed(newValue);
                    client.player.setClientPermissionLevel(server.getPermissionLevel(client.player.getGameProfile()));
                    for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
                        server.getCommandManager().sendCommandTree(serverPlayerEntity);
                    }
                })
                .build()
        );

        lanServerProperties.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("lanServerProperties.onlineMode"), server.isOnlineMode())
                .setDefaultValue(true)
                .setSaveConsumer(server::setOnlineMode)
                .build()
        );

        lanServerProperties.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("lanServerProperties.pvpEnabled"), server.isPvpEnabled())
                .setDefaultValue(true)
                .setSaveConsumer(server::setPvpEnabled)
                .build()
        );

        lanServerProperties.addEntry(entryBuilder.startIntField(new TranslatableText("lanServerProperties.maxPlayers"), server.getMaxPlayerCount())
                .setDefaultValue(8)
                .setMin(1)
                .setMax(128)
                .setSaveConsumer(newValue -> ((PlayerManagerAccessor)server.getPlayerManager()).setMaxPlayers(newValue))
                .build()
        );

        /*lanServerProperties.addEntry(entryBuilder.startStrField(new TranslatableText("option.examplemod.optionA"), server.getForcedGameMode().getName())
                .setDefaultValue("This is the default value") // Recommended: Used when user click "Reset"
                .setTooltip(new TranslatableText("This option is awesome!")) // Optional: Shown when the user hover over this option
                .setSaveConsumer(newValue -> ((IntegratedServerAccessor)server).setForcedGameMode(newValue)) // Recommended: Called when user save the config
                .build()); // Builds the option entry for cloth config*/

    }

    public Screen build() {
        return builder.build();
    }
}
