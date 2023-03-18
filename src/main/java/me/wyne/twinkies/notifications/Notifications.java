package me.wyne.twinkies.notifications;

import me.clip.placeholderapi.PlaceholderAPI;
import me.wyne.twinkies.Twinkies;
import me.wyne.twinkies.extension.ComponentExtensions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
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
                .color(NamedTextColor.BLUE)
                .appendNewline();

        if (plugin.getPlayerStorage().playerNicknames().containsKey(player.getUniqueId()))
        {
            playerInfo = ComponentExtensions.appendCollection(playerInfo, plugin.getPlayerStorage().playerNicknames().get(player.getUniqueId()), Style.style(NamedTextColor.WHITE));
        }

        playerInfo = playerInfo.appendNewline().append(Component.text("IP адреса:")).color(NamedTextColor.BLUE).appendNewline();

        if (plugin.getPlayerStorage().playerIps().containsKey(player.getUniqueId()))
        {
            playerInfo = ComponentExtensions.appendCollection(playerInfo, plugin.getPlayerStorage().playerIps().get(player.getUniqueId()), Style.style(NamedTextColor.WHITE));
        }

        playerInfo = playerInfo.appendNewline();

        if (plugin.getPlayerStorage().playerLastNickname().containsKey(player.getUniqueId()))
        {
            playerInfo = playerInfo.append(Component.text("Последний никнейм: ").color(NamedTextColor.BLUE));
            playerInfo = playerInfo.append(Component.text(plugin.getPlayerStorage().playerLastNickname().get(player.getUniqueId())).color(NamedTextColor.WHITE));
            playerInfo = playerInfo.appendNewline();
        }

        if (plugin.getPlayerStorage().playerLastIp().containsKey(player.getUniqueId()))
        {
            playerInfo = playerInfo.append(Component.text("Последний IP адрес: ").color(NamedTextColor.BLUE));
            playerInfo = playerInfo.append(Component.text(plugin.getPlayerStorage().playerLastIp().get(player.getUniqueId())).color(NamedTextColor.WHITE));
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
