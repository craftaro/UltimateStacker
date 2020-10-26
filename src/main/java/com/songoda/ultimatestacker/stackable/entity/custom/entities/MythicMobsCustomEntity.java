package com.songoda.ultimatestacker.stackable.entity.custom.entities;

import com.songoda.ultimatestacker.stackable.entity.custom.CustomEntity;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MobManager;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class MythicMobsCustomEntity extends CustomEntity {

    public  MythicMobsCustomEntity() {
        super(Bukkit.getPluginManager().getPlugin("MythicMobs"));
    }

    @Override
    public String getPluginName() {
        return "MythicMobs";
    }

    @Override
    public boolean isMatchingType(Entity entity) {
        return getMobManager().isActiveMob(entity.getUniqueId());
    }

    @Override
    public String getDisplayName(Entity entity) {
        return getMobManager().getMythicMobInstance(entity).getType().getDisplayName().toString();
    }

    @Override
    public boolean isSimilar(LivingEntity original, LivingEntity entity) {
        if (!isMatchingType(original) || !isMatchingType(entity)) return false;
        return getMob(original).getType().equals(getMob(entity).getType());
    }

    @Override
    public String getNBTIdentifier(Entity entity) {
        return getMob(entity).getType().getInternalName();
    }

    @Override
    public LivingEntity spawnFromIdentifier(String string, Location location) {
        if (getMobManager().getMobTypes().stream().map(MythicMob::getInternalName).noneMatch(t -> t.equals(string)))
            return null;
        return (LivingEntity)getMobManager().spawnMob(string, location).getEntity().getBukkitEntity();
    }

    private ActiveMob getMob(Entity entity) {
        return getMobManager().getMythicMobInstance(entity);
    }

    private MobManager getMobManager() {
        return ((MythicMobs) plugin).getMobManager();
    }
}
