package com.songoda.ultimatestacker.lootables;

import com.google.gson.annotations.SerializedName;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;

import java.util.*;

public class Loot {

    // Material used for this drop.
    @SerializedName("Command")
    private String command;

    // Material used for this drop.
    @SerializedName("Type")
    private Material material;

    // Data value for old crappy versions of Minecraft.
    @SerializedName("Data")
    private Short data;

    // The override for the item name.
    @SerializedName("Name")
    private String name = null;

    // The override for the item lore.
    @SerializedName("Lore")
    private List<String> lore = null;

    // The override for the item enchantments.
    @SerializedName("Enchantments")
    private Map<String, Integer> enchants = null;

    // Material used if entity died on fire.
    @SerializedName("Burned Type")
    private Material burnedMaterial = null;

    // Chance that this drop will take place.
    @SerializedName("Chance")
    private double chance = 100;

    // Minimum amount of this item.
    @SerializedName("Min")
    private int min = 1;

    // Maximum amount of this item.
    @SerializedName("Max")
    private int max = 1;

    // Will the looting enchantment be usable for this loot?
    @SerializedName("Looting")
    private boolean allowLootingEnchant = true;

    // The looting chance increase.
    @SerializedName("Looting Chance Increase")
    private Double lootingIncrease;

    // Should this drop only be applicable for specific entities?
    @SerializedName("Only Drop For")
    private List<EntityType> onlyDropFor;

    // How many child loots should drop?
    @SerializedName("Child Loot Drop Count Min")
    private Integer childDropCountMin;
    @SerializedName("Child Loot Drop Count Max")
    private Integer childDropCountMax;

    // Should this drop house child drops?
    @SerializedName("Child Loot")
    private List<Loot> childLoot;

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Short getData() {
        return data;
    }

    public void setData(Short data) {
        this.data = data;
    }

    public String getName() {
        return Methods.formatText(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getLore() {
        if (lore == null) return null;
        List<String> lore = new ArrayList<>();
        for (String line : this.lore)
            lore.add(Methods.formatText(line));

        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = new ArrayList<>(lore);
    }

    public Map<Enchantment, Integer> getEnchants() {
        if (enchants == null) return null;
        Map<Enchantment, Integer> enchants = new HashMap<>();
        for (Map.Entry<String, Integer> entry : this.enchants.entrySet())
            enchants.put(Enchantment.getByName(entry.getKey()), entry.getValue());
        return enchants;
    }

    public void setEnchants(Map<String, Integer> enchants) {
        this.enchants = enchants;
    }

    public Material getBurnedMaterial() {
        return burnedMaterial;
    }

    public void setBurnedMaterial(Material burnedMaterial) {
        this.burnedMaterial = burnedMaterial;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public boolean runChance(int looting) {
        return (Math.random() * 100) - (chance + (lootingIncrease == null ? 1
                : lootingIncrease * looting)) < 0 || chance == 100;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getAmountToDrop(int looting) {
        return min == max ? (max + getLooting(looting)) : new Random().nextInt((max + getLooting(looting)) - min + 1) + min;
    }

    public int getLooting(int looting) {
        return allowLootingEnchant ? looting : 0;
    }

    public boolean isAllowLootingEnchant() {
        return allowLootingEnchant;
    }

    public void setAllowLootingEnchant(boolean allowLootingEnchant) {
        this.allowLootingEnchant = allowLootingEnchant;
    }

    public void setLootingIncrease(double increase) {
        this.lootingIncrease = increase;
    }

    public List<Loot> getChild() {
        return childLoot == null ? new ArrayList<>() : new ArrayList<>(childLoot);
    }

    public void addChildLoots(Loot... loots) {
        this.childDropCountMin = 1;
        this.childDropCountMax = 1;
        this.childLoot = new ArrayList<>();
        this.childLoot.addAll(Arrays.asList(loots));
    }

    public List<Loot> getChildLoot() {
        return childLoot == null ? new ArrayList<>() : new ArrayList<>(childLoot);
    }

    public List<EntityType> getOnlyDropFor() {
        return onlyDropFor == null ? new ArrayList<>() : new ArrayList<>(onlyDropFor);
    }

    public void addOnlyDropFors(EntityType... types) {
        this.onlyDropFor = new ArrayList<>();
        this.onlyDropFor.addAll(Arrays.asList(types));
    }

    public void setChildDropCountMin(int childDropCountMin) {
        this.childDropCountMin = childDropCountMin;
    }

    public void setChildDropCountMax(int childDropCountMax) {
        this.childDropCountMax = childDropCountMax;
    }

    public int getChildDropCount() {
        if (childDropCountMin == null || childDropCountMax == null) return 0;
        return new Random().nextInt(childDropCountMax - min + 1) + childDropCountMin;
    }
}
