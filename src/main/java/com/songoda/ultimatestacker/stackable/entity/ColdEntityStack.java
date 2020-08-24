package com.songoda.ultimatestacker.stackable.entity;

import com.songoda.core.nms.NmsManager;
import com.songoda.core.nms.nbt.NBTEntity;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.stackable.Stackable;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.*;
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

    public StackedEntity addEntityToStackSilently(StackedEntity stackedEntity) {
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
        for (int i = 0; i < amount; i ++) {
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
        NBTEntity nbtEntity = NmsManager.getNbt().newEntity();
        nbtEntity.deSerialize(stackedEntities.getFirst().getSerializedEntity());
        LivingEntity newEntity = (LivingEntity)nbtEntity.spawn(location);
        stackedEntities.removeFirst();
        plugin.getDataManager().deleteStackedEntity(newEntity.getUniqueId());

        return newEntity;
    }

    @Override
    public int getAmount() {
        return stackedEntities.size() + 1;
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

    protected StackedEntity getStackedEntity(Entity entity, boolean newUUID) {
        UUID uuid = entity.getUniqueId();
        NBTEntity nbtEntity = NmsManager.getNbt().of(entity);
        if (newUUID) {
            uuid = UUID.randomUUID();
            nbtEntity.set("UUID", uuid);
        }
        return new StackedEntity(uuid, nbtEntity.serialize("Attributes"));
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
