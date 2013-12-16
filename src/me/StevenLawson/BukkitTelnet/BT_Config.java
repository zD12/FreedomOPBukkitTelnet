package me.StevenLawson.BukkitTelnet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.configuration.file.YamlConfiguration;

public class BT_Config
{
    public static final String CONFIG_FILENAME = "config.yml";
    private final SimpleConfigEntries configEntries = new SimpleConfigEntries();

    private BT_Config()
    {
        try
        {
            final File configFile = getConfigFile();
            if (configFile != null)
            {
                copyDefaultConfig(configFile);
            }

            load();
        }
        catch (Exception ex)
        {
            BT_Log.severe(ex);
        }
    }

    public final void load()
    {
        try
        {
            final YamlConfiguration config = new YamlConfiguration();

            final File configFile = getConfigFile();
            if (configFile == null)
            {
                return;
            }

            config.load(configFile);

            this.configEntries.setPort(config.getInt("port", this.configEntries.getPort()));
            this.configEntries.setAddress(config.getString("address", this.configEntries.getAddress()));
            this.configEntries.setPassword(config.getString("password", this.configEntries.getPassword()));

            final Map<String, List<String>> adminMap = this.configEntries.getAdmins();
            adminMap.clear();

            final Set<String> adminEntries = config.getConfigurationSection("admins").getKeys(false);
            for (String adminName : adminEntries)
            {
                adminMap.put(adminName, config.getStringList("admins." + adminName));
            }
        }
        catch (Exception ex)
        {
            BT_Log.severe(ex);
        }
    }

    private static void copyDefaultConfig(final File targetFile)
    {
        if (targetFile.exists())
        {
            return;
        }

        BT_Log.info("Installing default configuration file template: " + targetFile.getPath());

        try
        {
            final InputStream defaultConfig = getDefaultConfig();
            copy(defaultConfig, targetFile);
            defaultConfig.close();
        }
        catch (Exception ex)
        {
            BT_Log.severe(ex);
        }
    }

    private File getConfigFile()
    {
        try
        {
            return new File(BukkitTelnet.getPlugin().getDataFolder(), CONFIG_FILENAME);
        }
        catch (Exception ex)
        {
            BT_Log.severe(ex);
        }
        return null;
    }

    private static InputStream getDefaultConfig() throws IOException
    {
        try
        {
            return BukkitTelnet.getPlugin().getResource(CONFIG_FILENAME);
        }
        catch (Exception ex)
        {
            BT_Log.severe(ex);
        }
        throw new IOException();
    }

    private static void copy(InputStream in, File file) throws IOException
    {
        if (!file.exists())
        {
            file.getParentFile().mkdirs();
        }

        OutputStream out = new FileOutputStream(file);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
        {
            out.write(buf, 0, len);
        }
        out.close();
        in.close();
    }

    public SimpleConfigEntries getConfigEntries()
    {
        return configEntries;
    }

    public static class SimpleConfigEntries
    {
        private int _port = 8765;
        private String _address = null;
        private String _password = null;
        private final Map<String, List<String>> _admins = new HashMap<String, List<String>>();

        private SimpleConfigEntries()
        {
        }

        public int getPort()
        {
            return _port;
        }

        public void setPort(final int port)
        {
            this._port = port;
        }

        public String getAddress()
        {
            return _address;
        }

        public void setAddress(String address)
        {
            if (address == null || (address = address.trim()).isEmpty())
            {
                address = null;
            }
            this._address = address;
        }

        public String getPassword()
        {
            return _password;
        }

        public void setPassword(String password)
        {
            if (password == null || (password = password.trim()).isEmpty())
            {
                password = null;
            }
            this._password = password;
        }

        public Map<String, List<String>> getAdmins()
        {
            return _admins;
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
