package com.igngecko.zyphercrates.data; // Changed package name

import com.igngecko.zyphercrates.ZypherCrates; // Changed package/class name
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collections; // Import Collections
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class PlayerDataManager {

    private final ZypherCrates plugin; // Changed type
    private File playerDataFile;
    private FileConfiguration playerDataConfig;

    // ConcurrentHashMap for thread safety if accessed asynchronously (e.g., auto-save)
    private final Map<UUID, Map<String, Integer>> playerKeys = new ConcurrentHashMap<>();

    public PlayerDataManager(ZypherCrates plugin) { // Changed type
        this.plugin = plugin;
        this.playerDataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        // Ensure data folder exists before creating file
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        loadPlayerData();
    }

    public void loadPlayerData() {
        if (!playerDataFile.exists()) {
            try {
                // Attempt to create the file if it doesn't exist
                if (playerDataFile.createNewFile()) {
                    plugin.getLogger().info("Created playerdata.yml");
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create playerdata.yml!", e);
                return; // Don't proceed if file creation fails
            }
        }

        playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
        playerKeys.clear(); // Clear existing memory data before loading

        ConfigurationSection playersSection = playerDataConfig.getConfigurationSection(""); // Root section
        if (playersSection != null) {
             int count = 0;
            for (String uuidString : playersSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    // Get the section for the UUID directly
                    ConfigurationSection keysSection = playersSection.getConfigurationSection(uuidString);
                    if (keysSection != null) {
                        Map<String, Integer> keys = new HashMap<>();
                        for (String crateType : keysSection.getKeys(false)) {
                            if(keysSection.isInt(crateType)){ // Ensure value is an integer
                                keys.put(crateType.toLowerCase(), keysSection.getInt(crateType));
                            } else {
                                plugin.getLogger().warning("Invalid key amount found for player " + uuidString + ", crate " + crateType + " in playerdata.yml. Skipping.");
                            }
                        }
                         if(!keys.isEmpty()){
                             // Use ConcurrentHashMap's put method directly for thread safety
                             playerKeys.put(uuid, new ConcurrentHashMap<>(keys)); // Store as ConcurrentHashMap too
                             count++;
                         }
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID found in playerdata.yml: " + uuidString);
                } catch (Exception e){
                     plugin.getLogger().log(Level.SEVERE, "Error loading data for UUID " + uuidString + " from playerdata.yml", e);
                }
            }
             plugin.getLogger().info("Loaded key data for " + count + " players.");
        } else {
             plugin.getLogger().info("playerdata.yml is empty or not a valid config.");
        }
    }

     // Saves all currently loaded player data
     // Make synchronized to prevent concurrent modification issues during save iterations
    public synchronized void saveAllPlayerData() {
        YamlConfiguration tempConfig = new YamlConfiguration();

        // Iterate safely over the ConcurrentHashMap
        for (Map.Entry<UUID, Map<String, Integer>> entry : playerKeys.entrySet()) {
            UUID uuid = entry.getKey();
            Map<String, Integer> keys = entry.getValue(); // This is already a ConcurrentHashMap or HashMap if loaded previously
            if (keys != null && !keys.isEmpty()) { // Only save players with actual key data
                 // Create section in temp config
                 // Iterate safely over inner map as well if it's not ConcurrentHashMap
                 Map<String, Integer> keysToSave = new HashMap<>(keys); // Create copy to iterate over
                 for (Map.Entry<String, Integer> keyEntry : keysToSave.entrySet()) {
                     // Use path joining for safety/clarity
                     tempConfig.set(uuid.toString() + "." + keyEntry.getKey(), keyEntry.getValue());
                 }
            }
        }

        try {
            tempConfig.save(playerDataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save playerdata.yml!", e);
        }
    }

     // Save data for a specific player (useful after modifications)
     // This method might be less needed if relying solely on auto-save and shutdown save.
     // Keep it synchronized if called directly.
     public synchronized void savePlayerData(UUID uuid) {
         // Load current data from file first to avoid overwriting other players' data
         FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(playerDataFile);
         Map<String, Integer> keysInMemory = playerKeys.get(uuid);

         if (keysInMemory == null || keysInMemory.isEmpty()) {
             // If player has no keys in memory, remove their section from the file config
             currentConfig.set(uuid.toString(), null);
         } else {
             // Update or create the player's section in the file config
             Map<String, Integer> keysToSave = new HashMap<>(keysInMemory); // Create copy
             for (Map.Entry<String, Integer> entry : keysToSave.entrySet()) {
                 currentConfig.set(uuid.toString() + "." + entry.getKey(), entry.getValue());
             }
             // Ensure keys removed in memory are removed from file too
             ConfigurationSection fileSection = currentConfig.getConfigurationSection(uuid.toString());
             if(fileSection != null){
                 for(String keyInFile : fileSection.getKeys(false)){
                     if(!keysToSave.containsKey(keyInFile.toLowerCase())){
                         currentConfig.set(uuid.toString() + "." + keyInFile, null);
                     }
                 }
             }
         }

         try {
             currentConfig.save(playerDataFile);
         } catch (IOException e) {
             plugin.getLogger().log(Level.SEVERE, "Could not save player data for " + uuid + "!", e);
         }
     }


     // --- Key Management ---

     public void loadPlayer(Player player) {
        // Data is loaded initially, just ensure the map entry exists if needed using computeIfAbsent
        // Ensure the inner map is also thread-safe if modifying concurrently elsewhere
         playerKeys.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
     }

     public void unloadPlayer(UUID uuid) {
        // Optionally save data on quit, though auto-save should handle it
        // savePlayerData(uuid);
        // Don't remove from memory unless absolutely necessary for memory saving,
        // as frequent reloads might be less efficient than keeping it loaded.
         // playerKeys.remove(uuid); // Example if you need to unload
     }


    public int getKeys(UUID uuid, String crateType) {
        // Use ConcurrentHashMap's getOrDefault efficiently
        Map<String, Integer> keys = playerKeys.get(uuid);
        if (keys != null) {
            return keys.getOrDefault(crateType.toLowerCase(), 0);
        }
        return 0; // Return 0 if player map doesn't exist
    }

    public Map<String, Integer> getAllKeys(UUID uuid) {
         // Return an immutable copy or a new HashMap
        Map<String, Integer> keys = playerKeys.get(uuid);
        if (keys != null) {
             return new HashMap<>(keys); // Return a defensive copy
         }
         return Collections.emptyMap(); // Return empty map if player not found
    }

    public void setKeys(UUID uuid, String crateType, int amount) {
         if (amount < 0) amount = 0; // Ensure non-negative keys
         String crateLower = crateType.toLowerCase();
        final int finalAmount = amount; // For lambda/compute

        // Use compute to handle absent/present cases atomically
        playerKeys.compute(uuid, (k, v) -> {
            if (v == null) { // Player not in map yet
                 if (finalAmount > 0) { // Only create if amount > 0
                     Map<String, Integer> newMap = new ConcurrentHashMap<>();
                     newMap.put(crateLower, finalAmount);
                     return newMap;
                 } else {
                     return null; // Don't create map if setting to 0
                 }
            } else { // Player already exists
                 if (finalAmount == 0) {
                     v.remove(crateLower); // Remove crate if setting to zero
                 } else {
                     v.put(crateLower, finalAmount); // Update existing crate
                 }
                 // Remove player entry entirely if they have no keys left across all types
                 if (v.isEmpty()) return null;
                 return v; // Return the modified map
             }
        });
         // Consider saving here if immediate persistence is critical, otherwise rely on auto-save
         // savePlayerData(uuid); // Avoid saving frequently inside methods manipulating keys
    }

    public void addKeys(UUID uuid, String crateType, int amount) {
        if (amount <= 0) return; // Can't add zero or negative keys
         String crateLower = crateType.toLowerCase();
        // Ensure map exists and update atomically
        playerKeys.compute(uuid, (k, v) -> {
             if (v == null) v = new ConcurrentHashMap<>(); // Create if doesn't exist
             v.put(crateLower, v.getOrDefault(crateLower, 0) + amount);
             return v;
         });
        // savePlayerData(uuid); // Avoid frequent saves
    }

    public boolean takeKeys(UUID uuid, String crateType, int amount) {
         if (amount <= 0) return true; // Taking zero or negative is always "successful" but does nothing
         String crateLower = crateType.toLowerCase();
         final boolean[] success = {false}; // Using array for final variable workaround

        // Use computeIfPresent for atomic update if player exists
        playerKeys.computeIfPresent(uuid, (k, v) -> {
             int currentKeys = v.getOrDefault(crateLower, 0);
             if (currentKeys >= amount) {
                 int newAmount = currentKeys - amount;
                 if (newAmount == 0) {
                     v.remove(crateLower); // Remove entry if keys reach zero
                 } else {
                     v.put(crateLower, newAmount); // Update with new amount
                 }
                 success[0] = true; // Mark as successful
             }
             // Return null if the inner map becomes empty, removing the player entry
             if (v.isEmpty()) return null;
             return v; // Return the (potentially modified) map
         });

        // Only save if successful? No, rely on periodic saves.
        // if(success[0]){
            // savePlayerData(uuid);
        // }
        return success[0];
    }
}
