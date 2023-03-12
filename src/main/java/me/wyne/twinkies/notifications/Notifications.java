package me.wyne.twinkies.notifications;

import me.clip.placeholderapi.PlaceholderAPI;
import me.wyne.twinkies.Twinkies;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Notifications {

    private final Twinkies plugin;

    public Notifications(@NotNull final Twinkies plugin)
    {
        this.plugin = plugin;
    }

    public void sendNotification(@NotNull final Component message)
    {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (!player.hasPermission("twinkies.notifications"))
                continue;

            player.sendMessage(message);
        }
    }

    public void sendNotification(@NotNull final Component @NotNull ... messages)
    {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (!player.hasPermission("twinkies.notifications"))
                return;

            for (Component message : messages)
            {
                player.sendMessage(message);
            }
        }
    }

    public void sendNotification(@NotNull final Player player, @NotNull final String stringMessage)
    {
        Component message = plugin.getMiniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, stringMessage))
                .appendNewline()
                .append(plugin.getMiniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, plugin.getNotificationsConfig().getPlayerInfo()))
                        .hoverEvent(HoverEvent.showText(Component.text("Информация о игроке '")
                                .append(Component.text(player.getName()))
                                .append(Component.text("'")))));
        sendNotification(message);
    }
}
