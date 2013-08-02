package me.StevenLawson.BukkitTelnet;

import java.io.*;
import java.net.Socket;
import java.util.Set;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class BT_TelnetListener extends Handler implements CommandSender
{
    private enum FilterMode
    {
        FULL, NONCHAT_ONLY, CHAT_ONLY
    }
    private boolean is_running = false;
    private boolean is_authenticated = false;
    private boolean already_stopped = false;
    private String telnet_username = null;
    private Thread listenThread;
    private Socket clientSocket;
    private BufferedReader instream;
    private BufferedWriter outstream;
    private BukkitTelnet plugin;
    private String client_ip;
    private FilterMode filter_mode = FilterMode.FULL;
    private Formatter formatter = null;
    private static final String COMMAND_REGEX = "[^\\x20-\\x7E]";
    private static final String LOGIN_REGEX = "[^a-zA-Z0-9\\-\\.\\_]";

    public BT_TelnetListener(Socket socket, BukkitTelnet plugin)
    {
        this.is_running = true;
        this.clientSocket = socket;
        this.plugin = plugin;
        if (clientSocket.getInetAddress() != null)
        {
            this.client_ip = clientSocket.getInetAddress().getHostAddress();
        }

        startListener();
    }

    private void startListener()
    {
        listenThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                init();
            }
        });
        listenThread.start();
    }

    private void init()
    {
        try
        {
            instream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outstream = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        }
        catch (Throwable ex)
        {
            is_running = false;
            return;
        }

        //sendTelnetCommand(WILL, LINEMODE);
        //sendTelnetCommand(DO, LINEMODE);
        //sendTelnetCommand(WONT, ECHO);
        //sendTelnetCommand(DO, ECHO);

        writeOut("[BukkitTelnet] - Session Started!\r\n");

        authenticateLoop();
        commandLoop();
        shutdown();
    }

    private void authenticateLoop()
    {
        int tries = 0;

        while (is_running && clientSocket.isConnected() && !is_authenticated)
        {
            try
            {
                //Get Username:
                writeOut("Username: ");
                String username = instream.readLine().replaceAll(LOGIN_REGEX, "").trim();

                if (BT_Util.canBypassPassword(client_ip))
                {
                    writeOut("Skipping password, you are on an authorized IP address.\r\n");
                    is_authenticated = true;
                }
                else
                {
                    //sendTelnetCommand(WILL, ECHO);
                    //sendTelnetCommand(DONT, ECHO);

                    //Get Password:
                    writeOut("Password: ");
                    String password = instream.readLine().replaceAll(LOGIN_REGEX, "").trim();
                    writeOut("\r\n");

                    //sendTelnetCommand(WONT, ECHO);
                    //sendTelnetCommand(DO, ECHO);

                    if (password.equals(plugin.password))
                    {
                        is_authenticated = true;
                    }
                }

                if (is_authenticated)
                {
                    telnet_username = username;
                    writeOut("Logged In as " + getName() + ".\r\n:");
                    BT_Util.log(Level.INFO, client_ip + " logged in as \"" + getName() + "\".");
                    return;
                }
                else
                {
                    try
                    {
                        Thread.sleep(2000);
                    }
                    catch (InterruptedException ex)
                    {
                    }
                    writeOut("Invalid Username or Password.\r\n\r\n");
                }

                if (++tries >= 3)
                {
                    writeOut("Too many failed login attempts.\r\n");
                    return;
                }
            }
            catch (Throwable ex)
            {
                is_running = false;
                telnet_username = null;
                is_authenticated = false;
            }
        }
    }

    private void commandLoop()
    {
        if (!is_running || !is_authenticated)
        {
            return;
        }

        org.bukkit.Bukkit.getLogger().addHandler(this);

        while (is_running && clientSocket.isConnected() && is_authenticated)
        {
            String command = null;
            try
            {
                command = instream.readLine();
            }
            catch (IOException ex)
            {
            }

            if (command != null)
            {
                command = command.replaceAll(COMMAND_REGEX, "").trim();

                if (!command.isEmpty())
                {
                    if (command.toLowerCase().startsWith("telnet"))
                    {
                        if (command.equalsIgnoreCase("telnet.log")) // for legacy use
                        {
                            if (filter_mode == FilterMode.CHAT_ONLY)
                            {
                                filter_mode = FilterMode.FULL;
                                writeOut("Showing full console log.\r\n:");
                            }
                            else
                            {
                                filter_mode = FilterMode.CHAT_ONLY;
                                writeOut("Showing chat log only.\r\n:");
                            }
                        }
                        else if (command.toLowerCase().startsWith("telnet.filter"))
                        {
                            if (command.equalsIgnoreCase("telnet.filter full"))
                            {
                                filter_mode = FilterMode.FULL;
                                writeOut("Showing full console log.\r\n:");
                            }
                            else if (command.equalsIgnoreCase("telnet.filter chat"))
                            {
                                filter_mode = FilterMode.CHAT_ONLY;
                                writeOut("Showing chat log only.\r\n:");
                            }
                            else if (command.equalsIgnoreCase("telnet.filter nonchat"))
                            {
                                filter_mode = FilterMode.NONCHAT_ONLY;
                                writeOut("Showing everything but chat.\r\n:");
                            }
                            else
                            {
                                writeOut("Usage: telnet.filter <full | chat | nonchat>.\r\n:");
                            }
                        }
                        else if (command.equalsIgnoreCase("telnet.exit"))
                        {
                            shutdown();
                        }
                    }
                    else
                    {
                        RemoteServerCommandEvent event = new RemoteServerCommandEvent(this, command);

                        BukkitTelnet.plugin.getServer().getPluginManager().callEvent(event);

                        if (event.getCommand() == null || event.getCommand().equals(""))
                        {
                            continue;
                        }

                        plugin.getServer().dispatchCommand(this, command);
                    }
                }
                else
                {
                    writeOut(":");
                }
            }
        }
    }

    private void shutdown()
    {
        if (already_stopped)
        {
            return;
        }
        already_stopped = true;

        is_running = false;

        BT_Util.log(Level.INFO, "Closing connection: " + client_ip);
        org.bukkit.Bukkit.getLogger().removeHandler(this);

        if (!clientSocket.isClosed())
        {
            writeOut("[" + plugin.getDescription().getName() + "]: Closing connection.");
            try
            {
                clientSocket.close();
            }
            catch (IOException ex)
            {
            }
        }
    }

