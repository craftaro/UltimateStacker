package com.songoda.ultimatestacker.lootables;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.songoda.lootables.Lootables;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.utils.ServerVersion;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.loot.Lootable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class LootManager {

    private final Lootables instance;

    public LootManager() {
        this.instance = new Lootables();
    }

    private final String lootablesDir = UltimateStacker.getInstance().getDataFolder() + File.separator + "lootables";

    public void createDefaultLootables() {
        UltimateStacker plugin = UltimateStacker.getInstance();

        if (plugin.isServerVersionAtLeast(ServerVersion.V1_14)) {
            // Add Trader Llama.
            addLootable(new Lootable(EntityType.TRADER_LLAMA,
                    new LootBuilder()
                            .setMaterial(Material.LEATHER)
                            .setMin(0)
                            .setMax(2).build()));

            // Add Pillager.
            addLootable(new Lootable(EntityType.PILLAGER,
                    new LootBuilder()
                            .setMaterial(Material.ARROW)
                            .setMin(0)
                            .setMax(2).build()));

            // Add Ravager.
            addLootable(new Lootable(EntityType.RAVAGER,
                    new LootBuilder()
                            .setMaterial(Material.SADDLE).build()));

            // Add Cat.
            addLootable(new Lootable(EntityType.CAT,
                    new LootBuilder()
                            .setMaterial(Material.STRING).build()));

            // Add Panda.
            addLootable(new Lootable(EntityType.PANDA,
                    new LootBuilder()
                            .setMaterial(Material.BAMBOO)
                            .setMin(0)
                            .setMax(2).build()));
        }

        if (plugin.isServerVersionAtLeast(ServerVersion.V1_13)) {


            // Add Phantom.
            addLootable(new Lootable(EntityType.PHANTOM,
                    new LootBuilder()
                            .setMaterial(Material.PHANTOM_MEMBRANE)
                            .setMin(0)
                            .setMax(1)
                            .addOnlyDropFors(EntityType.PLAYER).build()));

            // Add Pufferfish.
            addLootable(new Lootable(EntityType.PUFFERFISH,
                    new LootBuilder()
                            .setMaterial(Material.PUFFERFISH).build(),
                    new LootBuilder()
                            .setMaterial(Material.BONE_MEAL)
                            .setChance(5).build()));

            // Add Salmon.
            addLootable(new Lootable(EntityType.SALMON,
                    new LootBuilder()
                            .setMaterial(Material.SALMON)
                            .setBurnedMaterial(Material.COOKED_SALMON).build(),
                    new LootBuilder()
                            .setMaterial(Material.BONE_MEAL)
                            .setChance(5).build()));

            // Add Tropical Fish.
            addLootable(new Lootable(EntityType.TROPICAL_FISH,
                    new LootBuilder()
                            .setMaterial(Material.TROPICAL_FISH).build(),
                    new LootBuilder()
                            .setMaterial(Material.BONE_MEAL)
                            .setChance(5).build(),
                    new LootBuilder()
                            .setMaterial(Material.BONE)
                            .setMin(1)
                            .setMax(2)
                            .setChance(25)
                            .addOnlyDropFors(EntityType.PLAYER).build()));

            // Add Dolphin.
            addLootable(new Lootable(EntityType.DOLPHIN,
                    new LootBuilder()
                            .setMaterial(Material.COD)
                            .setBurnedMaterial(Material.COOKED_COD)
                            .setMin(0)
                            .setMax(1).build()));

            // Add Cod.
            addLootable(new Lootable(EntityType.COD,
                    new LootBuilder()
                            .setMaterial(Material.COD)
                            .setBurnedMaterial(Material.COOKED_COD).build(),
                    new LootBuilder()
                            .setMaterial(Material.BONE_MEAL)
                            .setChance(5).build()));

            // Add Turtle.
            addLootable(new Lootable(EntityType.TURTLE,
                    new LootBuilder()
                            .setMaterial(Material.SEAGRASS)
                            .setMin(0)
                            .setMax(2).build()));

            // Add Drowned.
            addLootable(new Lootable(EntityType.DROWNED,
                    new LootBuilder()
                            .setMaterial(Material.ROTTEN_FLESH)
                            .setMin(0)
                            .setMax(2).build(),
                    new LootBuilder()
                            .setMaterial(Material.GOLD_INGOT)
                            .setChance(5)
                            .addOnlyDropFors(EntityType.PLAYER).build()));
        }

        if (plugin.isServerVersionAtLeast(ServerVersion.V1_12)) {
            // Add Parrot.
            addLootable(new Lootable(EntityType.PARROT,
                    new LootBuilder()
                            .setMaterial(Material.FEATHER)
                            .setMin(1)
                            .setMax(2).build()));
        }


        Loot fish1 = plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? new LootBuilder()
                .addChildLoot(new LootBuilder()
                                .setMaterial(Material.COD)
                                .setBurnedMaterial(Material.COOKED_COD)
                                .setChance(50).build(),
                        new LootBuilder()
                                .setMaterial(Material.PRISMARINE_CRYSTALS)
                                .setChance(33).build())
                .build()
                :
                new LootBuilder()
                        .addChildLoot(new LootBuilder()
                                        .setMaterial(Material.valueOf("RAW_FISH"))
                                        .setBurnedMaterial(Material.valueOf("COOKED_FISH"))
                                        .setChance(50).build(),
                                new LootBuilder()
                                        .setMaterial(Material.PRISMARINE_CRYSTALS)
                                        .setChance(33).build())
                        .build();

        Loot fish2 = plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? new LootBuilder()
                .setChance(2.5)
                .addChildLoot(new LootBuilder()
                                .setMaterial(Material.COD)
                                .setChance(60)
                                .setAllowLootingEnchant(false).build(),
                        new LootBuilder()
                                .setMaterial(Material.SALMON)
                                .setChance(25)
                                .setAllowLootingEnchant(false).build(),
                        new LootBuilder()
                                .setMaterial(Material.PUFFERFISH)
                                .setChance(13)
                                .setAllowLootingEnchant(false).build(),
                        new LootBuilder()
                                .setMaterial(Material.TROPICAL_FISH)
                                .setChance(2)
                                .setAllowLootingEnchant(false).build())
                .addOnlyDropFors(EntityType.PLAYER).build()
                :
                new LootBuilder()
                        .setChance(2.5)
                        .addChildLoot(new LootBuilder()
                                        .setMaterial(Material.valueOf("RAW_FISH"))
                                        .setChance(60)
                                        .setAllowLootingEnchant(false).build(),
                                new LootBuilder()
                                        .setMaterial(Material.valueOf("RAW_FISH"))
                                        .setData(1)
                                        .setChance(25)
                                        .setAllowLootingEnchant(false).build(),
                                new LootBuilder()
                                        .setMaterial(Material.valueOf("RAW_FISH"))
                                        .setData(3)
                                        .setChance(13)
                                        .setAllowLootingEnchant(false).build(),
                                new LootBuilder()
                                        .setMaterial(Material.valueOf("RAW_FISH"))
                                        .setData(2)
                                        .setChance(2)
                                        .setAllowLootingEnchant(false).build())
                        .addOnlyDropFors(EntityType.PLAYER).build();

        if (plugin.isServerVersionAtLeast(ServerVersion.V1_11)) {
            // Add Zombie Villager.
            addLootable(new Lootable(EntityType.ZOMBIE_VILLAGER,
                    new LootBuilder()
                            .setMaterial(Material.ROTTEN_FLESH)
                            .setMin(0)
                            .setMax(2).build(),
                    new LootBuilder()
                            .setChance(2.5)
                            .setChildDropCount(1)
                            .addOnlyDropFors(EntityType.PLAYER)
                            .addChildLoot(new LootBuilder().setMaterial(Material.IRON_INGOT)
                                            .setAllowLootingEnchant(false).build(),
                                    new LootBuilder().setMaterial(Material.CARROT)
                                            .setAllowLootingEnchant(false).build(),
                                    new LootBuilder().setMaterial(Material.POTATO)
                                            .setAllowLootingEnchant(false).build())
                            .build()));

            // Add Llama.
            addLootable(new Lootable(EntityType.LLAMA,
                    new LootBuilder()
                            .setMaterial(Material.LEATHER)
                            .setMin(0)
                            .setMax(2).build()));

            // Add Zombie Horse.
            addLootable(new Lootable(EntityType.ZOMBIE_HORSE,
                    new LootBuilder()
                            .setMaterial(Material.ROTTEN_FLESH)
                            .setMin(0)
                            .setMax(2).build()));
            // Add Elder Guardian.
            addLootable(new Lootable(EntityType.ELDER_GUARDIAN,
                    new LootBuilder()
                            .setMaterial(Material.PRISMARINE_SHARD)
                            .setMin(0)
                            .setMax(2).build(),
                    fish1,
                    new LootBuilder()
                            .setMaterial(Material.SPONGE)
                            .addOnlyDropFors(EntityType.PLAYER)
                            .setAllowLootingEnchant(false).build(),
                    fish2));

            // Add Mule.
            addLootable(new Lootable(EntityType.MULE,
                    new LootBuilder()
                            .setMaterial(Material.LEATHER)
                            .setMin(0)
                            .setMax(2).build()));

            // Add Stray.
            addLootable(new Lootable(EntityType.STRAY,
                    new LootBuilder()
                            .setMaterial(Material.ARROW)
                            .setMin(0)
                            .setMax(2).build(),
                    new LootBuilder()
                            .setMaterial(Material.BONE)
                            .setMin(0)
                            .setMax(2).build()));

            Loot witherSkull = plugin.isServerVersionAtLeast(ServerVersion.V1_13) ?
                    new LootBuilder()
                            .setMaterial(Material.WITHER_SKELETON_SKULL)
                            .setChance(2.5)
                            .addOnlyDropFors(EntityType.PLAYER).build()
                    :
                    new LootBuilder()
                            .setMaterial(Material.valueOf("SKULL_ITEM"))
                            .setData(1)
                            .setChance(2.5)
                            .addOnlyDropFors(EntityType.PLAYER).build();

            // Add Wither Skeleton.
            addLootable(new Lootable(EntityType.WITHER_SKELETON,
                    new LootBuilder()
                            .setMaterial(Material.COAL)
                            .setChance(33).build(),
                    new LootBuilder()
                            .setMaterial(Material.BONE)
                            .setMin(0)
                            .setMax(2).build(),
                    witherSkull));        // Add Skeleton Horse.
            addLootable(new Lootable(EntityType.SKELETON_HORSE,
                    new LootBuilder()
                            .setMaterial(Material.BONE)
                            .setMin(0)
                            .setMax(2).build()));

            // Add Donkey.
            addLootable(new Lootable(EntityType.DONKEY,
                    new LootBuilder()
                            .setMaterial(Material.LEATHER)
                            .setMin(0)
                            .setMax(2).build()));

            // Add Vindicator.
            addLootable(new Lootable(EntityType.VINDICATOR,
                    new LootBuilder()
                            .setMaterial(Material.EMERALD)
                            .setMin(0)
                            .setMax(1)
                            .addOnlyDropFors(EntityType.PLAYER).build()));

            // Add Evoker.
            addLootable(new Lootable(EntityType.EVOKER,
                    new LootBuilder()
                            .setMaterial(plugin.isServerVersionAtLeast(ServerVersion.V1_13)
                                    ? Material.TOTEM_OF_UNDYING : Material.valueOf("TOTEM"))
                            .setAllowLootingEnchant(false).build(),
                    new LootBuilder()
                            .setMaterial(Material.EMERALD)
                            .setChance(50)
                            .addOnlyDropFors(EntityType.PLAYER).build()));
        }

        if (plugin.isServerVersionAtLeast(ServerVersion.V1_11)) {


            // Shulker.
            addLootable(new Lootable(EntityType.SHULKER,
                    new LootBuilder()
                            .setMaterial(Material.SHULKER_SHELL)
                            .setChance(50)
                            .setLootingIncrease(6.25).build()));
        }

        if (plugin.isServerVersionAtLeast(ServerVersion.V1_13)) {
            // Add Polar Bear.
            addLootable(new Lootable(EntityType.POLAR_BEAR,
                    new LootBuilder()
                            .setMaterial(Material.COD)
                            .setChance(75)
                            .setMin(0)
                            .setMax(2).build(),
                    new LootBuilder()
                            .setMaterial(Material.SALMON)
                            .setChance(25)
                            .setMin(0)
                            .setMax(2).build()));
        } else if (plugin.isServerVersionAtLeast(ServerVersion.V1_10)) {
            // Add Polar Bear.
            addLootable(new Lootable(EntityType.POLAR_BEAR,
                    new LootBuilder()
                            .setMaterial(Material.valueOf("RAW_FISH"))
                            .setChance(75)
                            .setMin(0)
                            .setMax(2).build(),
                    new LootBuilder()
                            .setMaterial(Material.valueOf("RAW_FISH"))
                            .setData(1)
                            .setChance(25)
                            .setMin(0)
                            .setMax(2).build()));
        }

        // Add Pig.
        addLootable(new Lootable(EntityType.PIG,
                new LootBuilder()
                        .setMaterial(plugin.isServerVersionAtLeast(ServerVersion.V1_13)
                                ? Material.PORKCHOP : Material.valueOf("PORK"))
                        .setBurnedMaterial(plugin.isServerVersionAtLeast(ServerVersion.V1_13)
                                ? Material.COOKED_PORKCHOP : Material.valueOf("GRILLED_PORK"))
                        .setMin(1)
                        .setMax(3).build()));


        // Add Cow.
        addLootable(new Lootable(EntityType.COW,
                new LootBuilder()
                        .setMaterial(Material.LEATHER)
                        .setMin(0)
                        .setMax(2).build(),
                new LootBuilder()
                        .setMaterial(plugin.isServerVersionAtLeast(ServerVersion.V1_13)
                                ? Material.BEEF : Material.valueOf("RAW_BEEF"))
                        .setBurnedMaterial(Material.COOKED_BEEF)
                        .setMin(1)
                        .setMax(3).build()));

        // Add Mushroom Cow.
        addLootable(new Lootable(EntityType.MUSHROOM_COW,
                new LootBuilder()
                        .setMaterial(Material.LEATHER)
                        .setMin(0)
                        .setMax(2).build(),
                new LootBuilder()
                        .setMaterial(plugin.isServerVersionAtLeast(ServerVersion.V1_13)
                                ? Material.BEEF : Material.valueOf("RAW_BEEF"))
                        .setBurnedMaterial(Material.COOKED_BEEF)
                        .setMin(1)
                        .setMax(3).build()));

        // Add Chicken.
        addLootable(new Lootable(EntityType.CHICKEN,
                new LootBuilder()
                        .setMaterial(Material.FEATHER)
                        .setMin(0)
                        .setMax(2).build(),
                new LootBuilder()
                        .setMaterial(plugin.isServerVersionAtLeast(ServerVersion.V1_13)
                                ? Material.CHICKEN : Material.valueOf("RAW_CHICKEN"))
                        .setBurnedMaterial(Material.COOKED_CHICKEN).build()));
        // Add Zombie.
        addLootable(new Lootable(EntityType.ZOMBIE,
                new LootBuilder()
                        .setMaterial(Material.ROTTEN_FLESH)
                        .setMin(0)
                        .setMax(2).build(),
                new LootBuilder()
                        .setChance(2.5)
                        .setChildDropCount(1)
                        .setAllowLootingEnchant(false)
                        .addOnlyDropFors(EntityType.PLAYER)
                        .addChildLoot(new LootBuilder().setMaterial(Material.IRON_INGOT)
                                        .setAllowLootingEnchant(false).build(),
                                new LootBuilder().setMaterial(Material.CARROT)
                                        .setAllowLootingEnchant(false).build(),
                                new LootBuilder().setMaterial(Material.POTATO)
                                        .setAllowLootingEnchant(false).build())
                        .build()));

        // Add Husk.
        addLootable(new Lootable(EntityType.ZOMBIE,
                new LootBuilder()
                        .setMaterial(Material.ROTTEN_FLESH)
                        .setMin(0)
                        .setMax(2).build(),
                new LootBuilder()
                        .setChance(2.5)
                        .setChildDropCount(1)
                        .setAllowLootingEnchant(false)
                        .addOnlyDropFors(EntityType.PLAYER)
                        .addChildLoot(new LootBuilder().setMaterial(Material.IRON_INGOT)
                                        .setAllowLootingEnchant(false).build(),
                                new LootBuilder().setMaterial(Material.CARROT)
                                        .setAllowLootingEnchant(false).build(),
                                new LootBuilder().setMaterial(Material.POTATO)
                                        .setAllowLootingEnchant(false).build())
                        .build()));

        Loot discs;
        if (plugin.isServerVersionAtLeast(ServerVersion.V1_13)) {
            discs = new LootBuilder()
                    .setChildDropCount(1)
                    .addOnlyDropFors(EntityType.SKELETON,
                            EntityType.STRAY)
                    .addChildLoot(new LootBuilder().setMaterial(Material.MUSIC_DISC_11).build(),
                            new LootBuilder().setMaterial(Material.MUSIC_DISC_13).build(),
                            new LootBuilder().setMaterial(Material.MUSIC_DISC_BLOCKS).build(),
                            new LootBuilder().setMaterial(Material.MUSIC_DISC_CAT).build(),
                            new LootBuilder().setMaterial(Material.MUSIC_DISC_CHIRP).build(),
                            new LootBuilder().setMaterial(Material.MUSIC_DISC_FAR).build(),
                            new LootBuilder().setMaterial(Material.MUSIC_DISC_MALL).build(),
                            new LootBuilder().setMaterial(Material.MUSIC_DISC_MELLOHI).build(),
                            new LootBuilder().setMaterial(Material.MUSIC_DISC_STAL).build(),
                            new LootBuilder().setMaterial(Material.MUSIC_DISC_STRAD).build(),
                            new LootBuilder().setMaterial(Material.MUSIC_DISC_WAIT).build(),
                            new LootBuilder().setMaterial(Material.MUSIC_DISC_WARD).build())
                    .build();
        } else if (plugin.isServerVersionAtLeast(ServerVersion.V1_11)) {
            discs = new LootBuilder()
                    .setChildDropCount(1)
                    .addOnlyDropFors(EntityType.SKELETON,
                            EntityType.STRAY)
                    .addChildLoot(new LootBuilder().setMaterial(Material.valueOf("GOLD_RECORD")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("GREEN_RECORD")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("RECORD_3")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("RECORD_4")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("RECORD_5")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("RECORD_6")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("RECORD_7")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("RECORD_8")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("RECORD_9")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("RECORD_10")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("RECORD_11")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("RECORD_12")).build())
                    .build();
        } else {
            discs = new LootBuilder()
                    .setChildDropCount(1)
                    .addOnlyDropFors(EntityType.SKELETON)
                    .addChildLoot(new LootBuilder().setMaterial(Material.valueOf("GOLD_RECORD")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("GREEN_RECORD")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("RECORD_3")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("RECORD_4")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("RECORD_5")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("RECORD_6")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("RECORD_7")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("RECORD_8")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("RECORD_9")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("RECORD_10")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("RECORD_11")).build(),
                            new LootBuilder().setMaterial(Material.valueOf("RECORD_12")).build())
                    .build();
        }

        // Add Creeper.
        addLootable(new Lootable(EntityType.CREEPER,
                new LootBuilder()
                        .setMaterial(plugin.isServerVersionAtLeast(ServerVersion.V1_13)
                                ? Material.GUNPOWDER : Material.valueOf("SULPHUR"))
                        .setMin(0)
                        .setMax(2).build(),
                discs));

        // Add Guardian.
        addLootable(new Lootable(EntityType.GUARDIAN,
                new LootBuilder()
                        .setMaterial(Material.PRISMARINE_SHARD)
                        .setMin(0)
                        .setMax(2).build(),
                fish1,
                fish2));

        // Add Witch.
        addLootable(new Lootable(EntityType.WITCH,
                new LootBuilder()
                        .setChildDropCounMin(1)
                        .setChildDropCountMax(3)
                        .addChildLoot(new LootBuilder()
                                        .setMaterial(Material.GLOWSTONE_DUST)
                                        .setChance(12.5)
                                        .setMin(0)
                                        .setMax(2).build(),
                                new LootBuilder()
                                        .setMaterial(Material.SUGAR)
                                        .setChance(12.5)
                                        .setMin(0)
                                        .setMax(2).build(),
                                new LootBuilder()
                                        .setMaterial(Material.REDSTONE)
                                        .setChance(12.5)
                                        .setMin(0)
                                        .setMax(2).build(),
                                new LootBuilder()
                                        .setMaterial(Material.SPIDER_EYE)
                                        .setChance(12.5)
                                        .setMin(0)
                                        .setMax(2).build(),
                                new LootBuilder()
                                        .setMaterial(Material.GLASS_BOTTLE)
                                        .setChance(12.5)
                                        .setMin(0)
                                        .setMax(2).build(),
                                new LootBuilder()
                                        .setMaterial(plugin.isServerVersionAtLeast(ServerVersion.V1_13)
                                                ? Material.GUNPOWDER : Material.valueOf("SULPHUR"))
                                        .setChance(12.5)
                                        .setMin(0)
                                        .setMax(2).build(),
                                new LootBuilder()
                                        .setMaterial(Material.STICK)
                                        .setChance(25)
                                        .setMin(0)
                                        .setMax(2).build()
                        ).build()));

        // Add Sheep.
        addLootable(new Lootable(EntityType.SHEEP,
                new LootBuilder()
                        .setMaterial(Material.MUTTON)
                        .setBurnedMaterial(Material.COOKED_MUTTON)
                        .setMin(1)
                        .setMax(2).build(),
                new LootBuilder()
                        .setMaterial(plugin.isServerVersionAtLeast(ServerVersion.V1_13)
                                ? Material.WHITE_WOOL : Material.valueOf("WOOL"))
                        .setMin(2)
                        .setMax(2).build()));

        // Add Squid.
        addLootable(new Lootable(EntityType.SQUID,
                new LootBuilder()
                        .setMaterial(plugin.isServerVersionAtLeast(ServerVersion.V1_13)
                                ? Material.INK_SAC : Material.valueOf("INK_SACK"))
                        .setMin(1)
                        .setMax(3).build()));

        // Add Spider.
        addLootable(new Lootable(EntityType.SPIDER,
                new LootBuilder()
                        .setMaterial(Material.STRING)
                        .setMin(0)
                        .setMax(2).build(),
                new LootBuilder()
                        .setMaterial(Material.SPIDER_EYE)
                        .setChance(33)
                        .addOnlyDropFors(EntityType.PLAYER).build()));

        // Add Cave Spider.
        addLootable(new Lootable(EntityType.CAVE_SPIDER,
                new LootBuilder()
                        .setMaterial(Material.STRING)
                        .setMin(0)
                        .setMax(2).build(),
                new LootBuilder()
                        .setMaterial(Material.SPIDER_EYE)
                        .setChance(33)
                        .addOnlyDropFors(EntityType.PLAYER).build()));

        // Add Enderman.
        addLootable(new Lootable(EntityType.ENDERMAN,
                new LootBuilder()
                        .setMaterial(Material.ENDER_PEARL)
                        .setMin(0)
                        .setMax(1).build()));

        // Add Blaze.
        addLootable(new Lootable(EntityType.BLAZE,
                new LootBuilder()
                        .setMaterial(Material.BLAZE_ROD)
                        .setMin(0)
                        .setMax(1)
                        .addOnlyDropFors(EntityType.PLAYER).build()));

        // Add Horse.
        addLootable(new Lootable(EntityType.HORSE,
                new LootBuilder()
                        .setMaterial(Material.LEATHER)
                        .setMin(0)
                        .setMax(2).build()));

        // Magma Cube.
        addLootable(new Lootable(EntityType.MAGMA_CUBE,
                new LootBuilder()
                        .setMaterial(Material.MAGMA_CREAM)
                        .setChance(25).build()));
        // Add Skeleton.
        addLootable(new Lootable(EntityType.SKELETON,
                new LootBuilder()
                        .setMaterial(Material.ARROW)
                        .setMin(0)
                        .setMax(2).build(),
                new LootBuilder()
                        .setMaterial(Material.BONE)
                        .setMin(0)
                        .setMax(2).build()));

        // Add Snowman.
        addLootable(new Lootable(EntityType.SNOWMAN,
                new LootBuilder()
                        .setMaterial(plugin.isServerVersionAtLeast(ServerVersion.V1_13)
                                ? Material.SNOWBALL : Material.valueOf("SNOW_BALL"))
                        .setMin(0)
                        .setMax(15).build()));

        // Add Rabbit.
        addLootable(new Lootable(EntityType.RABBIT,
                new LootBuilder()
                        .setMaterial(Material.RABBIT_HIDE)
                        .setMin(0)
                        .setMax(1).build(),
                new LootBuilder()
                        .setMaterial(Material.RABBIT)
                        .setBurnedMaterial(Material.COOKED_RABBIT)
                        .setMin(0)
                        .setMax(1).build()));

        // Add Iron Golem.
        addLootable(new Lootable(EntityType.IRON_GOLEM,
                new LootBuilder()
                        .setMaterial(plugin.isServerVersionAtLeast(ServerVersion.V1_13)
                                ? Material.POPPY : Material.valueOf("RED_ROSE"))
                        .setMin(0)
                        .setMax(2).build(),
                new LootBuilder()
                        .setMaterial(Material.IRON_INGOT)
                        .setMin(3)
                        .setMax(5).build()));

        // Add Slime.
        addLootable(new Lootable(EntityType.SLIME,
                new LootBuilder()
                        .setMaterial(Material.SLIME_BALL)
                        .setMin(0)
                        .setMax(2).build()));

        // Add Ghast.
        addLootable(new Lootable(EntityType.GHAST,
                new LootBuilder()
                        .setMaterial(Material.GHAST_TEAR)
                        .setMin(0)
                        .setMax(1).build(),
                new LootBuilder()
                        .setMaterial(plugin.isServerVersionAtLeast(ServerVersion.V1_13)
                                ? Material.GUNPOWDER : Material.valueOf("SULPHUR"))
                        .setMin(0)
                        .setMax(2).build()));

        // Add Zombie Pigman
        addLootable(new Lootable(EntityType.PIG_ZOMBIE,
                new LootBuilder()
                        .setMaterial(Material.ROTTEN_FLESH)
                        .setMin(0)
                        .setMax(1).build(),
                new LootBuilder()
                        .setMaterial(Material.GOLD_NUGGET)
                        .setMin(0)
                        .setMax(1).build(),
                new LootBuilder()
                        .setMaterial(Material.GOLD_INGOT)
                        .setChance(2.5)
                        .addOnlyDropFors(EntityType.PLAYER).build()));

        // Add Wither.
        addLootable(new Lootable(EntityType.WITHER,
                new LootBuilder()
                        .setMaterial(Material.NETHER_STAR)
                        .setAllowLootingEnchant(false).build()));


        for (EntityType value : EntityType.values()) {
            if (value.isSpawnable() && value.isAlive() && !value.toString().contains("ARMOR")) {
                if (registeredLootables.containsKey(value)) continue;
                addLootable(new Lootable(value));
            }
        }

        // Save to file
        for (Lootable lootable : registeredLootables.values()) {
            try {
                File dir = new File(lootablesDir);
                dir.mkdir();

                File file = new File(lootablesDir + "/" + lootable.getType().name().toLowerCase() + ".json");
                if (file.exists()) continue;

                try (Writer writer = new FileWriter(file.getPath())) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    gson.toJson(lootable, writer);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        registeredLootables.clear();
    }
}
