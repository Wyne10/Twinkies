package me.wyne.twinkies;

import me.wyne.twinkies.listeners.JoinListener;
import me.wyne.twinkies.logging.LoggingConfig;
import me.wyne.twinkies.notifications.Notifications;
import me.wyne.twinkies.notifications.NotificationsConfig;
import me.wyne.twinkies.placeholderAPI.PlayerPlaceholders;
import me.wyne.twinkies.storage.NotificationsSettingsStorage;
import me.wyne.twinkies.storage.PlayerStorage;
import me.wyne.wutils.config.Config;
import me.wyne.wutils.log.Log;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public final class Twinkies extends JavaPlugin implements CommandExecutor, TabCompleter {

    // API Stuff
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    // Storage
    private final PlayerStorage playerStorage = new PlayerStorage(this);
    private final NotificationsSettingsStorage notificationsSettingsStorage = new NotificationsSettingsStorage(this);

    // Configs
    private final NotificationsConfig notificationsConfig = new NotificationsConfig();
    private final LoggingConfig loggingConfig = new LoggingConfig();

    // Placeholders
    private final PlayerPlaceholders playerPlaceholders = new PlayerPlaceholders(this);

    // Listeners
    private final JoinListener joinListener = new JoinListener(this);

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        // Initialize logger
        Log.registerLogger(getLogger());
        Log.registerConfig(loggingConfig);
        Log.registerLogDirectory(new File(getDataFolder(), "logs"));
        Log.registerExecutor(Executors.newSingleThreadExecutor());

        // Initialize notifications
        Notifications.registerPlugin(this);

        // Initialize configs
        Config.reloadConfigObjects(getConfig());

        Bukkit.getPluginManager().registerEvents(joinListener, this);

        this.getCommand("twinkies").setTabCompleter(this);
        this.getCommand("twinkies").setExecutor(this);

        playerPlaceholders.register();

        // Initialize storages
        playerStorage.createStorageFolder();
        notificationsSettingsStorage.createStorageFolder();
        playerStorage.createStorageFile();
        notificationsSettingsStorage.createStorageFile();
        playerStorage.loadData();
        notificationsSettingsStorage.loadData();
    }

    @NotNull
    public MiniMessage getMiniMessage() {
        return miniMessage;
    }

    @NotNull
    public PlayerStorage getPlayerStorage() {
        return playerStorage;
    }
    @NotNull
    public NotificationsSettingsStorage getNotificationsSettingsStorage() {
        return notificationsSettingsStorage;
    }

    @NotNull
    public NotificationsConfig getNotificationsConfig() {
        return notificationsConfig;
    }
    @NotNull
    public LoggingConfig getLogConfig() {
        return loggingConfig;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args)
    {
        List<String> result = new ArrayList<>();

        if (sender.isOp() && args.length == 1)
            result.add("reload");
        result.addAll(loggingConfig.loggingTabComplete(sender, args));
        result.addAll(notificationsSettingsStorage.tabComplete(sender, args));
        result.addAll(playerStorage.dataTabComplete(sender, args));
        result.addAll(playerStorage.playerTabComplete(sender, args));
        result.addAll(playerStorage.nickTabComplete(sender, args));
        result.addAll(playerStorage.ipTabComplete(sender, args));

        return result;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (sender.isOp() && args.length == 1 && args[0].equalsIgnoreCase("reload"))
        {
            if (Config.reloadConfigObjects(getConfig()))
                sender.sendMessage(Component.text("Конфиг успешно перезагружен!").color(NamedTextColor.GREEN));
        }
        playerStorage.onCommand(sender, args);
        loggingConfig.setLoggingSetting(sender, args);

        if (!(sender instanceof Player))
            return true;

        notificationsSettingsStorage.setSetting(sender, args);

        return false;
    }
}
