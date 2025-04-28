package com.igngecko.zyphercrates.listeners; // Changed package name

import com.igngecko.zyphercrates.ZypherCrates; // Changed package/class name
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

    private final ZypherCrates plugin; // Changed type

    public PlayerJoinListener(ZypherCrates plugin) { // Changed type
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Ensure player data is loaded/initialized in memory when they join
        plugin.getPlayerDataManager().loadPlayer(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
         // No action needed here by default, rely on auto-save/onDisable save.
         // If unloading is desired for memory:
         // plugin.getPlayerDataManager().unloadPlayer(player.getUniqueId());
    }
}
