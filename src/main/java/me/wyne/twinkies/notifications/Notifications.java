package me.wyne.twinkies.notifications;

import me.clip.placeholderapi.PlaceholderAPI;
import me.wyne.twinkies.Twinkies;
import me.wyne.twinkies.logging.WLog;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Notifications {

    public static void sendNotification(@NotNull final Twinkies plugin, @NotNull final String message)
    {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (!player.hasPermission("twinkies.notifications"))
                continue;

            player.sendMessage(plugin.getMiniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, message)));
        }
    }

    public static void sendNotification(@NotNull final Twinkies plugin, @NotNull final Component message)
    {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (!player.hasPermission("twinkies.notifications"))
                continue;

            player.sendMessage(message);
        }
    }

    public static void sendNotification(@NotNull final Twinkies plugin, @NotNull final String @NotNull ... messages)
    {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (!player.hasPermission("twinkies.notifications"))
                return;

            for (String message : messages)
            {
                player.sendMessage(plugin.getMiniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, message)));
            }
        }
    }

    public static void sendNotification(@NotNull final Twinkies plugin, @NotNull final Component @NotNull ... messages)
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


    public static void sendNotification(@NotNull final Twinkies plugin, @NotNull final Player player, @NotNull final String stringMessage)
    {
        Component playerInfo = Component.text("Информация о игроке '")
                .append(Component.text(player.getName()))
                .append(Component.text("'"))
                .appendNewline()
                .append(Component.text("Никнеймы:"))
                .appendNewline();

        if (plugin.getPlayerStorage().getPlayerNicknames(player) != null)
        {
            for (String nickname : plugin.getPlayerStorage().getPlayerNicknames(player))
            {
                playerInfo = playerInfo.append(Component.text(nickname)).appendNewline();
            }
        }

        playerInfo = playerInfo.appendNewline().append(Component.text("IP адреса:")).appendNewline();

        if (plugin.getPlayerStorage().getPlayerIps(player) != null)
        {
            int i = 0;
            for (String ip : plugin.getPlayerStorage().getPlayerIps(player))
            {
                if (i != plugin.getPlayerStorage().getPlayerIps(player).size() - 1)
                    playerInfo = playerInfo.append(Component.text(ip)).appendNewline();
                else
                    playerInfo = playerInfo.append(Component.text(ip));
                i++;
            }
        }

        Component message = plugin.getMiniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, stringMessage))
                .appendNewline()
                .append(plugin.getMiniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, plugin.getNotificationsConfig().getPlayerInfo()))
                        .hoverEvent(HoverEvent.showText(playerInfo)));
        sendNotification(plugin, message);
    }
}
