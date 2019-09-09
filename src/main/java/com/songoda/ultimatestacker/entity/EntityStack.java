package com.songoda.ultimatestacker.entity;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.lootables.loot.Drop;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.settings.Setting;
import com.songoda.ultimatestacker.utils.DropUtils;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

public class EntityStack {

    private UUID entity;
    private int amount;

    private Deque<Double> health = new ArrayDeque<>();
    UltimateStacker plugin = UltimateStacker.getInstance();

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
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12))
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
        List<Drop> preStackedDrops = new ArrayList<>();
        for (int i = 1; i < amount; i++) {
            if (i == 1) {
                drops.removeIf(it -> it.getItemStack() != null
                        && it.getItemStack().isSimilar(killed.getEquipment().getItemInHand()));
                for (ItemStack item : killed.getEquipment().getArmorContents()) {
                    drops.removeIf(it -> it.getItemStack() != null && it.getItemStack().isSimilar(item));
                }
            }
            if (custom)
                drops = plugin.getLootablesManager().getDrops(killed);
            preStackedDrops.addAll(drops);
        }
        if (!preStackedDrops.isEmpty())
            DropUtils.processStackedDrop(killed, preStackedDrops);

        killedLocation.getWorld().spawn(killedLocation, ExperienceOrb.class).setExperience(droppedExp * amount);

        if (killed.getKiller() == null) return;
        UltimateStacker.getInstance().addExp(killed.getKiller(), this);
    }

    private void handleSingleStackDeath(LivingEntity killed, List<Drop> drops) {
        EntityStackManager stackManager = plugin.getEntityStackManager();
        LivingEntity newEntity = plugin.getEntityUtils().newEntity(killed);

        updateHealth(newEntity);

        newEntity.getEquipment().clear();

        if (killed.getType() == EntityType.PIG_ZOMBIE)
            newEntity.getEquipment().setItemInHand(CompatibleMaterial.GOLDEN_SWORD.getItem());

        if (Setting.CARRY_OVER_METADATA_ON_DEATH.getBoolean()) {
            if (killed.hasMetadata("ES"))
                newEntity.setMetadata("ES", killed.getMetadata("ES").get(0));

            String entityMetadataKey = "mcMMO: Spawned Entity";
            if (killed.hasMetadata(entityMetadataKey))
                newEntity.setMetadata(entityMetadataKey, new FixedMetadataValue(UltimateStacker.getInstance(), true));
        }

        DropUtils.processStackedDrop(killed, drops);

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

        boolean killWholeStack = Setting.KILL_WHOLE_STACK_ON_DEATH.getBoolean()
                || plugin.getMobFile().getBoolean("Mobs." + killed.getType().name() + ".Kill Whole Stack");

        if (killWholeStack && getAmount() != 1) {
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
            handleSingleStackDeath(killed, drops);
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
