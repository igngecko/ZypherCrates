package com.igngecko.zyphercrates.crate; // Changed package name

import com.igngecko.zyphercrates.ZypherCrates; // Changed package/class name
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections; // Import Collections
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Reward {

    public enum RewardType {
        ITEM, COMMAND, MONEY, PERMISSION, UNKNOWN
    }

    private final RewardType type;
    private final double chance; // Percentage or weight
    private final String displayName;
    private final String rarity; // Optional, for broadcasts etc.

    // Specific reward data (only one set will be non-null/relevant depending on type)
    private final ItemStack item;
    private final List<String> commands;
    private final double moneyAmount;
    private final String permissionNode;
    private final int permissionDuration; // Duration in seconds (0 for permanent)

    public Reward(RewardType type, double chance, String displayName, String rarity,
                  ItemStack item, List<String> commands, double moneyAmount, String permissionNode, int permissionDuration) {
        this.type = type;
        this.chance = chance;
        this.displayName = displayName;
        this.rarity = rarity != null ? rarity : ""; // Ensure rarity is not null
        // Clone item stack to prevent modification of the original configured one
        this.item = (item != null) ? item.clone() : null;
        // Store unmodifiable list of commands
        this.commands = (commands != null) ? Collections.unmodifiableList(commands) : null;
        this.moneyAmount = moneyAmount;
        this.permissionNode = permissionNode;
        this.permissionDuration = permissionDuration;
    }

    public RewardType getType() {
        return type;
    }

    public double getChance() {
        return chance;
    }

    public String getDisplayName() {
        return displayName;
    }

     public String getRarity() {
        return rarity;
     }

    public void giveReward(Player player) {
        ZypherCrates plugin = ZypherCrates.getInstance(); // Changed main class reference
        if (plugin == null) {
             Bukkit.getLogger().severe("[ZypherCrates] Cannot give reward, plugin instance is null!");
             return;
        }
         Map<String, String> rewardPlaceholder = Collections.singletonMap("%reward%", getDisplayName()); // Use singletonMap

        switch (type) {
            case ITEM:
                if (item != null) {
                     // Give a clone of the reward item
                    HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item.clone());
                    if (!leftover.isEmpty()) {
                        // Drop leftover items on the ground at player's location
                        leftover.values().forEach(it -> player.getWorld().dropItemNaturally(player.getLocation(), it));
                        player.sendMessage(plugin.getMessageManager().getFormattedMessage("item-inventory-full", rewardPlaceholder));
                    } else {
                        player.sendMessage(plugin.getMessageManager().getFormattedMessage("reward-received", rewardPlaceholder));
                    }
                    player.updateInventory(); // Update client view
                } else {
                     plugin.getLogger().warning("Attempted to give null ITEM reward: " + displayName);
                     player.sendMessage(ChatColor.RED + "Error: Could not give item reward (item missing).");
                }
                break;

            case COMMAND:
                if (commands != null && !commands.isEmpty()) {
                     // Message can be configured in messages.yml
                     // player.sendMessage(plugin.getMessageManager().getFormattedMessage("command-executed", rewardPlaceholder));
                     player.sendMessage(plugin.getMessageManager().getFormattedMessage("reward-received", rewardPlaceholder)); // Use generic reward message for commands too?

                    Bukkit.getScheduler().runTask(plugin, () -> { // Ensure commands run on main thread
                        for (String command : commands) {
                            String processedCommand = command.replace("%player%", player.getName())
                                                             .replace("%uuid%", player.getUniqueId().toString());
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
                        }
                    });
                } else {
                    plugin.getLogger().warning("Attempted to execute null or empty COMMAND reward: " + displayName);
                    player.sendMessage(ChatColor.RED + "Error: Could not give command reward (command missing).");
                }
                break;

            case MONEY:
                Economy econ = ZypherCrates.getEconomy(); // Changed main class reference
                if (econ != null) {
                     econ.depositPlayer(player, moneyAmount);
                     Map<String, String> moneyPlaceholders = new HashMap<>();
                     moneyPlaceholders.put("%amount%", String.format(plugin.getConfigManager().getMoneyFormat(), moneyAmount)); // Add format from config later if needed
                     moneyPlaceholders.put("%reward%", getDisplayName()); // Include reward name if needed in message
                     player.sendMessage(plugin.getMessageManager().getFormattedMessage("money-received", moneyPlaceholders));
                } else {
                    plugin.getLogger().warning("Attempted to give MONEY reward (" + displayName + ") but Vault Economy is not available.");
                    player.sendMessage(ChatColor.RED + "Error: Could not give money reward, economy system not found.");
                }
                break;

            case PERMISSION:
                 Permission perms = ZypherCrates.getPermissions(); // Changed main class reference
                 OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId()); // Use OfflinePlayer for Vault perms
                 if (perms != null && permissionNode != null && !permissionNode.isEmpty()) {
                      Map<String, String> permPlaceholders = new HashMap<>();
                         permPlaceholders.put("%permission%", permissionNode);
                         permPlaceholders.put("%reward%", getDisplayName());
                      player.sendMessage(plugin.getMessageManager().getFormattedMessage("permission-received", permPlaceholders));

                     // Temporary permissions support in Vault is spotty. Add permanent and log duration.
                     // Consider using a dedicated permissions plugin API (like LuckPerms) for reliable temp perms.
                     if (perms.playerAdd(null, offlinePlayer, permissionNode)) {
                           plugin.getLogger().info("Gave permission '" + permissionNode + "' to " + player.getName()
                               + (permissionDuration > 0 ? " (Duration requested: " + permissionDuration + "s, Vault handles expiry based on perm system)" : " (Permanent)"));
                     } else {
                           plugin.getLogger().warning("Vault failed to add permission '" + permissionNode + "' for " + player.getName());
                            player.sendMessage(ChatColor.RED + "Error: Failed to grant permission via Vault.");
                     }

                 } else {
                      plugin.getLogger().warning("Attempted to give PERMISSION reward (" + displayName + ") but Vault Permissions is not available or node is empty.");
                      player.sendMessage(ChatColor.RED + "Error: Could not give permission reward, permission system not found or node invalid.");
                 }
                break;
             case UNKNOWN: // Explicitly handle UNKNOWN
                 plugin.getLogger().warning("Attempted to give reward of UNKNOWN type: " + displayName);
                 player.sendMessage(ChatColor.RED + "Error: Encountered an unknown reward type.");
                 break;
             default: // Should not be reached if all enum values are handled
                 plugin.getLogger().severe("Unhandled RewardType in giveReward(): " + type);
                 player.sendMessage(ChatColor.RED + "Error: Encountered an unhandled reward type.");
                 break;
        }
    }

}
