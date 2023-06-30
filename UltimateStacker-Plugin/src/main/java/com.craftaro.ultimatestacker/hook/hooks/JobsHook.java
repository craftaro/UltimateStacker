package com.craftaro.ultimatestacker.hook.hooks;

import com.craftaro.ultimatestacker.api.stack.entity.EntityStack;
import com.craftaro.ultimatestacker.hook.StackerHook;
import com.craftaro.core.hooks.jobs.JobsPlayerHandler;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class JobsHook implements StackerHook {

    @Override
    public void applyExperience(Player player, EntityStack entityStack) {
        if (player.getGameMode().equals(GameMode.CREATIVE) || entityStack.getHostEntity() == null)
            return;

        JobsPlayerHandler jPlayer = com.craftaro.core.hooks.JobsHook.getPlayer(player);
        if (jPlayer == null)
            return;

        for (int i = 1; i < entityStack.getAmount(); i++) {
            LivingEntity entity = entityStack.getHostEntity();
            jPlayer.killEntity(entity);
        }
    }
}
