package com.songoda.ultimatestacker.spawner;

import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;

public class SpawnerStack {

    private final Location location;
    private int amount;

    public SpawnerStack(Location location, int amount) {
        this.location = location;
        setAmount(amount);
    }

    public Location getLocation() {
        return location.clone();
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;

        CreatureSpawner creatureSpawner = (CreatureSpawner)location.getBlock().getState();
        creatureSpawner.setSpawnCount(4 * amount);
        creatureSpawner.update();
    }

    @Override
    public String toString() {
        return "SpawnerStack:{"
                + "Amount:\"" + amount + "\","
                + "Location:{"
                + "World:\"" + location.getWorld().getName() + "\","
                + "X:" + location.getBlockX() + ","
                + "Y:" + location.getBlockY() + ","
                + "Z:" + location.getBlockZ()
                + "}"
                + "}";
    }
}
