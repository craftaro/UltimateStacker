package com.craftaro.ultimatestacker.api.utils;

import org.bukkit.Location;

public interface Hologramable {

    Location getLocation();

    String getHologramName();

    boolean areHologramsEnabled();

    boolean isValid();

    String getHologramId();
}
