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
        BT_Config.getInstance().load();

        BT_TelnetServer.getInstance().startServer();
        
        BT_Log.info(plugin.getName() + " v" + plugin.getDescription().getVersion() + " enabled");

    }

    @Override
    public void onDisable()
    {
        BT_TelnetServer.getInstance().stopServer();
        
        BT_Log.info("Plugin disabled");
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
