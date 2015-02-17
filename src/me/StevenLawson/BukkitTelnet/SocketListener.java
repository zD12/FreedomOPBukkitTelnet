package me.StevenLawson.BukkitTelnet;

import me.StevenLawson.BukkitTelnet.session.ClientSession;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SocketListener extends Thread
{
    private final ServerSocket serverSocket;
    private final List<ClientSession> clientSessions;

    public SocketListener(ServerSocket serverSocket)
    {
        this.serverSocket = serverSocket;
        this.clientSessions = new ArrayList<ClientSession>();
    }

    @Override
    public void run()
    {
        while (!serverSocket.isClosed())
        {
            final Socket clientSocket;

            try
            {
                clientSocket = serverSocket.accept();
            }
            catch (IOException ex)
            {
                continue;
            }

            final ClientSession clientSession = new ClientSession(clientSocket);
            clientSessions.add(clientSession);
            clientSession.start();

            removeDisconnected();
        }
    }

    private void removeDisconnected()
    {
        final Iterator<ClientSession> it = clientSessions.iterator();

        while (it.hasNext())
        {
            final ClientSession session = it.next();

            if (!session.syncIsConnected())
            {
                TelnetLogAppender.getInstance().removeSession(session);
                it.remove();
            }
        }
    }

    public void triggerPlayerListUpdates(final String playerListData)
    {
        final Iterator<ClientSession> it = clientSessions.iterator();

        while (it.hasNext())
        {
            final ClientSession session = it.next();
            if (session != null)
            {
                session.syncTriggerPlayerListUpdate(playerListData);
            }
        }
    }

    public void stopServer()
    {
        try
        {
            serverSocket.close();
        }
        catch (IOException ex)
        {
            TelnetLogger.severe(ex);
        }

        for (ClientSession session : clientSessions)
        {
            session.syncTerminateSession();
        }

        clientSessions.clear();

    }
}
