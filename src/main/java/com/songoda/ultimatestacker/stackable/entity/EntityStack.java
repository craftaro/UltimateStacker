package com.songoda.ultimatestacker.stackable.entity;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.lootables.loot.Drop;
import com.songoda.core.lootables.loot.DropUtils;
import com.songoda.core.nms.NmsManager;
import com.songoda.core.nms.nbt.NBTEntity;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.events.EntityStackKillEvent;
import com.songoda.ultimatestacker.settings.Settings;
import com.songoda.ultimatestacker.stackable.entity.custom.CustomEntity;
import com.songoda.ultimatestacker.utils.Async;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class EntityStack extends StackedEntity {

    private static final UltimateStacker plugin = UltimateStacker.getInstance();

    public EntityStack(LivingEntity hostEntity) {
        super(hostEntity);
    }

    public EntityStack(LivingEntity hostEntity, int amount) {
        super(hostEntity, amount);
    }

    public synchronized EntityStack addEntityToStack(int amount) {
        setAmount(getAmount() + amount);
        return this;
    }

    public synchronized EntityStack removeEntityFromStack(int amount) {
        setAmount(getAmount() - amount);
        return this;
    }

    public LivingEntity getHostEntity() {
        return hostEntity;
    }

    protected synchronized void setHostEntity(LivingEntity hostEntity) {
        this.hostEntity = hostEntity;
    }

    private void handleWholeStackDeath(LivingEntity killed, List<Drop> drops, boolean custom, int droppedExp, EntityDeathEvent event) {

        EntityStack stack = plugin.getEntityStackManager().getStack(killed);
        // In versions 1.14 and below experience is not dropping. Because of this we are doing this ourselves.
        if (ServerVersion.isServerVersionAtOrBelow(ServerVersion.V1_14)) {
            Location killedLocation = killed.getLocation();
            if (droppedExp > 0)
                killedLocation.getWorld().spawn(killedLocation, ExperienceOrb.class).setExperience(droppedExp * getAmount());
        } else {
            event.setDroppedExp(droppedExp * getAmount());
        }


        if (plugin.getCustomEntityManager().getCustomEntity(killed) == null) {
            Async.run(() -> {
                drops.removeIf(it -> it.getItemStack() != null
                        && it.getItemStack().isSimilar(killed.getEquipment().getItemInHand()));
                for (ItemStack item : killed.getEquipment().getArmorContents()) {
                    drops.removeIf(it -> it.getItemStack() != null && it.getItemStack().isSimilar(item));
                }
                DropUtils.processStackedDrop(killed, plugin.getLootablesManager().getDrops(killed, getAmount()), event);
            });
        }

        event.getDrops().clear();
        stack.destroy();
        if (killed.getKiller() == null) return;
        plugin.addExp(killed.getKiller(), this);
    }

    private void handleSingleStackDeath(LivingEntity killed, List<Drop> drops, int droppedExp, EntityDeathEvent event) {
        EntityStackManager stackManager = plugin.getEntityStackManager();
        Bukkit.getPluginManager().callEvent(new EntityStackKillEvent(this, false));

        Vector velocity = killed.getVelocity().clone();
        killed.remove();
        LivingEntity newEntity = takeOneAndSpawnEntity(killed.getLocation());
        if (newEntity == null) {
            return;
        }

        // In versions 1.14 and below experience is not dropping. Because of this we are doing this ourselves.
        if (ServerVersion.isServerVersionAtOrBelow(ServerVersion.V1_14)) {
            Location killedLocation = killed.getLocation();
            if (droppedExp > 0)
                killedLocation.getWorld().spawn(killedLocation, ExperienceOrb.class).setExperience(droppedExp);
        }
        if (plugin.getCustomEntityManager().getCustomEntity(killed) == null) {
            DropUtils.processStackedDrop(killed, drops, event);
        }

        newEntity.setVelocity(velocity);
        stackManager.updateStack(killed, newEntity);
    }

    public void onDeath(LivingEntity killed, List<Drop> drops, boolean custom, int droppedExp, EntityDeathEvent event) {
        killed.setCustomName(null);
        killed.setCustomNameVisible(false);

        boolean killWholeStack = Settings.KILL_WHOLE_STACK_ON_DEATH.getBoolean()
                || plugin.getMobFile().getBoolean("Mobs." + killed.getType().name() + ".Kill Whole Stack");

        if (killWholeStack && getAmount() != 1) {
            handleWholeStackDeath(killed, drops, custom, droppedExp, event);
        } else if (getAmount() != 1) {
            List<String> reasons = Settings.INSTANT_KILL.getStringList();
            EntityDamageEvent lastDamageCause = killed.getLastDamageCause();

            if (lastDamageCause != null) {
                EntityDamageEvent.DamageCause cause = lastDamageCause.getCause();
                for (String s : reasons) {
                    if (!cause.name().equalsIgnoreCase(s)) continue;
                    handleWholeStackDeath(killed, drops, custom, Settings.NO_EXP_INSTANT_KILL.getBoolean() ? 0 : droppedExp, event);
                    return;
                }
            }
        }
        handleSingleStackDeath(killed, drops, droppedExp, event);
    }

    public synchronized LivingEntity takeOneAndSpawnEntity(Location location) {
        if (amount <= 0) return null;
        LivingEntity entity = Objects.requireNonNull(location.getWorld()).spawn(location, hostEntity.getClass());
        this.hostEntity = entity;
        setAmount(amount--);
        updateNameTag();
        return entity;
    }

    public synchronized void releaseHost() {
        LivingEntity oldHost = hostEntity;
        LivingEntity entity = takeOneAndSpawnEntity(hostEntity.getLocation());
        if (getAmount() >= 0) {
            plugin.getEntityStackManager().updateStack(oldHost, entity);
            updateNameTag();
        } else {
            destroy();
        }
    }

    public synchronized void destroy() {
        if (hostEntity == null) return;
        Bukkit.getScheduler().runTask(plugin, hostEntity::remove);
        hostEntity = null;
    }

    @Override
    public String toString() {
        return "EntityStack{" +
                "hostEntity=" + hostEntity +
                ", amount=" + amount +
                '}';
    }
}
