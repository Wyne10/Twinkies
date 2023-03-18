package me.wyne.twinkies.extension;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ComponentExtensions {

    public static Component appendCollection(@NotNull final Component component, @NotNull final Collection<String> collection, @NotNull Style style)
    {
        if (collection.isEmpty())
            return component;

        Component newComponent = component;

        for (String element : collection)
        {
            newComponent = newComponent.append(Component.text(element).style(style));
        }

        return newComponent;
    }

}
