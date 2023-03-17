package me.wyne.twinkies;

import me.wyne.twinkies.listeners.JoinListener;
import me.wyne.twinkies.logging.LoggingConfig;
import me.wyne.twinkies.notifications.NotificationType;
import me.wyne.twinkies.notifications.Notifications;
import me.wyne.twinkies.notifications.NotificationsSettings;
import me.wyne.twinkies.placeholderAPI.PlayerPlaceholders;
import me.wyne.twinkies.storage.NotificationsSettingsStorage;
import me.wyne.twinkies.wlog.WLog;
import me.wyne.twinkies.notifications.NotificationsConfig;
import me.wyne.twinkies.storage.PlayerStorage;
import me.wyne.twinkies.wconfig.WConfig;
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
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
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
        WLog.registerLogger(getLogger());
        WLog.registerConfig(loggingConfig);
        WLog.registerLogDirectory(new File(getDataFolder(), "logs"));
        WLog.registerWriteThread(Executors.newSingleThreadExecutor());

        // Initialize notifications
        Notifications.registerPlugin(this);

        // Initialize configs
        WConfig.registerConfigObject(notificationsConfig);
        WConfig.registerConfigObject(loggingConfig);
        WConfig.reloadConfigObjects(getConfig());

        Bukkit.getPluginManager().registerEvents(joinListener, this);

        this.getCommand("twinkies").setTabCompleter(this);
        this.getCommand("twinkies").setExecutor(this);

        playerPlaceholders.register();

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
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args)
    {
        if (!(sender instanceof Player))
            return null;

        List<String> result = new ArrayList<>();

        result.addAll(loggingTabComplete(sender, args));
        result.addAll(notificationsSettingsStorage.tabComplete(sender, args));

        return result;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!(sender instanceof Player))
            return true;

        notificationsSettingsStorage.setSetting(sender, args);
        setLoggingSetting(sender, args);

        return false;
    }

    public List<String> loggingTabComplete(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.logging"))
            return List.of();

        List<String> result = new ArrayList<>();

        if (args.length == 1)
        {
            result.add("logging");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("logging"))
        {
            for (Field field : LoggingConfig.class.getDeclaredFields())
            {
                if (args[1].isBlank())
                    result.add(field.getName());
                else
                {
                    if (field.getName().toLowerCase().contains(args[1].toLowerCase()))
                        result.add(field.getName());
                }
            }
        }

        return result;
    }

    public void setLoggingSetting(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.logging"))
            return;
        if (args.length != 2)
            return;
        if (!args[0].equalsIgnoreCase("logging"))
            return;
        if (loggingConfig.<Boolean>getSetting(args[1]) == null)
            return;

        Component setMessage = loggingConfig.setSetting(args[1], !loggingConfig.<Boolean>getSetting(args[1]));

        if (loggingConfig.<Boolean>getSetting(args[1]))
            sender.sendMessage(setMessage.append(Component.text(" включено.").color(NamedTextColor.GREEN)));
        else
            sender.sendMessage(setMessage.append(Component.text(" отключено.").color(NamedTextColor.RED)));
    }
}
