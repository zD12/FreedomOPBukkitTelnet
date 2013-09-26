package me.StevenLawson.BukkitTelnet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.scheduler.BukkitRunnable;

public final class BT_ClientSession extends Thread
{
    private static final Pattern NONASCII_FILTER = Pattern.compile("[^\\x20-\\x7E]");
    private static final Pattern LOGIN_NAME_FILTER = Pattern.compile("[^a-zA-Z0-9\\-\\.\\_]");
    //
    private final Socket clientSocket;
    private final String clientAddress;
    //
    private SessionLogHandler sessionLogHandler;
    private SessionCommandSender sessionCommandSender;
    private BufferedWriter writer;
    private BufferedReader reader;
    private String userName;
    private boolean hasTerminated = false;

    public BT_ClientSession(Socket clientSocket)
    {
        this.clientSocket = clientSocket;
        this.clientAddress = this.clientSocket.getInetAddress().getHostAddress();
        this.userName = this.clientAddress;

        BT_Log.info("Client connected: " + this.clientAddress);
    }

    public boolean isConnected()
    {
        synchronized (this.clientSocket)
        {
            return !this.clientSocket.isClosed();
        }
    }

    public void terminateSession()
    {
        if (this.hasTerminated)
        {
            return;
        }

        this.hasTerminated = true;

        BT_Log.info("Closing connection: " + this.clientAddress);

        if (this.sessionLogHandler != null)
        {
            BT_Log.getLogger().removeHandler(this.sessionLogHandler);
        }

        synchronized (this.clientSocket)
        {
            if (this.clientSocket != null)
            {
                try
                {
                    writeOutFormatted("Closing connection.");
                    this.clientSocket.close();
                }
                catch (Exception ex)
                {
                }
            }
        }
    }

    public String getUserName()
    {
        return this.userName;
    }

    public void writeOut(String message)
    {
        if (this.writer != null && this.isConnected())
        {
            try
            {
                this.writer.write(ChatColor.stripColor(message));
                this.writer.flush();
            }
            catch (IOException ex)
            {
            }
        }
    }

    public void writeOutFormatted(String message)
    {
        writeOut("[BukkitTelnet (Console)]: " + message + "\r\n:");
    }

    public void flush()
    {
        if (this.writer != null && this.isConnected())
        {
            try
            {
                this.writer.flush();
            }
            catch (IOException ex)
            {
            }
        }
    }

    public void sendBukkitCommand(final String _command)
    {
        final CommandSender commandSender = getCommandSender();

        try
        {
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    final Server server = Bukkit.getServer();
                    if (server != null)
                    {
                        final RemoteServerCommandEvent event = new RemoteServerCommandEvent(commandSender, _command);
                        server.getPluginManager().callEvent(event);
                        String command = event.getCommand();
                        if (command != null && !command.isEmpty())
                        {
                            server.dispatchCommand(commandSender, command);
                        }
                    }
                }
            }.runTask(BukkitTelnet.getPlugin());
        }
        catch (Exception ex)
        {
            BT_Log.severe(ex);
        }
    }

    public CommandSender getCommandSender()
    {
        if (this.sessionCommandSender == null)
        {
            this.sessionCommandSender = new SessionCommandSender(this);
        }
        return this.sessionCommandSender;
    }

    @Override
    public void run()
    {
        try
        {
            synchronized (this.clientSocket)
            {
                this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                this.writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            }
        }
        catch (IOException ex)
        {
            BT_Log.severe(ex);
        }

        if (this.reader == null || this.writer == null)
        {
            terminateSession();
            return;
        }

        writeOut(":");

        writeOutFormatted("Session Started.");

        authenticateSession();

        sessionMainLoop();

        terminateSession();
    }

    private void authenticateSession()
    {
        if (this.hasTerminated)
        {
            return;
        }

        try
        {
            writeOutFormatted("Authentication not yet implemented...");
        }
        catch (Exception ex)
        {
            BT_Log.severe(ex);
        }
    }

    private void sessionMainLoop()
    {
        if (this.hasTerminated)
        {
            return;
        }

        try
        {
            BT_Log.getLogger().addHandler(this.sessionLogHandler = new SessionLogHandler(this));

            while (this.isConnected())
            {
                String command = null;
                try
                {
                    command = this.reader.readLine();
                }
                catch (IOException ex)
                {
                }

                writeOut(":");

                if (command != null && !(command = stripNonAscii(command).trim()).isEmpty())
                {
                    sendBukkitCommand(command);
                }
            }
        }
        catch (Exception ex)
        {
            BT_Log.severe(ex);
        }
    }

    private static String stripNonAscii(String string)
    {
        return NONASCII_FILTER.matcher(string).replaceAll("");
    }

    private static class SessionLogHandler extends BT_Handler
    {
        private final BT_ClientSession session;

        public SessionLogHandler(BT_ClientSession session)
        {
            this.session = session;
        }

        @Override
        public void writeOut(String string)
        {
            this.session.writeOut(string);
        }

        @Override
        public void flush()
        {
            this.session.flush();
        }

        @Override
        public void close() throws SecurityException
        {
            this.session.terminateSession();
        }
    }

    private static class SessionCommandSender extends BT_CommandSender
    {
        private final BT_ClientSession session;

        public SessionCommandSender(BT_ClientSession session)
        {
            this.session = session;
        }

        @Override
        public void sendMessage(String message)
        {
            if (this.session.sessionLogHandler != null)
            {
                this.session.sessionLogHandler.writeOut(message + "\r\n:");
            }
        }

        @Override
        public String getName()
        {
            return this.session.getUserName();
        }
    }
}
