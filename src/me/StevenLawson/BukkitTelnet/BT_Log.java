package me.StevenLawson.BukkitTelnet;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;

public final class BT_Log
{
    private static final Logger LOGGER = Bukkit.getLogger();

    private BT_Log()
    {
        throw new AssertionError();
    }

    // Level.INFO:
    public static void info(String message)
    {
        info(message, false);
    }

    public static void info(String message, Boolean raw)
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

    public static void warning(String message, Boolean raw)
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

    public static void severe(String message, Boolean raw)
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
        if (!raw)
        {
            message = "[BukkitTelnet] " + message;
        }

        getLogger().log(level, message);
    }

    private static void log(Level level, Throwable throwable)
    {
        getLogger().log(level, null, throwable);
    }

    public static Logger getLogger()
    {
        return LOGGER;
    }
}
