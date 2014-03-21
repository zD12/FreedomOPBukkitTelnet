package me.StevenLawson.BukkitTelnet.api;

import org.bukkit.event.Cancellable;

public class TelnetPreLoginEvent extends TelnetEvent implements Cancellable
{
    private boolean cancelled = false;
    //
    private String name;
    private final String ip;
    private boolean bypassPassword;

    public TelnetPreLoginEvent(String ip, String name, boolean bypassPassword)
    {
        this.ip = ip;
        this.name = name;
        this.bypassPassword = bypassPassword;
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

    public void setName(String name)
    {
        this.name = name;
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
