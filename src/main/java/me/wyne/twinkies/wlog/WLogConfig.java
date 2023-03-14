package me.wyne.twinkies.wlog;

public interface WLogConfig {

    /**
     * @return Do info logging?
     */
    boolean logInfo();
    /**
     * @return Do warning logging?
     */
    boolean logWarn();
    /**
     * @return Do error logging?
     */
    boolean logError();

}
