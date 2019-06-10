package com.songoda.ultimatestacker.hook;

import com.songoda.ultimatestacker.entity.EntityStack;
import org.bukkit.entity.Player;

public interface StackerHook {

    /**
     * Applies experience to a player for a killed stack.
     *
     * @param player The player
     * @param entityStack The stack that was killed
     */
    void applyExperience(Player player, EntityStack entityStack);
    
}
