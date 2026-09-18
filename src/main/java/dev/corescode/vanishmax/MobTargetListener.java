package dev.corescode.vanishmax;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

/**
 * Prevents mobs from targeting vanished players.
 *
 * Handles two cases:
 *   1. A mob tries to target a vanished player → cancel the event
 *   2. A mob is already targeting someone and re-evaluates → cancel if target is vanished
 *
 * This covers both hostile mobs (zombies, skeletons, etc.) and
 * neutral mobs (wolves, iron golems, etc.).
 *
 * In Folia, EntityTargetEvent fires on the region thread of the mob,
 * which is the correct thread to cancel it on — no extra scheduling needed.
 */
public class MobTargetListener implements Listener {

    private final VanishManager vanishManager;

    public MobTargetListener(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        LivingEntity target = event.getTarget();

        // Only care about players being targeted
        if (!(target instanceof Player player)) return;

        // If the player is vanished, cancel the targeting
        if (vanishManager.isVanished(player)) {
            event.setCancelled(true);
            event.setTarget(null);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityTargetBase(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;

        if (vanishManager.isVanished(player)) {
            event.setCancelled(true);
            event.setTarget(null);
        }
    }
}