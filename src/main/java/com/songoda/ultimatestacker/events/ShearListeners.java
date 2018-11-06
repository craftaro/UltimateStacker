package com.songoda.ultimatestacker.events;

import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class ShearListeners implements Listener {

    private UltimateStacker instance;

    public ShearListeners(UltimateStacker instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onShearSheap(PlayerShearEntityEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Sheep)) return;
        EntityStackManager stackManager = instance.getEntityStackManager();
        if (!stackManager.isStacked(entity)) return;

        LivingEntity sheep = (LivingEntity) entity;

        event.setCancelled(true);

        Entity newEntity = sheep.getWorld().spawnEntity(sheep.getLocation(), sheep.getType());
        ((Sheep) newEntity).setSheared(true);
        newEntity.setVelocity(getRandomVector());
        ((Sheep) newEntity).setAge(((Sheep) sheep).getAge());
        ((Sheep) newEntity).setColor(((Sheep) sheep).getColor());

        int num = (int) Math.round(1 + (Math.random() * 3));


        Wool woolColor = new Wool(((Sheep) sheep).getColor());

        ItemStack wool = woolColor.toItemStack(num);
        sheep.getLocation().getWorld().dropItemNaturally(sheep.getEyeLocation(), wool);

        EntityStack stack = stackManager.getStack(sheep);
        stack.addAmount(-1);
        if (stack.getAmount() <= 1) {
            stackManager.removeStack(entity);
            sheep.setCustomNameVisible(false);
            sheep.setCustomName(null);
            ((Sheep) sheep).setSheared(false);
        }
    }

    private Vector getRandomVector() {
        return new Vector(ThreadLocalRandom.current().nextDouble(-1, 1.01), 0, ThreadLocalRandom.current().nextDouble(-1, 1.01)).normalize().multiply(0.5);
    }
}
