package com.igngecko.zyphercrates.config; // Changed package name

import com.igngecko.zyphercrates.ZypherCrates; // Changed package/class name
import com.igngecko.zyphercrates.utils.ColorUtils; // Added ColorUtils import
// import org.bukkit.ChatColor; // Replaced by ColorUtils
import org.bukkit.Material;
// import org.bukkit.configuration.ConfigurationSection; // Unused import
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
// import java.util.stream.Collectors; // No longer needed here directly

public class ConfigManager {

    private final ZypherCrates plugin; // Changed type
    private FileConfiguration config;

    private String guiTitle;
    private int guiSize;
    private boolean fillerEnabled;
    private Material fillerMaterial;
    private String fillerName;
    private List<String> fillerLore;
    private boolean broadcastRewards;
    private String minRarityToBroadcast;
    private int autoSaveInterval;


    public ConfigManager(ZypherCrates plugin) { // Changed type
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig(); // Ensures we have the latest version from disk
        config = plugin.getConfig();
        loadValues();
    }

    private void loadValues() {
        try {
             // Use ColorUtils to translate title
            guiTitle = ColorUtils.translate(config.getString("gui.title", "&1&lVirtual Crates"));
            guiSize = config.getInt("gui.size", 27);
            if (guiSize % 9 != 0 || guiSize < 9 || guiSize > 54) {
                plugin.getLogger().warning("Invalid GUI size (" + guiSize + ") in config.yml. Must be a multiple of 9 between 9 and 54. Using default (27).");
                guiSize = 27;
            }

            fillerEnabled = config.getBoolean("gui.filler.enabled", false);
            try {
                 String fillerMatString = config.getString("gui.filler.material", "BLACK_STAINED_GLASS_PANE");
                 fillerMaterial = Material.matchMaterial(fillerMatString);
                 if (fillerMaterial == null) {
                     plugin.getLogger().warning("Invalid filler material '"+ fillerMatString +"' in config.yml. Using BLACK_STAINED_GLASS_PANE.");
                     fillerMaterial = Material.BLACK_STAINED_GLASS_PANE;
                 }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid filler material specified in config.yml. Using BLACK_STAINED_GLASS_PANE.");
                fillerMaterial = Material.BLACK_STAINED_GLASS_PANE;
            }
             // Use ColorUtils for filler name and lore
            fillerName = ColorUtils.translate(config.getString("gui.filler.name", " "));
            fillerLore = ColorUtils.translate(config.getStringList("gui.filler.lore"));


            broadcastRewards = config.getBoolean("broadcast-rewards.enabled", true);
            minRarityToBroadcast = config.getString("broadcast-rewards.min-rarity-to-broadcast", "Rare"); // Rarity itself is usually not colored
            autoSaveInterval = config.getInt("auto-save-interval", 15);


        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error loading values from config.yml", e);
            // Set safe defaults if loading fails catastrophically
            guiTitle = ColorUtils.translate("&1&lVirtual Crates"); // Apply ColorUtils to default too
            guiSize = 27;
            fillerEnabled = false;
            fillerMaterial = Material.BLACK_STAINED_GLASS_PANE;
            fillerName = " ";
            fillerLore = Collections.emptyList();
            broadcastRewards = true;
            minRarityToBroadcast = "Rare";
            autoSaveInterval = 15;
        }
    }

    // --- Getters ---

    public String getGuiTitle() {
        return guiTitle;
    }

    public int getGuiSize() {
        return guiSize;
    }

     public boolean isFillerEnabled() {
        return fillerEnabled;
    }

    public Material getFillerMaterial() {
        return fillerMaterial;
    }

    public String getFillerName() {
        return fillerName;
    }

    public List<String> getFillerLore() {
        return fillerLore;
    }


    public boolean shouldBroadcastRewards() {
        return broadcastRewards;
    }

    public String getMinRarityToBroadcast() {
        return minRarityToBroadcast;
    }

    public int getAutoSaveInterval() {
        return autoSaveInterval;
    }

    // Added getter potentially needed by Reward class formatting
    public String getMoneyFormat() {
         // Example: load from config if added, otherwise default
         return config.getString("money-format", "%.2f");
    }
}
