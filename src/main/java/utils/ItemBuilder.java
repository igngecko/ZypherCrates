package com.igngecko.zyphercrates.utils; // Changed package name

import com.google.common.base.Preconditions;
// import com.igngecko.zyphercrates.ZypherCrates; // No longer needed directly here
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
// import org.jetbrains.annotations.NotNull; // Removed unused Jetbrains annotation
// import org.jetbrains.annotations.Nullable; // Removed unused Jetbrains annotation

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects; // Import Objects for requireNonNullElseGet
import java.util.stream.Collectors; // Import Collectors


/**
 * Simplified ItemBuilder utility.
 * Includes basic NBT handling via PersistentDataContainer.
 * Includes color translation using ColorUtils.
 */
public class ItemBuilder {

    // Instance should be set from the main plugin class on enable
    private static JavaPlugin PLUGIN_INSTANCE;

    private ItemStack itemStack;
    private ItemMeta itemMeta;


    /**
     * Initializes the ItemBuilder with a material and amount.
     * NOTE: You MUST call {@link ItemBuilder#setPluginInstance(JavaPlugin)} before using NBT methods.
     *
     * @param material the material
     * @param amount   the amount
     */
    public ItemBuilder(/*@NotNull*/ Material material, int amount) {
        // Ensure material is not null
        Objects.requireNonNull(material, "Material cannot be null");
        this.itemStack = new ItemStack(material, amount);
        // Get meta, using Plugin#getItemFactory#getItemMeta if available (safer)
        this.itemMeta = PLUGIN_INSTANCE != null ? PLUGIN_INSTANCE.getServer().getItemFactory().getItemMeta(material) : itemStack.getItemMeta();
         // Ensure itemMeta is not null after getting it
         if (this.itemMeta == null && PLUGIN_INSTANCE != null && material != Material.AIR) {
            // Fallback if factory returned null (shouldn't happen for valid materials other than AIR)
            this.itemMeta = itemStack.getItemMeta();
         }
    }
     /**
     * Initializes the ItemBuilder with an existing ItemStack.
     * NOTE: You MUST call {@link ItemBuilder#setPluginInstance(JavaPlugin)} before using NBT methods.
      *
      * @param itemStack the existing ItemStack (will be cloned)
      */
     public ItemBuilder(/*@NotNull*/ ItemStack itemStack) {
         // Ensure itemStack is not null
         Objects.requireNonNull(itemStack, "ItemStack cannot be null");
         this.itemStack = itemStack.clone(); // Clone to prevent modifying original
         // Get meta safely
         this.itemMeta = itemStack.hasItemMeta() ? itemStack.getItemMeta().clone() : null;
          if (this.itemMeta == null && PLUGIN_INSTANCE != null && this.itemStack.getType() != Material.AIR) {
              // If meta was null, try creating a default one using factory (unless it's AIR)
              this.itemMeta = PLUGIN_INSTANCE.getServer().getItemFactory().getItemMeta(this.itemStack.getType());
          }
     }


    /**
     * **REQUIRED** for NBT methods and safer Meta handling. Sets the plugin instance used.
     * Call this once from your plugin's onEnable method.
     *
     * @param plugin Your plugin instance.
     */
    public static void setPluginInstance(/*@NotNull*/ JavaPlugin plugin) {
         Objects.requireNonNull(plugin, "Plugin instance cannot be null");
         if (PLUGIN_INSTANCE == null) { // Only set if not already set
            PLUGIN_INSTANCE = plugin;
         }
    }


    /**
     * Sets the display name of the item. Translates standard and hex color codes.
     *
     * @param name the name to set
     * @return the ItemBuilder instance
     */
    public ItemBuilder setName(/*@NotNull*/ String name) {
         Objects.requireNonNull(name, "Name cannot be null");
        if (itemMeta != null) {
            // Use ColorUtils to translate the name
            itemMeta.setDisplayName(ColorUtils.translate(name));
        } else {
             logMetaWarning("setName");
        }
        return this;
    }

    public boolean hasName() {
        return itemMeta != null && itemMeta.hasDisplayName();
    }

    public String getName() {
        return itemMeta != null && itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : null;
    }

