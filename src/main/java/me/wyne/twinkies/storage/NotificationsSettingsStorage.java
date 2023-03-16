package me.wyne.twinkies.storage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.wyne.twinkies.Twinkies;
import me.wyne.twinkies.notifications.NotificationType;
import me.wyne.twinkies.notifications.NotificationsSettings;
import me.wyne.twinkies.wlog.WLog;
import me.wyne.twinkies.wstorage.JsonStorage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class NotificationsSettingsStorage extends JsonStorage {

    private final HashMap<UUID, NotificationsSettings> playerSettings = new HashMap<>();
    public HashMap<UUID, NotificationsSettings> playerSettings() {
        return playerSettings;
    }

    public NotificationsSettingsStorage(@NotNull final Twinkies plugin) {
        super(plugin, "notifSettings.json");
    }

    public void loadData() {
        executorService.execute(() -> {
            try {
                WLog.info("Загрузка данных из файла '" + storageFile.getName() + "'...");
                JsonObject playerObjects = (JsonObject) JsonParser.parseReader(new FileReader(storageFile));
                for (Map.Entry<String, JsonElement> playerObject : playerObjects.entrySet())
                {
                    playerSettings.put(UUID.fromString(playerObject.getKey()), gson.fromJson(playerObject.getValue(), NotificationsSettings.class));
                }
                WLog.info("Данные из файла '" + storageFile.getName() + "' загружены");
            } catch (FileNotFoundException e) {
                WLog.error("Произошла ошибка при загрузке данных из файла '" + storageFile.getName() + "'");
                WLog.error(e.getMessage());
            }
        });
    }

    public void initializePlayer(@NotNull final Player player)
    {
        if (!playerSettings.containsKey(player.getUniqueId()) && player.hasPermission("twinkies.notifications"))
        {
            playerSettings.put(player.getUniqueId(), new NotificationsSettings());
            save(playerSettings, player.getUniqueId(), new NotificationsSettings(), null);
        }
    }

    @Nullable
    public List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.notifications"))
            return null;

        List<String> result = new ArrayList<>();

        if (args.length == 1)
        {
            result.add("notif");
        }

        if (args.length == 2)
        {
            for (NotificationType notificationType : NotificationType.values())
            {
                result.add(notificationType.getSettingFieldName());
            }
        }

        return result;
    }

    public void setSetting(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.notifications"))
            return;
        if (args.length != 2)
            return;
        if (!args[0].equalsIgnoreCase("notif"))
            return;
        if (!NotificationType.contains(args[1]))
            return;

        try {
            sender.sendMessage(playerSettings.get(((Player)sender).getUniqueId()).toggleSetting(args[1]));
        } catch (IllegalAccessException e) {
            WLog.error("Произошла ошибка при попытке переключить настройку '" + args[1] + "' игрока '" + sender.getName() + "'");
        }
        save(null, ((Player)sender).getUniqueId(), playerSettings.get(((Player)sender).getUniqueId()).getSetting(args[1]), args[1]);
    }

}
