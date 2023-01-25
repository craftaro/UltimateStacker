package com.songoda.ultimatestacker.stackable.entity;

import com.songoda.core.nms.NmsManager;
import com.songoda.core.nms.nbt.NBTEntity;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.stackable.entity.custom.CustomEntity;
import com.songoda.ultimatestacker.utils.Stackable;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class ColdEntityStack implements Stackable {

    protected static final UltimateStacker plugin = UltimateStacker.getInstance();

    // The id to identify this stack in the database.
    private int id;

    // The unique id of the stacks host.
    protected UUID hostUniqueId;

    // These are the entities below the host.
    protected final Deque<StackedEntity> stackedEntities = new ConcurrentLinkedDeque<>();

    // The amount of duplicates of the host to create when loaded.
    protected int createDuplicates = 0;

    public ColdEntityStack(UUID hostUniqueId, int id) {
        this.hostUniqueId = hostUniqueId;
        this.id = id;
    }

    public ColdEntityStack(UUID hostUniqueId) {
        this.hostUniqueId = hostUniqueId;
    }

    public StackedEntity addEntityToStackSilently(Entity entity) {
        return addEntityToStackSilently(getStackedEntity(entity));
    }

    public List<StackedEntity> addRawEntitiesToStackSilently(List<LivingEntity> unstackedEntities) {
        List<StackedEntity> stackedEntities = unstackedEntities.stream()
                .map(this::getStackedEntity).collect(Collectors.toList());
        addEntitiesToStackSilently(stackedEntities);
        return stackedEntities;
    }

    public void addEntitiesToStackSilently(List<StackedEntity> stackedEntities) {
        stackedEntities.removeIf(Objects::isNull);
        this.stackedEntities.addAll(stackedEntities);
    }

    public synchronized StackedEntity addEntityToStackSilently(StackedEntity stackedEntity) {
        if (stackedEntity == null) return null;
        stackedEntities.push(stackedEntity);
        return stackedEntity;
    }

    public void moveEntitiesFromStack(EntityStack stack, int amount) {
        List<StackedEntity> stackedEntities = stack.takeEntities(amount);
        this.stackedEntities.addAll(stackedEntities);
        plugin.getDataManager().createStackedEntities(this, stackedEntities);
    }

    public List<StackedEntity> takeEntities(int amount) {
        List<StackedEntity> entities = new LinkedList<>();
        for (int i = 0; i < amount; i++) {
            StackedEntity entity = stackedEntities.pollFirst();
            if (entity != null)
                entities.add(entity);
        }
        plugin.getDataManager().deleteStackedEntities(entities);
        return entities;
    }

    public List<StackedEntity> takeAllEntities() {
        List<StackedEntity> entities = new LinkedList<>(stackedEntities);
        stackedEntities.clear();
        return entities;
    }

    public LivingEntity takeOneAndSpawnEntity(Location location) {
        if (stackedEntities.isEmpty()) return null;
        NBTEntity nbtEntity = NmsManager.getNbt().newEntity();
        nbtEntity.deSerialize(stackedEntities.getFirst().getSerializedEntity());

        for (CustomEntity customEntity : plugin.getCustomEntityManager().getRegisteredCustomEntities()) {
            String identifier = customEntity.getPluginName() + "_UltimateStacker";
            if (!nbtEntity.has(identifier)) continue;
            LivingEntity entity = customEntity.spawnFromIdentifier(nbtEntity.getString(identifier), location);
            if (entity == null) continue;
            stackedEntities.removeFirst();
            plugin.getDataManager().deleteStackedEntity(entity.getUniqueId());
            return entity;
        }

        LivingEntity newEntity = null;
        for (int i = 0; i < 5; i++) {
            newEntity = (LivingEntity) nbtEntity.spawn(location);

            if (newEntity != null) {
                stackedEntities.removeFirst();
                plugin.getDataManager().deleteStackedEntity(newEntity.getUniqueId());
                break;
            }
        }
        if (newEntity == null)
            plugin.getDataManager().deleteStackedEntity(hostUniqueId);

        return newEntity;
    }

    @Override
    public int getAmount() {
        return stackedEntities.size() + 1;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public void createDuplicates(int duplicates) {
        this.createDuplicates = duplicates;
    }

    public int getCreateDuplicates() {
        return createDuplicates;
    }

    protected StackedEntity getStackedEntity(Entity entity) {
        return getStackedEntity(entity, false);
    }

    protected synchronized StackedEntity getStackedEntity(Entity entity, boolean newUUID) {
        if (entity == null) return null;
        UUID uuid = entity.getUniqueId();
        NBTEntity nbtEntity = NmsManager.getNbt().of(entity);

        CustomEntity customEntity = plugin.getCustomEntityManager().getCustomEntity(entity);
        if (customEntity != null)
            nbtEntity.set(customEntity.getPluginName() + "_UltimateStacker", customEntity.getNBTIdentifier(entity));
        return new StackedEntity(uuid, nbtEntity.serialize());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getHostUniqueId() {
        return hostUniqueId;
    }

    public void setHostUniqueId(UUID hostUniqueId) {
        this.hostUniqueId = hostUniqueId;
    }
}
