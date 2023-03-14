package me.wyne.twinkies.listeners;

import me.wyne.twinkies.Twinkies;
import me.wyne.twinkies.logging.LoggingConfig;
import me.wyne.twinkies.notifications.NotificationsConfig;
import me.wyne.twinkies.storage.PlayerStorage;
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

        if (!p.hasPlayedBefore())
        {
            WLog.log(p, notifConfig.getRegister(), logConfig.logRegister());
            Notifications.sendNotification(p, notifConfig.getRegister());
        }

        // Save player if he isn't fully in database
        if (!storage.isPlayerSaved(p))
        {
            storage.savePlayerNickname(p, p.getName());
            storage.savePlayerIp(p, ip);
        }
        else
        {
            WLog.log(p, notifConfig.getJoin(), logConfig.logJoin());
            Notifications.sendNotification(p, notifConfig.getJoin());
        }

        // Handle new nick of player
        if (!storage.getCollection(storage.playerNicknames(), uuid).contains(p.getName()))
        {
            WLog.log(p, notifConfig.getNewNick(), logConfig.logNewNick());
            Notifications.sendNotification(p, notifConfig.getNewNick());
            storage.savePlayerNickname(p, p.getName());
        }
        else
        {
            // Handle nick change of player
            if (!storage.get(storage.playerLastNickname(), uuid).equals(p.getName()))
            {
                WLog.log(p, notifConfig.getChangeNick(), logConfig.logChangeNick());
                Notifications.sendNotification(p, notifConfig.getChangeNick());
            }
        }

        // Handle new IP of player
        if (!storage.getCollection(storage.playerIps(), uuid).contains(ip))
        {
            WLog.log(p, notifConfig.getNewIp(), logConfig.logNewIp());
            Notifications.sendNotification(p, notifConfig.getNewIp());
            storage.savePlayerIp(p, ip);
        }
        else
        {
            // Handle IP change of player
            if (!storage.get(storage.playerLastIp(), uuid).equals(ip))
            {
                WLog.log(p, notifConfig.getChangeIp(), logConfig.logChangeIp());
                Notifications.sendNotification(p, notifConfig.getChangeIp());
            }
        }

        for (OfflinePlayer player : Bukkit.getOfflinePlayers())
        {
            if (player == p)
                continue;

            // Handle dupe nick of player
            if (storage.getCollection(storage.playerNicknames(), player.getUniqueId()) != null && storage.getCollection(storage.playerNicknames(), player.getUniqueId()).contains(p.getName()))
            {
                WLog.log(p, player, notifConfig.getDupeNick(), logConfig.logDupeNick());
                Notifications.sendNotification(p, player, notifConfig.getDupeNick());
            }

            // Handle dupe IP of player
            if (storage.getCollection(storage.playerIps(), player.getUniqueId()) != null && storage.getCollection(storage.playerIps(), player.getUniqueId()).contains(ip))
            {
                WLog.log(p, player, notifConfig.getDupeIp(), logConfig.logDupeIp());
                Notifications.sendNotification(p, player, notifConfig.getDupeIp());
            }
        }
    }

}
