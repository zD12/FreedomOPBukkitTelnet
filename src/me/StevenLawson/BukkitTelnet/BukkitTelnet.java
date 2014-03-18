package me.StevenLawson.BukkitTelnet;

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitTelnet extends JavaPlugin
{
    public static BukkitTelnet plugin;
    public static Server server;

    @Override
    public void onLoad()
    {
        plugin = this;
        server = plugin.getServer();
        
        BT_Log.setPluginLogger(plugin.getLogger());
        BT_Log.setServerLogger(server.getLogger());
    }

    @Override
    public void onEnable()
    {
        BT_Config.getInstance().loadConfig();

        BT_TelnetServer.getInstance().startServer();

        BT_Log.info(plugin.getName() + " v" + plugin.getDescription().getVersion() + " enabled");

    }

    @Override
    public void onDisable()
    {
        BT_TelnetServer.getInstance().stopServer();

        BT_Log.info("Plugin disabled");
    }
}
