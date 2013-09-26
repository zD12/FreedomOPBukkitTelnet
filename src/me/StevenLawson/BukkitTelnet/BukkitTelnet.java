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

//        BT_Config.SimpleConfigEntries configEntries = BT_Config.getInstance().getConfigEntries();
//
//        String address = configEntries.getAddress();
//        String password = configEntries.getPassword();
//        int port = configEntries.getPort();
//
//        BT_Log.info("Config loaded - " + address + ":" + port + " - PW: " + password + " - Admins:");
//
//        Iterator<Map.Entry<String, List<String>>> it = configEntries.getAdmins().entrySet().iterator();
//        while (it.hasNext())
//        {
//            Map.Entry<String, List<String>> entry = it.next();
//            String name = entry.getKey();
//            List<String> ips = entry.getValue();
//            BT_Log.info("Admin: " + name + " - IPs: " + StringUtils.join(ips, ","));
//        }

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
