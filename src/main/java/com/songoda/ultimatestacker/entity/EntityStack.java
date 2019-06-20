package com.songoda.ultimatestacker.entity;

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

import java.util.List;
import java.util.UUID;

public class EntityStack {

    private UUID entity;
    private int amount;

    public EntityStack(Entity entity, int amount) {
        this(entity.getUniqueId(), amount);
    }

    public EntityStack(UUID uuid, int amount) {
        this.entity = uuid;
        this.setAmount(amount);
    }

    public void updateStack() {
        if (!Setting.ENTITY_HOLOGRAMS.getBoolean()) return;

        Entity entity = getEntityByUniqueId(this.entity);
        Bukkit.getScheduler().scheduleSyncDelayedTask(UltimateStacker.getInstance(), () -> {
            Entity entit = getEntityByUniqueId(this.entity);
            if (entit == null ||
                    !UltimateStacker.getInstance().getEntityStackManager().isStacked(entity)) return;

            entit.setCustomNameVisible(!Setting.HOLOGRAMS_ON_LOOK_ENTITY.getBoolean());
            entit.setCustomName(Methods.compileEntityName(entit, amount));
        }, entity == null ? 1L : 0L);

    }

    public Entity getEntity() {
        Entity entity = getEntityByUniqueId(this.entity);
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

    public Entity getEntityByUniqueId(UUID uniqueId) {
        if (UltimateStacker.getInstance().isServerVersionAtLeast(ServerVersion.V1_12))
            return Bukkit.getEntity(uniqueId);

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(uniqueId))
                    return entity;
            }
        }

        return null;
    }

    private void handleWholeStackDeath(LivingEntity killed, List<ItemStack> items, int droppedExp) {
        Location killedLocation = killed.getLocation();
        for (int i = 1; i < amount; i++) {
            if (i == 1) {
                items.removeIf(it -> it.isSimilar(killed.getEquipment().getItemInHand()));
                for (ItemStack item : killed.getEquipment().getArmorContents()) {
                    items.removeIf(it -> it.isSimilar(item));
                }
            }
            for (ItemStack item : items) {
                killedLocation.getWorld().dropItemNaturally(killedLocation, item);
            }
        }

        killedLocation.getWorld().spawn(killedLocation, ExperienceOrb.class).setExperience(droppedExp * amount);

        UltimateStacker.getInstance().addExp(killed.getKiller(), this);
    }

    private void handleSingleStackDeath(LivingEntity killed) {
        UltimateStacker instance = UltimateStacker.getInstance();
        EntityStackManager stackManager = instance.getEntityStackManager();
        LivingEntity newEntity = Methods.newEntity(killed);

        newEntity.getEquipment().clear();

        if (killed.getType() == EntityType.PIG_ZOMBIE)
            newEntity.getEquipment().setItemInHand(new ItemStack(instance.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.GOLDEN_SWORD : Material.valueOf("GOLD_SWORD")));

        if (Bukkit.getPluginManager().isPluginEnabled("EpicSpawners"))
            if (killed.hasMetadata("ES"))
                newEntity.setMetadata("ES", killed.getMetadata("ES").get(0));

        EntityStack entityStack = stackManager.updateStack(killed, newEntity);

        entityStack.addAmount(-1);

        if (entityStack.getAmount() <= 1) {
            stackManager.removeStack(newEntity);
            newEntity.setCustomNameVisible(false);
            newEntity.setCustomName(null);
        }
    }

    public void onDeath(LivingEntity killed, List<ItemStack> items, int droppedExp) {
        killed.setCustomName(null);
        killed.setCustomNameVisible(true);
        killed.setCustomName(Methods.formatText("&7"));

        if (Setting.KILL_WHOLE_STACK_ON_DEATH.getBoolean() && getAmount() != 1) {
            handleWholeStackDeath(killed, items, droppedExp);
        } else if (getAmount() != 1) {
            List<String> reasons = Setting.INSTANT_KILL.getStringList();
            EntityDamageEvent lastDamageCause = killed.getLastDamageCause();

            if (lastDamageCause != null) {
                EntityDamageEvent.DamageCause cause = lastDamageCause.getCause();
                for (String s : reasons) {
                    if (!cause.name().equalsIgnoreCase(s)) continue;
                    handleWholeStackDeath(killed, items, droppedExp);
                    return;
                }
            }
            handleSingleStackDeath(killed);
        }
    }

    @Override
    public String toString() {
        return "EntityStack:{"
                + "Entity:\"" + entity.toString() + "\","
                + "Amount:\"" + amount + "\","
                + "}";
    }
}
