package me.wyne.twinkies.placeholderAPI;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.wyne.twinkies.Twinkies;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerPlaceholders extends PlaceholderExpansion {

    private final Twinkies plugin;

    public PlayerPlaceholders(@NotNull final Twinkies plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "twinkies";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Wyne";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.1";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null || !player.hasPlayedBefore())
            return params;

        if (params.equalsIgnoreCase("last_nick"))
        {
            if (plugin.getPlayerStorage().get(plugin.getPlayerStorage().playerLastNickname(), player.getUniqueId()) != null)
                return plugin.getPlayerStorage().get(plugin.getPlayerStorage().playerLastNickname(), player.getUniqueId());
            else
                return params;
        }

        if (params.equalsIgnoreCase("last_ip"))
        {
            if (plugin.getPlayerStorage().get(plugin.getPlayerStorage().playerLastIp(), player.getUniqueId()) != null)
                return plugin.getPlayerStorage().get(plugin.getPlayerStorage().playerLastIp(), player.getUniqueId());
            else
                return params;
        }

        return params;
    }
}