    /**
     * Sets the lore of the item. Translates standard and hex color codes in each line.
     *
     * @param lore the list of lore lines
     * @return the ItemBuilder instance
     */
    public ItemBuilder setLore(/*@NotNull*/ List<String> lore) {
        Objects.requireNonNull(lore, "Lore list cannot be null");
        if (itemMeta != null) {
            // Use ColorUtils to translate the list
            itemMeta.setLore(ColorUtils.translate(lore));
        } else {
             logMetaWarning("setLore");
        }
        return this;
    }

    /**
     * Sets the lore of the item from varargs. Translates standard and hex color codes.
     *
     * @param lore the lore lines
     * @return the ItemBuilder instance
     */
    public ItemBuilder setLore(String... lore) {
        return setLore(Arrays.asList(lore));
    }

     /**
      * Adds a line to the item's lore. Translates standard and hex color codes.
      * @param line The line to add.
      * @return The ItemBuilder instance.
      */
     public ItemBuilder addLoreLine(/*@NotNull*/ String line) {
         Objects.requireNonNull(line, "Lore line cannot be null");
         if (itemMeta != null) {
             List<String> lore = itemMeta.hasLore() ? new ArrayList<>(itemMeta.getLore()) : new ArrayList<>();
             // Use ColorUtils to translate the new line
             lore.add(ColorUtils.translate(line));
             itemMeta.setLore(lore);
         } else {
              logMetaWarning("addLoreLine");
         }
         return this;
     }


    public boolean hasLore() {
        return itemMeta != null && itemMeta.hasLore();
    }

     public List<String> getLore() {
         // Note: This returns the already translated lore
         return itemMeta != null && itemMeta.hasLore() ? itemMeta.getLore() : null;
     }


    /**
     * Sets the amount of items in the stack.
     *
     * @param amount the amount
     * @return the ItemBuilder instance
     */
    public ItemBuilder setAmount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    /**
     * Adds an enchantment to the item.
     *
     * @param enchantment       the enchantment to add
     * @param level             the level of the enchantment
     * @param ignoreRestrictions whether to ignore level restrictions
     * @return the ItemBuilder instance
     */
    public ItemBuilder addEnchant(/*@NotNull*/ Enchantment enchantment, int level, boolean ignoreRestrictions) {
        Objects.requireNonNull(enchantment, "Enchantment cannot be null");
        if (itemMeta != null) {
            itemMeta.addEnchant(enchantment, level, ignoreRestrictions);
        } else {
             logMetaWarning("addEnchant");
        }
        return this;
    }

    /**
     * Adds multiple enchantments to the item.
     *
     * @param enchantments       a map of enchantments and their levels
     * @param ignoreRestrictions whether to ignore level restrictions
     * @return the ItemBuilder instance
     */
    public ItemBuilder addEnchants(/*@NotNull*/ Map<Enchantment, Integer> enchantments, boolean ignoreRestrictions) {
        Objects.requireNonNull(enchantments, "Enchantments map cannot be null");
         if (itemMeta != null && !enchantments.isEmpty()) {
             enchantments.forEach((enc, lvl) -> {
                 if (enc != null && lvl != null) { // Check for nulls in map
                    itemMeta.addEnchant(enc, lvl, ignoreRestrictions);
                 }
             });
        } else if (itemMeta == null) {
              logMetaWarning("addEnchants");
        }
        return this;
    }

