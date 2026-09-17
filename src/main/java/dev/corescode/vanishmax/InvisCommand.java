package dev.corescode.vanishmax;

import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class InvisCommand implements CommandExecutor, TabCompleter {

    private final VanishMaxPlugin plugin;
    private final VanishManager vanishManager;
    private final LevelManager levelManager;

    public InvisCommand(VanishMaxPlugin plugin, VanishManager vanishManager, LevelManager levelManager) {
        this.plugin = plugin;
        this.vanishManager = vanishManager;
        this.levelManager = levelManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Handle /vanishmax reload sub-command
        if (command.getName().equalsIgnoreCase("vanishmax")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("vanishmax.reload")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to reload Vanish Max.");
                    return true;
                }
                plugin.reloadConfig();
                plugin.getLevelManager().load();
                sender.sendMessage(ChatColor.GREEN + "Vanish Max config reloaded successfully.");
                return true;
            }
            sender.sendMessage(ChatColor.YELLOW + "Usage: /vanishmax reload");
            return true;
        }

        // /invis command
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use /invis.");
            return true;
        }

        if (!levelManager.hasAnyLevel(player)) {
            player.sendMessage(VanishManager.colorize(
                    plugin.getConfig().getString("messages.no-permission",
                            "&cYou don't have permission to use /invis.")));
            return true;
        }

        plugin.getServer().getGlobalRegionScheduler().run(plugin, task -> {
            vanishManager.toggleVanish(player);
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("vanishmax") && args.length == 1) {
            if ("reload".startsWith(args[0].toLowerCase())) {
                return List.of("reload");
            }
        }
        return Collections.emptyList();
    }
}