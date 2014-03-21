package me.StevenLawson.BukkitTelnet;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.pravian.bukkitlib.YamlConfig;

public class TelnetConfig
{
    private final YamlConfig config;
    private final SimpleConfigEntries configEntries;

    private TelnetConfig()
    {
        configEntries = new SimpleConfigEntries();
        config = new YamlConfig(BukkitTelnet.plugin, "config.yml", true);
    }

    public void loadConfig()
    {
        config.load();

        configEntries.setAddress(config.getString("address"));
        configEntries.setPort(config.getInt("port"));
        configEntries.setPassword(config.getString("password"));

        configEntries.clearAdmins();
        if (config.isConfigurationSection("admins"))
        {
            for (String admin : config.getConfigurationSection("admins").getKeys(false))
            {

                if (!config.isList("admins." + admin))
                {
                    continue;
                }

                configEntries.addAdmin(admin, config.getStringList("admins." + admin));
            }
        }

        if (configEntries.getPassword().isEmpty())
        {
            configEntries.setPassword(config.getDefaultConfig().getString("password"));
            TelnetLogger.warning("Password is undefined in config!");
            TelnetLogger.warning("Defaulting to " + configEntries.getPassword());
        }
    }

    public SimpleConfigEntries getConfigEntries()
    {
        return configEntries;
    }

    public static final class SimpleConfigEntries
    {
        private int port;
        private String address;
        private String password;
        private final Map<String, List<String>> admins;

        private SimpleConfigEntries()
        {
            admins = new HashMap<String, List<String>>();
        }

        public int getPort()
        {
            return port;
        }

        public void setPort(int port)
        {
            this.port = port;
        }

        public String getAddress()
        {
            return address;
        }

        public void setAddress(String address)
        {
            this.address = address;
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword(String password)
        {
            this.password = password;
        }

        public Map<String, List<String>> getAdmins()
        {
            return Collections.unmodifiableMap(admins);
        }

        private void clearAdmins()
        {
            admins.clear();
        }

        private void addAdmin(String name, List<String> ips)
        {
            admins.put(name, ips);
        }
    }

    public static TelnetConfig getInstance()
    {
        return TelnetConfigHolder.INSTANCE;
    }

    private static class TelnetConfigHolder
    {
        private static final TelnetConfig INSTANCE = new TelnetConfig();
    }
}
