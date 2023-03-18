package me.wyne.twinkies.listeners;

import me.wyne.twinkies.Twinkies;
import me.wyne.twinkies.logging.LoggingConfig;
import me.wyne.twinkies.notifications.NotificationType;
import me.wyne.twinkies.notifications.NotificationsConfig;
import me.wyne.twinkies.storage.PlayerStorage;
import me.wyne.twinkies.wlog.LogMessage;
import me.wyne.twinkies.wlog.WLog;
import me.wyne.twinkies.notifications.Notifications;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class JoinListener implements Listener {

    private final Twinkies plugin;
    private final NotificationsConfig notifConfig;
    private final LoggingConfig logConfig;
    private final PlayerStorage storage;

    public JoinListener(@NotNull final Twinkies plugin)
    {
        this.plugin = plugin;
        this.notifConfig = plugin.getNotificationsConfig();
        this.logConfig = plugin.getLogConfig();
        this.storage = plugin.getPlayerStorage();
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e)
    {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        String ip = p.getAddress().getAddress().getHostAddress();

        plugin.getNotificationsSettingsStorage().initializePlayer(e.getPlayer());

        if (!p.hasPlayedBefore())
        {
            WLog.log(LogMessage.builder(notifConfig.getRegister()).replaceAll("(<br>|<newline>)+", "<nl>").stripTags().setPlaceholders(p).build(), "<nl>+", logConfig.logRegister());
            Notifications.sendNotification(p, notifConfig.getRegister(), NotificationType.REGISTER);
        }

        // Save player if he isn't fully in database
        if (!storage.isPlayerSaved(p))
        {
            storage.save(storage.playerLastNickname(), uuid, p.getName(), "last-nickname");
            storage.save(storage.playerLastIp(), uuid, ip, "last-ip");
            storage.saveCollection(storage.playerNicknames(), uuid, p.getName(), "nicknames");
            storage.saveCollection(storage.playerIps(), uuid, ip, "ips");
        }
        else
        {
            WLog.log(LogMessage.builder(notifConfig.getJoin()).stripTags().setPlaceholders(p).build(), logConfig.logJoin());
            Notifications.sendNotification(p, notifConfig.getJoin(), NotificationType.JOIN);
        }

        // Handle new nick of player
        if (!storage.getCollection(storage.playerNicknames(), uuid).contains(p.getName()))
        {
            WLog.log(LogMessage.builder(notifConfig.getNewNick()).replaceAll("(<br>|<newline>)+", "<nl>").stripTags().setPlaceholders(p).build(), "<nl>+", logConfig.logNewNick());
            Notifications.sendNotification(p, notifConfig.getNewNick(), NotificationType.NEW_NICK);
            storage.save(storage.playerLastNickname(), uuid, p.getName(), "last-nickname");
            storage.saveCollection(storage.playerNicknames(), uuid, p.getName(), "nicknames");
        }
        else
        {
            // Handle nick change of player
            if (!storage.get(storage.playerLastNickname(), uuid).equals(p.getName()))
            {
                WLog.log(LogMessage.builder(notifConfig.getChangeNick()).replaceAll("(<br>|<newline>)+", "<nl>").stripTags().setPlaceholders(p).build(), "<nl>+", logConfig.logChangeNick());
                Notifications.sendNotification(p, notifConfig.getChangeNick(), NotificationType.CHANGE_NICK);
            }
        }

        // Handle new IP of player
        if (!storage.getCollection(storage.playerIps(), uuid).contains(ip))
        {
            WLog.log(LogMessage.builder(notifConfig.getNewIp()).replaceAll("(<br>|<newline>)+", "<nl>").stripTags().setPlaceholders(p).build(), "<nl>+", logConfig.logNewIp());
            Notifications.sendNotification(p, notifConfig.getNewIp(), NotificationType.NEW_IP);
            storage.save(storage.playerLastIp(), uuid, ip, "last-ip");
            storage.saveCollection(storage.playerIps(), uuid, ip, "ips");
        }
        else
        {
            // Handle IP change of player
            if (!storage.get(storage.playerLastIp(), uuid).equals(ip))
            {
                WLog.log(LogMessage.builder(notifConfig.getChangeIp()).replaceAll("(<br>|<newline>)+", "<nl>").stripTags().setPlaceholders(p).build(), "<nl>+", logConfig.logChangeIp());
                Notifications.sendNotification(p, notifConfig.getChangeIp(), NotificationType.CHANGE_IP);
            }
        }

        for (OfflinePlayer player : Bukkit.getOfflinePlayers())
        {
            if (player == p)
                continue;

            // Handle dupe nick of player
            if (storage.getCollection(storage.playerNicknames(), player.getUniqueId()).contains(p.getName()))
            {
                WLog.log(LogMessage.builder(notifConfig.getDupeNick()).replaceAll("(<br>|<newline>)+", "<nl>").replaceAll("%player_dupe%+", player.getName()).stripTags().setPlaceholders(p).build(), "<nl>+", logConfig.logDupeNick());
                Notifications.sendNotification(p, player, notifConfig.getDupeNick(), NotificationType.DUPE_NICK);
            }

            // Handle dupe IP of player
            if (storage.getCollection(storage.playerIps(), player.getUniqueId()).contains(ip))
            {
                WLog.log(LogMessage.builder(notifConfig.getDupeIp()).replaceAll("(<br>|<newline>)+", "<nl>").replaceAll("%player_dupe%+", player.getName()).stripTags().setPlaceholders(p).build(), "<nl>+", logConfig.logDupeIp());
                Notifications.sendNotification(p, player, notifConfig.getDupeIp(), NotificationType.DUPE_IP);
            }
        }
    }

}
