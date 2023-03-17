package me.wyne.twinkies.wsettings;

import me.wyne.twinkies.wlog.WLog;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public abstract class WSettings {

    /**
     * Get {@link Setting} value from inheritor by {@link Setting} field name or by {@link SettingReference}.
     * @param setting {@link Setting} field name of {@link SettingReference}
     * @return {@link Setting} value
     * @param <SettingType> {@link Setting} value type
     */
    @Nullable
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

    /**
     * Get {@link Setting} from inheritor by {@link Setting} field name or by {@link SettingReference} and then set new value.
     * @param setting {@link Setting} field name of {@link SettingReference}
     * @param newValue New {@link Setting} value
     * @return {@link Setting} setMessage as component
     */
    @NotNull
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
