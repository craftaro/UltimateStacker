package com.songoda.ultimatestacker.spawner;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.utils.Reflection;
import com.songoda.ultimatestacker.utils.ServerVersion;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;

public class SpawnerStack {

    private final Location location;
    private int amount = 1;

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

        int count = 4 * amount;
        int maxNearby = amount > 6 ? amount + 3 : 6;
        CreatureSpawner creatureSpawner = (CreatureSpawner)location.getBlock().getState();
        if (UltimateStacker.getInstance().isServerVersionAtLeast(ServerVersion.V1_12)) {
            creatureSpawner.setMaxNearbyEntities(maxNearby);
            creatureSpawner.setSpawnCount(count);
        } else {
            Reflection.updateSpawner(creatureSpawner, count, maxNearby);
        }
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
