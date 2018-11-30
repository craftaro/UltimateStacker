package com.songoda.ultimatestacker.spawner;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.utils.Reflection;
import com.songoda.ultimatestacker.utils.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
        Reflection.setRange(creatureSpawner, 4 * amount, amount > 6 ? amount + 3 : 6);
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
