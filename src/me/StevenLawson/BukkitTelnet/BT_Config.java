package me.StevenLawson.BukkitTelnet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.pravian.bukkitlib.YamlConfig;

public class BT_Config
{
    private YamlConfig config = null;
    private final SimpleConfigEntries configEntries;
    
    private BT_Config()
    {
        configEntries = new SimpleConfigEntries();
    }
    
    public void loadConfig()
    {
        if (config == null)
        {
            config = new YamlConfig(BukkitTelnet.plugin, "config.yml", true);
        }
        
        config.load();
        
        configEntries.setAddress(config.getString("address"));
        configEntries.setPort(config.getInt("port"));
        configEntries.setPassword(config.getString("password"));
        configEntries.getAdmins().clear();
        
        if (config.isConfigurationSection("admins"))
        {
            for (String admin : config.getConfigurationSection("admins").getKeys(false))
            {
                
                if (!config.isList("admins." + admin))
                {
                    continue;
                }
                
                configEntries.getAdmins().put(admin, config.getStringList("admins." + admin));
            }
        }
        
        if (configEntries.getPassword().equals(""))
        {
            configEntries.setPassword(config.getDefaultConfig().getString("password"));
            BT_Log.warning("Password set to blank in config!");
            BT_Log.warning("Defaulting to " + configEntries.getPassword());
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
            return admins;
        }
    }
    
    public static BT_Config getInstance()
    {
        return BT_ConfigHolder.INSTANCE;
    }
    
    private static class BT_ConfigHolder
    {
        private static final BT_Config INSTANCE = new BT_Config();
    }
}
