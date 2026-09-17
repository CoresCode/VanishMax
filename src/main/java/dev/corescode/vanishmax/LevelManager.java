package dev.corescode.vanishmax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class LevelManager {

    private final VanishMaxPlugin plugin;
    private final Logger log;
    private List<VanishLevel> levels = new ArrayList<>();

    public LevelManager(VanishMaxPlugin plugin) {
        this.plugin = plugin;
        this.log    = plugin.getLogger();
    }

    public void load() {
        levels.clear();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("levels");
        if (section == null) {
            log.severe("No 'levels' section found in config.yml! Vanish Max will not function.");
            return;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry == null) continue;
            int rank = entry.getInt("rank", 0);
            levels.add(new VanishLevel(key.toLowerCase(), rank));
            log.info("Loaded level: " + key + " (rank " + rank + ", perm: vanishmax." + key.toLowerCase() + ")");
        }

        levels.sort(Comparator.comparingInt(VanishLevel::getRank));

        if (levels.isEmpty()) {
            log.severe("No levels loaded from config.yml!");
        } else {
            log.info("Loaded " + levels.size() + " vanish levels.");
        }
    }

    public VanishLevel getLevelOf(Player player) {
        VanishLevel highest = null;
        for (VanishLevel level : levels) {
            if (player.hasPermission(level.getPermission())) {
                if (highest == null || level.getRank() > highest.getRank()) {
                    highest = level;
                }
            }
        }
        return highest;
    }

    public boolean hasAnyLevel(Player player) {
        return getLevelOf(player) != null;
    }

    public VisibilityResult getVisibility(Player observer, VanishLevel vanishedLevel) {
        VanishLevel observerLevel = getLevelOf(observer);
        if (observerLevel == null) return VisibilityResult.INVISIBLE;
        return observerLevel.getRank() >= vanishedLevel.getRank()
                ? VisibilityResult.VISIBLE
                : VisibilityResult.INVISIBLE;
    }

    public enum VisibilityResult {
        INVISIBLE,
        VISIBLE
    }

    public List<VanishLevel> getLevels() {
        return Collections.unmodifiableList(levels);
    }
}