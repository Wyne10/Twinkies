package me.wyne.twinkies.wconfig;

import me.wyne.twinkies.wlog.WLog;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class WConfig {

    private static final Set<Object> registeredConfigObjects = new HashSet<>();

    /**
     * Registered objects will be reloaded on {@link #reloadConfigObjects(FileConfiguration)}.
     * @param object Object to register
     */
    public static void registerConfigObject(@NotNull final Object object)
    {
        registeredConfigObjects.add(object);
    }

    /**
     * Load data from {@link FileConfiguration} to registered objects.
     * @param config Config to load data from
     */
    public static void reloadConfigObjects(@NotNull final FileConfiguration config) {
        try
        {
            WLog.info("Перезагрузка конфига...");
            for (Object object : registeredConfigObjects)
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
