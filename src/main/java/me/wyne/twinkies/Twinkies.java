package me.wyne.twinkies;

import me.wyne.twinkies.listeners.JoinListener;
import me.wyne.twinkies.logging.LoggingConfig;
import me.wyne.twinkies.wlog.WLog;
import me.wyne.twinkies.notifications.NotificationsConfig;
import me.wyne.twinkies.notifications.NotificationsSettings;
import me.wyne.twinkies.storage.PlayerStorage;
import me.wyne.twinkies.wconfig.WConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public final class Twinkies extends JavaPlugin implements CommandExecutor {

    // API Stuff
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    // Storage
    private final PlayerStorage playerStorage = new PlayerStorage(this);

    // Configs
    private final NotificationsConfig notificationsConfig = new NotificationsConfig();
    private final LoggingConfig loggingConfig = new LoggingConfig();

    // Listeners
    private final JoinListener joinListener = new JoinListener(this);

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        WLog.registerLogger(getLogger());
        WLog.registerConfig(loggingConfig);

        WConfig.registerClass(notificationsConfig);
        WConfig.registerClass(loggingConfig);
        try {
            WLog.info("Перезагрузка конфига...");
            WConfig.reloadFields(getConfig());
        } catch (IllegalAccessException e) {
            WLog.error("Произошла ошибка при перезагрузке конфига");
            WLog.error(e.getMessage());
        }
        WLog.info("Конфиг перезагружен");

        Bukkit.getPluginManager().registerEvents(joinListener, this);

        playerStorage.createStorageFolder();
        playerStorage.createStorageFile();
        playerStorage.loadData();
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
    public NotificationsConfig getNotificationsConfig() {
        return notificationsConfig;
    }
    @NotNull
    public LoggingConfig getLogConfig() {
        return loggingConfig;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        return false;
    }
}
