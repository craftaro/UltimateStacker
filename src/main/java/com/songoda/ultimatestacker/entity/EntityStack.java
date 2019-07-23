package com.songoda.ultimatestacker.entity;

import com.songoda.lootables.loot.Drop;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.utils.Methods;
import com.songoda.ultimatestacker.utils.ServerVersion;
import com.songoda.ultimatestacker.utils.settings.Setting;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

public class EntityStack {

    private UUID entity;
    private int amount;

    private Deque<Double> health = new ArrayDeque<>();

    public EntityStack(LivingEntity entity, int amount) {
        this(entity.getUniqueId(), amount);
        health.add(entity.getHealth());
    }

    public EntityStack(UUID uuid, int amount) {
        this.entity = uuid;
        this.setAmount(amount);
    }

    public void updateStack() {
        if (!Setting.ENTITY_HOLOGRAMS.getBoolean()) return;

        Bukkit.getScheduler().scheduleSyncDelayedTask(UltimateStacker.getInstance(), () -> {
            Entity entit = getEntityByUniqueId(this.entity);
            if (entit == null ||
                    !UltimateStacker.getInstance().getEntityStackManager().isStacked(entity)) return;

            entit.setCustomNameVisible(!Setting.HOLOGRAMS_ON_LOOK_ENTITY.getBoolean());
            entit.setCustomName(Methods.compileEntityName(entit, amount));
        }, entity == null ? 1L : 0L);

    }

    public LivingEntity getEntity() {
        LivingEntity entity = getEntityByUniqueId(this.entity);
        if (entity == null) {
            UltimateStacker.getInstance().getEntityStackManager().removeStack(this.entity);
            return null;
        }
        return entity;
    }

    protected void setEntity(Entity entity) {
        this.entity = entity.getUniqueId();
    }

    public void addAmount(int amount) {
        this.amount = this.amount + amount;
        updateStack();
    }

    public UUID getEntityUniqueId() {
        return entity;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        if (amount == 1) {
            Entity entity = getEntityByUniqueId(this.entity);
            if (entity == null) return;

            UltimateStacker.getInstance().getEntityStackManager().removeStack(this.entity);
            entity.setCustomName(null);
            entity.setCustomNameVisible(false);
            return;
        }
        this.amount = amount;
        if (amount != 0)
            updateStack();
    }

    private LivingEntity getEntityByUniqueId(UUID uniqueId) {
        if (UltimateStacker.getInstance().isServerVersionAtLeast(ServerVersion.V1_12))
            return (LivingEntity) Bukkit.getEntity(uniqueId);

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(uniqueId))
                    return (LivingEntity) entity;
            }
        }

        return null;
    }

    private void handleWholeStackDeath(LivingEntity killed, List<Drop> drops, boolean custom, int droppedExp) {
        Location killedLocation = killed.getLocation();
        for (int i = 1; i < amount; i++) {
            if (i == 1) {
                drops.removeIf(it -> it.getItemStack() != null
                        && it.getItemStack().isSimilar(killed.getEquipment().getItemInHand()));
                for (ItemStack item : killed.getEquipment().getArmorContents()) {
                    drops.removeIf(it -> it.getItemStack() != null && it.getItemStack().isSimilar(item));
                }
            }
            if (custom)
                drops = UltimateStacker.getInstance().getLootablesManager().getDrops(killed);
            for (Drop drop : drops) {
                Methods.processDrop(killed, drop);
            }
        }

        killedLocation.getWorld().spawn(killedLocation, ExperienceOrb.class).setExperience(droppedExp * amount);

        if (killed.getKiller() == null) return;
        UltimateStacker.getInstance().addExp(killed.getKiller(), this);
    }

    private void handleSingleStackDeath(LivingEntity killed) {
        UltimateStacker instance = UltimateStacker.getInstance();
        EntityStackManager stackManager = instance.getEntityStackManager();
        LivingEntity newEntity = Methods.newEntity(killed);

        updateHealth(newEntity);

        newEntity.getEquipment().clear();

        if (killed.getType() == EntityType.PIG_ZOMBIE)
            newEntity.getEquipment().setItemInHand(new ItemStack(instance.isServerVersionAtLeast(ServerVersion.V1_13)
                    ? Material.GOLDEN_SWORD : Material.valueOf("GOLD_SWORD")));

        if (Setting.CARRY_OVER_METADATA_ON_DEATH.getBoolean()) {
            if (killed.hasMetadata("ES"))
                newEntity.setMetadata("ES", killed.getMetadata("ES").get(0));

            String entityMetadataKey = "mcMMO: Spawned Entity";
            if (killed.hasMetadata(entityMetadataKey))
                newEntity.setMetadata(entityMetadataKey, new FixedMetadataValue(UltimateStacker.getInstance(), true));
        }

        EntityStack entityStack = stackManager.updateStack(killed, newEntity);

        entityStack.addAmount(-1);

        if (entityStack.getAmount() <= 1) {
            stackManager.removeStack(newEntity);
            newEntity.setCustomNameVisible(false);
            newEntity.setCustomName(null);
        }
    }

    public void onDeath(LivingEntity killed, List<Drop> drops, boolean custom, int droppedExp) {
        killed.setCustomName(null);
        killed.setCustomNameVisible(true);
        killed.setCustomName(Methods.formatText("&7"));

        if (Setting.KILL_WHOLE_STACK_ON_DEATH.getBoolean() && getAmount() != 1) {
            handleWholeStackDeath(killed, drops, custom, droppedExp);
        } else if (getAmount() != 1) {
            List<String> reasons = Setting.INSTANT_KILL.getStringList();
            EntityDamageEvent lastDamageCause = killed.getLastDamageCause();

            if (lastDamageCause != null) {
                EntityDamageEvent.DamageCause cause = lastDamageCause.getCause();
                for (String s : reasons) {
                    if (!cause.name().equalsIgnoreCase(s)) continue;
                    handleWholeStackDeath(killed, drops, custom, Setting.NO_EXP_INSTANT_KILL.getBoolean() ? 0 : droppedExp);
                    return;
                }
            }
            handleSingleStackDeath(killed);
        }
    }

    public void updateHealth(LivingEntity entity) {
        if (entity == null) return;
        entity.setHealth(Setting.STACK_ENTITY_HEALTH.getBoolean()
                && !this.health.isEmpty() ? this.health.removeFirst() : entity.getMaxHealth());
    }

    public void addHealth(double health) {
        this.health.addLast(health);
    }

    public void mergeHealth(EntityStack stack) {
        this.health.addAll(stack.health);
    }

    @Override
    public String toString() {
        return "EntityStack:{"
                + "Entity:\"" + entity.toString() + "\","
                + "Amount:\"" + amount + "\","
                + "}";
    }
}
