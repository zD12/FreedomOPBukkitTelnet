package com.bekvon.bukkit.mctelnet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

public class TelnetUtil
{
    private static final Logger log = Logger.getLogger("Minecraft");

    private TelnetUtil()
    {
    }

    public static void createDefaultConfiguration(String name, MCTelnet tfm, File plugin_file)
    {
        File actual = new File(tfm.getDataFolder(), name);
        if (!actual.exists())
        {
            log.info("[" + tfm.getDescription().getName() + "]: Installing default configuration file template: " + actual.getPath());
            InputStream input = null;
            try
            {
                JarFile file = new JarFile(plugin_file);
                ZipEntry copy = file.getEntry(name);
                if (copy == null)
                {
                    log.severe("[" + tfm.getDescription().getName() + "]: Unable to read default configuration: " + actual.getPath());
                    return;
                }
                input = file.getInputStream(copy);
            }
            catch (IOException ioex)
            {
                log.severe("[" + tfm.getDescription().getName() + "]: Unable to read default configuration: " + actual.getPath());
            }
            if (input != null)
            {
                FileOutputStream output = null;

                try
                {
                    tfm.getDataFolder().mkdirs();
                    output = new FileOutputStream(actual);
                    byte[] buf = new byte[8192];
                    int length = 0;
                    while ((length = input.read(buf)) > 0)
                    {
                        output.write(buf, 0, length);
                    }

                    log.info("[" + tfm.getDescription().getName() + "]: Default configuration file written: " + actual.getPath());
                }
                catch (IOException ioex)
                {
                    log.log(Level.SEVERE, "[" + tfm.getDescription().getName() + "]: Unable to write default configuration: " + actual.getPath(), ioex);
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
