package com.songoda.ultimatestacker.storage;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.utils.ConfigWrapper;
import com.songoda.ultimatestacker.utils.Methods;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.spawner.SpawnerStack;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public abstract class Storage {

    protected final UltimateStacker instance;
    protected final ConfigWrapper dataFile;

    public Storage(UltimateStacker instance) {
        this.instance = instance;
        this.dataFile = new ConfigWrapper(instance, "", "data.yml");
        this.dataFile.createNewFile(null, "UltimateStacker Data File");
        this.dataFile.getConfig().options().copyDefaults(true);
        this.dataFile.saveConfig();
    }

    public abstract boolean containsGroup(String group);

    public abstract List<StorageRow> getRowsByGroup(String group);

    public abstract void prepareSaveItem(String group, StorageItem... items);

    public void updateData(UltimateStacker instance) {

        for (SpawnerStack stack : instance.getSpawnerStackManager().getStacks()) {
            prepareSaveItem("spawners", new StorageItem("location", Methods.serializeLocation(stack.getLocation())),
                    new StorageItem("amount", stack.getAmount()));
        }
    }

    public abstract void doSave();

    public abstract void save();

    public abstract void makeBackup();

    public abstract void closeConnection();

}
