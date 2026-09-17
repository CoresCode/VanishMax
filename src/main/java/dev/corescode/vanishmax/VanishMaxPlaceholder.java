package dev.corescode.vanishmax;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

/**
 * Registers the %vanishmax_vanished% placeholder with PlaceholderAPI.
 *
 * Usage in TAB plugin's groups.yml tagsuffix:
 *   tagsuffix: "%vanishmax_vanished%"
 *
 * Returns:
 *   " §a[Vanish]"  — when the player is vanished (light green, space before tag)
 *   ""             — when the player is not vanished (empty string, no effect)
 *
 * The space before [Vanish] ensures it sits cleanly next to the username
 * without touching it.
 */
public class VanishMaxPlaceholder extends PlaceholderExpansion {

    private final VanishMaxPlugin plugin;
    private final VanishManager vanishManager;

    // Light green [Vanish] tag with a leading space so it sits after the username
    private static final String VANISH_TAG = " §a[Vanish]§r";

    public VanishMaxPlaceholder(VanishMaxPlugin plugin, VanishManager vanishManager) {
        this.plugin        = plugin;
        this.vanishManager = vanishManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "vanishmax";
    }

    @Override
    public @NotNull String getAuthor() {
        return "CoresCode";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        // Keep this expansion registered even if PlaceholderAPI reloads
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    /**
     * Handles %vanishmax_<placeholder>% requests.
     *
     * Supported placeholders:
     *   %vanishmax_vanished%  → " [Vanish]" or ""
     */
    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (params.equalsIgnoreCase("vanished")) {
            // Only works for online players
            if (offlinePlayer == null || !offlinePlayer.isOnline()) return "";
            Player player = offlinePlayer.getPlayer();
            if (player == null) return "";
            return vanishManager.isVanished(player) ? VANISH_TAG : "";
        }
        return null; // Unknown placeholder
    }
}