package me.wyne.twinkies.notifications;

import me.clip.placeholderapi.PlaceholderAPI;
import me.wyne.twinkies.Twinkies;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Notifications {

    public static void sendNotification(@NotNull final Twinkies plugin, @NotNull final Component message)
    {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (!player.hasPermission("twinkies.notifications"))
                continue;

            player.sendMessage(message);
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

    @NotNull
    public static Component getPlayerInfo(@NotNull final Twinkies plugin, @NotNull final OfflinePlayer player)
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

        return playerInfo;
    }

    public static void sendNotification(@NotNull final Twinkies plugin, @NotNull final Player player, @NotNull final String stringMessage)
    {
        Component message = plugin.getMiniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, stringMessage))
                .replaceText(
                        TextReplacementConfig.builder()
                                .matchLiteral(player.getName())
                                .replacement(Component.text(player.getName()).decorate(TextDecoration.UNDERLINED)
                                        .hoverEvent(HoverEvent.showText(getPlayerInfo(plugin, player)))).build());
        sendNotification(plugin, message);
    }

    public static void sendNotification(@NotNull final Twinkies plugin, @NotNull final Player player, @NotNull final OfflinePlayer dupePlayer, @NotNull final String stringMessage)
    {
        if (!dupePlayer.hasPlayedBefore())
            return;

        Component message = plugin.getMiniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, stringMessage))
                .replaceText(
                        TextReplacementConfig.builder()
                                .matchLiteral(player.getName())
                                .replacement(Component.text(player.getName()).decorate(TextDecoration.UNDERLINED)
                                        .hoverEvent(HoverEvent.showText(getPlayerInfo(plugin, player)))).build())
                .replaceText(
                        TextReplacementConfig.builder()
                                .matchLiteral("%player_dupe%")
                                .replacement(Component.text(dupePlayer.getName()).decorate(TextDecoration.UNDERLINED)
                                        .hoverEvent(HoverEvent.showText(getPlayerInfo(plugin, dupePlayer)))).build()
                );
        sendNotification(plugin, message);
    }
}
