package me.wyne.twinkies.storage;

import me.wyne.twinkies.Twinkies;
import me.wyne.twinkies.notifications.NotificationsSettings;
import me.wyne.twinkies.wstorage.JsonStorage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class NotificationsSettingsStorage extends JsonStorage {

    private final HashMap<UUID, NotificationsSettings> settings = new HashMap<>();

    public NotificationsSettingsStorage(@NotNull final Twinkies plugin) {
        super(plugin, "notifSettings.json");
    }

    public void loadData() {

    }
}
