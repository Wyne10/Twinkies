package me.wyne.twinkies.logging;

import me.wyne.twinkies.wconfig.ConfigField;

public class LoggingConfig {

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
    @ConfigField(path = "log-change-ip")
    private boolean logChangeIp = true;
    @ConfigField(path = "log-new-ip")
    private boolean logNewIp = true;
    @ConfigField(path = "log-dupe-ip")
    private boolean logDupeIp = true;

    public boolean isLogError() {
        return logError;
    }

    public boolean isLogWarn() {
        return logWarn;
    }

    public boolean isLogInfo() {
        return logInfo;
    }

    public boolean isLogRegister() {
        return logRegister;
    }

    public boolean isLogJoin() {
        return logJoin;
    }

    public boolean isLogChangeNick() {
        return logChangeNick;
    }

    public boolean isLogNewNick() {
        return logNewNick;
    }

    public boolean isLogChangeIp() {
        return logChangeIp;
    }

    public boolean isLogNewIp() {
        return logNewIp;
    }

    public boolean isLogDupeIp() {
        return logDupeIp;
    }
}
