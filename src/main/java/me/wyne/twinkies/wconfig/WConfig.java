package me.wyne.twinkies.wconfig;

import me.wyne.twinkies.wlog.WLog;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class WConfig {

    private static final Set<Object> registeredConfigClasses = new HashSet<>();

    public static void registerConfigClass(@NotNull final Object object)
    {
        registeredConfigClasses.add(object);
    }

    public static void reloadConfigClasses(@NotNull final FileConfiguration config) {
        try
        {
            WLog.info("Перезагрузка конфига...");
            for (Object object : registeredConfigClasses)
            {
                for(Field field  : object.getClass().getDeclaredFields())
                {
                    if (field.isAnnotationPresent(ConfigField.class))
                    {
                        field.setAccessible(true);
                        field.set(object, config.get(field.getAnnotation(ConfigField.class).path()));
                        field.setAccessible(false);
                    }
                }
            }
        }
        catch (IllegalAccessException e)
        {
            WLog.error("Произошла ошибка при перезагрузке конфига");
            WLog.error(e.getMessage());
        }
        WLog.info("Конфиг перезагружен");
    }

}
