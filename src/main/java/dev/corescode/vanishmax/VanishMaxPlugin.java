package dev.corescode.vanishmax;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class VanishMaxPlugin extends JavaPlugin {

    private static VanishMaxPlugin instance;
    private LevelManager levelManager;
    private VanishManager vanishManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        levelManager  = new LevelManager(this);
        levelManager.load();

        vanishManager = new VanishManager(this, levelManager);
        vanishManager.loadData();

        // Register commands
        InvisCommand invisCommand = new InvisCommand(this, vanishManager, levelManager);
        getCommand("invis").setExecutor(invisCommand);
        getCommand("invis").setTabCompleter(invisCommand);
        getCommand("vanishmax").setExecutor(invisCommand);
        getCommand("vanishmax").setTabCompleter(invisCommand);

        // Register listeners
        getServer().getPluginManager().registerEvents(
                new PlayerListener(this, vanishManager), this);

        // Prevent mobs from targeting vanished players
        getServer().getPluginManager().registerEvents(
                new MobTargetListener(vanishManager), this);

        // Make vanished players invincible to all damage
        getServer().getPluginManager().registerEvents(
                new DamageListener(vanishManager), this);

        // Register PlaceholderAPI expansion if PAPI is present
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new VanishMaxPlaceholder(this, vanishManager).register();
            getLogger().info("PlaceholderAPI found — %vanishmax_vanished% placeholder registered.");
        } else {
            getLogger().warning("PlaceholderAPI not found — %vanishmax_vanished% placeholder will not work.");
        }

        getLogger().info("Vanish Max enabled with " + levelManager.getLevels().size() + " levels.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Vanish Max disabled.");
    }

    public LevelManager getLevelManager()   { return levelManager; }
    public VanishManager getVanishManager() { return vanishManager; }
    public static VanishMaxPlugin getInstance() { return instance; }
}