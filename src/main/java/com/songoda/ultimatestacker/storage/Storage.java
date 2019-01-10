package com.songoda.ultimatestacker.storage;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.utils.ConfigWrapper;

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

    public abstract void doSave();

    public abstract void closeConnection();

}
