package me.wyne.twinkies.notifications;

import me.wyne.twinkies.wlog.WLog;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class NotificationsSettings {

    @Setting(toggleMessage = "о регистрации")
    private boolean sendRegister = true;
    @Setting(toggleMessage = "о присоединении")
    private boolean sendJoin = true;
    @Setting(toggleMessage = "о смене ника")
    private boolean sendChangeNick = true;
    @Setting(toggleMessage = "о новом нике")
    private boolean sendNewNick = true;
    @Setting(toggleMessage = "о дублирующемся нике")
    private boolean sendDupeNick = true;
    @Setting(toggleMessage = "о смене IP")
    private boolean sendChangeIp = true;
    @Setting(toggleMessage = "о новом IP")
    private boolean sendNewIp = true;
    @Setting(toggleMessage = "о дублирующемся IP")
    private boolean sendDupeIp = true;

    public boolean getSetting(@NotNull final NotificationType notificationType)
    {
        for (Field field : getClass().getDeclaredFields())
        {
            if (field.getName().equalsIgnoreCase(notificationType.getSettingFieldName()))
            {
                try {
                    return field.getBoolean(this);
                } catch (IllegalAccessException e) {
                    WLog.error("Произошла ошибка при попытке получить настройку '" + notificationType.getSettingFieldName() + "'");
                }
            }
        }
        return false;
    }

    public boolean getSetting(@NotNull final String settingFieldName)
    {
        for (Field field : getClass().getDeclaredFields())
        {
            if (field.getName().equalsIgnoreCase(settingFieldName))
            {
                try {
                    return field.getBoolean(this);
                } catch (IllegalAccessException e) {
                    WLog.error("Произошла ошибка при попытке получить настройку '" + settingFieldName + "'");
                }
            }
        }
        return false;
    }

    public Component toggleSetting(@NotNull final String settingFieldName) throws IllegalAccessException {
        for(Field field : getClass().getDeclaredFields())
        {
            String toggleMessage = field.getAnnotation(Setting.class).toggleMessage();
            if (field.getName().equalsIgnoreCase(settingFieldName))
            {
                field.setAccessible(true);
                field.set(this, !field.getBoolean(this));
                field.setAccessible(false);
                if (!field.getBoolean(this))
                    return Component.text("Уведомления ")
                            .append(Component.text(toggleMessage))
                            .append(Component.text(" отключены.").color(NamedTextColor.RED));
                else
                    return Component.text("Уведомления ")
                            .append(Component.text(toggleMessage))
                            .append(Component.text(" включены.").color(NamedTextColor.GREEN));
            }
        }

        return Component.empty();
    }
}
