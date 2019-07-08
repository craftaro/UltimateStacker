package com.songoda.ultimatestacker.lootables;

import com.google.gson.annotations.SerializedName;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lootable {

    // The entity applicable to this lootable.
    @SerializedName("Type")
    private final EntityType type;

    // Registered loot.
    @SerializedName("Loot")
    private final List<Loot> registeredLoot = new ArrayList<>();

    public Lootable(EntityType type, Loot... loots) {
        this.type = type;
        registeredLoot.addAll(Arrays.asList(loots));
    }

    public List<Loot> getRegisteredLoot() {
        return new ArrayList<>(registeredLoot);
    }

    public EntityType getType() {
        return type;
    }
}
