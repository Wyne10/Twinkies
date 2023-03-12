package me.wyne.twinkies;

import me.wyne.twinkies.storage.PlayerStorage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class Twinkies extends JavaPlugin implements CommandExecutor {

    private final PlayerStorage playerStorage = new PlayerStorage(this);

    @Override
    public void onEnable() {
        playerStorage.createStorageFolder();
        playerStorage.createStorageFile();
        playerStorage.loadData();
    }

    public PlayerStorage getPlayerStorage() {
        return playerStorage;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
/*        playerStorage.savePlayerNickname((Player) sender, args[0]);
        playerStorage.savePlayerIp((Player)sender, ((Player)sender).getAddress().getAddress().getHostAddress());*/
        playerStorage.clearPlayerNicknames((Player)sender);
        playerStorage.clearPlayerIps((Player)sender);
        return false;
    }
}
