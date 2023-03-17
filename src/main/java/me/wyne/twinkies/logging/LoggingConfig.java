package me.wyne.twinkies.logging;

import me.wyne.twinkies.wconfig.ConfigField;
import me.wyne.twinkies.wlog.WLogConfig;
import me.wyne.twinkies.wsettings.Setting;
import me.wyne.twinkies.wsettings.WSettings;

public class LoggingConfig extends WSettings implements WLogConfig {

    @Setting(setMessage = "Логирование ошибок")
    @ConfigField(path = "log-err")
    private boolean logError = true;
    @Setting(setMessage = "Логирование предупреждений")
    @ConfigField(path = "log-warn")
    private boolean logWarn = true;
    @Setting(setMessage = "Логирование информации")
    @ConfigField(path = "log-info")
    private boolean logInfo = true;
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
}
