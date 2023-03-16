package me.wyne.twinkies.wsettings;

import me.wyne.twinkies.wlog.WLog;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public abstract class WSettings {

    public <SettingType> SettingType getSetting(@NotNull final String setting) {
        for (Field field : getClass().getDeclaredFields())
        {
            if (!field.isAnnotationPresent(Setting.class))
                continue;
            if (field.isAnnotationPresent(SettingReference.class))
            {
                if (field.getAnnotation(SettingReference.class).reference().equalsIgnoreCase(setting))
                {
                    field.setAccessible(true);
                    try {
                        return (SettingType) field.get(this);
                    } catch (IllegalAccessException e) {
                        WLog.error("Произошла ошибка при попытке получить настройку '" + setting + "'");
                        return null;
                    }
                }
            }
            else
            {
                if (field.getName().equalsIgnoreCase(setting))
                {
                    field.setAccessible(true);
                    try {
                        return (SettingType) field.get(this);
                    } catch (IllegalAccessException e) {
                        WLog.error("Произошла ошибка при попытке получить настройку '" + setting + "'");
                        return null;
                    }
                }
            }
        }
        return null;
    }

    public Component setSetting(@NotNull final String setting, @NotNull final Object newValue) {
        for (Field field : getClass().getDeclaredFields())
        {
            if (!field.isAnnotationPresent(Setting.class))
                continue;
            if (field.isAnnotationPresent(SettingReference.class))
            {
                if (field.getAnnotation(SettingReference.class).reference().equalsIgnoreCase(setting))
                {
                    try {
                        field.setAccessible(true);
                        field.set(this, newValue);
                    } catch (IllegalAccessException e) {
                        WLog.error("Произошла ошибка при попытке установить настройку '" + setting + "'");
                        return Component.empty();
                    }
                    return Component.text(field.getAnnotation(Setting.class).setMessage());
                }
            }
            else
            {
                if (field.getName().equalsIgnoreCase(setting))
                {
                    try {
                        field.setAccessible(true);
                        field.set(this, newValue);
                    } catch (IllegalAccessException e) {
                        WLog.error("Произошла ошибка при попытке установить настройку '" + setting + "'");
                        return Component.empty();
                    }
                    return Component.text(field.getAnnotation(Setting.class).setMessage());
                }
            }
        }
        return Component.empty();
    }
}
