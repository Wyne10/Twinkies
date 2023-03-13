package me.wyne.twinkies.logging;

import me.clip.placeholderapi.PlaceholderAPI;
import me.wyne.twinkies.Twinkies;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WLog {

    public static void info(@NotNull final Twinkies plugin, @NotNull final String message)
    {
        if (plugin.getLogConfig().isLogInfo())
            plugin.getLogger().info(message);
    }

    public static void warn(@NotNull final Twinkies plugin, @NotNull final String message)
    {
        if (plugin.getLogConfig().isLogWarn())
            plugin.getLogger().info(message);
    }

    public static void error(@NotNull final Twinkies plugin, @NotNull final String message)
    {
        if (plugin.getLogConfig().isLogError())
            plugin.getLogger().severe(message);
    }

    public static void log(@NotNull final Twinkies plugin, @NotNull final Player player, @NotNull final String message, final boolean doLog)
    {
        if (doLog)
        {
            for (String msg : message.split("(<br>|<newline>)+"))
            {
                plugin.getLogger().info(plugin.getMiniMessage().stripTags(PlaceholderAPI.setPlaceholders(player, msg)));
            }
        }
    }

    public static void log(@NotNull final Twinkies plugin, @NotNull final Player player, @NotNull final OfflinePlayer dupePlayer, @NotNull final String message, final boolean doLog)
    {
        if (doLog)
        {
            for (String msg : message.split("(<br>|<newline>)+"))
            {
                plugin.getLogger().info(plugin.getMiniMessage().stripTags(PlaceholderAPI.setPlaceholders(player, msg)).replaceAll("(%player_dupe%)+", dupePlayer.getName()));
            }
        }
    }

}
