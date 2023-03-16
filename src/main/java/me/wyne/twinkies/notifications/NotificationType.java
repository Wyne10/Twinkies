package me.wyne.twinkies.notifications;

import org.jetbrains.annotations.NotNull;

public enum NotificationType {

    REGISTER("register"),
    JOIN("join"),
    CHANGE_NICK("changeNick"),
    NEW_NICK("newNick"),
    DUPE_NICK("dupeNick"),
    CHANGE_IP("changeIp"),
    NEW_IP("newIp"),
    DUPE_IP("dupeIp");

    private String settingReference;
    NotificationType(@NotNull final String settingReference)
    {
        this.settingReference = settingReference;
    }

    public String getSettingReference() {
        return settingReference;
    }
}
