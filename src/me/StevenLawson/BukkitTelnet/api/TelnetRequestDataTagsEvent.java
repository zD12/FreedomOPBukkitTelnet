package me.StevenLawson.BukkitTelnet.api;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TelnetRequestDataTagsEvent extends TelnetEvent
{
    private final Map<Player, Map<String, Object>> dataTags = new HashMap<Player, Map<String, Object>>();

    public TelnetRequestDataTagsEvent()
    {
        for (final Player player : Bukkit.getServer().getOnlinePlayers())
        {
            dataTags.put(player, new HashMap<String, Object>());
        }
    }

    public Map<Player, Map<String, Object>> getDataTags()
    {
        return dataTags;
    }
}
