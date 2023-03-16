package me.wyne.twinkies.notifications;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class NotificationsSettings {

    @Setting(toggleMessage = "Уведомления о регистрации")
    private boolean sendRegister = true;
    @Setting(toggleMessage = "Уведомления о присоединении")
    private boolean sendJoin = true;
    @Setting(toggleMessage = "Уведомления о смене ника")
    private boolean sendChangeNick = true;
    @Setting(toggleMessage = "Уведомления о новом нике")
    private boolean sendNewNick = true;
    @Setting(toggleMessage = "Уведомления о дублирующемся нике")
    private boolean sendDupeNick = true;
    @Setting(toggleMessage = "Уведомления о смене IP")
    private boolean sendChangeIp = true;
    @Setting(toggleMessage = "Уведомления о новом IP")
    private boolean sendNewIp = true;
    @Setting(toggleMessage = "Уведомления о дублирующемся IP")
    private boolean sendDupeIp = true;

    public boolean getSetting(@NotNull final NotificationType notificationType)
    {
        try {
            return getClass().getDeclaredField(notificationType.getSettingFieldName()).getBoolean(this);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return false;
        }
    }

    public boolean getSetting(@NotNull final String settingFieldName)
    {
        try {
            return getClass().getDeclaredField(settingFieldName).getBoolean(this);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return false;
        }
    }

    public Component toggleSetting(@NotNull final String settingFieldName) {
        try {
            Field setting = getClass().getDeclaredField(settingFieldName);
            String toggleMessage = setting.getAnnotation(Setting.class).toggleMessage();
            setting.setAccessible(true);
            setting.set(this, !setting.getBoolean(this));
            setting.setAccessible(false);
            if (!setting.getBoolean(this))
                return Component.text(toggleMessage)
                        .append(Component.text(" отключены.").color(NamedTextColor.RED));
            else
                return Component.text(toggleMessage)
                        .append(Component.text(" включены.").color(NamedTextColor.GREEN));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return Component.empty();
        }
    }
}
