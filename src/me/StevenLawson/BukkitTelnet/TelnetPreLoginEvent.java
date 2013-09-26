package me.StevenLawson.BukkitTelnet;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TelnetPreLoginEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    //
    private final String ip;
    private final String name;
    private boolean bypassPassword;
    private boolean cancelled;

    public TelnetPreLoginEvent(String ip, String name, boolean bypassPassword)
    {
        this.ip = ip;
        this.name = name;
        this.bypassPassword = bypassPassword;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
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

    public String getIp()
    {
        return ip;
    }

    public String getName()
    {
        return name;
    }

    public boolean canBypassPassword()
    {
        return bypassPassword;
    }

    public void setBypassPassword(boolean bypassPassword)
    {
        this.bypassPassword = bypassPassword;
    }
}
