package com.bekvon.bukkit.mctelnet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class TelnetListener extends Handler implements CommandSender
{
    private static final Logger log = Logger.getLogger("Minecraft");
    private boolean is_running = false;
    private boolean is_authenticated = false;
    private boolean already_stopped = false;
    private String telnet_username = null;
    private Thread listenThread;
    private Socket clientSocket;
    private BufferedReader instream;
    private BufferedWriter outstream;
    private MCTelnet plugin;
    private String client_ip;
    private boolean show_full_log = true;
    private static final String COMMAND_REGEX = "[^\\x20-\\x7E]";
    private static final String LOGIN_REGEX = "[^a-zA-Z0-9\\-\\.\\_]";

    public TelnetListener(Socket socket, MCTelnet plugin)
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

        writeOut("[MCTelnet] - Session Started!\r\n");

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

                if (TelnetUtil.canBypassPassword(client_ip, plugin))
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

        Logger.getLogger("Minecraft").addHandler(this);

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
                        if (command.equalsIgnoreCase("telnet.log"))
                        {
                            show_full_log = !show_full_log;
                            if (show_full_log)
                            {
                                writeOut("Showing full console log.\r\n:");
                            }
                            else
                            {
                                writeOut("Showing chat log only.\r\n:");
                            }
                        }
                    }
                    else
                    {
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

        log.log(Level.INFO, "[" + plugin.getDescription().getName() + "]: Closing connection: " + client_ip);
        Logger.getLogger("Minecraft").removeHandler(this);

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
    public void publish(LogRecord record)
    {
        String message = ChatColor.stripColor(record.getMessage());
        
        if (show_full_log || message.startsWith("<") || message.startsWith("[Server:") || message.startsWith("[CONSOLE]<"))
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

    public void sendMessage(String string)
    {
        writeOut(ChatColor.stripColor(string) + "\r\n:");
    }

    public Server getServer()
    {
        return plugin.getServer();
    }

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

    public boolean isPermissionSet(String string)
    {
        return true;
    }

    public boolean isPermissionSet(Permission prmsn)
    {
        return true;
    }

    public boolean hasPermission(String string)
    {
        return true;
    }

    public boolean hasPermission(Permission prmsn)
    {
        return true;
    }

    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln)
    {
        return null;
    }

    public PermissionAttachment addAttachment(Plugin plugin)
    {
        return null;
    }

    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln, int i)
    {
        return null;
    }

    public PermissionAttachment addAttachment(Plugin plugin, int i)
    {
        return null;
    }

    public void removeAttachment(PermissionAttachment pa)
    {
        return;
    }

    public void recalculatePermissions()
    {
        return;
    }

    public Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        return null;
    }

    public boolean isOp()
    {
        return true;
    }

    public void setOp(boolean bln)
    {
        return;
    }
}
