package me.StevenLawson.BukkitTelnet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.apache.commons.lang.exception.ExceptionUtils;

public class BT_Util
{
    private static final Logger logger = Logger.getLogger("Minecraft-Server");

    protected BT_Util()
    {
    }

    public static void log(Level level, String message)
    {
        logger.log(level, "[{0}]: {1}", new Object[]
                {
                    BukkitTelnet.plugin.getDescription().getName(), message
                });
    }

    public static boolean canBypassPassword(String user_ip)
    {
        user_ip = user_ip.trim();

        if (BukkitTelnet.plugin.bypass_password_ips == null)
        {
            return false;
        }

        if (BukkitTelnet.plugin.bypass_password_ips.contains(user_ip))
        {
            return true;
        }
        else
        {
            for (String test_ip : BukkitTelnet.plugin.bypass_password_ips)
            {
                if (fuzzyIpMatch(test_ip, user_ip, 3))
                {
                    return true;
                }
            }
        }

        return false;
    }

    public static void createDefaultConfiguration(String name, File plugin_file)
    {
        File actual = new File(BukkitTelnet.plugin.getDataFolder(), name);
        if (!actual.exists())
        {
            log(Level.INFO, "Installing default configuration file template: " + actual.getPath());
            InputStream input = null;
            try
            {
                JarFile file = new JarFile(plugin_file);
                ZipEntry copy = file.getEntry(name);
                if (copy == null)
                {
                    log(Level.SEVERE, "Unable to read default configuration: " + actual.getPath());
                    return;
                }
                input = file.getInputStream(copy);
            }
            catch (IOException ioex)
            {
                log(Level.SEVERE, "Unable to read default configuration: " + actual.getPath());
            }
            if (input != null)
            {
                FileOutputStream output = null;

                try
                {
                    BukkitTelnet.plugin.getDataFolder().mkdirs();
                    output = new FileOutputStream(actual);
                    byte[] buf = new byte[8192];
                    int length;
                    while ((length = input.read(buf)) > 0)
                    {
                        output.write(buf, 0, length);
                    }

                    log(Level.INFO, "Default configuration file written: " + actual.getPath());
                }
                catch (IOException ioex)
                {
                    log(Level.SEVERE, "Unable to write default configuration: " + actual.getPath() + "\n" + ExceptionUtils.getStackTrace(ioex));
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

    public static boolean fuzzyIpMatch(String a, String b, int required_octets)
    {
        boolean is_match = true;

        String[] a_parts = a.split("\\.");
        String[] b_parts = b.split("\\.");

        if (a_parts.length != 4 || b_parts.length != 4)
        {
            return false;
        }

        if (required_octets > 4)
        {
            required_octets = 4;
        }
        else if (required_octets < 1)
        {
            required_octets = 1;
        }

        for (int i = 0; i < required_octets && i < 4; i++)
        {
            if (a_parts[i].equals("*") || b_parts[i].equals("*"))
            {
                continue;
            }

            if (!a_parts[i].equals(b_parts[i]))
            {
                is_match = false;
                break;
            }
        }

        return is_match;
    }
}
