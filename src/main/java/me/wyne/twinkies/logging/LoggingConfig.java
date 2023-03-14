package me.wyne.twinkies.logging;

import me.wyne.twinkies.wconfig.ConfigField;
import me.wyne.twinkies.wlog.WLogConfig;

public class LoggingConfig implements WLogConfig {

    @ConfigField(path = "log-err")
    private boolean logError = true;
    @ConfigField(path = "log-warn")
    private boolean logWarn = true;
    @ConfigField(path = "log-info")
    private boolean logInfo = true;
    @ConfigField(path = "log-register")
    private boolean logRegister = true;
    @ConfigField(path = "log-join")
    private boolean logJoin = true;
    @ConfigField(path = "log-change-nick")
    private boolean logChangeNick = true;
    @ConfigField(path = "log-new-nick")
    private boolean logNewNick = true;
    @ConfigField(path = "log-dupe-nick")
    private boolean logDupeNick = true;
    @ConfigField(path = "log-change-ip")
    private boolean logChangeIp = true;
    @ConfigField(path = "log-new-ip")
    private boolean logNewIp = true;
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
