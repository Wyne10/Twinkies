package me.wyne.twinkies.listeners;

import me.wyne.twinkies.Twinkies;
import me.wyne.twinkies.wlog.WLog;
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
        if (!e.getPlayer().hasPlayedBefore())
        {
            WLog.log(e.getPlayer(), plugin.getNotificationsConfig().getRegister(), plugin.getLogConfig().logRegister());
            Notifications.sendNotification(plugin, e.getPlayer(), plugin.getNotificationsConfig().getRegister());
        }

        // Save player if he isn't fully in database
        if (!plugin.getPlayerStorage().isPlayerSaved(e.getPlayer()))
        {
            plugin.getPlayerStorage().savePlayerNickname(e.getPlayer(), e.getPlayer().getName());
            plugin.getPlayerStorage().savePlayerIp(e.getPlayer(), e.getPlayer().getAddress().getAddress().getHostName());
        }
        else
        {
            WLog.log(e.getPlayer(), plugin.getNotificationsConfig().getJoin(), plugin.getLogConfig().logJoin());
            Notifications.sendNotification(plugin, e.getPlayer(), plugin.getNotificationsConfig().getJoin());
        }

        // Handle new nick of player
        if (!plugin.getPlayerStorage().getPlayerNicknames(e.getPlayer()).contains(e.getPlayer().getName()))
        {
            WLog.log(e.getPlayer(), plugin.getNotificationsConfig().getNewNick(), plugin.getLogConfig().logNewNick());
            Notifications.sendNotification(plugin, e.getPlayer(), plugin.getNotificationsConfig().getNewNick());
            plugin.getPlayerStorage().savePlayerNickname(e.getPlayer(), e.getPlayer().getName());
        }
        else
        {
            // Handle nick change of player
            if (!plugin.getPlayerStorage().getPlayerLastNickname(e.getPlayer()).equals(e.getPlayer().getName()))
            {
                WLog.log(e.getPlayer(), plugin.getNotificationsConfig().getChangeNick(), plugin.getLogConfig().logChangeNick());
                Notifications.sendNotification(plugin, e.getPlayer(), plugin.getNotificationsConfig().getChangeNick());
            }
        }

        // Handle new IP of player
        if (!plugin.getPlayerStorage().getPlayerIps(e.getPlayer()).contains(e.getPlayer().getAddress().getAddress().getHostName()))
        {
            WLog.log(e.getPlayer(), plugin.getNotificationsConfig().getNewIp(), plugin.getLogConfig().logNewIp());
            Notifications.sendNotification(plugin, e.getPlayer(), plugin.getNotificationsConfig().getNewIp());
            plugin.getPlayerStorage().savePlayerIp(e.getPlayer(), e.getPlayer().getAddress().getAddress().getHostAddress());
        }
        else
        {
            // Handle IP change of player
            if (!plugin.getPlayerStorage().getPlayerLastIp(e.getPlayer()).equals(e.getPlayer().getAddress().getAddress().getHostName()))
            {
                WLog.log(e.getPlayer(), plugin.getNotificationsConfig().getChangeIp(), plugin.getLogConfig().logChangeIp());
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
                WLog.log(e.getPlayer(), player, plugin.getNotificationsConfig().getDupeNick(), plugin.getLogConfig().logDupeNick());
                Notifications.sendNotification(plugin, e.getPlayer(), player, plugin.getNotificationsConfig().getDupeNick());
            }

            // Handle dupe IP of player
            if (plugin.getPlayerStorage().getPlayerIps(player) != null && plugin.getPlayerStorage().getPlayerIps(player).contains(e.getPlayer().getAddress().getAddress().getHostName()))
            {
                WLog.log(e.getPlayer(), player, plugin.getNotificationsConfig().getDupeIp(), plugin.getLogConfig().logDupeIp());
                Notifications.sendNotification(plugin, e.getPlayer(), player, plugin.getNotificationsConfig().getDupeIp());
            }
        }
    }

}
