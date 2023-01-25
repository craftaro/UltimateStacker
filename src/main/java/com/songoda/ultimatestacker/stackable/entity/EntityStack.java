package com.songoda.ultimatestacker.stackable.entity;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.lootables.loot.Drop;
import com.songoda.core.lootables.loot.DropUtils;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.events.EntityStackKillEvent;
import com.songoda.ultimatestacker.settings.Settings;
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
import java.util.concurrent.ThreadLocalRandom;

public class EntityStack extends ColdEntityStack {

    // This is the host entity which is not stored in serialized nbt.
    private LivingEntity hostEntity;

    public EntityStack(LivingEntity hostEntity) {
        super(hostEntity.getUniqueId());
        this.hostEntity = hostEntity;
    }

    public EntityStack(LivingEntity hostEntity, ColdEntityStack coldEntityStack) {
        this(hostEntity);
        this.setId(coldEntityStack.getId());
        this.stackedEntities.addAll(coldEntityStack.stackedEntities);
    }

    public StackedEntity addEntityToStack(Entity entity) {
        StackedEntity stackedEntity = addEntityToStackSilently(entity);
        updateStack();
        return stackedEntity;
    }

    @Override
    public List<StackedEntity> takeEntities(int amount) {
        List<StackedEntity> entities = super.takeEntities(amount);
        if (this.stackedEntities.isEmpty())
            destroy(true);
        return entities;
    }

    @Override
    public List<StackedEntity> takeAllEntities() {
        destroy(true);
        return super.takeAllEntities();
    }

    public void updateStack() {
        Async.run(() -> {
            if (createDuplicates != 0) {
                List<StackedEntity> stackedEntities = new ArrayList<>();
                try {
                    for (int i = 0; i < createDuplicates; i++) {
                        StackedEntity entity = addEntityToStackSilently(getStackedEntity(hostEntity, true));
                        if (entity != null)
                            stackedEntities.add(entity);
                    }
                    plugin.getDataManager().createStackedEntities(this, stackedEntities);

                    createDuplicates = 0;
                    updateNametag();
                } catch (Exception ignored) {
                    //Ignored for now
                }
            }
        });
        updateNametag();
    }

    public void updateNametag() {
        if (hostEntity == null) {
            //Delay with 1 tick to make sure the entity is loaded.
            Bukkit.getScheduler().scheduleSyncDelayedTask(UltimateStacker.getInstance(), this::updateNametag, 1L);
            return;
        }
        hostEntity.setCustomNameVisible(!Settings.HOLOGRAMS_ON_LOOK_ENTITY.getBoolean());
        hostEntity.setCustomName(Methods.compileEntityName(hostEntity, getAmount()));
    }

    public LivingEntity getHostEntity() {
        if (hostEntity == null) {
            plugin.getEntityStackManager().removeStack(this.hostUniqueId);
            return null;
        }
        return hostEntity;
    }

    public StackedEntity getHostAsStackedEntity() {
        return getStackedEntity(hostEntity);
    }

    protected void setHostEntity(LivingEntity hostEntity) {
        this.hostEntity = hostEntity;
        this.hostUniqueId = hostEntity.getUniqueId();
    }

    private void handleWholeStackDeath(LivingEntity killed, List<Drop> drops, boolean custom, int droppedExp, EntityDeathEvent event) {
        plugin.getDataManager().deleteHost(this);

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
        plugin.getEntityStackManager().removeStack(event.getEntity());
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

        updateStack();

        if (stackedEntities.isEmpty()) {
            destroy();
        }
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
            handleSingleStackDeath(killed, drops, droppedExp, event);
        }
    }

    public void releaseHost() {
        LivingEntity oldHost = hostEntity;
        LivingEntity entity = takeOneAndSpawnEntity(hostEntity.getLocation());
        if (!stackedEntities.isEmpty()) {
            destroy(false);
            plugin.getEntityStackManager().updateStack(oldHost, entity);
            entity.setVelocity(new Vector(ThreadLocalRandom.current().nextDouble(-1, 1.01),
                    0, ThreadLocalRandom.current().nextDouble(-1, 1.01)).normalize().multiply(0.5));
        } else {
            destroy();
        }
        updateStack();

    }

    public void destroy() {
        destroy(true);
    }

    public void destroy(boolean full) {
        if (full)
            plugin.getEntityStackManager().removeStack(this.hostUniqueId);
        if (hostEntity != null) {
            try {
                hostEntity.setCustomNameVisible(false);
                hostEntity.setCustomName(null);
            } catch (NullPointerException ignored) {}
        }
        hostEntity = null;
        hostUniqueId = null;
    }

    @Override
    public String toString() {
        return "EntityStack{" +
                "hostEntity=" + hostEntity +
                '}';
    }
}
