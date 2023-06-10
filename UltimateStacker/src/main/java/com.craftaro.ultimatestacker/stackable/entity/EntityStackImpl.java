package com.craftaro.ultimatestacker.stackable.entity;

import com.craftaro.ultimatestacker.UltimateStacker;
import com.craftaro.ultimatestacker.api.events.entity.EntityStackKillEvent;
import com.craftaro.ultimatestacker.api.stack.entity.EntityStack;
import com.craftaro.ultimatestacker.settings.Settings;
import com.craftaro.ultimatestacker.utils.Async;
import com.craftaro.ultimatestacker.utils.Methods;
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

public class EntityStackImpl implements EntityStack {

    private final UltimateStacker plugin = UltimateStacker.getInstance();
    private int amount;
    private LivingEntity hostEntity;

    /**
     * Gets an existing stack from an entity or creates a new one if it doesn't exist.
     * @param entity The entity to get the stack from.
     */
    public EntityStackImpl(LivingEntity entity) {
        if (entity == null) return;
        if (!UltimateStacker.getInstance().getEntityStackManager().isStackedEntity(entity)) {
            entity.setMetadata("US_AMOUNT", new FixedMetadataValue(UltimateStacker.getInstance(), 1));
            this.amount = 1;
        } else {
            //get the amount from the entity
            this.amount = entity.getMetadata("US_AMOUNT").get(0).asInt();
        }
        this.hostEntity = entity;
        updateNameTag();
    }

    /**
     * Creates a new stack or overrides an existing stack.
     * @param entity The entity to create the stack for.
     * @param amount The amount of entities in the stack.
     */
    public EntityStackImpl(LivingEntity entity, int amount) {
        if (entity == null) return;
        this.hostEntity = entity;
        this.amount = amount;
        entity.setMetadata("US_AMOUNT", new FixedMetadataValue(UltimateStacker.getInstance(), amount));
        updateNameTag();
    }

    @Override
    public EntityType getType() {
        return hostEntity.getType();
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public void setAmount(int amount) {
        this.amount = amount;
        this.hostEntity.setMetadata("US_AMOUNT", new FixedMetadataValue(UltimateStacker.getInstance(), amount));
        updateNameTag();
    }

    @Override
    public void add(int amount) {
        this.amount += amount;
    }

    @Override
    public void take(int amount) {
        this.amount -= amount;
    }

    @Override
    public UUID getUuid() {
        return hostEntity.getUniqueId();
    }

    @Override
    public LivingEntity getHostEntity() {
        return hostEntity;
    }

    private void handleWholeStackDeath(LivingEntity killed, List<Drop> drops, boolean custom, int droppedExp, EntityDeathEvent event) {

        EntityStack stack = plugin.getEntityStackManager().getStackedEntity(killed);
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
        destroy();
        if (killed.getKiller() == null) return;
        plugin.addExp(killed.getKiller(), this);
    }

    private void handleSingleStackDeath(LivingEntity killed, List<Drop> drops, int droppedExp, EntityDeathEvent event) {
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
        plugin.getEntityStackManager().updateStack(killed, newEntity);
    }

    public void onDeath(LivingEntity killed, List<Drop> drops, boolean custom, int droppedExp, EntityDeathEvent event) {
        killed.setCustomName(null);
        killed.setCustomNameVisible(false);

        boolean killWholeStack = Settings.KILL_WHOLE_STACK_ON_DEATH.getBoolean()
                || plugin.getMobFile().getBoolean("Mobs." + killed.getType().name() + ".Kill Whole Stack");

        if (killWholeStack && getAmount() > 1) {
            handleWholeStackDeath(killed, drops, custom, droppedExp, event);
        } else if (getAmount() > 1) {
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

    @Override
    public synchronized LivingEntity takeOneAndSpawnEntity(Location location) {
        if (amount <= 0) return null;
        LivingEntity entity = Objects.requireNonNull(location.getWorld()).spawn(location, hostEntity.getClass());
        if (Settings.NO_AI.getBoolean()) {
            EntityUtils.setUnaware(entity);
        }
        this.hostEntity = entity;
        setAmount(amount--);
        updateNameTag();
        return entity;
    }

    @Override
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

    @Override
    public synchronized void destroy() {
        if (hostEntity == null) return;
        Bukkit.getScheduler().runTask(plugin, hostEntity::remove);
        hostEntity = null;
    }

    private void updateNameTag() {
        if (hostEntity == null) {
            return;
        }
        hostEntity.setCustomNameVisible(!Settings.HOLOGRAMS_ON_LOOK_ENTITY.getBoolean());
        hostEntity.setCustomName(Methods.compileEntityName(hostEntity, getAmount()));
    }
}
