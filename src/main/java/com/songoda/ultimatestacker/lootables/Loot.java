package com.songoda.ultimatestacker.lootables;

import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Loot {

    // Material used for this drop.
    @SerializedName("Type")
    private Material material;

    // Data value for old crappy versions of Minecraft.
    @SerializedName("Data")
    private Short data;

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

    public Short getData() {
        return data;
    }

    public void setData(Short data) {
        this.data = data;
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
