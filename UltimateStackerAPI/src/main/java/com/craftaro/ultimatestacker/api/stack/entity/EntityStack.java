package com.craftaro.ultimatestacker.api.stack.entity;

import com.craftaro.ultimatestacker.api.events.entity.EntityStackKillEvent;
import com.craftaro.ultimatestacker.api.utils.StackableEntity;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.lootables.loot.Drop;
import com.songoda.core.lootables.loot.DropUtils;
import com.songoda.core.utils.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public interface EntityStack extends StackableEntity {


    EntityType getType();

    UUID getUuid();

    LivingEntity getHostEntity();

    LivingEntity takeOneAndSpawnEntity(Location location);

    void releaseHost();

    void destroy();
}
