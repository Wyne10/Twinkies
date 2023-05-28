package me.wyne.twinkies.notifications;

import org.jetbrains.annotations.NotNull;

public enum NotificationType {

    REGISTER("sendRegister"),
    JOIN("sendJoin"),
    CHANGE_NICK("sendChangeNick"),
    NEW_NICK("sendNewNick"),
    DUPE_NICK("sendDupeNick"),
    CHANGE_IP("sendChangeIp"),
    NEW_IP("sendNewIp"),
    DUPE_IP("sendDupeIp");

    private String settingFieldName;
    NotificationType(@NotNull final String settingReference)
    {
        this.settingFieldName = settingReference;
    }

    public String getSettingFieldName() {
        return settingFieldName;
    }
    public static boolean contains(@NotNull final String settingFieldName)
    {
        for (NotificationType notificationType : values())
        {
            if (notificationType.getSettingFieldName().equalsIgnoreCase(settingFieldName))
            {
                return true;
            }
        }

        return false;
    }
}
