package com.igngecko.zyphercrates.gui; // Changed package name

import com.igngecko.zyphercrates.ZypherCrates; // Changed package/class name
import com.igngecko.zyphercrates.config.ConfigManager; // Changed package name
import com.igngecko.zyphercrates.crate.Crate; // Changed package name
import com.igngecko.zyphercrates.data.PlayerDataManager; // Changed package name
import com.igngecko.zyphercrates.utils.ItemBuilder; // Changed package name
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
// import org.bukkit.Material; // Unused import
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
// import org.bukkit.inventory.meta.ItemMeta; // Unused import
// import org.jetbrains.annotations.NotNull; // Removed unused Jetbrains annotation

import java.util.ArrayList; // Import List
import java.util.List;
import java.util.stream.Collectors;

public class CrateGUI implements InventoryHolder {

    private final ZypherCrates plugin; // Changed type
    // Removed template inv, create fresh each time openInventory is called
    // private final Inventory inv;
    private final ConfigManager configManager;
    private final PlayerDataManager playerDataManager;

     // Tag to identify our GUI inventory easily (can be used in listener if needed)
     public static final String GUI_TAG = "ZypherCratesGUI"; // Changed tag

    public CrateGUI(ZypherCrates plugin) { // Changed type
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.playerDataManager = plugin.getPlayerDataManager();

        // No need to create template inv here anymore
    }

    // This now returns the newly created inventory
    public Inventory openInventory(Player player) {
         // Create a fresh inventory for the specific player each time
         String title = configManager.getGuiTitle(); // Get title dynamically
         int size = configManager.getGuiSize(); // Get size dynamically
         Inventory playerInv = Bukkit.createInventory(this, size, title);

         // Fill empty slots if enabled
         if (configManager.isFillerEnabled()) {
             // Build filler item safely
             try {
                 ItemStack filler = new ItemBuilder(configManager.getFillerMaterial())
                         .setName(configManager.getFillerName())
                         .setLore(configManager.getFillerLore())
                         .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                         .build();
                 for (int i = 0; i < size; i++) {
                     // Only set filler if slot is currently empty (allows crates to override)
                     if (playerInv.getItem(i) == null) {
                         playerInv.setItem(i, filler);
                     }
                 }
             } catch (Exception e) {
                 plugin.getLogger().severe("Failed to create filler item: " + e.getMessage());
                 // Continue without filler if it fails
             }
         }


        // Populate with crate items
        for (Crate crate : plugin.getCrateManager().getAllCrates()) {
            int slot = crate.getGuiSlot();
             // Validate slot against the actual size being used
            if (slot >= 0 && slot < size) {
                 // Create display item *before* setting filler, so crate items override filler
                ItemStack displayItem = createCrateDisplayItem(crate, player);
                 playerInv.setItem(slot, displayItem);
            } else {
                plugin.getLogger().warning("Crate '" + crate.getId() + "' has an invalid slot ("+ slot +") for GUI size ("+size+"). It will not be displayed.");
            }
        }

        player.openInventory(playerInv);
        return playerInv; // Return the created inventory
    }

     private ItemStack createCrateDisplayItem(Crate crate, Player player) {
        ItemStack originalItem = crate.getGuiItem(); // Get the base item from Crate config
        if (originalItem == null) {
             plugin.getLogger().severe("Crate " + crate.getId() + " has a NULL GUI item!");
             return null; // Or return a default error item
        }
         ItemBuilder builder = new ItemBuilder(originalItem.clone()); // Clone to avoid modifying the original

         int playerKeys = playerDataManager.getKeys(player.getUniqueId(), crate.getId());
         String keyPlaceholder = "%zc_keys_" + crate.getId() + "%"; // Changed placeholder prefix

         // Replace key placeholder in name
         if (builder.hasName()) {
             builder.setName(builder.getName().replace(keyPlaceholder, String.valueOf(playerKeys)));
         }

         // Replace key placeholder in lore
         if (builder.hasLore()) {
              // Need mutable list to modify
             List<String> currentLore = builder.getLore();
             if (currentLore != null) {
                  List<String> updatedLore = new ArrayList<>();
                  for(String line : currentLore){
                      updatedLore.add(line.replace(keyPlaceholder, String.valueOf(playerKeys)));
                  }
                 builder.setLore(updatedLore);
              }
         }

          // Apply enchantment glow if configured in crate
         if (crate.isGuiItemEnchanted()) {
             builder.addGlow();
         }


         // Add NBT tag to easily identify crate items on click
         builder.addNBTString("zc_crate_id", crate.getId()); // Changed NBT key

         return builder.build();
     }


    // @NotNull // Removed annotation
    @Override
    public Inventory getInventory() {
        // IMPORTANT: This method is required by InventoryHolder but can be problematic
        // if it returns a shared/stale inventory. Since we create a new inventory
        // in openInventory(), returning null here might be safer, although Bukkit's
        // behavior with null InventoryHolders can vary.
        // Best practice: Let the listener get the inventory via event.getView().getTopInventory()
        // Returning null might cause issues in some edge cases or plugins interacting with inventories.
        // Let's return a dummy inventory for compliance, but emphasize it shouldn't be used directly.
        return Bukkit.createInventory(this, 9, "Dummy ZypherCrates Holder"); // Return a dummy inv
    }
}
