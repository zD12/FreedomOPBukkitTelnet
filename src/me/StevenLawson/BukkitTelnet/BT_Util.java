package me.StevenLawson.BukkitTelnet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

public class BT_Util
{
    private static final Logger log = Logger.getLogger("Minecraft");

    protected BT_Util()
    {
    }

    public static boolean canBypassPassword(String user_ip, BukkitTelnet plugin)
    {
        if (plugin.bypass_password_ips == null)
        {
            return false;
        }
        else if (plugin.bypass_password_ips.contains(user_ip.trim()))
        {
            return true;
        }
        else
        {
            String[] user_ip_parts = user_ip.trim().split("\\.");
            
            if (user_ip_parts.length == 4)
            {               
                for (String test_ip : plugin.bypass_password_ips)
                {
                    String[] test_ip_parts = test_ip.trim().split("\\.");
                    
                    if (test_ip_parts.length == 4)
                    {
                        if (user_ip_parts[0].equals(test_ip_parts[0]) && user_ip_parts[1].equals(test_ip_parts[1]) && user_ip_parts[2].equals(test_ip_parts[2]))
                        {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }

    public static void createDefaultConfiguration(String name, BukkitTelnet plugin, File plugin_file)
    {
        File actual = new File(plugin.getDataFolder(), name);
        if (!actual.exists())
        {
            log.info("[" + plugin.getDescription().getName() + "]: Installing default configuration file template: " + actual.getPath());
            InputStream input = null;
            try
            {
                JarFile file = new JarFile(plugin_file);
                ZipEntry copy = file.getEntry(name);
                if (copy == null)
                {
                    log.severe("[" + plugin.getDescription().getName() + "]: Unable to read default configuration: " + actual.getPath());
                    return;
                }
                input = file.getInputStream(copy);
            }
            catch (IOException ioex)
            {
                log.severe("[" + plugin.getDescription().getName() + "]: Unable to read default configuration: " + actual.getPath());
            }
            if (input != null)
            {
                FileOutputStream output = null;

                try
                {
                    plugin.getDataFolder().mkdirs();
                    output = new FileOutputStream(actual);
                    byte[] buf = new byte[8192];
                    int length;
                    while ((length = input.read(buf)) > 0)
                    {
                        output.write(buf, 0, length);
                    }

                    log.info("[" + plugin.getDescription().getName() + "]: Default configuration file written: " + actual.getPath());
                }
                catch (IOException ioex)
                {
                    log.log(Level.SEVERE, "[" + plugin.getDescription().getName() + "]: Unable to write default configuration: " + actual.getPath(), ioex);
                }
                finally
                {
                    try
                    {
                        if (input != null)
                        {
                            input.close();
                        }
                    }
                    catch (IOException ioex)
                    {
                    }

                    try
                    {
                        if (output != null)
                        {
                            output.close();
                        }
                    }
                    catch (IOException ioex)
                    {
                    }
                }
            }
        }
    }
}
