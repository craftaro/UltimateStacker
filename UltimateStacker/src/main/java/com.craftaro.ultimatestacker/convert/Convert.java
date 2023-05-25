package com.craftaro.ultimatestacker.convert;

public interface Convert {

    String getName();

    boolean canEntities();

    boolean canSpawners();

    void convertEntities();

    void convertSpawners();

    void disablePlugin();
}
