package com.songoda.ultimatestacker.lootables;


import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public final class LootBuilder {

    private final Loot loot;

    public LootBuilder() {
        this.loot = new Loot();
    }

    public LootBuilder setMaterial(Material material) {
        this.loot.setMaterial(material);
        return this;
    }

    public LootBuilder setData(int data) {
        return setData((short)data);
    }

    public LootBuilder setData(short data) {
        this.loot.setData(data);
        return this;
    }

    public LootBuilder setBurnedMaterial(Material material) {
        this.loot.setBurnedMaterial(material);
        return this;
    }

    public LootBuilder setChance(double chance) {
        this.loot.setChance(chance);
        return this;
    }

    public LootBuilder setMin(int min) {
        this.loot.setMin(min);
        return this;
    }

    public LootBuilder setMax(int max) {
        this.loot.setMax(max);
        return this;
    }

    public LootBuilder setAllowLootingEnchant(boolean allow) {
        this.loot.setAllowLootingEnchant(allow);
        return this;
    }

    public LootBuilder setLootingIncrease(double increase) {
        this.loot.setLootingIncrease(increase);
        return this;
    }

    public LootBuilder addOnlyDropFors(EntityType... types) {
        this.loot.addOnlyDropFors(types);
        return this;
    }

    public LootBuilder addChildLoot(Loot... loots) {
        this.loot.addChildLoots(loots);
        return this;
    }

    public LootBuilder setChildDropCount(int count) {
        this.loot.setChildDropCountMin(count);
        this.loot.setChildDropCountMax(count);
        return this;
    }

    public LootBuilder setChildDropCounMin(int count) {
        this.loot.setChildDropCountMin(count);
        return this;
    }

    public LootBuilder setChildDropCountMax(int count) {
        this.loot.setChildDropCountMax(count);
        return this;
    }
    
    public Loot build() {
        return this.loot;
    }
}