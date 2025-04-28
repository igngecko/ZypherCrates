package com.igngecko.zyphercrates.crate; // Changed package name

import org.bukkit.inventory.ItemStack;

import java.util.Collections; // Import Collections
import java.util.List;
import java.util.Random;

public class Crate {

    private final String id;
    private final int guiSlot;
    private final ItemStack guiItem; // The actual ItemStack for the GUI
    private final boolean guiItemEnchanted; // Separate flag for glow effect
    private final List<Reward> rewards;
    private final double totalWeight; // Can be total percentage (100) or total weight
    private final boolean useWeights;
    private final Random random = new Random();

    public Crate(String id, int guiSlot, ItemStack guiItem, boolean guiItemEnchanted, List<Reward> rewards, double totalWeight, boolean useWeights) {
        this.id = id;
        this.guiSlot = guiSlot;
        this.guiItem = guiItem;
        this.guiItemEnchanted = guiItemEnchanted;
        // Store an unmodifiable list to prevent external modification
        this.rewards = Collections.unmodifiableList(rewards);
        this.totalWeight = totalWeight;
        this.useWeights = useWeights;
    }

    public String getId() {
        return id;
    }

    public int getGuiSlot() {
        return guiSlot;
    }

    public ItemStack getGuiItem() {
        return guiItem.clone(); // Return a clone to prevent modification
    }

     public boolean isGuiItemEnchanted() {
        return guiItemEnchanted;
     }

    public List<Reward> getRewards() {
        return rewards; // Return the unmodifiable list
    }

    // Selects a reward based on chance/weight
    public Reward getRandomReward() {
        if (rewards.isEmpty()) {
            return null;
        }
        // Handle edge case where totalWeight is 0 or less (invalid config)
        if (totalWeight <= 0) {
             if (!rewards.isEmpty()) return rewards.get(random.nextInt(rewards.size())); // Pick one completely at random if weights are invalid
             else return null;
        }

        double randomValue = random.nextDouble() * totalWeight; // Scale random value to total weight/percentage
        double cumulativeWeight = 0;

        for (Reward reward : rewards) {
            cumulativeWeight += reward.getChance(); // Chance here acts as weight if useWeights is true
            if (randomValue <= cumulativeWeight) {
                return reward;
            }
        }

        // Fallback in case of floating point inaccuracies or empty rewards list (should not happen with checks)
        // Return the last reward if loop finishes slightly due to precision issues
         return rewards.get(rewards.size() - 1);
    }

    public boolean usesWeights() {
        return useWeights;
    }

    public double getTotalWeight() {
        return totalWeight;
    }
}
