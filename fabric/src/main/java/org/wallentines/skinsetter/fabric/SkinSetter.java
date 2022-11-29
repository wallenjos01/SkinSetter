package org.wallentines.skinsetter.fabric;

import net.fabricmc.api.ModInitializer;
import org.wallentines.midnightcore.api.text.LangRegistry;
import org.wallentines.midnightcore.fabric.event.player.PlayerJoinEvent;
import org.wallentines.midnightcore.fabric.event.player.PlayerLeaveEvent;
import org.wallentines.midnightcore.fabric.event.server.CommandLoadEvent;
import org.wallentines.midnightcore.fabric.event.server.ServerStopEvent;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.skinsetter.common.LoginManager;
import org.wallentines.skinsetter.common.SkinSetterImpl;
import org.wallentines.skinsetter.fabric.command.MainCommand;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SkinSetter implements ModInitializer {

    private SkinSetterImpl api;

    @Override
    public void onInitialize() {

        // Determine the data folder
        Path dataFolder = Paths.get("config/SkinSetter");

        // Lang
        ConfigSection langDefaults = JsonConfigProvider.INSTANCE.loadFromStream(getClass().getResourceAsStream("/skinsetter/lang/en_us.json"));

        // After Modules Loaded
        api = new SkinSetterImpl(dataFolder, langDefaults);

        ConfigSection esp = JsonConfigProvider.INSTANCE.loadFromStream(getClass().getResourceAsStream("/skinsetter/lang/es_mx.json"));
        api.getLangProvider().loadEntries("es_mx", LangRegistry.fromConfigSection(esp));


        // Events
        Event.register(CommandLoadEvent.class, this, event -> MainCommand.register(event.getDispatcher(), event.getBuildContext()));
        Event.register(ServerStopEvent.class, this, event -> api.getSkinRegistry().save());
        Event.register(PlayerJoinEvent.class, this, event -> LoginManager.applyLoginSkin(FabricPlayer.wrap(event.getPlayer()), api.getSkinRegistry()));
        Event.register(PlayerLeaveEvent.class, this, event -> LoginManager.savePersistentSkin(FabricPlayer.wrap(event.getPlayer()), api.getDefaultSkin()));

    }
}