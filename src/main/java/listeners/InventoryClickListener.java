package com.igngecko.zyphercrates.listeners; // Changed package name

import com.igngecko.zyphercrates.ZypherCrates; // Changed package/class name
import com.igngecko.zyphercrates.config.ConfigManager; // Changed package name
import com.igngecko.zyphercrates.config.MessageManager; // Changed package name
import com.igngecko.zyphercrates.crate.Crate; // Changed package name
import com.igngecko.zyphercrates.crate.Reward; // Changed package name
import com.igngecko.zyphercrates.data.PlayerDataManager; // Changed package name
import com.igngecko.zyphercrates.gui.CrateGUI; // Changed package name
import com.igngecko.zyphercrates.utils.ItemBuilder; // Changed package name
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder; // Import InventoryHolder
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.logging.Level;

public class InventoryClickListener implements Listener {

    private final ZypherCrates plugin; // Changed type
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final PlayerDataManager playerDataManager;

    public InventoryClickListener(ZypherCrates plugin) { // Changed type
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.messageManager = plugin.getMessageManager();
        this.playerDataManager = plugin.getPlayerDataManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        Inventory topInventory = view.getTopInventory(); // The inventory clicked in

         // Ensure top inventory is not null
        if (topInventory == null) {
             return;
         }

        InventoryHolder holder = topInventory.getHolder();

        // Check if the clicked inventory is our Crate GUI using the holder
        if (holder instanceof CrateGUI) {
            // Prevent taking items out of the GUI or interacting with player inventory while GUI open
            event.setCancelled(true);

            // Check if click was in the *top* inventory (the GUI)
            if (event.getClickedInventory() != topInventory) {
                return; // Click was in player's inventory, ignore
            }


            ItemStack clickedItem = event.getCurrentItem();
            Player player = (Player) event.getWhoClicked();

            // Ensure the clicked item is valid
            if (clickedItem == null || clickedItem.getType() == Material.AIR || clickedItem.getType().isAir()) {
                return;
            }

             // Check if it's a filler item - do nothing
             if (configManager.isFillerEnabled()) {
                 // Robust check: material, name (if exists), and maybe lore match?
                 ItemStack fillerTemplate = null;
                 try {
                     fillerTemplate = new ItemBuilder(configManager.getFillerMaterial())
                                       .setName(configManager.getFillerName())
                                       .setLore(configManager.getFillerLore())
                                       .build();
                 } catch (Exception ignored) {} // Ignore if filler fails to build

                 if (fillerTemplate != null && clickedItem.isSimilar(fillerTemplate)) {
                      return; // It's a filler item
                 }
             }


            // Try to get crate ID from NBT first (more reliable)
            String crateId = ItemBuilder.getNBTString(clickedItem, "zc_crate_id"); // Changed NBT key


            if (crateId != null) {
                Crate crate = plugin.getCrateManager().getCrate(crateId);
                if (crate != null) {
                    handleCrateClick(player, crate);
                } else {
                     plugin.getLogger().warning("Clicked item in GUI corresponds to unknown crate ID (from NBT): " + crateId);
                }
            } else {
                 // Fallback: NBT missing (e.g., old item?), maybe log this?
                 plugin.getLogger().fine("Clicked item in GUI slot " + event.getSlot() + " (" + clickedItem.getType() + ") is missing the 'zc_crate_id' NBT tag.");
                 // Don't attempt fallback matching here, NBT should be the source of truth.
            }
        }
        // else: Click was not in our Crate GUI, ignore.
    }

