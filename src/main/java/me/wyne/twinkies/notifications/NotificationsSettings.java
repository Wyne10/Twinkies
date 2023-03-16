package me.wyne.twinkies.notifications;

import me.wyne.twinkies.wlog.WLog;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class NotificationsSettings {

    @Setting(reference = "register", toggleMessage = "о регистрации")
    private boolean sendRegister = true;
    @Setting(reference = "join", toggleMessage = "о присоединении")
    private boolean sendJoin = true;
    @Setting(reference = "changeNick", toggleMessage = "о смене ника")
    private boolean sendChangeNick = true;
    @Setting(reference = "newNick", toggleMessage = "о новом нике")
    private boolean sendNewNick = true;
    @Setting(reference = "dupeNick", toggleMessage = "о дублирующемся нике")
    private boolean sendDupeNick = true;
    @Setting(reference = "changeIp", toggleMessage = "о смене IP")
    private boolean sendChangeIp = true;
    @Setting(reference = "newIp", toggleMessage = "о новом IP")
    private boolean sendNewIp = true;
    @Setting(reference = "dupeIp", toggleMessage = "о дублирующемся IP")
    private boolean sendDupeIp = true;

    public boolean getSetting(NotificationType notificationType)
    {
        for (Field field : getClass().getDeclaredFields())
        {
            if (field.getAnnotation(Setting.class).reference().equalsIgnoreCase(notificationType.getSettingReference()))
            {
                try {
                    return field.getBoolean(this);
                } catch (IllegalAccessException e) {
                    WLog.error("Произошла ошибка при попытке получить настройку '" + notificationType.getSettingReference());
                }
            }
        }
        return false;
    }

    public Component toggleSetting(@NotNull final String settingReference) throws IllegalAccessException {
        for(Field field : getClass().getDeclaredFields())
        {
            String reference = field.getAnnotation(Setting.class).reference();
            String toggleMessage = field.getAnnotation(Setting.class).toggleMessage();
            if (reference.equalsIgnoreCase(settingReference))
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
