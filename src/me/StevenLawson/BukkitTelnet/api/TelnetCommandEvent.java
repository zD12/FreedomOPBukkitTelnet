package me.StevenLawson.BukkitTelnet.api;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TelnetCommandEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    //
    private CommandSender sender;
    private String command;

    public TelnetCommandEvent(CommandSender sender, String command)
    {
        this.sender = sender;
        this.command = command;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    //public static HandlerList getHandlerList()
    //{
    //    return handlers;
    //}
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