//    public static final int WILL = 251; //Sender wants to do something.
//    public static final int WONT = 252; //Sender doesn't want to do something.
//    public static final int DO = 253; //Sender wants the other end to do something.
//    public static final int DONT = 254; //Sender wants the other not to do something.
//    
//    public static final int ECHO = 1;
//    public static final int LINEMODE = 34;
//    
//    private void sendTelnetCommand(int command, int option)
//    {
//        writeOut(("" + ((char) 255) + ((char) command) + ((char) option)));
//    }
    private void writeOut(String message)
    {
        if (outstream != null)
        {
            if (clientSocket.isConnected())
            {
                try
                {
                    outstream.write(message);
                    outstream.flush();
                }
                catch (IOException ex)
                {
                    is_running = false;
                }
            }
        }
    }

    public boolean isAlive()
    {
        return is_running;
    }

    public void killClient()
    {
        shutdown();
    }

    @Override
    public Formatter getFormatter()
    {
        if (this.formatter == null)
        {
            this.formatter = super.getFormatter();

            Handler[] handlers = org.bukkit.Bukkit.getLogger().getHandlers();
            for (Handler handler : handlers)
            {
                if (handler.getClass().getName().startsWith("org.bukkit.craftbukkit"))
                {
                    Formatter _formatter = handler.getFormatter();
                    if (_formatter != null)
                    {
                        this.formatter = _formatter;
                        break;
                    }
                }
            }
        }
        return this.formatter;
    }

    @Override
    public void publish(LogRecord record)
    {
        if (record == null)
        {
            return;
        }

        String message = record.getMessage();

        Formatter _formatter = getFormatter();
        if (_formatter != null)
        {
            message = _formatter.formatMessage(record);
        }

        message = ChatColor.stripColor(message);

        if (filter_mode == FilterMode.CHAT_ONLY)
        {
            if (message.startsWith("<") || message.startsWith("[Server:") || message.startsWith("[CONSOLE]<"))
            {
                writeOut(message + "\r\n:");
            }
        }
        else if (filter_mode == FilterMode.NONCHAT_ONLY)
        {
            if (!(message.startsWith("<") || message.startsWith("[Server:") || message.startsWith("[CONSOLE]<")))
            {
                writeOut(message + "\r\n:");
            }
        }
        else
        {
            writeOut(message + "\r\n:");
        }
    }

    @Override
    public void flush()
    {
        if (clientSocket.isConnected())
        {
            try
            {
                outstream.flush();
            }
            catch (IOException ex)
            {
            }
        }
    }

    @Override
    public void close() throws SecurityException
    {
        shutdown();
    }

    @Override
    public void sendMessage(String string)
    {
        writeOut(ChatColor.stripColor(string) + "\r\n:");
    }

    @Override
    public void sendMessage(String[] strings)
    {
        for (String string : strings)
        {
            sendMessage(string);
        }
    }

    @Override
    public Server getServer()
    {
        return plugin.getServer();
    }

    @Override
    public String getName()
    {
        if (telnet_username != null)
        {
            return telnet_username;
        }
        else
        {
            return plugin.getDescription().getName();
        }
    }

    @Override
    public boolean isPermissionSet(String string)
    {
        return true;
    }

    @Override
    public boolean isPermissionSet(Permission prmsn)
    {
        return true;
    }

    @Override
    public boolean hasPermission(String string)
    {
        return true;
    }

    @Override
    public boolean hasPermission(Permission prmsn)
    {
        return true;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln)
    {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin)
    {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln, int i)
    {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int i)
    {
        return null;
    }

    @Override
    public void removeAttachment(PermissionAttachment pa)
    {
    }

    @Override
    public void recalculatePermissions()
    {
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        return null;
    }

    @Override
    public boolean isOp()
    {
        return true;
    }

    @Override
    public void setOp(boolean bln)
    {
    }
}
