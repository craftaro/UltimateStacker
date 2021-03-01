package com.songoda.ultimatestacker.stackable.entity;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.lootables.loot.Drop;
import com.songoda.lootables.loot.DropUtils;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.settings.Settings;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
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
        if (createDuplicates != 0) {
            List<StackedEntity> stackedEntities = new ArrayList<>();
            for (int i = 0; i < createDuplicates; i++)
                stackedEntities.add(addEntityToStackSilently(getStackedEntity(hostEntity, true)));
            plugin.getDataManager().createStackedEntities(this, stackedEntities);

            createDuplicates = 0;
        }
        if (!Settings.ENTITY_NAMETAGS.getBoolean()) return;

        Bukkit.getScheduler().scheduleSyncDelayedTask(UltimateStacker.getInstance(), () -> {
            if (hostEntity == null) return;

            hostEntity.setCustomNameVisible(!Settings.HOLOGRAMS_ON_LOOK_ENTITY.getBoolean());
            hostEntity.setCustomName(Methods.compileEntityName(hostEntity, getAmount()));
        }, hostEntity == null ? 1L : 0L);

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
        plugin.getEntityStackManager().removeStack(event.getEntity());
        plugin.getDataManager().deleteHost(this);

        Location killedLocation = killed.getLocation();
        List<Drop> preStackedDrops = new ArrayList<>();
        for (int i = 1; i < getAmount(); i++) {
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

        DropUtils.processStackedDrop(killed, preStackedDrops, event);

        if (droppedExp > 0)
            killedLocation.getWorld().spawn(killedLocation, ExperienceOrb.class).setExperience(droppedExp * getAmount());

        if (killed.getKiller() == null) return;
        plugin.addExp(killed.getKiller(), this);
    }

    private void handleSingleStackDeath(LivingEntity killed, List<Drop> drops, int droppedExp, EntityDeathEvent event) {
        EntityStackManager stackManager = plugin.getEntityStackManager();

        killed.remove();
        LivingEntity newEntity = takeOneAndSpawnEntity(killed.getLocation());

        //if (!EntityUtils.isAware(killed))
        //    EntityUtils.setUnaware(newEntity);

        // In versions 1.14 and below experience is not dropping. Because of this we are doing this ourselves.
        if (ServerVersion.isServerVersionAtOrBelow(ServerVersion.V1_14)) {
            Location killedLocation = killed.getLocation();
            if (droppedExp > 0)
                killedLocation.getWorld().spawn(killedLocation, ExperienceOrb.class).setExperience(droppedExp);
        }

        DropUtils.processStackedDrop(killed, drops, event);

        newEntity.setVelocity(killed.getVelocity());
        stackManager.updateStack(killed, newEntity);

        updateStack();

        if (stackedEntities.isEmpty())
            destroy();
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
            hostEntity.setCustomNameVisible(false);
            hostEntity.setCustomName(null);
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