    private void handleCrateClick(Player player, Crate crate) {
        // Check permission to open THIS specific crate
        String cratePermission = "zyphercrates.open." + crate.getId(); // Changed permission node base
        String wildcardPermission = "zyphercrates.open.*"; // Changed permission node base
        if (!player.hasPermission(cratePermission) && !player.hasPermission(wildcardPermission)) {
             Map<String, String> placeholders = Map.of("%crate%", crate.getId());
              // Find a specific message or use the generic one
              // Using generic 'no-permission' for simplicity now
             player.sendMessage(messageManager.getFormattedMessage("no-permission", placeholders));
             return;
        }

        // Check if player has keys
        if (playerDataManager.getKeys(player.getUniqueId(), crate.getId()) > 0) {
            // Take one key
            if (playerDataManager.takeKeys(player.getUniqueId(), crate.getId(), 1)) {
                player.closeInventory(); // Close GUI before giving reward
                player.sendMessage(messageManager.getFormattedMessage("crate-opened", Map.of("%crate%", crate.getId())));

                 // Get random reward
                 Reward reward = crate.getRandomReward();
                 if (reward != null) {
                     giveRewardToPlayer(player, reward, crate); // Separate method for reward logic + broadcast

                     // Update the GUI *if* it were to stay open or re-open immediately
                     // This requires passing the inventory instance or re-fetching & updating.
                     // Since we close it, updating isn't strictly necessary here.

                      // If re-opening, do it after a short delay:
                      // Bukkit.getScheduler().runTaskLater(plugin, () -> {
                      //    if (player.isOnline()) { // Check if player is still online
                      //        plugin.getCrateGUI().openInventory(player); // Re-open the GUI
                      //    }
                      // }, 5L); // Delay in ticks

                 } else {
                      plugin.getLogger().log(Level.SEVERE, "Failed to get a random reward from crate: " + crate.getId() + " even though it has rewards defined!");
                      player.sendMessage(messageManager.getPrefix() + "&cAn internal error occurred while opening the crate. Please contact an administrator.");
                      // Give back the key as compensation?
                      playerDataManager.addKeys(player.getUniqueId(), crate.getId(), 1);
                      plugin.getLogger().info("Returned key to " + player.getName() + " for crate " + crate.getId() + " due to reward selection error.");
                 }

            } else {
                 // This should theoretically not happen if the initial check passed and takeKeys is atomic, but handle defensively
                 plugin.getLogger().warning("Failed to take key from " + player.getName() + " for crate " + crate.getId() + " despite initial check passing. (Possible concurrency issue?)");
                 player.sendMessage(messageManager.getFormattedMessage("not-enough-keys", Map.of("%crate%", crate.getId())));
                 // Refresh GUI to show potentially correct key count
                  Bukkit.getScheduler().runTask(plugin, () -> { // Run on main thread
                       if (player.isOnline()) plugin.getCrateGUI().openInventory(player);
                   });
            }
        } else {
             player.sendMessage(messageManager.getFormattedMessage("not-enough-keys", Map.of("%crate%", crate.getId())));
              // Optionally play a sound or visual cue for failure
              // Example: player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        }
    }


    private void giveRewardToPlayer(Player player, Reward reward, Crate crate) {
         reward.giveReward(player);

         // Handle Broadcast
         if (configManager.shouldBroadcastRewards()) {
            String minRarityStr = configManager.getMinRarityToBroadcast();
             boolean shouldBroadcast = false;

             // Simple logic: Broadcast if enabled AND (minRarity is empty OR reward's rarity is not empty)
             // This means any reward with *any* rarity string gets broadcasted if a minRarity is set,
             // or ALL rewards get broadcasted if minRarity is empty/null.
             // A more complex system would require defining rarity levels/order in config.
             boolean rewardHasRarity = reward.getRarity() != null && !reward.getRarity().isEmpty();
             boolean minRarityIsSet = minRarityStr != null && !minRarityStr.isEmpty();

             if (!minRarityIsSet) { // If no minimum rarity set, broadcast all
                 shouldBroadcast = true;
             } else if (rewardHasRarity) {
                  // If minimum is set, broadcast if the reward *has* a rarity string.
                  // TODO: Add actual comparison logic if needed (e.g., comparing levels of "Common", "Rare", etc.)
                  // For now, just checking existence. Assumes setting *any* rarity makes it broadcast-worthy if min is set.
                  // Example comparison: if (isRarityGreaterOrEqual(reward.getRarity(), minRarityStr)) { ... }
                  shouldBroadcast = true; // Simplified logic
             }
             // else: minRarity is set, but reward has no rarity -> don't broadcast


             if (shouldBroadcast) {
                  Map<String, String> placeholders = Map.of(
                          "%player%", player.getDisplayName(), // Use display name for chat niceness
                          "%reward%", reward.getDisplayName(), // Reward display name
                          "%crate%", crate.getId() // Crate ID
                  );
                  String broadcastMessage = messageManager.getFormattedMessage("reward-broadcast", placeholders);
                  // Send broadcast to players with appropriate permission? Or globally? Global for now.
                  Bukkit.broadcastMessage(broadcastMessage);
             }
         }
    }
}
