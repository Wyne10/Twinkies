package me.wyne.twinkies.logging;

import me.wyne.twinkies.Twinkies;
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

    public static void log(@NotNull final Twinkies plugin, @NotNull final String message, final boolean doLog)
    {
        if (doLog)
            plugin.getLogger().info(message);
    }

}
