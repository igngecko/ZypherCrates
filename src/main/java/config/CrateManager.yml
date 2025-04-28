package com.igngecko.zyphercrates.config; // Changed package name

import com.igngecko.zyphercrates.ZypherCrates; // Changed package/class name
import com.igngecko.zyphercrates.crate.Crate; // Changed package name
import com.igngecko.zyphercrates.crate.Reward; // Changed package name
import com.igngecko.zyphercrates.utils.ColorUtils; // Added ColorUtils import
import com.igngecko.zyphercrates.utils.ItemBuilder; // Changed package name
// import org.bukkit.ChatColor; // Replaced by ColorUtils
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CrateManager {

    private final ZypherCrates plugin; // Changed type
    private FileConfiguration cratesConfig = null;
    private File cratesFile = null;
    private final Map<String, Crate> crates = new LinkedHashMap<>(); // Use LinkedHashMap to preserve order potentially for GUI

    public CrateManager(ZypherCrates plugin) { // Changed type
        this.plugin = plugin;
        this.cratesFile = new File(plugin.getDataFolder(), "crates.yml");
        loadCrates();
    }

    public void loadCrates() {
        if (!cratesFile.exists()) {
            plugin.saveResource("crates.yml", false);
        }

        cratesConfig = YamlConfiguration.loadConfiguration(cratesFile);

        // Look for defaults in the jar
        InputStream defConfigStream = plugin.getResource("crates.yml");
        if (defConfigStream != null) {
             try (InputStreamReader reader = new InputStreamReader(defConfigStream)) { // Use try-with-resources
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(reader);
                cratesConfig.setDefaults(defConfig);
             } catch (IOException e){
                 plugin.getLogger().log(Level.WARNING, "Failed to close default crates.yml stream.", e);
             }
             // Don't copy defaults blindly, let user manage their crate file mostly
             // cratesConfig.options().copyDefaults(true); // Avoid overwriting user crates unless file is empty
             // try { cratesConfig.save(cratesFile); } catch (IOException e) { e.printStackTrace(); }
        }

        crates.clear();
        ConfigurationSection crateSection = cratesConfig.getConfigurationSection(""); // Get top-level keys
        if (crateSection == null) {
             plugin.getLogger().severe("Could not read crates from crates.yml! Is it empty or malformed?");
             return;
        }


        for (String crateId : crateSection.getKeys(false)) {
            ConfigurationSection section = cratesConfig.getConfigurationSection(crateId);
            if (section == null) {
                 plugin.getLogger().warning("Skipping invalid crate section: " + crateId);
                continue;
            }

            try {
                Crate crate = parseCrate(crateId, section);
                if (crate != null) {
                    crates.put(crateId.toLowerCase(), crate); // Store keys in lowercase for consistency
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to parse crate: " + crateId, e);
            }
        }
         plugin.getLogger().info("Loaded " + crates.size() + " crate types.");
    }

    private Crate parseCrate(String id, ConfigurationSection section) {
        // GUI Item Parsing
        ConfigurationSection guiSection = section.getConfigurationSection("gui");
        if (guiSection == null) {
             plugin.getLogger().warning("Crate '" + id + "' is missing 'gui' section. Skipping.");
             return null;
        }

        int slot = guiSection.getInt("slot", -1);
         if (slot < 0) {
             plugin.getLogger().warning("Crate '" + id + "' has invalid or missing GUI slot. Skipping.");
             return null;
         }

        ConfigurationSection itemSection = guiSection.getConfigurationSection("gui.item");
        if (itemSection == null) {
             plugin.getLogger().warning("Crate '" + id + "' is missing 'gui.item' section. Skipping.");
             return null;
        }
         // Parse GUI item using the helper (which now uses ColorUtils via ItemBuilder)
         ItemStack guiItem = parseItemStack(itemSection, id + ".gui.item");
        if(guiItem == null){
             plugin.getLogger().warning("Crate '" + id + "' failed to parse GUI item. Skipping.");
            return null;
        }
        boolean enchanted = itemSection.getBoolean("enchanted", false);


        // Rewards Parsing
        ConfigurationSection rewardsSection = section.getConfigurationSection("rewards");
        if (rewardsSection == null) {
            plugin.getLogger().warning("Crate '" + id + "' is missing 'rewards' section. Skipping.");
            return null;
        }

        List<Reward> rewards = new ArrayList<>();
        double totalChance = 0;
        boolean useWeights = false;

        for (String rewardKey : rewardsSection.getKeys(false)) {
            ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(rewardKey);
            if (rewardSection == null) continue;

            try {
                 Reward.RewardType type = Reward.RewardType.UNKNOWN; // Default to unknown
                 String typeString = rewardSection.getString("type", "UNKNOWN").toUpperCase();
                 try {
                    type = Reward.RewardType.valueOf(typeString);
                 } catch (IllegalArgumentException e) {
                     plugin.getLogger().warning("Reward '" + rewardKey + "' in crate '" + id + "' has unknown type: " + typeString + ". Skipping.");
                     continue; // Skip this reward if type is invalid
                 }

                double chance = rewardSection.getDouble("chance", 0.0);
                 // Translate reward display name
                String displayName = ColorUtils.translate(rewardSection.getString("display_name", "A Reward"));
                String rarity = rewardSection.getString("rarity", ""); // Optional rarity

                 if (chance <= 0) {
                     plugin.getLogger().warning("Reward '" + rewardKey + "' in crate '" + id + "' has invalid chance <= 0. Skipping.");
                     continue;
                 }
                 totalChance += chance;

                Reward reward = null;
                switch (type) {
                    case ITEM:
                        ConfigurationSection rewardItemSection = rewardSection.getConfigurationSection("item");
                         if(rewardItemSection == null){
                             plugin.getLogger().warning("Reward '" + rewardKey + "' (ITEM) in crate '" + id + "' is missing 'item' section. Skipping.");
                             continue;
                         }
                         // Parse reward item (ItemBuilder handles colors)
                        ItemStack item = parseItemStack(rewardItemSection, id + ".rewards." + rewardKey);
                        if(item != null) {
                             reward = new Reward(type, chance, displayName, rarity, item, null, 0.0, null, 0);
                        } else {
                             plugin.getLogger().warning("Reward '" + rewardKey + "' (ITEM) in crate '" + id + "' failed to parse item. Skipping.");
                             continue;
                        }
                        break;
                    case COMMAND:
                        List<String> commands = rewardSection.getStringList("commands");
                        if (commands.isEmpty()) {
                             plugin.getLogger().warning("Reward '" + rewardKey + "' (COMMAND) in crate '" + id + "' has no commands listed. Skipping.");
                            continue;
                        }
                         // Commands are usually not colored, but translate just in case? No, commands should remain raw.
                         reward = new Reward(type, chance, displayName, rarity, null, commands, 0.0, null, 0);
                        break;
                    case MONEY:
                         if (ZypherCrates.getEconomy() == null) { // Changed main class reference
                            plugin.getLogger().warning("Reward '" + rewardKey + "' (MONEY) in crate '" + id + "' cannot be loaded - Vault Economy not found.");
                            continue;
                         }
                         double moneyAmount = rewardSection.getDouble("amount", 0.0);
                         if (moneyAmount <= 0) {
                              plugin.getLogger().warning("Reward '" + rewardKey + "' (MONEY) in crate '" + id + "' has invalid amount <= 0. Skipping.");
                             continue;
                         }
                         reward = new Reward(type, chance, displayName, rarity, null, null, moneyAmount, null, 0);
                        break;
                    case PERMISSION:
                        if (ZypherCrates.getPermissions() == null) { // Changed main class reference
                           plugin.getLogger().warning("Reward '" + rewardKey + "' (PERMISSION) in crate '" + id + "' cannot be loaded - Vault Permissions not found.");
                           continue;
                        }
                        String permission = rewardSection.getString("permission");
                         int duration = rewardSection.getInt("duration", 0); // Duration in seconds, 0 for permanent
                        if (permission == null || permission.isEmpty()) {
                            plugin.getLogger().warning("Reward '" + rewardKey + "' (PERMISSION) in crate '" + id + "' is missing permission node. Skipping.");
                           continue;
                        }
                        reward = new Reward(type, chance, displayName, rarity, null, null, 0.0, permission, duration);
                        break;
                    // case UNKNOWN was handled by the try-catch above
                    default:
                         plugin.getLogger().warning("Unhandled reward type for reward '" + rewardKey + "' in crate '" + id + "': " + type + ". Skipping.");
                         continue;
                }
                if(reward != null) rewards.add(reward);

             } catch (Exception e) {
                 plugin.getLogger().log(Level.SEVERE, "Error parsing reward '" + rewardKey + "' in crate '" + id + "'. Skipping.", e);
            }
        }

         if (rewards.isEmpty()) {
             plugin.getLogger().warning("Crate '" + id + "' has no valid rewards defined. Skipping crate.");
             return null;
         }

        // Determine if using percentages or weights
        useWeights = totalChance > 100.0 || (totalChance < 99.9 && totalChance > 0); // Allow for slight float inaccuracies if total is near 100
        if (!useWeights && Math.abs(totalChance - 100.0) > 0.1) { // If using percentages, warn if not close to 100
            plugin.getLogger().warning("Total chance for crate '" + id + "' is " + String.format("%.2f", totalChance) + "%. It should be close to 100% if not using weights.");
        } else if (useWeights) {
            plugin.getLogger().info("Crate '" + id + "' is using weights (total chance = " + String.format("%.2f", totalChance) + ").");
        }


        return new Crate(id, slot, guiItem, enchanted, rewards, totalChance, useWeights);
    }

     // This method now relies on ItemBuilder which will use ColorUtils
    private ItemStack parseItemStack(ConfigurationSection itemSection, String pathForError) {
         String matString = itemSection.getString("material");
         if(matString == null){
             plugin.getLogger().warning("Missing material for item at '" + pathForError + "'.");
             return null;
         }

         Material material = null;
         try {
            material = Material.matchMaterial(matString);
         } catch (IllegalArgumentException e) {
             // Handles cases where matchMaterial might throw internal errors on very old/invalid strings
         }

         if (material == null) {
              plugin.getLogger().warning("Invalid material '" + matString + "' for item at '" + pathForError + "'.");
             return null;
         }

        int amount = itemSection.getInt("amount", 1);
         ItemBuilder builder = new ItemBuilder(material, amount);

         // ItemBuilder's setName and setLore will handle color translation via ColorUtils
        if (itemSection.contains("name")) {
             String name = itemSection.getString("name");
             if (name != null) {
                 builder.setName(name); // Pass raw string to ItemBuilder
             }
        }

        if (itemSection.contains("lore")) {
            List<String> lore = itemSection.getStringList("lore");
            builder.setLore(lore); // Pass raw list to ItemBuilder
        }

         if (itemSection.contains("enchants")) {
             ConfigurationSection enchantsSection = itemSection.getConfigurationSection("enchants");
             // Handle both list format and map format for enchants
             if (enchantsSection != null) {
                 // Map format: enchant_name: level
                 for (String enchantKey : enchantsSection.getKeys(false)) {
                     Enchantment enchant = Enchantment.getByName(enchantKey.toUpperCase());
                     int level = enchantsSection.getInt(enchantKey);
                     if (enchant != null && level > 0) {
                          builder.addEnchant(enchant, level, true); // Ignore level restriction for config flexibility
                     } else {
                          plugin.getLogger().warning("Invalid enchantment definition '" + enchantKey + ": " + level + "' for item at '" + pathForError + "'.");
                     }
                 }
             } else {
                 // List format: ENCHANTMENT_NAME:level
                 List<String> enchantStrings = itemSection.getStringList("enchants");
                 for (String enchantString : enchantStrings) {
                     try {
                         String[] parts = enchantString.split(":");
                         if (parts.length == 2) {
                             Enchantment enchant = Enchantment.getByName(parts[0].toUpperCase());
                             int level = Integer.parseInt(parts[1]);
                             if (enchant != null && level > 0) {
                                 builder.addEnchant(enchant, level, true); // Ignore level restriction for config flexibility
                             } else {
                                 plugin.getLogger().warning("Invalid enchantment name or level '" + parts[0] + ":" + level + "' for item at '" + pathForError + "'.");
                             }
                         } else {
                              plugin.getLogger().warning("Invalid enchantment format '" + enchantString + "' for item at '" + pathForError + "'. Use format: ENCHANTMENT_NAME:level or map format.");
                         }
                     } catch (NumberFormatException e) {
                         plugin.getLogger().warning("Invalid enchantment level in '" + enchantString + "' for item at '" + pathForError + "'.");
                     } catch (Exception e) {
                         plugin.getLogger().log(Level.SEVERE, "Error parsing enchantment '" + enchantString + "' for item at '" + pathForError + "'.", e);
                     }
                 }
             }
         }

         if (itemSection.contains("custom-model-data")) {
             int cmd = itemSection.getInt("custom-model-data");
             builder.setCustomModelData(cmd);
         }


        return builder.build();
    }


    public Crate getCrate(String id) {
        return crates.get(id.toLowerCase());
    }

    public Collection<Crate> getAllCrates() {
        return Collections.unmodifiableCollection(crates.values());
    }

     public Set<String> getCrateIds() {
        return Collections.unmodifiableSet(crates.keySet());
     }
}
