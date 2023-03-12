package me.wyne.twinkies.listeners;

import me.wyne.twinkies.Twinkies;
import me.wyne.twinkies.logging.WLog;
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
        if (plugin.getPlayerStorage().getPlayerNicknames(e.getPlayer()) == null)
        {
            WLog.log(plugin, "Зарегистрирован новый игрок", plugin.getLogConfig().isLogRegister());
            WLog.log(plugin, "Никнейм: '" + e.getPlayer().getName() + "'", plugin.getLogConfig().isLogRegister());
            WLog.log(plugin, "IP: '" + e.getPlayer().getAddress().getAddress().getHostName() + "'", plugin.getLogConfig().isLogRegister());
            plugin.getPlayerStorage().savePlayerNickname(e.getPlayer(), e.getPlayer().getName());
            plugin.getPlayerStorage().savePlayerIp(e.getPlayer(), e.getPlayer().getAddress().getAddress().getHostName());
            return;
        }

        if (!plugin.getPlayerStorage().getPlayerNicknames(e.getPlayer()).contains(e.getPlayer().getName()))
        {
            plugin.getPlayerStorage().savePlayerNickname(e.getPlayer(), e.getPlayer().getName());
            plugin.getNotifications().sendNotification(e.getPlayer(), plugin.getNotificationsConfig().getNewNick());
        }
    }

}
