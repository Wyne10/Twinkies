package me.wyne.twinkies.notifications;

import me.clip.placeholderapi.PlaceholderAPI;
import me.wyne.twinkies.Twinkies;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
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
            if (!plugin.getNotificationsSettingsStorage().playerSettings().containsKey(player.getUniqueId()) ||
                    !plugin.getNotificationsSettingsStorage().playerSettings().get(player.getUniqueId()).<Boolean>getSetting(notificationType.getSettingFieldName()))
                continue;

            player.sendMessage(message);
        }
    }

    public static void sendNotification(@NotNull final Player player, @NotNull final String stringMessage, @NotNull final NotificationType notificationType)
    {
        Component message = plugin.getMiniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, stringMessage))
                .replaceText(
                        TextReplacementConfig.builder()
                                .matchLiteral(player.getName())
                                .replacement(Component.text(player.getName()).decorate(TextDecoration.UNDERLINED)
                                        .hoverEvent(HoverEvent.showText(plugin.getPlayerStorage().getPlayerInfo(player, null, null))).clickEvent(ClickEvent.suggestCommand("/twinkies data player " + player.getName()))).build());
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
                                        .hoverEvent(HoverEvent.showText(plugin.getPlayerStorage().getPlayerInfo(player, null, null))).clickEvent(ClickEvent.suggestCommand("/twinkies data player " + player.getName()))).build())
                .replaceText(
                        TextReplacementConfig.builder()
                                .matchLiteral("%player_dupe%")
                                .replacement(Component.text(dupePlayer.getName()).decorate(TextDecoration.UNDERLINED)
                                        .hoverEvent(HoverEvent.showText(plugin.getPlayerStorage().getPlayerInfo(dupePlayer, null, null))).clickEvent(ClickEvent.suggestCommand("/twinkies data player " + dupePlayer.getName()))).build());
        sendNotification(message, notificationType);
    }

}
