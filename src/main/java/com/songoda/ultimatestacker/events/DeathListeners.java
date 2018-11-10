package com.songoda.ultimatestacker.events;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class DeathListeners implements Listener {

    private final UltimateStacker instance;

    public DeathListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity killed = event.getEntity();

        EntityStackManager stackManager = instance.getEntityStackManager();

        if (!stackManager.isStacked(killed)) return;

        killed.setCustomName(null);
        killed.setCustomNameVisible(false);

        EntityStack stack = stackManager.getStack(killed);

        if (instance.getConfig().getBoolean("Entity.Kill Whole Stack On Death")) {
            for (int i = 1; i < stack.getAmount(); i++) {
                LivingEntity newEntity = newEntity(killed);
                newEntity.damage(99999);
            }
        } else {
            Entity newEntity = newEntity(killed);
            stack = stackManager.updateStack(killed, newEntity);

            stack.addAmount(-1);
            if (stack.getAmount() <= 1) {
                stackManager.removeStack(newEntity);
                newEntity.setCustomNameVisible(false);
                newEntity.setCustomName(null);
            }
        }
    }

    private LivingEntity newEntity(LivingEntity killed) {
        LivingEntity newEntity = (LivingEntity) killed.getWorld().spawnEntity(killed.getLocation(), killed.getType());
        newEntity.setVelocity(killed.getVelocity());
        if (killed instanceof Ageable && !((Ageable) killed).isAdult()) {
            ((Ageable) newEntity).setBaby();
        } else if (killed instanceof Sheep) {
            ((Sheep) newEntity).setColor(((Sheep) killed).getColor());
        } else if (killed instanceof Villager) {
            ((Villager) newEntity).setProfession(((Villager) killed).getProfession());
        }

        newEntity.setFireTicks(killed.getFireTicks());
        newEntity.addPotionEffects(killed.getActivePotionEffects());

        return newEntity;
    }
}
