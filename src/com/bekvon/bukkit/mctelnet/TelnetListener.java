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
import net.minecraft.server.MinecraftServer;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.ConfigurationNode;

public class TelnetListener extends Handler implements CommandSender
{
	private boolean run;
	private boolean isAuth;
	private String authUser;
	private Thread listenThread;
	Socket clientSocket;
	MinecraftServer mcserv;
	BufferedReader instream;
	BufferedWriter outstream;
	MCTelnet parent;
	String ip;
	String passRegex = "[^a-zA-Z0-9\\-\\.\\_]";
	String commandRegex = "[^a-zA-Z0-9 \\-\\.\\_\\\"]";

	public TelnetListener(Socket inSock, MCTelnet iparent)
	{
		run = true;
		clientSocket = inSock;
		parent = iparent;
		passRegex = parent.getConfiguration().getString("passwordRegex", passRegex);
		commandRegex = parent.getConfiguration().getString("commandRegex", commandRegex);
		ip = clientSocket.getInetAddress().toString();
		listenThread = new Thread(new Runnable()
		{
			public void run()
			{
				mainLoop();
			}
		});
		listenThread.start();
	}

	private void mainLoop()
	{
		try
		{
			instream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			outstream = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			//sendTelnetCommand(251,3);
			//sendTelnetCommand(253,3);
			sendTelnetCommand(251, 34);
			sendTelnetCommand(253, 34);
			sendTelnetCommand(252, 1);
			sendTelnetCommand(253, 1);
			outstream.write("[MCTelnet] - Session Started!\r\n");
			outstream.flush();
		}
		catch (IOException ex)
		{
			Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
			run = false;
		}
		if (!clientSocket.getInetAddress().isLoopbackAddress() || !parent.getConfiguration().getBoolean("allowAuthlessLocalhost", false))
		{
			authenticateLoop();
		}
		else
		{
			isAuth = true;
			authUser = parent.getConfiguration().getString("rootUser");
		}
		commandLoop();
		shutdown();
	}

	private void authenticateLoop()
	{
		int retrys = 0;
		while (run && clientSocket.isConnected() && isAuth == false)
		{
			try
			{
				outstream.write("Username:");
				outstream.flush();
				String username = instream.readLine().replaceAll(passRegex, "");
				sendTelnetCommand(251, 1);
				sendTelnetCommand(254, 1);
				outstream.write("Password:");
				outstream.flush();
				String pw = instream.readLine().replaceAll(passRegex, "");
				outstream.write("\r\n");
				sendTelnetCommand(252, 1);
				sendTelnetCommand(253, 1);
				ConfigurationNode parentnode = parent.getConfiguration().getNode("users");
				if (parentnode != null)
				{
					ConfigurationNode usernode = parentnode.getNode(username);
					if (usernode != null)
					{
						String userpw = usernode.getString("password");
						if (usernode.getBoolean("passEncrypted", false))
						{
							pw = MCTelnet.hashPassword(pw);
						}
						if (pw.equals(userpw))
						{
							authUser = username;
							isAuth = true;
						}
					}
				}
				if (isAuth)
				{
					outstream.write("Logged In as " + authUser + "!\r\n:");
					outstream.flush();
				}
				else
				{
					Thread.sleep(2000);
					outstream.write("Invalid Username or Password!\r\n\r\n");
					outstream.flush();
				}
				retrys++;
				if (retrys == 3 && isAuth == false)
				{
					try
					{
						outstream.write("Too many failed login attempts!");
						outstream.flush();
					}
					catch (Exception ex)
					{
					}
					return;
				}
			}
			catch (Exception ex)
			{
				run = false;
				authUser = null;
				isAuth = false;
			}
		}
	}

	private void commandLoop()
	{
		try
		{
			if (isAuth)
			{
				String[] validCommands = new String[0];

				Logger.getLogger("Minecraft").addHandler(this);

				while (run && clientSocket.isConnected() && isAuth)
				{
					String command = "";
					command = instream.readLine().replaceAll(commandRegex, "");
					if (command.equals("exit"))
					{
						run = false;
						clientSocket.close();
						return;
					}
					if (!clientSocket.isClosed())
					{
						parent.getServer().dispatchCommand(this, command);
						System.out.println("[MCTelnet] " + authUser + " issued command: " + command);
					}
				}
			}
		}
		catch (Exception ex)
		{
		}
	}

	public boolean isAlive()
	{
		return run;
	}

	public void killClient()
	{
		try
		{
			run = false;
			outstream.write("[MCTelnet] - Closing Connection!");
			clientSocket.close();
		}
		catch (IOException ex)
		{
		}
	}

	private void shutdown()
	{
		try
		{
			run = false;
			Logger.getLogger("Minecraft").removeHandler(this);
			Logger.getLogger("Minecraft").log(Level.INFO, "[MCTelnet] Closing connection: " + ip);
			if (!clientSocket.isClosed())
			{
				outstream.write("[MCTelnet] - Closing Connection!");
				clientSocket.close();
			}
			mcserv = null;
			parent = null;
		}
		catch (Exception ex)
		{
			Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
			run = false;
		}
	}

	@Override
	public void publish(LogRecord record)
	{
		try
		{
			if (!clientSocket.isClosed())
			{
				outstream.write(ChatColor.stripColor(record.getMessage()) + "\r\n:");
				outstream.flush();
			}
		}
		catch (IOException ex)
		{
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

	public void sendMessage(String string)
	{
		if (clientSocket.isConnected())
		{
			try
			{
				string = ChatColor.stripColor(string);
				outstream.write(string + "\r\n:");
				outstream.flush();
			}
			catch (IOException ex)
			{
			}
		}
	}

	public boolean isOp()
	{
		if (authUser.equalsIgnoreCase("console"))
		{
			return true;
		}
		if (parent.getConfiguration().getBoolean("allowOPsAll", false))
		{
			return parent.getServer().getPlayer(authUser).isOp();
		}
		return false;
	}

	public boolean isPlayer()
	{
		return false;
	}

	public Server getServer()
	{
		return parent.getServer();
	}

	@Override
	public void close() throws SecurityException
	{
		shutdown();
	}

	private void sendTelnetCommand(int command, int option)
	{
		if (clientSocket.isConnected())
		{
			try
			{
				String tcmd = ("" + ((char) 255) + ((char) command) + ((char) option));
				outstream.write(tcmd);
				outstream.flush();
			}
			catch (IOException ex)
			{
			}
		}
	}

	public String getName()
	{
		return authUser;
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

	public void setOp(boolean bln)
	{
		return;
	}
}
