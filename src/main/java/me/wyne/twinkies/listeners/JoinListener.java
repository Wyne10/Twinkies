package me.wyne.twinkies.listeners;

import me.wyne.twinkies.Twinkies;
import me.wyne.twinkies.logging.WLog;
import me.wyne.twinkies.notifications.Notifications;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class JoinListener implements Listener {

    private final Twinkies plugin;

    public JoinListener(@NotNull final Twinkies plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e)
    {
        // Register player if he isn't fully in database
        if (!plugin.getPlayerStorage().isPlayerRegistered(e.getPlayer()))
        {
            WLog.log(plugin, e.getPlayer(), plugin.getNotificationsConfig().getRegister(), plugin.getLogConfig().isLogRegister());
            Notifications.sendNotification(plugin, e.getPlayer(), plugin.getNotificationsConfig().getRegister());
            plugin.getPlayerStorage().savePlayerNickname(e.getPlayer(), e.getPlayer().getName());
            plugin.getPlayerStorage().savePlayerIp(e.getPlayer(), e.getPlayer().getAddress().getAddress().getHostName());
        }
        else
        {
            WLog.log(plugin, e.getPlayer(), plugin.getNotificationsConfig().getJoin(), plugin.getLogConfig().isLogJoin());
            Notifications.sendNotification(plugin, e.getPlayer(), plugin.getNotificationsConfig().getJoin());
        }

        // Handle new nick of player
        if (!plugin.getPlayerStorage().getPlayerNicknames(e.getPlayer()).contains(e.getPlayer().getName()))
        {
            WLog.log(plugin, e.getPlayer(), plugin.getNotificationsConfig().getNewNick(), plugin.getLogConfig().isLogNewNick());
            Notifications.sendNotification(plugin, e.getPlayer(), plugin.getNotificationsConfig().getNewNick());
            plugin.getPlayerStorage().savePlayerNickname(e.getPlayer(), e.getPlayer().getName());
        }
        else
        {
            // Handle nick change of player
            if (!plugin.getPlayerStorage().getPlayerLastNickname(e.getPlayer()).equals(e.getPlayer().getName()))
            {
                WLog.log(plugin, e.getPlayer(), plugin.getNotificationsConfig().getChangeNick(), plugin.getLogConfig().isLogChangeNick());
                Notifications.sendNotification(plugin, e.getPlayer(), plugin.getNotificationsConfig().getChangeNick());
            }
        }

        // Handle new IP of player
        if (!plugin.getPlayerStorage().getPlayerIps(e.getPlayer()).contains(e.getPlayer().getAddress().getAddress().getHostName()))
        {
            WLog.log(plugin, e.getPlayer(), plugin.getNotificationsConfig().getNewIp(), plugin.getLogConfig().isLogNewIp());
            Notifications.sendNotification(plugin, e.getPlayer(), plugin.getNotificationsConfig().getNewIp());
            plugin.getPlayerStorage().savePlayerIp(e.getPlayer(), e.getPlayer().getAddress().getAddress().getHostAddress());
        }
        else
        {
            // Handle IP change of player
            if (!plugin.getPlayerStorage().getPlayerLastIp(e.getPlayer()).equals(e.getPlayer().getAddress().getAddress().getHostName()))
            {
                WLog.log(plugin, e.getPlayer(), plugin.getNotificationsConfig().getChangeIp(), plugin.getLogConfig().isLogChangeIp());
                Notifications.sendNotification(plugin, e.getPlayer(), plugin.getNotificationsConfig().getChangeIp());
            }
        }

        for (OfflinePlayer player : Bukkit.getOfflinePlayers())
        {
            if (player == e.getPlayer())
                continue;

            // Handle dupe nick of player
            if (plugin.getPlayerStorage().getPlayerNicknames(player) != null && plugin.getPlayerStorage().getPlayerNicknames(player).contains(e.getPlayer().getName()))
            {
                WLog.log(plugin, e.getPlayer(), player, plugin.getNotificationsConfig().getDupeNick(), plugin.getLogConfig().isLogDupeNick());
                Notifications.sendNotification(plugin, e.getPlayer(), player, plugin.getNotificationsConfig().getDupeNick());
            }

            // Handle dupe IP of player
            if (plugin.getPlayerStorage().getPlayerIps(player) != null && plugin.getPlayerStorage().getPlayerIps(player).contains(e.getPlayer().getAddress().getAddress().getHostName()))
            {
                WLog.log(plugin, e.getPlayer(), player, plugin.getNotificationsConfig().getDupeIp(), plugin.getLogConfig().isLogDupeIp());
                Notifications.sendNotification(plugin, e.getPlayer(), player, plugin.getNotificationsConfig().getDupeIp());
            }
        }
    }

}