    /**
     * Removes an enchantment from the item.
     * @param enchantment The enchantment to remove.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder removeEnchant(/*@NotNull*/ Enchantment enchantment) {
        Objects.requireNonNull(enchantment, "Enchantment cannot be null");
        if (itemMeta != null) {
            itemMeta.removeEnchant(enchantment);
        } else {
             logMetaWarning("removeEnchant");
        }
        return this;
    }


    /**
     * Adds ItemFlags to the item.
     *
     * @param flags the flags to add
     * @return the ItemBuilder instance
     */
    public ItemBuilder addFlag(ItemFlag... flags) {
        Objects.requireNonNull(flags, "Flags cannot be null");
         if (itemMeta != null) {
            itemMeta.addItemFlags(flags);
        } else {
             logMetaWarning("addFlag");
        }
        return this;
    }

    /**
     * Removes ItemFlags from the item.
     * @param flags The flags to remove.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder removeFlag(ItemFlag... flags) {
        Objects.requireNonNull(flags, "Flags cannot be null");
         if (itemMeta != null) {
            itemMeta.removeItemFlags(flags);
        } else {
              logMetaWarning("removeFlag");
        }
        return this;
    }

    /**
     * Sets the item to be unbreakable.
     *
     * @param unbreakable true to make unbreakable, false otherwise
     * @return the ItemBuilder instance
     */
    public ItemBuilder setUnbreakable(boolean unbreakable) {
         if (itemMeta != null) {
            // Catch potential NoSuchMethodError for older versions before 1.11
            try {
                itemMeta.setUnbreakable(unbreakable);
            } catch (NoSuchMethodError e) {
                 logMetaWarning("setUnbreakable (not supported on this server version)");
            }
        } else {
              logMetaWarning("setUnbreakable");
        }
        return this;
    }


     /**
     * Adds the enchantment glow effect without adding an actual enchantment.
     * Uses a dummy enchantment and hides it with an ItemFlag.
     *
     * @return The ItemBuilder instance.
     */
    public ItemBuilder addGlow() {
         if (itemMeta != null) {
            // Use a relatively unused enchant like LURE or LOYALTY if available
            Enchantment glowEnchant = Enchantment.LURE; // Default choice
            itemMeta.addEnchant(glowEnchant, 1, false); // Add dummy enchant
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS); // Hide it
        } else {
              logMetaWarning("addGlow");
        }
        return this;
    }


     /**
     * Removes the enchantment glow effect if added via {@link #addGlow()}.
     *
     * @return The ItemBuilder instance.
     */
    public ItemBuilder removeGlow() {
         Enchantment glowEnchant = Enchantment.LURE; // Must match the one used in addGlow()
         if (itemMeta != null && itemMeta.hasItemFlag(ItemFlag.HIDE_ENCHANTS) && itemMeta.hasEnchant(glowEnchant)) {
             itemMeta.removeEnchant(glowEnchant);
             // Only remove the HIDE_ENCHANTS flag if this was the *only* enchant or if no other enchants are present
             if (!itemMeta.hasEnchants()) {
                 itemMeta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
             }
        } else if (itemMeta == null) {
            logMetaWarning("removeGlow");
        }
        return this;
    }


    /**
     * Sets the damage (durability) of the item.
     * Only works for items with durability (implements Damageable).
     *
     * @param damage The amount of damage.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder setDamage(int damage) {
        if (itemMeta instanceof Damageable) {
            ((Damageable) itemMeta).setDamage(damage);
        } else if (itemMeta != null) {
            // Log warning if trying to set damage on non-damageable item?
        } else {
             logMetaWarning("setDamage");
        }
        return this;
    }


     /**
     * Sets the color of a leather armor piece.
     *
     * @param color The color to set.
     * @return The ItemBuilder instance.
     */
     public ItemBuilder setLeatherArmorColor(/*@NotNull*/ Color color) {
         Objects.requireNonNull(color, "Color cannot be null");
         if (itemMeta instanceof LeatherArmorMeta) {
             ((LeatherArmorMeta) itemMeta).setColor(color);
         } else if (itemMeta != null) {
              // Log warning if trying on non-leather armor
         } else {
             logMetaWarning("setLeatherArmorColor");
         }
         return this;
     }

    /**
     * Sets the potion data for a potion item.
     *
     * @param potionData The PotionData to set.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder setPotionData(/*@NotNull*/ PotionData potionData) {
        Objects.requireNonNull(potionData, "PotionData cannot be null");
        if (itemMeta instanceof PotionMeta) {
            ((PotionMeta) itemMeta).setBasePotionData(potionData);
        } else if (itemMeta != null) {
             // Log warning if trying on non-potion
        } else {
             logMetaWarning("setPotionData");
        }
        return this;
    }

     /**
     * Sets the custom model data for the item.
     * Requires 1.14+
     *
     * @param data The custom model data value.
     * @return The ItemBuilder instance.
     */
     public ItemBuilder setCustomModelData(int data) {
        if (itemMeta != null) {
             try {
                 itemMeta.setCustomModelData(data);
             } catch (NoSuchMethodError e) {
                 // Handle older versions that don't have this method gracefully
                 if (PLUGIN_INSTANCE != null)
                     PLUGIN_INSTANCE.getLogger().warning("CustomModelData is not supported on this server version.");
             }
        } else {
            logMetaWarning("setCustomModelData");
        }
        return this;
     }

    // --- NBT Methods using PersistentDataContainer ---

     private static NamespacedKey getKey(String key) {
        Objects.requireNonNull(PLUGIN_INSTANCE, "Plugin instance not set! Call ItemBuilder.setPluginInstance() first.");
        // Basic validation for key format if desired (e.g., lowercase, no spaces)
        // Using Objects.requireNonNullElse ensures key is not null before toLowerCase()
        return new NamespacedKey(PLUGIN_INSTANCE, Objects.requireNonNullElse(key, "null_key").toLowerCase());
     }

     private PersistentDataContainer getPersistentDataContainer() {
         if (itemMeta == null) {
             logMetaWarning("getPersistentDataContainer (needed for NBT)");
             return null; // Return null if meta is missing
         }
         return itemMeta.getPersistentDataContainer();
     }

    /**
     * Adds a String NBT tag to the item.
     * Requires {@link ItemBuilder#setPluginInstance(JavaPlugin)} to be called first.
     *
     * @param key   The key for the tag.
     * @param value The String value.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder addNBTString(/*@NotNull*/ String key, /*@NotNull*/ String value) {
        Objects.requireNonNull(key, "NBT key cannot be null");
        Objects.requireNonNull(value, "NBT value cannot be null");
        PersistentDataContainer container = getPersistentDataContainer();
        if (container != null) {
            container.set(getKey(key), PersistentDataType.STRING, value);
        }
        return this;
    }

     /**
     * Adds an Integer NBT tag to the item.
      * Requires {@link ItemBuilder#setPluginInstance(JavaPlugin)} to be called first.
      *
      * @param key   The key for the tag.
      * @param value The Integer value.
      * @return The ItemBuilder instance.
      */
    public ItemBuilder addNBTInt(/*@NotNull*/ String key, int value) {
        Objects.requireNonNull(key, "NBT key cannot be null");
        PersistentDataContainer container = getPersistentDataContainer();
        if (container != null) {
            container.set(getKey(key), PersistentDataType.INTEGER, value);
        }
        return this;
    }

    /**
     * Adds a Double NBT tag to the item.
     * Requires {@link ItemBuilder#setPluginInstance(JavaPlugin)} to be called first.
     *
     * @param key   The key for the tag.
     * @param value The Double value.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder addNBTDouble(/*@NotNull*/ String key, double value) {
        Objects.requireNonNull(key, "NBT key cannot be null");
        PersistentDataContainer container = getPersistentDataContainer();
        if (container != null) {
            container.set(getKey(key), PersistentDataType.DOUBLE, value);
        }
        return this;
    }

    /**
     * Removes an NBT tag from the item.
     * Requires {@link ItemBuilder#setPluginInstance(JavaPlugin)} to be called first.
     *
     * @param key The key of the tag to remove.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder removeNBT(/*@NotNull*/ String key) {
        Objects.requireNonNull(key, "NBT key cannot be null");
         PersistentDataContainer container = getPersistentDataContainer();
        if (container != null) {
            container.remove(getKey(key));
        }
        return this;
    }

    /**
     * Checks if the item has a specific NBT tag.
     * Requires {@link ItemBuilder#setPluginInstance(JavaPlugin)} to be called first.
     *
     * @param key The key to check.
     * @param type The PersistentDataType to check for (e.g., PersistentDataType.STRING).
     * @return True if the tag exists with the specified type, false otherwise.
     */
     public <T, Z> boolean hasNBT(/*@NotNull*/ String key, /*@NotNull*/ PersistentDataType<T, Z> type) {
         Objects.requireNonNull(key, "NBT key cannot be null");
         Objects.requireNonNull(type, "NBT type cannot be null");
          PersistentDataContainer container = getPersistentDataContainer();
         if (container != null) {
             return container.has(getKey(key), type);
         }
         return false;
     }

    /**
     * Gets a String NBT tag from an ItemStack.
     * Requires {@link ItemBuilder#setPluginInstance(JavaPlugin)} to be called first.
     * Returns null if the tag is missing, the item has no meta, or the tag is not a String.
     *
     * @param itemStack The ItemStack to check.
     * @param key       The key of the tag.
     * @return The String value, or null.
     */
    // @Nullable
    public static String getNBTString(/*@NotNull*/ ItemStack itemStack, /*@NotNull*/ String key) {
        Objects.requireNonNull(itemStack, "ItemStack cannot be null");
        Objects.requireNonNull(key, "NBT key cannot be null");
        Objects.requireNonNull(PLUGIN_INSTANCE, "Plugin instance not set! Call ItemBuilder.setPluginInstance() first.");
         if (itemStack.hasItemMeta()) {
             ItemMeta meta = itemStack.getItemMeta();
              if(meta != null){ // Double check meta isn't null
                 PersistentDataContainer container = meta.getPersistentDataContainer();
                  if(container.has(getKey(key), PersistentDataType.STRING)){
                     return container.get(getKey(key), PersistentDataType.STRING);
                  }
              }
         }
        return null;
    }

    /**
     * Gets an Integer NBT tag from an ItemStack.
     * Requires {@link ItemBuilder#setPluginInstance(JavaPlugin)} to be called first.
     * Returns null if the tag is missing, the item has no meta, or the tag is not an Integer.
     *
     * @param itemStack The ItemStack to check.
     * @param key       The key of the tag.
     * @return The Integer value, or null.
     */
    // @Nullable
    public static Integer getNBTInt(/*@NotNull*/ ItemStack itemStack, /*@NotNull*/ String key) {
        Objects.requireNonNull(itemStack, "ItemStack cannot be null");
        Objects.requireNonNull(key, "NBT key cannot be null");
        Objects.requireNonNull(PLUGIN_INSTANCE, "Plugin instance not set! Call ItemBuilder.setPluginInstance() first.");
        if (itemStack.hasItemMeta()) {
             ItemMeta meta = itemStack.getItemMeta();
              if(meta != null){
                 PersistentDataContainer container = meta.getPersistentDataContainer();
                  if(container.has(getKey(key), PersistentDataType.INTEGER)){
                     return container.get(getKey(key), PersistentDataType.INTEGER);
                  }
              }
        }
        return null;
    }

     /**
     * Gets a Double NBT tag from an ItemStack.
      * Requires {@link ItemBuilder#setPluginInstance(JavaPlugin)} to be called first.
      * Returns null if the tag is missing, the item has no meta, or the tag is not a Double.
      *
      * @param itemStack The ItemStack to check.
      * @param key       The key of the tag.
      * @return The Double value, or null.
      */
     // @Nullable
     public static Double getNBTDouble(/*@NotNull*/ ItemStack itemStack, /*@NotNull*/ String key) {
         Objects.requireNonNull(itemStack, "ItemStack cannot be null");
         Objects.requireNonNull(key, "NBT key cannot be null");
        Objects.requireNonNull(PLUGIN_INSTANCE, "Plugin instance not set! Call ItemBuilder.setPluginInstance() first.");
         if (itemStack.hasItemMeta()) {
             ItemMeta meta = itemStack.getItemMeta();
              if(meta != null){
                 PersistentDataContainer container = meta.getPersistentDataContainer();
                  if(container.has(getKey(key), PersistentDataType.DOUBLE)){
                     return container.get(getKey(key), PersistentDataType.DOUBLE);
                  }
              }
         }
         return null;
     }

    /**
     * Builds the ItemStack with all the applied modifications.
     * Applies the potentially modified ItemMeta back to the ItemStack.
     *
     * @return the final ItemStack (a clone of the internal stack with updated meta).
     */
    public ItemStack build() {
        if (itemMeta != null) {
            itemStack.setItemMeta(itemMeta); // Apply the modified meta back to the stack
        }
        return itemStack; // Return the internal stack instance which has been modified
    }

    // Helper to log warnings if meta is null when expected
    private void logMetaWarning(String operation) {
        if (PLUGIN_INSTANCE != null && itemStack != null) { // Check itemStack not null either
             PLUGIN_INSTANCE.getLogger().warning("ItemBuilder: Attempted to perform '" + operation + "' on item type '" + itemStack.getType() + "' which has null ItemMeta. Operation skipped.");
        } else if (PLUGIN_INSTANCE != null) {
             PLUGIN_INSTANCE.getLogger().warning("ItemBuilder: Attempted to perform '" + operation + "' but ItemMeta is null (ItemBuilder might not be initialized correctly or item type has no meta). Operation skipped.");
        }
        // Avoid logging if PLUGIN_INSTANCE is null, as logging wouldn't work anyway.
    }
}
