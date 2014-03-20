package me.StevenLawson.BukkitTelnet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import me.StevenLawson.BukkitTelnet.TelnetConfig.SimpleConfigEntries;

public class TelnetServer
{
    private SocketListener socketListener;

    private TelnetServer()
    {
    }

    public void startServer()
    {
        // If the server is running, stop it
        stopServer();

        final SimpleConfigEntries config = TelnetConfig.getInstance().getConfigEntries();

        // Server address, optional.
        final InetAddress hostAddress;

        final String address = config.getAddress();
        if (address != null)
        {
            try
            {
                hostAddress = InetAddress.getByName(address);
            }
            catch (UnknownHostException ex)
            {
                TelnetLogger.severe("Cannot start server - Invalid address: " + config.getAddress());
                TelnetLogger.severe(ex);
                return;
            }
        }
        else
        {
            hostAddress = null;
        }

        // Server socket
        ServerSocket serversocket;

        try
        {
            if (hostAddress == null)
            {
                serversocket = new ServerSocket(config.getPort());
            }
            else
            {
                serversocket = new ServerSocket(config.getPort(), 50, hostAddress);
            }
        }
        catch (IOException ex)
        {
            TelnetLogger.severe("Cannot start server - " + "Cant bind to " + (hostAddress == null ? "*" : hostAddress) + ":" + config.getPort());
            TelnetLogger.severe(ex);
            return;
        }

        socketListener = new SocketListener(serversocket);
        socketListener.start();

        final String host = serversocket.getInetAddress().getHostAddress().replace("0.0.0.0", "*");
        TelnetLogger.info("Server started on " + host + ":" + serversocket.getLocalPort());
    }

    public void stopServer()
    {
        if (socketListener == null)
        {
            return;
        }

        socketListener.stopServer();
    }

    public static TelnetServer getInstance()
    {
        return BT_TelnetServerHolder.INSTANCE;
    }

    private static class BT_TelnetServerHolder
    {
        private static final TelnetServer INSTANCE = new TelnetServer();
    }
}
