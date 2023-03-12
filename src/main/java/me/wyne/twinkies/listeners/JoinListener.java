package me.wyne.twinkies.listeners;

import me.wyne.twinkies.Twinkies;
import me.wyne.twinkies.logging.WLog;
import me.wyne.twinkies.notifications.Notifications;
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
        // Register player if he isn't in database
        if (plugin.getPlayerStorage().getPlayerNicknames(e.getPlayer()) == null)
        {
            WLog.log(plugin, "Зарегистрирован новый игрок", plugin.getLogConfig().isLogRegister());
            WLog.log(plugin, "Никнейм: '" + e.getPlayer().getName() + "'", plugin.getLogConfig().isLogRegister());
            WLog.log(plugin, "IP: '" + e.getPlayer().getAddress().getAddress().getHostName() + "'", plugin.getLogConfig().isLogRegister());
            Notifications.sendNotification(plugin, plugin.getNotificationsConfig().getRegister());
            plugin.getPlayerStorage().savePlayerNickname(e.getPlayer(), e.getPlayer().getName());
            plugin.getPlayerStorage().savePlayerIp(e.getPlayer(), e.getPlayer().getAddress().getAddress().getHostName());
        }
        else
        {
            WLog.log(plugin, "Игрок '" + e.getPlayer().getName() + "' вошёл на сервер с IP '" + e.getPlayer().getAddress().getAddress().getHostName() + "'", plugin.getLogConfig().isLogJoin());
            Notifications.sendNotification(plugin, e.getPlayer(), plugin.getNotificationsConfig().getJoin());
        }

        // Handle new nick of player
        if (!plugin.getPlayerStorage().getPlayerNicknames(e.getPlayer()).contains(e.getPlayer().getName()))
        {
            WLog.log(plugin, "Игрок '" + e.getPlayer().getName() + "' вошёл на сервер с новым никнеймом", plugin.getLogConfig().isLogNewNick());
            Notifications.sendNotification(plugin, e.getPlayer(), plugin.getNotificationsConfig().getNewNick());
            plugin.getPlayerStorage().savePlayerNickname(e.getPlayer(), e.getPlayer().getName());
        }

        // Handle new IP of player
        if (!plugin.getPlayerStorage().getPlayerIps(e.getPlayer()).contains(e.getPlayer().getAddress().getAddress().getHostName()))
        {
            WLog.log(plugin, "Игрок '" + e.getPlayer().getName() + "' вошёл на сервер с нового IP адреса: '" + e.getPlayer().getAddress().getAddress().getHostAddress() + "'", plugin.getLogConfig().isLogNewIp());
            Notifications.sendNotification(plugin, e.getPlayer(), plugin.getNotificationsConfig().getNewIp());
            plugin.getPlayerStorage().savePlayerIp(e.getPlayer(), e.getPlayer().getAddress().getAddress().getHostAddress());
        }
    }

}
