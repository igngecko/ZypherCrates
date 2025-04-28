package com.igngecko.zyphercrates.commands; // Changed package name

import com.igngecko.zyphercrates.ZypherCrates; // Changed package/class name
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
// import org.jetbrains.annotations.NotNull; // Removed unused Jetbrains annotation

public class CratesCommand implements CommandExecutor {

    private final ZypherCrates plugin; // Changed type

    public CratesCommand(ZypherCrates plugin) { // Changed type
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(/*@NotNull*/ CommandSender sender, /*@NotNull*/ Command command, /*@NotNull*/ String label, /*@NotNull*/ String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageManager().getPrefix() + "Only players can open the crates GUI.");
            return true;
        }

        Player player = (Player) sender;

        // Check permission to use the base command
        if (!player.hasPermission("zyphercrates.use")) { // Changed permission node
            player.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
            return true;
        }

        plugin.getCrateGUI().openInventory(player);
        // Optionally send a message
        // player.sendMessage(plugin.getMessageManager().getMessage("gui-opened"));

        return true;
    }
}
