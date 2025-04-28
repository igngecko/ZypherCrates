package com.igngecko.zyphercrates.commands; // Changed package name

import com.igngecko.zyphercrates.ZypherCrates; // Changed package/class name
import com.igngecko.zyphercrates.config.CrateManager; // Changed package name
import com.igngecko.zyphercrates.config.MessageManager; // Changed package name
import com.igngecko.zyphercrates.data.PlayerDataManager; // Changed package name
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
// Removed unused Jetbrains annotations
// import org.jetbrains.annotations.NotNull;
// import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class AdminCommand implements CommandExecutor, TabCompleter {

    private final ZypherCrates plugin; // Changed type
    private final PlayerDataManager playerDataManager;
    private final CrateManager crateManager;
    private final MessageManager messageManager;

    public AdminCommand(ZypherCrates plugin) { // Changed type
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
        this.crateManager = plugin.getCrateManager();
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(/*@NotNull*/ CommandSender sender, /*@NotNull*/ Command command, /*@NotNull*/ String label, /*@NotNull*/ String[] args) {
        // Check base admin permission
        if (!sender.hasPermission("zyphercrates.admin")) { // Changed permission node
            sender.sendMessage(messageManager.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "give":
                handleGive(sender, args);
                break;
            case "take":
                handleTake(sender, args);
                break;
            case "set":
                handleSet(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            default:
                sendUsage(sender);
                break;
        }

        return true;
    }

    private void sendUsage(CommandSender sender) {
         // Use the multi-line message from messages.yml (which should be updated)
         for (String line : messageManager.getRawMessageList("zc-usage")) { // Changed message key
             sender.sendMessage(line);
         }
    }

    private void handleGive(CommandSender sender, String[] args) {
        // /zc give <player> <crate> [amount]
        if (args.length < 3 || args.length > 4) {
            sendUsage(sender);
            return;
        }
        // Sub-permission check (optional but good practice)
        // if (!sender.hasPermission("zyphercrates.admin.give")) { ... }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            sender.sendMessage(messageManager.getMessage("player-not-found"));
            return;
        }

        String crateId = args[2].toLowerCase();
        if (crateManager.getCrate(crateId) == null) {
            sender.sendMessage(messageManager.getMessage("invalid-crate"));
            return;
        }

        int amount = 1;
        if (args.length == 4) {
            try {
                amount = Integer.parseInt(args[3]);
                if (amount <= 0) {
                    sender.sendMessage(messageManager.getMessage("invalid-amount"));
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(messageManager.getMessage("invalid-amount"));
                return;
            }
        }

        playerDataManager.addKeys(target.getUniqueId(), crateId, amount);
         Map<String, String> placeholders = Map.of(
                 "%player%", target.getName() != null ? target.getName() : "Unknown", // Handle potential null name for offline players not played before
                 "%crate%", crateId,
                 "%amount%", String.valueOf(amount)
         );
        sender.sendMessage(messageManager.getFormattedMessage("keys-given", placeholders));
        if(target.isOnline()) {
             target.getPlayer().sendMessage(messageManager.getMessage("keys-updated")); // Notify player if online
        }
    }

    private void handleTake(CommandSender sender, String[] args) {
        // /zc take <player> <crate> [amount]
        if (args.length < 3 || args.length > 4) {
            sendUsage(sender);
            return;
        }
        // if (!sender.hasPermission("zyphercrates.admin.take")) { ... }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
         if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            sender.sendMessage(messageManager.getMessage("player-not-found"));
            return;
        }

        String crateId = args[2].toLowerCase();
         if (crateManager.getCrate(crateId) == null) {
            sender.sendMessage(messageManager.getMessage("invalid-crate"));
            return;
        }

        int amount = 1;
        if (args.length == 4) {
            try {
                amount = Integer.parseInt(args[3]);
                 if (amount <= 0) {
                    sender.sendMessage(messageManager.getMessage("invalid-amount"));
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(messageManager.getMessage("invalid-amount"));
                return;
            }
        }

         // Check if player has enough keys BEFORE taking
        int currentKeys = playerDataManager.getKeys(target.getUniqueId(), crateId);
        if (currentKeys < amount) {
            sender.sendMessage(messageManager.getPrefix() + "&c" + (target.getName() != null ? target.getName() : "Unknown") + " only has " + currentKeys + " " + crateId + " keys.");
            return;
        }


        playerDataManager.takeKeys(target.getUniqueId(), crateId, amount);
        Map<String, String> placeholders = Map.of(
                "%player%", target.getName() != null ? target.getName() : "Unknown",
                "%crate%", crateId,
                "%amount%", String.valueOf(amount)
        );
        sender.sendMessage(messageManager.getFormattedMessage("keys-taken", placeholders));
        if(target.isOnline()) {
             target.getPlayer().sendMessage(messageManager.getMessage("keys-updated"));
        }
    }

    private void handleSet(CommandSender sender, String[] args) {
        // /zc set <player> <crate> <amount>
        if (args.length != 4) {
            sendUsage(sender);
            return;
        }
        // if (!sender.hasPermission("zyphercrates.admin.set")) { ... }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
         if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            sender.sendMessage(messageManager.getMessage("player-not-found"));
            return;
        }

        String crateId = args[2].toLowerCase();
         if (crateManager.getCrate(crateId) == null) {
            sender.sendMessage(messageManager.getMessage("invalid-crate"));
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[3]);
             if (amount < 0) { // Allow setting to 0
                sender.sendMessage(messageManager.getMessage("invalid-amount"));
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(messageManager.getMessage("invalid-amount"));
            return;
        }

        playerDataManager.setKeys(target.getUniqueId(), crateId, amount);
         Map<String, String> placeholders = Map.of(
                 "%player%", target.getName() != null ? target.getName() : "Unknown",
                 "%crate%", crateId,
                 "%amount%", String.valueOf(amount)
         );
        sender.sendMessage(messageManager.getFormattedMessage("keys-set", placeholders));
         if(target.isOnline()) {
             target.getPlayer().sendMessage(messageManager.getMessage("keys-updated"));
         }
    }

    private void handleReload(CommandSender sender) {
        // if (!sender.hasPermission("zyphercrates.admin.reload")) { ... }
        sender.sendMessage(messageManager.getPrefix() + "&eReloading configurations...");
        if (plugin.loadPluginConfigs()) {
            sender.sendMessage(messageManager.getMessage("reload-success"));
        } else {
            sender.sendMessage(messageManager.getPrefix() + "&cFailed to reload configurations. Check console for errors.");
        }
    }


    // @Nullable
    @Override
    public List<String> onTabComplete(/*@NotNull*/ CommandSender sender, /*@NotNull*/ Command command, /*@NotNull*/ String alias, /*@NotNull*/ String[] args) {
        // Check base admin permission for tab completion
        if (!sender.hasPermission("zyphercrates.admin")) { // Changed permission node
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.addAll(Arrays.asList("give", "take", "set", "reload"));
        } else if (args.length == 2) {
            // Suggest players for give/take/set
            if (Arrays.asList("give", "take", "set").contains(args[0].toLowerCase())) {
                suggestions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
                 // Optionally suggest offline players who have played before
                 // Arrays.stream(Bukkit.getOfflinePlayers()).filter(OfflinePlayer::hasPlayedBefore).map(OfflinePlayer::getName).forEach(suggestions::add);
            }
        } else if (args.length == 3) {
            // Suggest crate types for give/take/set
            if (Arrays.asList("give", "take", "set").contains(args[0].toLowerCase())) {
                suggestions.addAll(crateManager.getCrateIds());
            }
        } else if (args.length == 4) {
            // Suggest amounts (e.g., 1, 10, 50) for give/take/set
             if (Arrays.asList("give", "take", "set").contains(args[0].toLowerCase())) {
                 // Suggest amount 1 for give/take, amount 0 for set
                 if (args[0].equalsIgnoreCase("set")) {
                    suggestions.add("0");
                 }
                suggestions.addAll(Arrays.asList("1", "10", "64"));
            }
        }

        String currentArg = args[args.length - 1].toLowerCase();
        StringUtil.copyPartialMatches(currentArg, suggestions, completions);
        Collections.sort(completions);
        return completions;
    }
}
