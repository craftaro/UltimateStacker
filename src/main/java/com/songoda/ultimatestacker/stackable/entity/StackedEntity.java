package com.songoda.ultimatestacker.stackable.entity;

import java.util.UUID;

public class StackedEntity {

    private final UUID uniqueId;
    private final byte[] serializedEntity;

    public StackedEntity(UUID uniqueId, byte[] serializedEntity) {
        this.uniqueId = uniqueId;
        this.serializedEntity = serializedEntity;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public byte[] getSerializedEntity() {
        return serializedEntity;
    }
}
