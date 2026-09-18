package dev.corescode.vanishmax;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class VanishManager {

    private final VanishMaxPlugin plugin;
    private final LevelManager levelManager;
    private final Logger log;

    private final Set<UUID> vanishedPlayers = ConcurrentHashMap.newKeySet();

    // Players who had fire resistance BEFORE vanishing — don't strip it on unvanish
    private final Set<UUID> hadFireResistanceBefore = ConcurrentHashMap.newKeySet();

    // Players who had night vision BEFORE vanishing — don't strip it on unvanish
    private final Set<UUID> hadNightVisionBefore = ConcurrentHashMap.newKeySet();

    private static final int INFINITE = Integer.MAX_VALUE;

    private File dataFile;
    private YamlConfiguration dataConfig;

    public VanishManager(VanishMaxPlugin plugin, LevelManager levelManager) {
        this.plugin       = plugin;
        this.levelManager = levelManager;
        this.log          = plugin.getLogger();
    }

    // Persistence
    public void loadData() {
        dataFile = new File(plugin.getDataFolder(), "vanished.yml");
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                log.severe("Could not create vanished.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        List<String> saved = dataConfig.getStringList("vanished");
        for (String uuidStr : saved) {
            try {
                vanishedPlayers.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException ignored) {}
        }
        log.info("Loaded " + vanishedPlayers.size() + " persisted vanish state(s).");
    }

    private void saveData() {
        List<String> uuids = new ArrayList<>();
        for (UUID uuid : vanishedPlayers) uuids.add(uuid.toString());
        dataConfig.set("vanished", uuids);
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            log.severe("Could not save vanished.yml: " + e.getMessage());
        }
    }

    // Toggle
    public void toggleVanish(Player player) {
        if (vanishedPlayers.contains(player.getUniqueId())) {
            unvanish(player);
        } else {
            vanish(player);
        }
    }

    private void vanish(Player player) {
        vanishedPlayers.add(player.getUniqueId());
        saveData();

        // Message — safe on any thread
        String raw = plugin.getConfig().getString("messages.vanish-on", "&aYou are now in vanish mode.");
        player.sendMessage(colorize(raw));

        VanishLevel playerLevel = levelManager.getLevelOf(player);

        // Track if they already had fire resistance
        if (player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
            hadFireResistanceBefore.add(player.getUniqueId());
        } else {
            hadFireResistanceBefore.remove(player.getUniqueId());
        }
        // Track if they already had night vision before
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            hadNightVisionBefore.add(player.getUniqueId());
        } else {
            hadNightVisionBefore.remove(player.getUniqueId());
        }

        // Hide/show — Global Region Scheduler (no player entity context needed)
        Bukkit.getGlobalRegionScheduler().run(plugin, task -> {
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other.getUniqueId().equals(player.getUniqueId())) continue;
                updateVisibilityOf(player, playerLevel, other);
            }
        });

        player.getScheduler().run(plugin, task -> {
            if (!player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.FIRE_RESISTANCE,
                        INFINITE, // infinite duration
                        0,        // amplifier: Fire Resistance I
                        true,     // ambient
                        false,    // no particles
                        false     // no HUD icon
                ));
            }

            if (!player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.NIGHT_VISION,
                        INFINITE,
                         0,
                        true,
                        false,
                        false
                ));
            }
            
        }, null);
    }


    private void unvanish(Player player) {
        vanishedPlayers.remove(player.getUniqueId());
        saveData();

        String raw = plugin.getConfig().getString("messages.vanish-off", "&aYou are no longer in vanish mode.");
        player.sendMessage(colorize(raw));

        Bukkit.getGlobalRegionScheduler().run(plugin, task -> {
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other.getUniqueId().equals(player.getUniqueId())) continue;
                other.showPlayer(plugin, player);
            }
        });

        player.getScheduler().run(plugin, task -> {
            // Only remove if WE gave it — don't strip pre-existing fire resistance
            if (!hadFireResistanceBefore.contains(player.getUniqueId())) {
                player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
            }
            hadFireResistanceBefore.remove(player.getUniqueId());

            // Only remove night vision if WE gave it
            if (!hadNightVisionBefore.contains(player.getUniqueId())) {
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
            hadNightVisionBefore.remove(player.getUniqueId());
        }, null);
    }

    // Join / Quit
    public void onPlayerJoin(Player joining) {
        if (vanishedPlayers.contains(joining.getUniqueId())) {
            VanishLevel joiningLevel = levelManager.getLevelOf(joining);

            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other.getUniqueId().equals(joining.getUniqueId())) continue;
                updateVisibilityOf(joining, joiningLevel, other);
            }

            // Re-apply fire resistance on rejoin — Entity Scheduler
            joining.getScheduler().run(plugin, task -> {
                if (!joining.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                    joining.addPotionEffect(new PotionEffect(
                            PotionEffectType.FIRE_RESISTANCE, INFINITE, 0, true, false, false));
                }
                if (!joining.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                    joining.addPotionEffect(new PotionEffect(
                            PotionEffectType.NIGHT_VISION, INFINITE, 0, true, false, false));
                }
            }, null);

            joining.sendMessage(colorize("&7[Vanish Max] &eYou are still in vanish mode from your last session."));
        }

        // Hide already-vanished players from/to the new joiner
        for (UUID vanishedUUID : vanishedPlayers) {
            if (vanishedUUID.equals(joining.getUniqueId())) continue;
            Player vanishedPlayer = Bukkit.getPlayer(vanishedUUID);
            if (vanishedPlayer == null || !vanishedPlayer.isOnline()) continue;
            VanishLevel vanishedLevel = levelManager.getLevelOf(vanishedPlayer);
            updateVisibilityOf(vanishedPlayer, vanishedLevel, joining);
        }
    }

    public void onPlayerQuit(Player player) {
        hadFireResistanceBefore.remove(player.getUniqueId());
        hadNightVisionBefore.remove(player.getUniqueId());
    }

    // Visibility
    private void updateVisibilityOf(Player vanishedPlayer, VanishLevel vanishedLevel, Player observer) {
        if (vanishedLevel == null) {
            observer.hidePlayer(plugin, vanishedPlayer);
            return;
        }
        LevelManager.VisibilityResult result = levelManager.getVisibility(observer, vanishedLevel);
        switch (result) {
            case VISIBLE   -> observer.showPlayer(plugin, vanishedPlayer);
            case INVISIBLE -> observer.hidePlayer(plugin, vanishedPlayer);
        }
    }

    // Utility
    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public static String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}