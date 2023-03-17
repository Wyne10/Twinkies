package me.wyne.twinkies.wlog;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WLog {

    private static Logger logger = null;
    private static WLogConfig config = null;
    private static ExecutorService executorService;
    private static File directory = null;

    /**
     * @return Are logger and config registered?
     */
    public static boolean isActive()
    {
        return logger != null && config != null;
    }

    /**
     * Register {@link Logger} to log all messages.
     * @param logger Logger to register
     */
    public static void registerLogger(@NotNull final Logger logger)
    {
        WLog.logger = logger;
    }

    /**
     * Register {@link WLogConfig} to control {@link #info(String)}, {@link #warn(String)} and {@link #error(String)} logging.
     * @param config Config to register
     */
    public static void registerConfig(@NotNull final WLogConfig config)
    {
        WLog.config = config;
    }

    /**
     * Register directory to save log files to.
     * @param directory Directory to register
     */
    public static void registerLogDirectory(@NotNull final File directory)
    {
        WLog.directory = directory;
    }

    /**
     * Register {@link ExecutorService} that will write logs to {@link #directory}.
     * @param executorService Thread to register
     */
    public static void registerWriteThread(@NotNull final ExecutorService executorService)
    {
        WLog.executorService = executorService;
    }

    public static void info(@NotNull final String message)
    {
        if (isActive() && config.logInfo())
        {
            logger.info(message);
            writeLog(Level.INFO, message);
        }
    }

    public static void warn(@NotNull final String message)
    {
        if (isActive() && config.logWarn())
        {
            logger.info(message);
            writeLog(Level.WARNING, message);
        }
    }

    public static void error(@NotNull final String message)
    {
        if (isActive() && config.logError())
        {
            logger.severe(message);
            writeLog(Level.SEVERE, message);
        }
    }

    /**
     * Log {@link LogMessage}.
     * @param logMessage {@link LogMessage} to log
     * @param doLog Log or don't log. Useful when using some logging config
     */
    public static void log(@NotNull final LogMessage logMessage, final boolean doLog)
    {
        if (isActive() && doLog)
        {
            logger.log(logMessage.getLevel(), logMessage.getMessage());
            writeLog(logMessage.getLevel(), logMessage.getMessage());
        }
    }

    /**
     * Log {@link LogMessage} and split {@link LogMessage} by regex.
     * @param logMessage {@link LogMessage} to log
     * @param splitRegex Regex to split {@link LogMessage} by
     * @param doLog Log or don't log. Useful when using some logging config
     */
    public static void log(@NotNull final LogMessage logMessage, @NotNull final String splitRegex, final boolean doLog)
    {
        if (isActive() && doLog)
        {
            for (String message : logMessage.getMessage().split(splitRegex))
            {
                logger.log(logMessage.getLevel(), message);
                writeLog(logMessage.getLevel(), message);
            }
        }
    }

    /**
     * Write log to {@link #directory}.
     * @param level Log {@link Level}
     * @param log Message to log
     */
    private static void writeLog(@NotNull final Level level, @NotNull final String log)
    {
        if (directory == null || executorService == null)
            return;

        executorService.execute(() -> {
            String levelMessage = "INFO";

            if (level == Level.WARNING)
                levelMessage = "WARN";
            else if (level == Level.SEVERE)
                levelMessage = "ERROR";

            String writeLog = "[" + new SimpleDateFormat("hh:mm:ss").format(new Date()) + " " + levelMessage + "] " + log;

            File logFile = new File(directory, LocalDate.now() + ".txt");

            try {
                if (!directory.exists())
                    directory.mkdirs();
                if (!logFile.exists())
                    logFile.createNewFile();

                PrintWriter writer = new PrintWriter(new FileWriter(logFile, true));
                writer.println(writeLog);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                error("Произошла ошибка при попытке записи лога в файл '" + LocalDate.now() + ".txt'");
            }
        });
    }
}
