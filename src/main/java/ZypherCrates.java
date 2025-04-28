package com.igngecko.zyphercrates; // Changed package name

import com.igngecko.zyphercrates.commands.AdminCommand; // Changed package name
import com.igngecko.zyphercrates.commands.CratesCommand; // Changed package name
import com.igngecko.zyphercrates.config.ConfigManager; // Changed package name
import com.igngecko.zyphercrates.config.CrateManager; // Changed package name
import com.igngecko.zyphercrates.config.MessageManager; // Changed package name
import com.igngecko.zyphercrates.data.PlayerDataManager; // Changed package name
import com.igngecko.zyphercrates.gui.CrateGUI; // Changed package name
import com.igngecko.zyphercrates.listeners.InventoryClickListener; // Changed package name
import com.igngecko.zyphercrates.listeners.PlayerJoinListener; // Changed package name
import com.igngecko.zyphercrates.utils.ItemBuilder; // Changed package name
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Level;

public final class ZypherCrates extends JavaPlugin { // Renamed class

    private static ZypherCrates instance; // Changed type
    private ConfigManager configManager;
    private MessageManager messageManager;
    private CrateManager crateManager;
    private PlayerDataManager playerDataManager;
    private CrateGUI crateGUI;

    private static Economy econ = null;
    private static Permission perms = null;

    private BukkitTask autoSaveTask;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Enabling ZypherCrates v" + getDescription().getVersion()); // Changed name in message

        // Initialize managers
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        crateManager = new CrateManager(this);
        playerDataManager = new PlayerDataManager(this);
        crateGUI = new CrateGUI(this);
        ItemBuilder.setPluginInstance(this); // Set instance for ItemBuilder NBT

        // Load configurations
        if (!loadPluginConfigs()) {
             getLogger().severe("Failed to load critical configurations. Disabling plugin.");
             getServer().getPluginManager().disablePlugin(this);
             return;
        }

        // Setup Vault
        if (!setupEconomy() ) {
            getLogger().warning("Vault Economy hook not found! Money rewards will not function.");
        } else {
             getLogger().info("Vault Economy hook successful.");
        }
        if (!setupPermissions()) {
            getLogger().warning("Vault Permissions hook not found! Permission rewards will not function.");
        } else {
            getLogger().info("Vault Permissions hook successful.");
        }

        // Register commands (Using same command names for now)
        this.getCommand("crates").setExecutor(new CratesCommand(this));
        this.getCommand("zc").setExecutor(new AdminCommand(this)); // Changed alias 'vkc' to 'zc' for main admin command
        this.getCommand("zc").setTabCompleter(new AdminCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // Start auto-save task
        startAutoSaveTask();

        getLogger().info("ZypherCrates has been enabled successfully."); // Changed name in message
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling ZypherCrates..."); // Changed name in message

        // Stop auto-save task
        if (autoSaveTask != null && !autoSaveTask.isCancelled()) {
            autoSaveTask.cancel();
        }

        // Save player data one last time
        if (playerDataManager != null) {
            playerDataManager.saveAllPlayerData();
            getLogger().info("Player data saved.");
        }

        getLogger().info("ZypherCrates has been disabled."); // Changed name in message
        instance = null;
        econ = null;
        perms = null;
    }

    public boolean loadPluginConfigs() {
        try {
            configManager.loadConfig();
            messageManager.loadMessages();
            crateManager.loadCrates(); // Load crates after messages potentially needed for parsing errors
            playerDataManager.loadPlayerData(); // Load player data last
            return true;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "An error occurred while loading configurations!", e);
            return false;
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupPermissions() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            return false;
        }
        perms = rsp.getProvider();
        return perms != null;
    }

     private void startAutoSaveTask() {
        long intervalTicks = configManager.getAutoSaveInterval() * 60 * 20; // Minutes to ticks
        if (intervalTicks <= 0) {
            getLogger().info("Auto-save disabled.");
            return;
        }

        autoSaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            getLogger().info("Auto-saving player data...");
            playerDataManager.saveAllPlayerData();
             getLogger().info("Auto-save complete.");
        }, intervalTicks, intervalTicks);
         getLogger().info("Auto-save scheduled every " + configManager.getAutoSaveInterval() + " minutes.");
    }

    public static ZypherCrates getInstance() { // Changed return type
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public CrateManager getCrateManager() {
        return crateManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

     public CrateGUI getCrateGUI() {
        return crateGUI;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static Permission getPermissions() {
        return perms;
    }
}
