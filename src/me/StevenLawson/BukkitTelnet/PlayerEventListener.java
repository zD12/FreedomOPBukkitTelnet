package me.StevenLawson.BukkitTelnet;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class PlayerEventListener implements Listener
{
    public PlayerEventListener()
    {
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        triggerPlayerListUpdates();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event)
    {
        triggerPlayerListUpdates();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        triggerPlayerListUpdates();
    }

    private static BukkitTask updateTask = null;

    private static void triggerPlayerListUpdates()
    {
        if (updateTask != null)
        {
            updateTask.cancel();
        }

        updateTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                final SocketListener socketListener = TelnetServer.getInstance().getSocketListener();
                if (socketListener != null)
                {
                    socketListener.triggerPlayerListUpdates(generatePlayerList());
                }
            }
        }.runTaskLater(BukkitTelnet.plugin, 20L * 2L);
    }

    @SuppressWarnings("unchecked")
    private static String generatePlayerList()
    {
        final JSONArray players = new JSONArray();

        for (final Player player : Bukkit.getServer().getOnlinePlayers())
        {
            final HashMap<String, String> info = new HashMap<String, String>();

            info.put("name", player.getName());
            info.put("ip", player.getAddress().getAddress().getHostAddress());

            players.add(info);
        }

        final JSONObject response = new JSONObject();
        response.put("players", players);

        return response.toJSONString();
    }
}
