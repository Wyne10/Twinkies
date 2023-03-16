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

    private static Twinkies plugin;

    public static void registerPlugin(@NotNull final Twinkies plugin)
    {
        Notifications.plugin = plugin;
    }

    public static void sendNotification(@NotNull final Component message, @NotNull final NotificationType notificationType)
    {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (!player.hasPermission("twinkies.notifications"))
                continue;
            if (!plugin.getNotificationsSettingsStorage().get(plugin.getNotificationsSettingsStorage().playerSettings(), player.getUniqueId()).<Boolean>getSetting(notificationType.getSettingFieldName()))
                continue;

            player.sendMessage(message);
        }
    }

    @NotNull
    public static Component getPlayerInfo(@NotNull final OfflinePlayer player)
    {
        Component playerInfo = Component.text("Информация о игроке '")
                .append(Component.text(player.getName()))
                .append(Component.text("'"))
                .appendNewline()
                .append(Component.text("Никнеймы:"))
                .appendNewline();

        if (plugin.getPlayerStorage().getCollection(plugin.getPlayerStorage().playerNicknames(), player.getUniqueId()) != null)
        {
            for (String nickname : plugin.getPlayerStorage().getCollection(plugin.getPlayerStorage().playerNicknames(), player.getUniqueId()))
            {
                playerInfo = playerInfo.append(Component.text(nickname)).appendNewline();
            }
        }

        playerInfo = playerInfo.appendNewline().append(Component.text("IP адреса:")).appendNewline();

        if (plugin.getPlayerStorage().getCollection(plugin.getPlayerStorage().playerIps(), player.getUniqueId()) != null)
        {
            int i = 0;
            for (String ip : plugin.getPlayerStorage().getCollection(plugin.getPlayerStorage().playerIps(), player.getUniqueId()))
            {
                if (i != plugin.getPlayerStorage().getCollection(plugin.getPlayerStorage().playerIps(), player.getUniqueId()).size() - 1)
                    playerInfo = playerInfo.append(Component.text(ip)).appendNewline();
                else
                    playerInfo = playerInfo.append(Component.text(ip));
                i++;
            }
        }

        return playerInfo;
    }

    public static void sendNotification(@NotNull final Player player, @NotNull final String stringMessage, @NotNull final NotificationType notificationType)
    {
        Component message = plugin.getMiniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, stringMessage))
                .replaceText(
                        TextReplacementConfig.builder()
                                .matchLiteral(player.getName())
                                .replacement(Component.text(player.getName()).decorate(TextDecoration.UNDERLINED)
                                        .hoverEvent(HoverEvent.showText(getPlayerInfo(player)))).build());
        sendNotification(message, notificationType);
    }

    public static void sendNotification(@NotNull final Player player, @NotNull final OfflinePlayer dupePlayer, @NotNull final String stringMessage, @NotNull final NotificationType notificationType)
    {
        if (!dupePlayer.hasPlayedBefore())
            return;

        Component message = plugin.getMiniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, stringMessage))
                .replaceText(
                        TextReplacementConfig.builder()
                                .matchLiteral(player.getName())
                                .replacement(Component.text(player.getName()).decorate(TextDecoration.UNDERLINED)
                                        .hoverEvent(HoverEvent.showText(getPlayerInfo(player)))).build())
                .replaceText(
                        TextReplacementConfig.builder()
                                .matchLiteral("%player_dupe%")
                                .replacement(Component.text(dupePlayer.getName()).decorate(TextDecoration.UNDERLINED)
                                        .hoverEvent(HoverEvent.showText(getPlayerInfo(dupePlayer)))).build());
        sendNotification(message, notificationType);
    }

}
