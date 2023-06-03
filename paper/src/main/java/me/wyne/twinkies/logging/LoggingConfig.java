package me.wyne.twinkies.logging;

import me.wyne.wutils.config.ConfigField;
import me.wyne.wutils.config.Config;
import me.wyne.wutils.log.Log;
import me.wyne.wutils.log.LogConfig;
import me.wyne.wutils.settings.Setting;
import me.wyne.wutils.settings.Settings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class LoggingConfig extends Config implements LogConfig {

    @Setting(setMessage = "Логирование ошибок")
    @ConfigField(path = "log-err")
    private boolean logError = true;
    @Setting(setMessage = "Логирование предупреждений")
    @ConfigField(path = "log-warn")
    private boolean logWarn = true;
    @Setting(setMessage = "Логирование информации")
    @ConfigField(path = "log-info")
    private boolean logInfo = true;
    @Setting(setMessage = "Логирование запросов json")
    @ConfigField(path = "log-jsonQuery")
    private boolean logJsonQuery = false;
    @Setting(setMessage = "Логирование уведомлений о регистрации")
    @ConfigField(path = "log-register")
    private boolean logRegister = true;
    @Setting(setMessage = "Логирование уведомлений о присоединении")
    @ConfigField(path = "log-join")
    private boolean logJoin = true;
    @Setting(setMessage = "Логирование уведомлений о смене ника")
    @ConfigField(path = "log-change-nick")
    private boolean logChangeNick = true;
    @Setting(setMessage = "Логирование уведомлений о новом нике")
    @ConfigField(path = "log-new-nick")
    private boolean logNewNick = true;
    @Setting(setMessage = "Логирование уведомлений о дублирующемся нике")
    @ConfigField(path = "log-dupe-nick")
    private boolean logDupeNick = true;
    @Setting(setMessage = "Логирование уведомлений о смене IP")
    @ConfigField(path = "log-change-ip")
    private boolean logChangeIp = true;
    @Setting(setMessage = "Логирование уведомлений о новом IP")
    @ConfigField(path = "log-new-ip")
    private boolean logNewIp = true;
    @Setting(setMessage = "Логирование уведомлений о дублирующемся IP")
    @ConfigField(path = "log-dupe-ip")
    private boolean logDupeIp = true;

    public boolean logError() {
        return logError;
    }

    public boolean logWarn() {
        return logWarn;
    }

    public boolean logInfo() {
        return logInfo;
    }

    public boolean logJsonQuery() { return logJsonQuery; }

    public boolean logRegister() {
        return logRegister;
    }

    public boolean logJoin() {
        return logJoin;
    }

    public boolean logChangeNick() {
        return logChangeNick;
    }

    public boolean logNewNick() {
        return logNewNick;
    }

    public boolean logDupeNick() {
        return logDupeNick;
    }

    public boolean logChangeIp() {
        return logChangeIp;
    }

    public boolean logNewIp() {
        return logNewIp;
    }

    public boolean logDupeIp() {
        return logDupeIp;
    }

    @NotNull
    public List<String> loggingTabComplete(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.logging"))
            return List.of();

        List<String> result = new ArrayList<>();

        if (args.length == 1)
            result.add("logging");

        if (args.length == 2 && args[0].equalsIgnoreCase("logging"))
        {
            for (Field field : LoggingConfig.class.getDeclaredFields())
            {
                if (args[1].isBlank())
                    result.add(field.getName());
                else
                {
                    if (field.getName().toLowerCase().contains(args[1].toLowerCase()))
                        result.add(field.getName());
                }
            }
        }

        return result;
    }

    public void setLoggingSetting(@NotNull final CommandSender sender, @NotNull final String[] args)
    {
        if (!sender.hasPermission("twinkies.logging"))
            return;
        if (args.length != 2)
            return;
        if (!args[0].equalsIgnoreCase("logging"))
            return;
        if (Settings.<Boolean>getSetting(this, args[1]) == null)
            return;

        String setMessage = Settings.setSetting(this, args[1], !Settings.<Boolean>getSetting(this, args[1]));

        if (Settings.<Boolean>getSetting(this, args[1]))
        {
            sender.sendMessage(Component.text(setMessage).append(Component.text(" включено.").color(NamedTextColor.GREEN)));
            if (sender instanceof Player)
                Log.info("Игрок '" + sender.getName() + "' включил " + setMessage.toLowerCase());
            else
                Log.info("Консоль включила " + setMessage.toLowerCase());
        }

        else
        {
            sender.sendMessage(Component.text(setMessage).append(Component.text(" отключено.").color(NamedTextColor.RED)));
            if (sender instanceof Player)
                Log.info("Игрок '" + sender.getName() + "' отключил " + setMessage.toLowerCase());
            else
                Log.info("Консоль отключила " + setMessage.toLowerCase());
        }
    }
}
