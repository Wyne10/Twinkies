package me.wyne.twinkies.wlog;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class WLog {

    private static Logger logger = null;
    private static WLogConfig config = null;

    public static void registerLogger(@NotNull final Logger logger)
    {
        WLog.logger = logger;
    }

    public static void registerConfig(@NotNull final WLogConfig config)
    {
        WLog.config = config;
    }

    public static void info(@NotNull final String message)
    {
        if (config.logInfo())
            logger.info(message);
    }

    public static void warn(@NotNull final String message)
    {
        if (config.logWarn())
            logger.info(message);
    }

    public static void error(@NotNull final String message)
    {
        if (config.logError())
            logger.severe(message);
    }

    public static void log(@NotNull final Player player, @NotNull final String message, final boolean doLog)
    {
        if (doLog)
        {
            for (String msg : message.split("(<br>|<newline>)+"))
            {
                logger.info(MiniMessage.miniMessage().stripTags(PlaceholderAPI.setPlaceholders(player, msg)));
            }
        }
    }

    public static void log(@NotNull final Player player, @NotNull final OfflinePlayer dupePlayer, @NotNull final String message, final boolean doLog)
    {
        if (doLog)
        {
            for (String msg : message.split("(<br>|<newline>)+"))
            {
                logger.info(MiniMessage.miniMessage().stripTags(PlaceholderAPI.setPlaceholders(player, msg)).replaceAll("(%player_dupe%)+", dupePlayer.getName()));
            }
        }
    }

}
