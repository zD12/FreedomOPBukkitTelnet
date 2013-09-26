package me.StevenLawson.BukkitTelnet;

import org.bukkit.plugin.java.JavaPlugin;

public class BukkitTelnet extends JavaPlugin
{
    private static BukkitTelnet plugin = null;

    @Override
    public void onLoad()
    {
        plugin = this;
    }

    @Override
    public void onEnable()
    {
        BT_Log.info("Plugin enabled.");

        BT_Config.getInstance().load();

        BT_TelnetServer.getInstance().startServer();
    }

    @Override
    public void onDisable()
    {
        BT_Log.info("Plugin disabled.");

        BT_TelnetServer.getInstance().stopServer();
    }

    public static BukkitTelnet getPlugin() throws PluginNotLoadedException
    {
        if (plugin == null)
        {
            throw new PluginNotLoadedException();
        }

        return plugin;
    }

    public static class PluginNotLoadedException extends Exception
    {
        public PluginNotLoadedException()
        {
        }
    }
}
