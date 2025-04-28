package com.igngecko.zyphercrates.config; // Changed package name

import com.igngecko.zyphercrates.ZypherCrates; // Changed package/class name
import com.igngecko.zyphercrates.utils.ColorUtils; // Added ColorUtils import
// import org.bukkit.ChatColor; // Replaced by ColorUtils
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections; // Import for Collections.emptyList()
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class MessageManager {

    private final ZypherCrates plugin; // Changed type
    private FileConfiguration messagesConfig = null;
    private File messagesFile = null;
    private final Map<String, String> messages = new HashMap<>();
    private final Map<String, List<String>> messageLists = new HashMap<>();
    private String prefix = "";

    public MessageManager(ZypherCrates plugin) { // Changed type
        this.plugin = plugin;
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        loadMessages();
    }

    public void loadMessages() {
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        // Look for defaults in the jar
        InputStream defConfigStream = plugin.getResource("messages.yml");
        if (defConfigStream != null) {
            try (InputStreamReader reader = new InputStreamReader(defConfigStream)){ // Use try-with-resources
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(reader);
                messagesConfig.setDefaults(defConfig);
                messagesConfig.options().copyDefaults(true); // Copy defaults if keys missing
                try {
                    messagesConfig.save(messagesFile);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Could not save messages.yml", e);
                }
            } catch (IOException e){
                 plugin.getLogger().log(Level.WARNING, "Failed to close default messages.yml stream.", e);
            }
        }

        messages.clear();
        messageLists.clear();
        // Load and translate prefix first using ColorUtils
        prefix = ColorUtils.translate(messagesConfig.getString("prefix", "&8[&cZC&8] &r"));

        for (String key : messagesConfig.getKeys(true)) {
            // Avoid loading parent keys like 'gui' if they aren't strings/lists themselves
             if (!messagesConfig.isConfigurationSection(key)) {
                 if (messagesConfig.isString(key) && !key.equals("prefix")) { // Don't double-translate prefix
                    messages.put(key, loadAndTranslateString(key));
                } else if (messagesConfig.isList(key)) {
                     messageLists.put(key, loadAndTranslateStringList(key));
                 }
             }
        }
         plugin.getLogger().info("Loaded " + (messages.size() + messageLists.size()) + " message entries (prefix excluded from count).");
    }

    // Helper to load and translate a string using ColorUtils
    private String loadAndTranslateString(String path) {
        String message = messagesConfig.getString(path, "&cMissing message: " + path);
        return ColorUtils.translate(message);
    }

    // Helper to load and translate a list of strings using ColorUtils
    private List<String> loadAndTranslateStringList(String path) {
        List<String> list = messagesConfig.getStringList(path);
        if (list.isEmpty() && messagesConfig.getDefaults() != null && messagesConfig.getDefaults().isList(path)) {
            list = messagesConfig.getDefaults().getStringList(path);
        }

        if (list.isEmpty()) {
            plugin.getLogger().warning("Missing or empty message list: " + path);
             // Return a translated default message within the list
            return Collections.singletonList(ColorUtils.translate("&cMissing list: " + path));
        }
         // Translate the list using ColorUtils
        return ColorUtils.translate(list);
    }


    public String getMessage(String key) {
        // Get raw (already translated) message and prepend prefix unless {no_prefix}
        String rawMessage = getRawMessage(key);
        if(rawMessage.contains("{no_prefix}")) {
             return rawMessage.replace("{no_prefix}", "");
        }
        // Prefix is already translated during loadMessages
        return prefix + rawMessage;
    }

    public String getRawMessage(String key) {
         // messages map now stores translated strings
         return messages.getOrDefault(key, ColorUtils.translate("&cUnknown message: " + key));
    }

     public List<String> getMessageList(String key) {
         // Apply prefix logic to the already translated list
         List<String> list = getRawMessageList(key); // Get raw (translated) list first
         if(list == null || list.isEmpty()) {
              // Return translated default message in a list
              return Collections.singletonList(prefix + ColorUtils.translate("&cUnknown list: " + key));
         }

         return list.stream().map(line -> {
             if(line.contains("{no_prefix}")) {
                 return line.replace("{no_prefix}", "");
             }
             // Prefix is already translated
             return prefix + line;
         }).collect(Collectors.toList());
     }

     public List<String> getRawMessageList(String key) {
         // messageLists map now stores translated lists
        return messageLists.getOrDefault(key, Collections.singletonList(ColorUtils.translate("&cUnknown list: " + key)));
     }

    public String getPrefix() {
        // Prefix is already translated
        return prefix;
    }

     // --- Helper for placeholders ---
     // These formatting methods now work on already translated strings/lists
    public String getFormattedMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key); // This gets the translated message + prefix logic
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue() != null ? entry.getValue() : ""); // Handle null placeholders
        }
        return message;
    }

    public String getFormattedRawMessage(String key, Map<String, String> placeholders) {
        String message = getRawMessage(key); // Gets the raw translated message
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue() != null ? entry.getValue() : ""); // Handle null placeholders
        }
        return message;
    }

     public List<String> getFormattedMessageList(String key, Map<String, String> placeholders) {
         List<String> messageList = getMessageList(key); // Gets translated list + prefix logic
         return messageList.stream().map(line -> {
            String formattedLine = line;
             for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                 formattedLine = formattedLine.replace(entry.getKey(), entry.getValue() != null ? entry.getValue() : ""); // Handle null placeholders
             }
             return formattedLine;
         }).collect(Collectors.toList());
     }

    public List<String> getFormattedRawMessageList(String key, Map<String, String> placeholders) {
        List<String> messageList = getRawMessageList(key); // Gets raw translated list
         return messageList.stream().map(line -> {
            String formattedLine = line;
             for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                 formattedLine = formattedLine.replace(entry.getKey(), entry.getValue() != null ? entry.getValue() : ""); // Handle null placeholders
             }
             return formattedLine;
         }).collect(Collectors.toList());
     }
}
