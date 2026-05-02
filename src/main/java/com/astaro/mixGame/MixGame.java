package com.astaro.mixGame;

import com.astaro.mixGame.Events.GameListener;
import com.astaro.mixGame.Events.SelectionListener;
import com.astaro.mixGame.Events.SignListener;
import com.astaro.mixGame.Events.TabListener;
import com.astaro.mixGame.Game.ArenaManager;
import com.astaro.mixGame.Setup.SetupManager;
import com.astaro.mixGame.Signs.SignManager;
import com.astaro.mixGame.config.CommandManager;
import com.astaro.mixGame.config.Config;
import com.astaro.mixGame.services.*;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.plasticable.servermanager.model.ServerState;
import ru.plasticable.servermanager.plugin.paper.ServerManagerPaperPlugin;

public final class MixGame extends JavaPlugin {

    public static boolean isDebug = false;

    public static MixGame instance;
    public static LoggerService loggerService;


    private SettingsService settingsService;
    private ChatService chatService;
    private ItemService itemService;
    private FloorService floorService;
    private ArenaManager arenaManager;
    private SignManager signManager;
    private SetupManager setupManager;
    private DatabaseService databaseService;

    private NotificationService notificationService;
    private MusicService musicService;
    private PlaceholderService placeholderService;

    private ServerManagerPaperPlugin serverManager;

    private GameListener gameListener;


    @Override
    public void onEnable() {
        instance = this;
        if (this.getConfig().getBoolean("debug", false)) isDebug = true;
        loggerService = new LoggerService();
        settingsService = new SettingsService(this);
        saveDefaultConfig();
        settingsService.reload();
        chatService = new ChatService();
        chatService.saveDefaultLangs();
        chatService.load(this.getConfig().getString("lang", "ru"));
        databaseService = new DatabaseService(this);
        databaseService.connect();
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderService = new PlaceholderService(this);
            placeholderService.register();
        }
        if (getServer().getPluginManager().isPluginEnabled("ServerManagerPlugin")) {
            getLogger().info("DEBUG: My Server ID is: [" + serverManager.getServerId() + "]");
            serverManager = (ServerManagerPaperPlugin) getServer().getPluginManager().getPlugin("ServerManagerPlugin");
        }
        notificationService = new NotificationService(this);
        musicService = new MusicService(this);
        musicService.loadSongs();
        itemService = new ItemService(this);
        floorService = new FloorService();
        arenaManager = new ArenaManager(this);
        arenaManager.loadAllArenas();
        signManager = new SignManager();
        setupManager = new SetupManager();

        CommandManager commandManager = new CommandManager();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(commandManager.registerCommands());
        });

        var pluginManager = Bukkit.getPluginManager();
        this.gameListener = new GameListener(this);
        pluginManager.registerEvents(gameListener, this);
        pluginManager.registerEvents(new SelectionListener(this), this);
        pluginManager.registerEvents(new SignListener(this), this);
        pluginManager.registerEvents(new TabListener(this), this);

        Bukkit.getConsoleSender().sendMessage(
                MiniMessage.miniMessage().deserialize(
                        "<gradient:#ff5555:#ffaa00:#55ff55>MixGame</gradient> <white>"
                                + this.getPluginMeta().getVersion() + "</white> <green>is now enabled!</green>"
                )
        );
        if (this.serverManager != null) {
            serverManager.setServerState(ServerState.WAITING_FOR_PLAYERS);
        }
    }

    @Override
    public void onDisable() {
        this.databaseService.close();
    }

    public void reload(){
        settingsService.reload();
        chatService.load(this.getConfig().getString("lang", "en"));
        Bukkit.getConsoleSender().sendMessage(
                MiniMessage.miniMessage().deserialize(
                        "<gradient:#ff5555:#ffaa00:#55ff55>MixGame</gradient> <white>"
                                + this.getPluginMeta().getVersion() + "</white> <green>reloaded!</green>"
                )
        );
    }

    public Config getSettings() {
        return this.settingsService.get();
    }

    public ChatService getChatService() {
        return this.chatService;
    }

    public MusicService getMusicService() {
        return this.musicService;
    }

    public ItemService getItemService() {
        return this.itemService;
    }

    public NotificationService getNotificationService() {
        return this.notificationService;
    }

    public FloorService getFloorService() {
        return this.floorService;
    }

    public ArenaManager getArenaManager() {
        return this.arenaManager;
    }

    public SignManager getSignManager() {
        return this.signManager;
    }

    public SetupManager getSetupManager() {
        return this.setupManager;

    }

    public GameListener getGameListener() {
        return this.gameListener;
    }

    public DatabaseService getDatabase() {
        return this.databaseService;
    }

    public PlaceholderService getPlaceholderService() {
        return this.placeholderService;
    }

    public ServerManagerPaperPlugin getServerManager() {
        return this.serverManager;
    }

}
