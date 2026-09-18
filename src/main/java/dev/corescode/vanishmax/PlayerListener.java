package dev.corescode.vanishmax;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player join/quit.
 */
public class PlayerListener implements Listener {

    private final VanishMaxPlugin plugin;
    private final VanishManager vanishManager;

    public PlayerListener(VanishMaxPlugin plugin, VanishManager vanishManager) {
        this.plugin = plugin;
        this.vanishManager = vanishManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (vanishManager.isVanished(player)) {
            event.setJoinMessage(null);
        }

        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> {
            vanishManager.onPlayerJoin(player);
        }, 2L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (vanishManager.isVanished(player)) {
            event.setQuitMessage(null);
        }

        plugin.getServer().getGlobalRegionScheduler().run(plugin, task -> {
            vanishManager.onPlayerQuit(player);
        });
    }
}