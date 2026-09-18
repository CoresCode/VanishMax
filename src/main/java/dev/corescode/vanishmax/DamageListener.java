package dev.corescode.vanishmax;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageListener implements Listener {

    private final VanishManager vanishManager;

    public DamageListener(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.KILL) return;

        if (!(event.getEntity() instanceof Player player)) return;

        if (vanishManager.isVanished(player)) {
            event.setCancelled(true);
        }
    }
}