package com.bekvon.bukkit.mctelnet;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.InetAddress;
import java.util.Iterator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class MCTelnet extends JavaPlugin
{
    private static final String CONFIG_FILE = "config.yml";
    
    private static final Logger log = Logger.getLogger("Minecraft");
    
    private ServerSocket listenerSocket = null;
    private ArrayList<TelnetListener> clientHolder;
    private Thread listenerThread = null;
    private boolean is_running = false;
    private int port = 8765;
    private InetAddress listenAddress = null;
    
    protected String password = null;
    
    @Override
    public void onEnable()
    {
        try
        {
            log.log(Level.INFO, "[" + getDescription().getName() + "]: Enabled - Version " + this.getDescription().getVersion() + " by bekvon, revamped by Madgeek1450.");
            log.log(Level.INFO, "[" + getDescription().getName() + "]: Starting server.");
            
            TelnetUtil.createDefaultConfiguration(CONFIG_FILE, this, getFile());
            FileConfiguration config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), CONFIG_FILE));
            
            password = config.getString("password", null);
            if (password == null)
            {
                log.log(Level.SEVERE, "[" + getDescription().getName() + "]: Password is not defined in config file! Can't start server!");
                return;
            }

            port = config.getInt("port", port);

            String address = config.getString("address", null);
            if (address != null)
            {
                try
                {
                    listenAddress = null;
                    listenAddress = InetAddress.getByName(address);
                }
                catch (UnknownHostException ex)
                {
                    log.log(Level.SEVERE, "[" + getDescription().getName() + "]: Unknown host: " + address);
                    return;
                }
            }
            else
            {
                address = "*";
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

                log.log(Level.INFO, "[" + getDescription().getName() + "]: Server started on " + host_ip + ":" + port);
            }
            catch (IOException ex)
            {
                log.log(Level.SEVERE, "[" + getDescription().getName() + "]: Cant bind to " + address + ":" + port);
            }

            clientHolder = new ArrayList<TelnetListener>();
            
            is_running = true;

            listenerThread = new Thread(new Runnable()
            {
                public void run()
                {
                    acceptConnections();
                }
            });
            listenerThread.start();
        }
        catch (Throwable ex)
        {
            log.log(Level.SEVERE, "[" + getDescription().getName() + "]: Error starting plugin!", ex);
        }
    }
    
    @Override
    public void onDisable()
    {
        is_running = false;
        
        log.log(Level.INFO, "[" + getDescription().getName() + "]: Stopping server.");
        
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
                for (TelnetListener listener : clientHolder)
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
            log.log(Level.SEVERE, null, ex);
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
                    clientHolder.add(new TelnetListener(client, this));

                    log.info("[" + getDescription().getName() + "]: Client connected: " + client.getInetAddress().getHostAddress());

                    Iterator<TelnetListener> listeners = clientHolder.iterator();
                    while (listeners.hasNext())
                    {
                        TelnetListener listener = listeners.next();
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
}
