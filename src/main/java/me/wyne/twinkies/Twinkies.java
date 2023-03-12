package me.wyne.twinkies;

import me.wyne.twinkies.listeners.JoinListener;
import me.wyne.twinkies.logging.LoggingConfig;
import me.wyne.twinkies.logging.WLog;
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

    // Settings
    private final HashMap<UUID, NotificationsSettings> notificationsSettings = new HashMap();

    // Listeners
    private final JoinListener joinListener = new JoinListener(this);

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        WConfig.registerClass(notificationsConfig);
        WConfig.registerClass(loggingConfig);
        try {
            WLog.info(this, "Перезагрузка конфига...");
            WConfig.reloadFields(getConfig());
        } catch (IllegalAccessException e) {
            WLog.error(this, "Произошла ошибка при перезагрузке конфига");
            WLog.error(this, e.getMessage());
        }
        WLog.info(this, "Конфиг перезагружен");

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

    @NotNull
    public NotificationsSettings getNotificationSettings(@NotNull final Player player) {
        if (!notificationsSettings.containsKey(player.getUniqueId()))
        {
            notificationsSettings.put(player.getUniqueId(), new NotificationsSettings());
        }
        return notificationsSettings.get(player.getUniqueId());
    }
    public void setNotificationsSettings(@NotNull final Player player, @NotNull final NotificationsSettings settings)
    {
        notificationsSettings.put(player.getUniqueId(), settings);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
/*        playerStorage.savePlayerNickname((Player) sender, args[0]);
        playerStorage.savePlayerIp((Player)sender, ((Player)sender).getAddress().getAddress().getHostAddress());*/
        playerStorage.clearPlayerNicknames((Player)sender);
        playerStorage.clearPlayerIps((Player)sender);
        return false;
    }
}
