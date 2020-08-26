package com.songoda.ultimatestacker.stackable;

import org.bukkit.Location;

public interface Hologramable {

    Location getLocation();

    String getHologramName();

    boolean areHologramsEnabled();

    boolean isValid();
}
