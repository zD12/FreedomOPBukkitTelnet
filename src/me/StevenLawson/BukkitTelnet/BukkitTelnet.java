package me.StevenLawson.BukkitTelnet;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitTelnet extends JavaPlugin
{
    private static final String CONFIG_FILE = "config.yml";
    public static BukkitTelnet plugin = null;

    @Override
    public void onEnable()
    {
        BukkitTelnet.plugin = this;

        BT_Util.log(Level.INFO, "Enabled - Version " + this.getDescription().getVersion() + " by bekvon / Madgeek1450.");
        BT_Util.log(Level.INFO, "Starting server.");

        loadConfig();
        startServer();
    }

    @Override
    public void onDisable()
    {
        BT_Util.log(Level.INFO, "Stopping server.");

        stopServer();
    }
    protected int port = 8765;
    protected String address = null;
    protected String password = null;
    protected List<String> bypass_password_ips = null;

    private void loadConfig()
    {
        BT_Util.createDefaultConfiguration(CONFIG_FILE, getFile());
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), CONFIG_FILE));

        port = config.getInt("port", port);
        address = config.getString("address", null);
        password = config.getString("password", null);

        bypass_password_ips = (List<String>) config.getList("bypass_password_ips", null);
        if (bypass_password_ips == null)
        {
            bypass_password_ips = new ArrayList<String>();
        }
        if (bypass_password_ips.isEmpty())
        {
            bypass_password_ips.add("127.0.0.1");
        }
    }
    private ServerSocket listenerSocket = null;
    private ArrayList<BT_TelnetListener> clientHolder;
    private Thread listenerThread = null;
    private boolean is_running = false;
    private InetAddress listenAddress = null;

    private void startServer()
    {
        try
        {
            if (password == null)
            {
                BT_Util.log(Level.SEVERE, "Password is not defined in config file! Can't start server!");
                return;
            }

            if (address != null)
            {
                try
                {
                    listenAddress = null;
                    listenAddress = InetAddress.getByName(address);
                }
                catch (UnknownHostException ex)
                {
                    BT_Util.log(Level.SEVERE, "Unknown host: " + address);
                    return;
                }
            }

            try
            {
                if (listenAddress != null)
                {
                    listenerSocket = new java.net.ServerSocket(port, 10, listenAddress);
                }
                else
                {
                    listenerSocket = new java.net.ServerSocket(port);
                }

                String host_ip = listenerSocket.getInetAddress().getHostAddress();
                if (host_ip.equals("0.0.0.0"))
                {
                    host_ip = "*";
                }

                BT_Util.log(Level.INFO, "Server started on " + host_ip + ":" + port);
            }
            catch (IOException ex)
            {
                BT_Util.log(Level.SEVERE, "Cant bind to " + (address == null ? "*" : address) + ":" + port);
            }

            clientHolder = new ArrayList<BT_TelnetListener>();

            is_running = true;

            listenerThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    acceptConnections();
                }
            });
            listenerThread.start();
        }
        catch (Throwable ex)
        {
            BT_Util.log(Level.SEVERE, "Error starting server!\n" + ExceptionUtils.getStackTrace(ex));
        }
    }

    private void acceptConnections()
    {
        while (is_running)
        {
            Socket client = null;
            try
            {
                client = listenerSocket.accept();
                if (client != null)
                {
                    clientHolder.add(new BT_TelnetListener(client, this));

                    BT_Util.log(Level.INFO, "Client connected: " + client.getInetAddress().getHostAddress());

                    Iterator<BT_TelnetListener> listeners = clientHolder.iterator();
                    while (listeners.hasNext())
                    {
                        BT_TelnetListener listener = listeners.next();
                        if (!listener.isAlive())
                        {
                            listeners.remove();
                        }
                    }
                }
            }
            catch (IOException ex)
            {
                is_running = false;
            }
        }

        this.setEnabled(false);
    }

    private void stopServer()
    {
        is_running = false;

        try
        {
            Thread.sleep(250);
        }
        catch (Throwable ex)
        {
        }

        if (clientHolder != null)
        {
            try
            {
                for (BT_TelnetListener listener : clientHolder)
                {
                    listener.killClient();
                }
                clientHolder.clear();
                clientHolder = null;
            }
            catch (Throwable ex)
            {
            }
        }

        if (listenerSocket != null)
        {
            try
            {
                synchronized (listenerSocket)
                {
                    if (listenerSocket != null)
                    {
                        listenerSocket.close();
                    }
                }
                listenerSocket = null;
            }
            catch (Throwable ex)
            {
            }
        }

        try
        {
            Thread.sleep(250);
        }
        catch (Throwable ex)
        {
        }
    }
}
