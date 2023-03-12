package me.wyne.twinkies.wconfig;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class WConfig {

    private static final Set<Object> registeredObjects = new HashSet<>();

    public static void registerClass(@NotNull final Object object)
    {
        registeredObjects.add(object);
    }

    public static void reloadFields(@NotNull final FileConfiguration config) throws IllegalAccessException {
        for (Object object : registeredObjects)
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

}
