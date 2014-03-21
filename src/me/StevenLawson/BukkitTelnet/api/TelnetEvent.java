package me.StevenLawson.BukkitTelnet.api;

import org.bukkit.event.HandlerList;
import org.bukkit.event.server.ServerEvent;

public abstract class TelnetEvent extends ServerEvent
{
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}
