package me.StevenLawson.BukkitTelnet.api;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;

public class TelnetCommandEvent extends TelnetEvent implements Cancellable
{
    private boolean cancelled;
    private CommandSender sender;
    private String command;

    public TelnetCommandEvent(CommandSender sender, String command)
    {
        this.cancelled = false;
        this.sender = sender;
        this.command = command;
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel)
    {
        cancelled = cancel;
    }

    public CommandSender getSender()
    {
        return sender;
    }

    public void setSender(CommandSender sender)
    {
        this.sender = sender;
    }

    public String getCommand()
    {
        return command;
    }

    public void setCommand(String command)
    {
        if (command == null)
        {
            command = "";
        }

        this.command = command;
    }
}
