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

        TelnetLogger.setPluginLogger(plugin.getLogger());
        TelnetLogger.setServerLogger(server.getLogger());
    }

    @Override
    public void onEnable()
    {
        TelnetConfig.getInstance().loadConfig();

        TelnetServer.getInstance().startServer();

        TelnetLogger.info(plugin.getName() + " v" + plugin.getDescription().getVersion() + " enabled");

    }

    @Override
    public void onDisable()
    {
        TelnetServer.getInstance().stopServer();

        TelnetLogger.info("Plugin disabled.");
    }
}
