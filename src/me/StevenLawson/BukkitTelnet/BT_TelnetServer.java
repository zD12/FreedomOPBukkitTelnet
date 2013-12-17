package me.StevenLawson.BukkitTelnet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class BT_TelnetServer
{
    private String password;
    private ServerListener serverListener = null;

    private BT_TelnetServer()
    {
    }

    public void startServer()
    {
        this.stopServer();

        final BT_Config.SimpleConfigEntries configEntries = BT_Config.getInstance().getConfigEntries();

        final String address = configEntries.getAddress();
        final int port = configEntries.getPort();

        this.password = configEntries.getPassword();

        if (this.password == null)
        {
            BT_Log.warning("Telnet password is not defined in config file. Can't start server.");
            return;
        }

        InetAddress hostAddress = null;

        if (address != null)
        {
            try
            {
                hostAddress = InetAddress.getByName(address);
            }
            catch (UnknownHostException ex)
            {
                BT_Log.severe(ex);
            }
        }

        ServerSocket serverSocket = null;

        try
        {
            if (hostAddress == null)
            {
                serverSocket = new ServerSocket(port);
            }
            else
            {
                serverSocket = new ServerSocket(port, 50, hostAddress);
            }

            String hostIP = serverSocket.getInetAddress().getHostAddress();
            if (hostIP.equals("0.0.0.0"))
            {
                hostIP = "*";
            }

            BT_Log.info("Server started on " + hostIP + ":" + serverSocket.getLocalPort());
        }
        catch (IOException ex)
        {
            BT_Log.severe("Cant bind to " + (hostAddress == null ? "*" : hostAddress) + ":" + port);
            BT_Log.severe(ex);
        }

        if (serverSocket != null)
        {
            (this.serverListener = new ServerListener(serverSocket)).start();
        }
    }

    public void stopServer()
    {
        if (this.serverListener != null)
        {
            this.serverListener.stopServer();
        }
    }

    public String getPassword()
    {
        return this.password;
    }

    private static class ServerListener extends Thread
    {
        private final ServerSocket serverSocket;
        private final List<BT_ClientSession> clientSessions = Collections.synchronizedList(new ArrayList<BT_ClientSession>());

        public ServerListener(ServerSocket serverSocket)
        {
            this.serverSocket = serverSocket;
        }

        @Override
        public void run()
        {
            try
            {
                while (!this.serverSocket.isClosed())
                {
                    synchronized (clientSessions)
                    {
                        final BT_ClientSession clientSession = new BT_ClientSession(this.serverSocket.accept());
                        clientSessions.add(clientSession);
                        clientSession.start();

                        final Iterator<BT_ClientSession> it = clientSessions.iterator();
                        while (it.hasNext())
                        {
                            if (!it.next().isConnected())
                            {
                                it.remove();
                            }
                        }
                    }
                }
            }
            catch (IOException ex)
            {
            }
        }

        public void stopServer()
        {
            try
            {
                this.serverSocket.close();
            }
            catch (IOException ex)
            {
            }

            synchronized (clientSessions)
            {
                final Iterator<BT_ClientSession> it = clientSessions.iterator();
                while (it.hasNext())
                {
                    it.next().terminateSession();
                    it.remove();
                }
            }
        }
    }

    public static BT_TelnetServer getInstance()
    {
        return BT_TelnetServerHolder.INSTANCE;
    }

    private static class BT_TelnetServerHolder
    {
        private static final BT_TelnetServer INSTANCE = new BT_TelnetServer();
    }
}
