package com.songoda.ultimatestacker.lootables;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.lootables.Lootables;
import com.songoda.lootables.Modify;
import com.songoda.lootables.loot.Drop;
import com.songoda.lootables.loot.Loot;
import com.songoda.lootables.loot.LootBuilder;
import com.songoda.lootables.loot.LootManager;
import com.songoda.lootables.loot.Lootable;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.settings.Settings;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LootablesManager {

    private final Lootables lootables;

    private final LootManager lootManager;

    private final String lootablesDir = UltimateStacker.getInstance().getDataFolder() + File.separator + "lootables";

    public LootablesManager() {
        this.lootables = new Lootables(lootablesDir);
        this.lootManager = new LootManager(lootables);
    }

    public List<Drop> getDrops(LivingEntity entity) {
        List<Drop> toDrop = new ArrayList<>();

        if (entity instanceof Ageable && !((Ageable) entity).isAdult()
                || !lootManager.getRegisteredLootables().containsKey(entity.getType().name())) return toDrop;

        Lootable lootable = lootManager.getRegisteredLootables().get(entity.getType().name());
        int looting = entity.getKiller() != null
                && entity.getKiller().getItemInHand().containsEnchantment(Enchantment.LOOT_BONUS_MOBS)
                ? entity.getKiller().getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS)
                : 0;

        int rerollChance = Settings.REROLL.getBoolean() ? looting / (looting + 1) : 0;

        for (Loot loot : lootable.getRegisteredLoot())
            toDrop.addAll(runLoot(entity, loot, rerollChance, looting));

        return toDrop;
    }

    private List<Drop> runLoot(LivingEntity entity, Loot loot, int rerollChance, int looting) {
        Modify modify = null;
        if (entity instanceof Sheep) {
            modify = (Loot loot2) -> {
                CompatibleMaterial material = loot2.getMaterial();
                if (material.name().contains("WOOL") && ((Sheep) entity).getColor() != null) {
                    if (((Sheep) entity).isSheared()) return null;
                    if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13))
                        loot2.setMaterial(CompatibleMaterial.valueOf(((Sheep) entity).getColor() + "_WOOL"));

                }
                return loot2;
            };
        }
        EntityType killer = null;
        Entity killerEntity = null;
        if (entity.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            killerEntity = ((EntityDamageByEntityEvent) entity.getLastDamageCause()).getDamager();
            killer = killerEntity.getType();
            if (killerEntity instanceof Projectile) {
                Projectile projectile = (Projectile) killerEntity;
                if (projectile.getShooter() instanceof Entity) {
                    killerEntity = ((Entity) projectile.getShooter());
                    killer = killerEntity.getType();
                }
            }
        }
        return lootManager.runLoot(modify,
                entity.getFireTicks() > 0,
                killerEntity instanceof Creeper && ((Creeper) killerEntity).isPowered(),
                entity.getKiller() != null ? entity.getKiller().getItemInHand() : null,
                killer,
                loot,
                rerollChance,
                looting);
    }

    public void createDefaultLootables() {
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14)) {
            // Add Trader Llama.
            lootManager.addLootable(new Lootable("TRADER_LLAMA",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.LEATHER)
                            .setMin(0)
                            .setMax(2).build()));

            // Add Pillager.
            lootManager.addLootable(new Lootable("PILLAGER",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.ARROW)
                            .setMin(0)
                            .setMax(2).build()));

            // Add Ravager.
            lootManager.addLootable(new Lootable("RAVAGER",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.SADDLE).build()));

            // Add Cat.
            lootManager.addLootable(new Lootable("CAT",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.STRING).build()));

            // Add Panda.
            lootManager.addLootable(new Lootable("PANDA",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.BAMBOO)
                            .setMin(0)
                            .setMax(2).build()));
        }

        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) {

            // Add Phantom.
            lootManager.addLootable(new Lootable("PHANTOM",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.PHANTOM_MEMBRANE)
                            .setMin(0)
                            .setMax(1)
                            .addOnlyDropFors(EntityType.PLAYER).build()));

            // Add Pufferfish.
            lootManager.addLootable(new Lootable("PUFFERFISH",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.PUFFERFISH).build(),
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.BONE_MEAL)
                            .setChance(5).build()));

            // Add Salmon.
            lootManager.addLootable(new Lootable("SALMON",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.SALMON)
                            .setBurnedMaterial(CompatibleMaterial.COOKED_SALMON).build(),
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.BONE_MEAL)
                            .setChance(5).build()));

            // Add Tropical Fish.
            lootManager.addLootable(new Lootable("TROPICAL_FISH",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.TROPICAL_FISH).build(),
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.BONE_MEAL)
                            .setChance(5).build(),
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.BONE)
                            .setMin(1)
                            .setMax(2)
                            .setChance(25)
                            .addOnlyDropFors(EntityType.PLAYER).build()));

            // Add Dolphin.
            lootManager.addLootable(new Lootable("DOLPHIN",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.COD)
                            .setBurnedMaterial(CompatibleMaterial.COOKED_COD)
                            .setMin(0)
                            .setMax(1).build()));

            // Add Cod.
            lootManager.addLootable(new Lootable("COD",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.COD)
                            .setBurnedMaterial(CompatibleMaterial.COOKED_COD).build(),
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.BONE_MEAL)
                            .setChance(5).build()));

            // Add Turtle.
            lootManager.addLootable(new Lootable("TURTLE",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.SEAGRASS)
                            .setMin(0)
                            .setMax(2).build()));

            // Add Drowned.
            lootManager.addLootable(new Lootable("DROWNED",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.ROTTEN_FLESH)
                            .setMin(0)
                            .setMax(2).build(),
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.GOLD_INGOT)
                            .setChance(5)
                            .addOnlyDropFors(EntityType.PLAYER).build()));
        }

        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12)) {
            // Add Parrot.
            lootManager.addLootable(new Lootable("PARROT",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.FEATHER)
                            .setMin(1)
                            .setMax(2).build()));
        }


        Loot fish1 = new LootBuilder()
                .addChildLoot(new LootBuilder()
                                .setMaterial(CompatibleMaterial.COD)
                                .setBurnedMaterial(CompatibleMaterial.COOKED_COD)
                                .setChance(50).build(),
                        new LootBuilder()
                                .setMaterial(CompatibleMaterial.PRISMARINE_CRYSTALS)
                                .setChance(33).build())
                .build();

        Loot fish2 = new LootBuilder()
                .setChance(2.5)
                .addChildLoot(new LootBuilder()
                                .setMaterial(CompatibleMaterial.COD)
                                .setChance(60)
                                .setAllowLootingEnchant(false).build(),
                        new LootBuilder()
                                .setMaterial(CompatibleMaterial.SALMON)
                                .setChance(25)
                                .setAllowLootingEnchant(false).build(),
                        new LootBuilder()
                                .setMaterial(CompatibleMaterial.PUFFERFISH)
                                .setChance(13)
                                .setAllowLootingEnchant(false).build(),
                        new LootBuilder()
                                .setMaterial(CompatibleMaterial.TROPICAL_FISH)
                                .setChance(2)
                                .setAllowLootingEnchant(false).build())
                .addOnlyDropFors(EntityType.PLAYER).build();

        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)) {
            // Add Zombie Villager.
            lootManager.addLootable(new Lootable("ZOMBIE_VILLAGER",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.ROTTEN_FLESH)
                            .setMin(0)
                            .setMax(2).build(),
                    new LootBuilder()
                            .setChance(2.5)
                            .setChildDropCount(1)
                            .addOnlyDropFors(EntityType.PLAYER)
                            .addChildLoot(new LootBuilder().setMaterial(CompatibleMaterial.IRON_INGOT)
                                            .setAllowLootingEnchant(false).build(),
                                    new LootBuilder().setMaterial(CompatibleMaterial.CARROT)
                                            .setAllowLootingEnchant(false).build(),
                                    new LootBuilder().setMaterial(CompatibleMaterial.POTATO)
                                            .setAllowLootingEnchant(false).build())
                            .build()));

            // Add Llama.
            lootManager.addLootable(new Lootable("LLAMA",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.LEATHER)
                            .setMin(0)
                            .setMax(2).build()));

            // Add Zombie Horse.
            lootManager.addLootable(new Lootable("ZOMBIE_HORSE",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.ROTTEN_FLESH)
                            .setMin(0)
                            .setMax(2).build()));
            // Add Elder Guardian.
            lootManager.addLootable(new Lootable("ELDER_GUARDIAN",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.PRISMARINE_SHARD)
                            .setMin(0)
                            .setMax(2).build(),
                    fish1,
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.SPONGE)
                            .addOnlyDropFors(EntityType.PLAYER)
                            .setAllowLootingEnchant(false).build(),
                    fish2));

            // Add Mule.
            lootManager.addLootable(new Lootable("MULE",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.LEATHER)
                            .setMin(0)
                            .setMax(2).build()));

            // Add Stray.
            lootManager.addLootable(new Lootable("STRAY",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.ARROW)
                            .setMin(0)
                            .setMax(2).build(),
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.BONE)
                            .setMin(0)
                            .setMax(2).build()));

            Loot witherSkull = new LootBuilder()
                    .setMaterial(CompatibleMaterial.WITHER_SKELETON_SKULL)
                    .setChance(2.5)
                    .setAllowLootingEnchant(false)
                    .addOnlyDropFors(EntityType.PLAYER).build();

            // Add Wither Skeleton.
            lootManager.addLootable(new Lootable("WITHER_SKELETON",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.COAL)
                            .setChance(33).build(),
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.BONE)
                            .setMin(0)
                            .setMax(2).build(),
                    witherSkull));        // Add Skeleton Horse.
            lootManager.addLootable(new Lootable("SKELETON_HORSE",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.BONE)
                            .setMin(0)
                            .setMax(2).build()));

            // Add Donkey.
            lootManager.addLootable(new Lootable("DONKEY",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.LEATHER)
                            .setMin(0)
                            .setMax(2).build()));

            // Add Vindicator.
            lootManager.addLootable(new Lootable("VINDICATOR",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.EMERALD)
                            .setMin(0)
                            .setMax(1)
                            .addOnlyDropFors(EntityType.PLAYER).build()));

            // Add Evoker.
            lootManager.addLootable(new Lootable("EVOKER",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.TOTEM_OF_UNDYING)
                            .setAllowLootingEnchant(false).build(),
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.EMERALD)
                            .setChance(50)
                            .addOnlyDropFors(EntityType.PLAYER).build()));
        }

        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)) {


            // Shulker.
            lootManager.addLootable(new Lootable("SHULKER",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.SHULKER_SHELL)
                            .setChance(50)
                            .setLootingIncrease(6.25).build()));
        }

        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) {
            // Add Polar Bear.
            lootManager.addLootable(new Lootable("POLAR_BEAR",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.COD)
                            .setChance(75)
                            .setMin(0)
                            .setMax(2).build(),
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.SALMON)
                            .setChance(25)
                            .setMin(0)
                            .setMax(2).build()));
        } else if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_10)) {
            // Add Polar Bear.
            lootManager.addLootable(new Lootable("POLAR_BEAR",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.COD)
                            .setChance(75)
                            .setMin(0)
                            .setMax(2).build(),
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.SALMON)
                            .setChance(25)
                            .setMin(0)
                            .setMax(2).build()));
        }

        // Add Pig.
        lootManager.addLootable(new Lootable("PIG",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.PORKCHOP)
                        .setBurnedMaterial(CompatibleMaterial.COOKED_PORKCHOP)
                        .setMin(1)
                        .setMax(3).build()));


        // Add Cow.
        lootManager.addLootable(new Lootable("COW",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.LEATHER)
                        .setMin(0)
                        .setMax(2).build(),
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.BEEF)
                        .setBurnedMaterial(CompatibleMaterial.COOKED_BEEF)
                        .setMin(1)
                        .setMax(3).build()));

        // Add Mushroom Cow.
        lootManager.addLootable(new Lootable("MUSHROOM_COW",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.LEATHER)
                        .setMin(0)
                        .setMax(2).build(),
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.BEEF)
                        .setBurnedMaterial(CompatibleMaterial.COOKED_BEEF)
                        .setMin(1)
                        .setMax(3).build()));

        // Add Chicken.
        lootManager.addLootable(new Lootable("CHICKEN",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.FEATHER)
                        .setMin(0)
                        .setMax(2).build(),
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.CHICKEN)
                        .setBurnedMaterial(CompatibleMaterial.COOKED_CHICKEN).build()));
        // Add Zombie.
        lootManager.addLootable(new Lootable("ZOMBIE",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.ROTTEN_FLESH)
                        .setMin(0)
                        .setMax(2).build(),
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.ZOMBIE_HEAD)
                        .setRequireCharged(true).build(),
                new LootBuilder()
                        .setChance(2.5)
                        .setChildDropCount(1)
                        .setAllowLootingEnchant(false)
                        .addOnlyDropFors(EntityType.PLAYER)
                        .addChildLoot(new LootBuilder().setMaterial(CompatibleMaterial.IRON_INGOT)
                                        .setAllowLootingEnchant(false).build(),
                                new LootBuilder().setMaterial(CompatibleMaterial.CARROT)
                                        .setAllowLootingEnchant(false).build(),
                                new LootBuilder().setMaterial(CompatibleMaterial.POTATO)
                                        .setAllowLootingEnchant(false).build())
                        .build()));

        // Add Husk.
        lootManager.addLootable(new Lootable("ZOMBIE",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.ROTTEN_FLESH)
                        .setMin(0)
                        .setMax(2).build(),
                new LootBuilder()
                        .setChance(2.5)
                        .setChildDropCount(1)
                        .setAllowLootingEnchant(false)
                        .addOnlyDropFors(EntityType.PLAYER)
                        .addChildLoot(new LootBuilder().setMaterial(CompatibleMaterial.IRON_INGOT)
                                        .setAllowLootingEnchant(false).build(),
                                new LootBuilder().setMaterial(CompatibleMaterial.CARROT)
                                        .setAllowLootingEnchant(false).build(),
                                new LootBuilder().setMaterial(CompatibleMaterial.POTATO)
                                        .setAllowLootingEnchant(false).build())
                        .build()));

        // Add Creeper.
        lootManager.addLootable(new Lootable("CREEPER",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.GUNPOWDER)
                        .setMin(0)
                        .setMax(2).build(),
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.CREEPER_HEAD)
                        .setRequireCharged(true).build(),
                new LootBuilder()
                        .setChildDropCount(1)
                        .addOnlyDropFors(EntityType.SKELETON,
                                ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11) ? EntityType.STRAY : null)
                        .addChildLoot(new LootBuilder().setMaterial(CompatibleMaterial.MUSIC_DISC_11).build(),
                                new LootBuilder().setMaterial(CompatibleMaterial.MUSIC_DISC_13).build(),
                                new LootBuilder().setMaterial(CompatibleMaterial.MUSIC_DISC_BLOCKS).build(),
                                new LootBuilder().setMaterial(CompatibleMaterial.MUSIC_DISC_CAT).build(),
                                new LootBuilder().setMaterial(CompatibleMaterial.MUSIC_DISC_CHIRP).build(),
                                new LootBuilder().setMaterial(CompatibleMaterial.MUSIC_DISC_FAR).build(),
                                new LootBuilder().setMaterial(CompatibleMaterial.MUSIC_DISC_MALL).build(),
                                new LootBuilder().setMaterial(CompatibleMaterial.MUSIC_DISC_MELLOHI).build(),
                                new LootBuilder().setMaterial(CompatibleMaterial.MUSIC_DISC_STAL).build(),
                                new LootBuilder().setMaterial(CompatibleMaterial.MUSIC_DISC_STRAD).build(),
                                new LootBuilder().setMaterial(CompatibleMaterial.MUSIC_DISC_WAIT).build(),
                                new LootBuilder().setMaterial(CompatibleMaterial.MUSIC_DISC_WARD).build())
                        .build()));

        // Add Guardian.
        lootManager.addLootable(new Lootable("GUARDIAN",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.PRISMARINE_SHARD)
                        .setMin(0)
                        .setMax(2).build(),
                fish1,
                fish2));

        // Add Witch.
        lootManager.addLootable(new Lootable("WITCH",
                new LootBuilder()
                        .setChildDropCounMin(1)
                        .setChildDropCountMax(3)
                        .addChildLoot(new LootBuilder()
                                        .setMaterial(CompatibleMaterial.GLOWSTONE_DUST)
                                        .setChance(12.5)
                                        .setMin(0)
                                        .setMax(2).build(),
                                new LootBuilder()
                                        .setMaterial(CompatibleMaterial.SUGAR)
                                        .setChance(12.5)
                                        .setMin(0)
                                        .setMax(2).build(),
                                new LootBuilder()
                                        .setMaterial(CompatibleMaterial.REDSTONE)
                                        .setChance(12.5)
                                        .setMin(0)
                                        .setMax(2).build(),
                                new LootBuilder()
                                        .setMaterial(CompatibleMaterial.SPIDER_EYE)
                                        .setChance(12.5)
                                        .setMin(0)
                                        .setMax(2).build(),
                                new LootBuilder()
                                        .setMaterial(CompatibleMaterial.GLASS_BOTTLE)
                                        .setChance(12.5)
                                        .setMin(0)
                                        .setMax(2).build(),
                                new LootBuilder()
                                        .setMaterial(CompatibleMaterial.GUNPOWDER)
                                        .setChance(12.5)
                                        .setMin(0)
                                        .setMax(2).build(),
                                new LootBuilder()
                                        .setMaterial(CompatibleMaterial.STICK)
                                        .setChance(25)
                                        .setMin(0)
                                        .setMax(2).build()
                        ).build()));

        // Add Sheep.
        lootManager.addLootable(new Lootable("SHEEP",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.MUTTON)
                        .setBurnedMaterial(CompatibleMaterial.COOKED_MUTTON)
                        .setMin(1)
                        .setMax(2).build(),
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.WHITE_WOOL)
                        .setMin(2)
                        .setMax(2).build()));

        // Add Squid.
        lootManager.addLootable(new Lootable("SQUID",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.INK_SAC)
                        .setMin(1)
                        .setMax(3).build()));

        // Add Spider.
        lootManager.addLootable(new Lootable("SPIDER",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.STRING)
                        .setMin(0)
                        .setMax(2).build(),
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.SPIDER_EYE)
                        .setChance(33)
                        .addOnlyDropFors(EntityType.PLAYER).build()));

        // Add Cave Spider.
        lootManager.addLootable(new Lootable("CAVE_SPIDER",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.STRING)
                        .setMin(0)
                        .setMax(2).build(),
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.SPIDER_EYE)
                        .setChance(33)
                        .addOnlyDropFors(EntityType.PLAYER).build()));

        // Add Enderman.
        lootManager.addLootable(new Lootable("ENDERMAN",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.ENDER_PEARL)
                        .setMin(0)
                        .setMax(1).build()));

        // Add Blaze.
        lootManager.addLootable(new Lootable("BLAZE",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.BLAZE_ROD)
                        .setMin(0)
                        .setMax(1)
                        .addOnlyDropFors(EntityType.PLAYER).build()));

        // Add Horse.
        lootManager.addLootable(new Lootable("HORSE",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.LEATHER)
                        .setMin(0)
                        .setMax(2).build()));

        // Magma Cube.
        lootManager.addLootable(new Lootable("MAGMA_CUBE",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.MAGMA_CREAM)
                        .setChance(25).build()));
        // Add Skeleton.
        lootManager.addLootable(new Lootable("SKELETON",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.ARROW)
                        .setMin(0)
                        .setMax(2).build(),
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.BONE)
                        .setMin(0)
                        .setMax(2).build(),
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.SKELETON_SKULL)
                        .setRequireCharged(true).build()));

        // Add Snowman.
        lootManager.addLootable(new Lootable("SNOWMAN",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.SNOWBALL)
                        .setMin(0)
                        .setMax(15).build()));

        // Add Rabbit.
        lootManager.addLootable(new Lootable("RABBIT",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.RABBIT_HIDE)
                        .setMin(0)
                        .setMax(1).build(),
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.RABBIT_FOOT)
                        .setMin(0)
                        .setMax(1)
                        .setChance(10).build(),
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.RABBIT)
                        .setBurnedMaterial(CompatibleMaterial.COOKED_RABBIT)
                        .setMin(0)
                        .setMax(1).build()));

        // Add Iron Golem.
        lootManager.addLootable(new Lootable("IRON_GOLEM",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.POPPY)
                        .setMin(0)
                        .setMax(2).build(),
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.IRON_INGOT)
                        .setMin(3)
                        .setMax(5).build()));

        // Add Slime.
        lootManager.addLootable(new Lootable("SLIME",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.SLIME_BALL)
                        .setMin(0)
                        .setMax(2).build()));

        // Add Ghast.
        lootManager.addLootable(new Lootable("GHAST",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.GHAST_TEAR)
                        .setMin(0)
                        .setMax(1).build(),
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.GUNPOWDER)
                        .setMin(0)
                        .setMax(2).build()));

        // Add Zombie Pigman
        if (ServerVersion.isServerVersionBelow(ServerVersion.V1_16))
            lootManager.addLootable(new Lootable("PIG_ZOMBIE",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.ROTTEN_FLESH)
                            .setMin(0)
                            .setMax(1).build(),
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.GOLD_NUGGET)
                            .setMin(0)
                            .setMax(1).build(),
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.GOLD_INGOT)
                            .setChance(2.5)
                            .addOnlyDropFors(EntityType.PLAYER).build()));
        else {
            // Add Strider
            lootManager.addLootable(new Lootable("STRIDER",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.STRING)
                            .setMin(0)
                            .setMax(5).build()));

            // Add Hoglin
            lootManager.addLootable(new Lootable("HOGLIN",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.PORKCHOP)
                            .setBurnedMaterial(CompatibleMaterial.COOKED_PORKCHOP)
                            .setMin(2)
                            .setMax(4).build(),
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.LEATHER)
                            .setMin(0)
                            .setMax(2).build()));

            // Add Zombified Piglin
            lootManager.addLootable(new Lootable("ZOMBIFIED_PIGLIN",
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.ROTTEN_FLESH)
                            .setMin(0)
                            .setMax(1).build(),
                    new LootBuilder()
                            .setMaterial(CompatibleMaterial.GOLD_NUGGET)
                            .setMin(0)
                            .setMax(1).build()));

            // Add Piglin
            lootManager.addLootable(new Lootable("PIGLIN"));
        }

        // Add Wither.
        lootManager.addLootable(new Lootable("WITHER",
                new LootBuilder()
                        .setMaterial(CompatibleMaterial.NETHER_STAR)
                        .setAllowLootingEnchant(false).build()));

        // Add Villager.
        lootManager.addLootable(new Lootable("VILLAGER",
                new LootBuilder().build()));

        // Add Silverfish.
        lootManager.addLootable(new Lootable("SILVERFISH",
                new LootBuilder().build()));

        lootManager.saveLootables(true);
    }

    public LootManager getLootManager() {
        return lootManager;
    }
}
