package me.wyne.twinkies.wsettings;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If applied to {@link Setting} then {@link WSettings} will access this {@link Setting} by your own reference {@link String} and not by a field name.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SettingReference {
    /**
     * @return Reference {@link String} to get {@link Setting} by.
     */
    String reference();
}
