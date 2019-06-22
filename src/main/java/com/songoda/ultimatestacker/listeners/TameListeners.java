package com.songoda.ultimatestacker.listeners;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import com.songoda.ultimatestacker.utils.Methods;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class TameListeners implements Listener {

    private UltimateStacker instance;

    public TameListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTame(EntityTameEvent event) {
        Entity entity = event.getEntity();

        EntityStackManager stackManager = instance.getEntityStackManager();
        if (!stackManager.isStacked(entity)) return;

        Tameable tameable = (Tameable) entity;

        EntityStack stack = instance.getEntityStackManager().getStack(entity);

        if (stack.getAmount() <= 1) return;

        Entity newEntity = Methods.newEntity((LivingEntity) tameable);

        instance.getEntityStackManager().addStack(new EntityStack(newEntity, stack.getAmount() - 1));
        stack.setAmount(1);
        instance.getEntityStackManager().removeStack(entity);
        entity.setVelocity(getRandomVector());
    }

    private Vector getRandomVector() {
        return new Vector(ThreadLocalRandom.current().nextDouble(-1, 1.01), 0, ThreadLocalRandom.current().nextDouble(-1, 1.01)).normalize().multiply(0.5);
    }
}
