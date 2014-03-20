package me.StevenLawson.BukkitTelnet;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;

public final class TelnetLogger
{
    private static final Logger FALLBACK_LOGGER = Bukkit.getLogger();
    private static Logger serverLogger = null;
    private static Logger pluginLogger = null;

    private TelnetLogger()
    {
        throw new AssertionError();
    }

    // Level.INFO:
    public static void info(String message)
    {
        info(message, false);
    }

    public static void info(String message, boolean raw)
    {
        log(Level.INFO, message, raw);
    }

    public static void info(Throwable ex)
    {
        log(Level.INFO, ex);
    }

    // Level.WARNING:
    public static void warning(String message)
    {
        info(message, false);
    }

    public static void warning(String message, boolean raw)
    {
        log(Level.WARNING, message, raw);
    }

    public static void warning(Throwable ex)
    {
        log(Level.WARNING, ex);
    }

    // Level.SEVERE:
    public static void severe(String message)
    {
        info(message, false);
    }

    public static void severe(String message, boolean raw)
    {
        log(Level.SEVERE, message, raw);
    }

    public static void severe(Throwable ex)
    {
        log(Level.SEVERE, ex);
    }

    // Utility
    private static void log(Level level, String message, boolean raw)
    {
        getLogger(raw).log(level, message);
    }

    private static void log(Level level, Throwable throwable)
    {
        getLogger(false).log(level, null, throwable);
    }

    public static void setServerLogger(Logger logger)
    {
        serverLogger = logger;
    }

    public static void setPluginLogger(Logger logger)
    {
        pluginLogger = logger;
    }

    public static Logger getLogger(boolean raw)
    {
        if (raw || pluginLogger == null)
        {
            return (serverLogger != null ? serverLogger : FALLBACK_LOGGER);
        }
        else
        {
            return pluginLogger;
        }
    }
}
