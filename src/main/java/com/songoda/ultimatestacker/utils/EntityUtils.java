package com.songoda.ultimatestacker.utils;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.Check;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.settings.Settings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;

import java.util.*;
import java.util.stream.Collectors;

public class EntityUtils {

    UltimateStacker plugin = UltimateStacker.getInstance();

    private final List<String> checks = Settings.STACK_CHECKS.getStringList();
    private final boolean stackFlyingDown = Settings.ONLY_STACK_FLYING_DOWN.getBoolean();
    private final boolean keepFire = Settings.KEEP_FIRE.getBoolean();
    private final boolean keepPotion = Settings.KEEP_POTION.getBoolean();
    private final boolean stackWholeChunk = Settings.STACK_WHOLE_CHUNK.getBoolean();
    private final int searchRadius = Settings.SEARCH_RADIUS.getInt();

    private final Map<CachedChunk, Entity[]> cachedChunks = new HashMap<>();

    public void clearChunkCache() {
        this.cachedChunks.clear();
    }


    private Set<CachedChunk> getNearbyChunks(Location location, double radius, boolean singleChunk) {
        World world = location.getWorld();
        Set<CachedChunk> chunks = new HashSet<>();
        if (world == null) return chunks;

        CachedChunk firstChunk = new CachedChunk(location);
        chunks.add(firstChunk);

        if (singleChunk) return chunks;

        int minX = (int) Math.floor(((location.getX() - radius) - 2.0D) / 16.0D);
        int maxX = (int) Math.floor(((location.getX() + radius) + 2.0D) / 16.0D);
        int minZ = (int) Math.floor(((location.getZ() - radius) - 2.0D) / 16.0D);
        int maxZ = (int) Math.floor(((location.getZ() + radius) + 2.0D) / 16.0D);

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                if (firstChunk.getX() == x && firstChunk.getZ() == z) continue;
                chunks.add(new CachedChunk(world.getName(), x, z));
            }
        }
        return chunks;
    }

    private List<LivingEntity> getNearbyEntities(Location location, double radius, boolean singleChunk) {
        List<LivingEntity> entities = new ArrayList<>();
        for (CachedChunk chunk : getNearbyChunks(location, radius, singleChunk)) {
            Entity[] entityArray;
            if (cachedChunks.containsKey(chunk)) {
                entityArray = cachedChunks.get(chunk);
            } else {
                entityArray = chunk.getEntities();
                cachedChunks.put(chunk, entityArray);
            }
            for (Entity e : entityArray) {
                if (e.getWorld() != location.getWorld()
                        || !(e instanceof LivingEntity)
                        || (!singleChunk && location.distanceSquared(e.getLocation()) >= radius * radius)) continue;
                entities.add((LivingEntity) e);
            }
        }
        return entities;
    }

    public int getSimilarStacksInChunk(LivingEntity entity) {
        int count = 0;
        for (LivingEntity e : getNearbyEntities(entity.getLocation(), -1, true)) {
            if (entity.getType() == e.getType() && plugin.getEntityStackManager().isStacked(e))
                count++;
        }
        return count;
    }

    public LivingEntity newEntity(LivingEntity toClone) {
        LivingEntity newEntity = (LivingEntity) toClone.getWorld().spawnEntity(toClone.getLocation(), toClone.getType());

        Player player = toClone.getKiller();
        if (player == null
                || !Settings.DISABLE_KNOCKBACK.getBoolean()
                || player.getItemInHand().getEnchantmentLevel(Enchantment.KNOCKBACK) != 0) {
            newEntity.setVelocity(toClone.getVelocity());
        }

        for (String checkStr : checks) {
            Check check = Check.valueOf(checkStr);
            switch (check) {
                case AGE: {
                    if (!(toClone instanceof Ageable) || ((Ageable) toClone).isAdult()) break;
                    ((Ageable) newEntity).setBaby();
                    break;
                }
                case NERFED: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)) break;
                    if (!toClone.hasAI()) newEntity.setAI(false);
                }
                case IS_TAMED: {
                    if (!(toClone instanceof Tameable)) break;
                    ((Tameable) newEntity).setTamed(((Tameable) toClone).isTamed());
                }
                case ANIMAL_OWNER: {
                    if (!(toClone instanceof Tameable)) break;
                    ((Tameable) newEntity).setOwner(((Tameable) toClone).getOwner());
                }
                case SKELETON_TYPE: {
                    if (!(toClone instanceof Skeleton)
                            || ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)) break;
                    ((Skeleton) newEntity).setSkeletonType(((Skeleton) toClone).getSkeletonType());
                    break;
                }
                case SHEEP_COLOR: {
                    if (!(toClone instanceof Sheep)) break;
                    ((Sheep) newEntity).setColor(((Sheep) toClone).getColor());
                    break;
                }
                case SHEEP_SHEARED: {
                    if (!(toClone instanceof Sheep)) break;
                    ((Sheep) newEntity).setSheared(((Sheep) toClone).isSheared());
                    break;
                }
                case SNOWMAN_DERPED: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)
                            || !(toClone instanceof Snowman)) break;
                    ((Snowman) newEntity).setDerp(((Snowman) toClone).isDerp());
                    break;
                }
                case LLAMA_COLOR: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)
                            || !(toClone instanceof Llama)) break;
                    ((Llama) newEntity).setColor(((Llama) toClone).getColor());
                    break;
                }
                case LLAMA_STRENGTH: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)
                            || !(toClone instanceof Llama)) break;
                    ((Llama) newEntity).setStrength(((Llama) toClone).getStrength());
                    break;
                }
                case VILLAGER_PROFESSION: {
                    if (!(toClone instanceof Villager)) break;
                    ((Villager) newEntity).setProfession(((Villager) toClone).getProfession());
                    break;
                }
                case SLIME_SIZE: {
                    if (!(toClone instanceof Slime)) break;
                    ((Slime) newEntity).setSize(((Slime) toClone).getSize());
                    break;
                }
                case HORSE_JUMP: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)
                            || !(toClone instanceof AbstractHorse)) break;
                    ((AbstractHorse) newEntity).setJumpStrength(((AbstractHorse) toClone).getJumpStrength());
                    break;
                }
                case HORSE_COLOR: {
                    if (!(toClone instanceof Horse)) break;
                    ((Horse) newEntity).setColor(((Horse) toClone).getColor());
                    break;
                }
                case HORSE_STYLE: {
                    if (!(toClone instanceof Horse)) break;
                    ((Horse) newEntity).setStyle(((Horse) toClone).getStyle());
                    break;
                }
                case ZOMBIE_BABY: {
                    if (!(toClone instanceof Zombie)) break;
                    ((Zombie) newEntity).setBaby(((Zombie) toClone).isBaby());
                    break;
                }
                case WOLF_COLLAR_COLOR: {
                    if (!(toClone instanceof Wolf)) break;
                    ((Wolf) newEntity).setCollarColor(((Wolf) toClone).getCollarColor());
                    break;
                }
                case OCELOT_TYPE: {
                    if (!(toClone instanceof Ocelot)
                            || ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14)) break;
                    ((Ocelot) newEntity).setCatType(((Ocelot) toClone).getCatType());
                }
                case CAT_TYPE: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14)
                            || !(toClone instanceof Cat)) break;
                    ((Cat) newEntity).setCatType(((Cat) toClone).getCatType());
                    break;
                }
                case RABBIT_TYPE: {
                    if (!(toClone instanceof Rabbit)) break;
                    ((Rabbit) newEntity).setRabbitType(((Rabbit) toClone).getRabbitType());
                    break;
                }
                case PARROT_TYPE: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12)
                            || !(toClone instanceof Parrot)) break;
                    ((Parrot) newEntity).setVariant(((Parrot) toClone).getVariant());
                    break;
                }
                case PUFFERFISH_STATE: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                            || !(toClone instanceof PufferFish)) break;
                    ((PufferFish) newEntity).setPuffState(((PufferFish) toClone).getPuffState());
                    break;
                }
                case TROPICALFISH_PATTERN: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                            || !(toClone instanceof TropicalFish)) break;
                    ((TropicalFish) newEntity).setPattern(((TropicalFish) toClone).getPattern());
                    break;
                }
                case TROPICALFISH_PATTERN_COLOR: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                            || !(toClone instanceof TropicalFish)) break;
                    ((TropicalFish) newEntity).setPatternColor(((TropicalFish) toClone).getPatternColor());
                    break;
                }
                case TROPICALFISH_BODY_COLOR: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                            || !(toClone instanceof TropicalFish)) break;
                    ((TropicalFish) newEntity).setBodyColor(((TropicalFish) toClone).getBodyColor());
                    break;
                }
                case PHANTOM_SIZE: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                            || !(toClone instanceof Phantom)) break;
                    ((Phantom) newEntity).setSize(((Phantom) toClone).getSize());
                    break;
                }
            }
        }

        if (keepFire)
            newEntity.setFireTicks(toClone.getFireTicks());
        if (keepPotion)
            newEntity.addPotionEffects(toClone.getActivePotionEffects());

        return newEntity;
    }

    public List<LivingEntity> getSimilarEntitiesAroundEntity(LivingEntity initialEntity, Location location) {
        // Create a list of all entities around the initial entity of the same type.
        List<LivingEntity> entityList = getNearbyEntities(location, searchRadius, stackWholeChunk)
                .stream().filter(entity -> entity.getType() == initialEntity.getType() && entity != initialEntity)
                .collect(Collectors.toCollection(LinkedList::new));

        if (stackFlyingDown && Methods.canFly(initialEntity))
            entityList.removeIf(entity -> entity.getLocation().getY() > initialEntity.getLocation().getY());

        for (String checkStr : checks) {
            Check check = Check.getCheck(checkStr);
            if (check == null) continue;
            switch (check) {
                case SPAWN_REASON: {
                    if (initialEntity.hasMetadata("US_REASON"))
                        entityList.removeIf(entity -> entity.hasMetadata("US_REASON") && !entity.getMetadata("US_REASON").get(0).asString().equals("US_REASON"));
                }
                case AGE: {
                    if (!(initialEntity instanceof Ageable)) break;

                    if (((Ageable) initialEntity).isAdult()) {
                        entityList.removeIf(entity -> !((Ageable) entity).isAdult());
                    } else {
                        entityList.removeIf(entity -> ((Ageable) entity).isAdult());
                    }
                    break;
                }
                case NERFED: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)) break;
                    entityList.removeIf(entity -> entity.hasAI() != initialEntity.hasAI());
                }
                case IS_TAMED: {
                    if (!(initialEntity instanceof Tameable)) break;
                    if (((Tameable) initialEntity).isTamed()) {
                        entityList.removeIf(entity -> !((Tameable) entity).isTamed());
                    } else {
                        entityList.removeIf(entity -> ((Tameable) entity).isTamed());
                    }
                }
                case ANIMAL_OWNER: {
                    if (!(initialEntity instanceof Tameable)) break;

                    Tameable tameable = ((Tameable) initialEntity);
                    entityList.removeIf(entity -> ((Tameable) entity).getOwner() != tameable.getOwner());
                }
                case PIG_SADDLE: {
                    if (!(initialEntity instanceof Pig)) break;
                    entityList.removeIf(entity -> ((Pig) entity).hasSaddle());
                    break;
                }
                case SKELETON_TYPE: {
                    if (!(initialEntity instanceof Skeleton)) break;

                    Skeleton skeleton = (Skeleton) initialEntity;
                    entityList.removeIf(entity -> ((Skeleton) entity).getSkeletonType() != skeleton.getSkeletonType());
                    break;
                }
                case SHEEP_COLOR: {
                    if (!(initialEntity instanceof Sheep)) break;

                    Sheep sheep = ((Sheep) initialEntity);
                    entityList.removeIf(entity -> ((Sheep) entity).getColor() != sheep.getColor());
                    break;
                }
                case SHEEP_SHEARED: {
                    if (!(initialEntity instanceof Sheep)) break;

                    Sheep sheep = ((Sheep) initialEntity);
                    if (sheep.isSheared()) {
                        entityList.removeIf(entity -> !((Sheep) entity).isSheared());
                    } else {
                        entityList.removeIf(entity -> ((Sheep) entity).isSheared());
                    }
                    break;
                }
                case SNOWMAN_DERPED: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)
                            || !(initialEntity instanceof Snowman)) break;

                    Snowman snowman = ((Snowman) initialEntity);
                    if (snowman.isDerp()) {
                        entityList.removeIf(entity -> !((Snowman) entity).isDerp());
                    } else {
                        entityList.removeIf(entity -> ((Snowman) entity).isDerp());
                    }
                    break;
                }
                case LLAMA_COLOR: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)
                            || !(initialEntity instanceof Llama)) break;
                    Llama llama = ((Llama) initialEntity);
                    entityList.removeIf(entity -> ((Llama) entity).getColor() != llama.getColor());
                    break;
                }
                case LLAMA_STRENGTH: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)
                            || !(initialEntity instanceof Llama)) break;
                    Llama llama = ((Llama) initialEntity);
                    entityList.removeIf(entity -> ((Llama) entity).getStrength() != llama.getStrength());
                    break;
                }
                case VILLAGER_PROFESSION: {
                    if (!(initialEntity instanceof Villager)) break;
                    Villager villager = ((Villager) initialEntity);
                    entityList.removeIf(entity -> ((Villager) entity).getProfession() != villager.getProfession());
                    break;
                }
                case SLIME_SIZE: {
                    if (!(initialEntity instanceof Slime)) break;
                    Slime slime = ((Slime) initialEntity);
                    entityList.removeIf(entity -> ((Slime) entity).getSize() != slime.getSize());
                    break;
                }
                case HORSE_CARRYING_CHEST: {
                    if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)) {
                        if (!(initialEntity instanceof ChestedHorse)) break;
                        entityList.removeIf(entity -> ((ChestedHorse) entity).isCarryingChest());
                    } else {
                        if (!(initialEntity instanceof Horse)) break;
                        entityList.removeIf(entity -> ((Horse) entity).isCarryingChest());
                    }
                    break;
                }
                case HORSE_HAS_ARMOR: {
                    if (!(initialEntity instanceof Horse)) break;
                    entityList.removeIf(entity -> ((Horse) entity).getInventory().getArmor() != null);
                    break;
                }
                case HORSE_HAS_SADDLE: {
                    if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                            && initialEntity instanceof AbstractHorse) {
                        entityList.removeIf(entity -> ((AbstractHorse) entity).getInventory().getSaddle() != null);
                        break;
                    }
                    if (!(initialEntity instanceof Horse)) break;
                    entityList.removeIf(entity -> ((Horse) entity).getInventory().getSaddle() != null);
                    break;
                }
                case HORSE_JUMP: {
                    if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)) {
                        if (!(initialEntity instanceof AbstractHorse)) break;
                        AbstractHorse horse = ((AbstractHorse) initialEntity);
                        entityList.removeIf(entity -> ((AbstractHorse) entity).getJumpStrength() != horse.getJumpStrength());
                    } else {
                        if (!(initialEntity instanceof Horse)) break;
                        Horse horse = ((Horse) initialEntity);
                        entityList.removeIf(entity -> ((Horse) entity).getJumpStrength() != horse.getJumpStrength());

                    }
                    break;
                }
                case HORSE_COLOR: {
                    if (!(initialEntity instanceof Horse)) break;
                    Horse horse = ((Horse) initialEntity);
                    entityList.removeIf(entity -> ((Horse) entity).getColor() != horse.getColor());
                    break;
                }
                case HORSE_STYLE: {
                    if (!(initialEntity instanceof Horse)) break;
                    Horse horse = ((Horse) initialEntity);
                    entityList.removeIf(entity -> ((Horse) entity).getStyle() != horse.getStyle());
                    break;
                }
                case ZOMBIE_BABY: {
                    if (!(initialEntity instanceof Zombie)) break;
                    Zombie zombie = (Zombie) initialEntity;
                    entityList.removeIf(entity -> ((Zombie) entity).isBaby() != zombie.isBaby());
                    break;
                }
                case WOLF_COLLAR_COLOR: {
                    if (!(initialEntity instanceof Wolf)) break;
                    Wolf wolf = (Wolf) initialEntity;
                    entityList.removeIf(entity -> ((Wolf) entity).getCollarColor() != wolf.getCollarColor());
                    break;
                }
                case OCELOT_TYPE: {
                    if (!(initialEntity instanceof Ocelot)) break;
                    Ocelot ocelot = (Ocelot) initialEntity;
                    entityList.removeIf(entity -> ((Ocelot) entity).getCatType() != ocelot.getCatType());
                }
                case CAT_TYPE: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14)
                            || !(initialEntity instanceof Cat)) break;
                    Cat cat = (Cat) initialEntity;
                    entityList.removeIf(entity -> ((Cat) entity).getCatType() != cat.getCatType());
                    break;
                }
                case HAS_EQUIPMENT: {
                    if (initialEntity.getEquipment() == null) break;
                    boolean imEquipped = isEquipped(initialEntity);
                    if (imEquipped)
                        entityList = new ArrayList<>();
                    else
                        entityList.removeIf(this::isEquipped);
                    break;
                }
                case RABBIT_TYPE: {
                    if (!(initialEntity instanceof Rabbit)) break;
                    Rabbit rabbit = (Rabbit) initialEntity;
                    entityList.removeIf(entity -> ((Rabbit) entity).getRabbitType() != rabbit.getRabbitType());
                    break;
                }
                case PARROT_TYPE: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12)
                            || !(initialEntity instanceof Parrot)) break;
                    Parrot parrot = (Parrot) initialEntity;
                    entityList.removeIf(entity -> ((Parrot) entity).getVariant() != parrot.getVariant());
                    break;
                }
                case PUFFERFISH_STATE: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                            || !(initialEntity instanceof PufferFish)) break;
                    PufferFish pufferFish = (PufferFish) initialEntity;
                    entityList.removeIf(entity -> ((PufferFish) entity).getPuffState() != pufferFish.getPuffState());
                    break;
                }
                case TROPICALFISH_PATTERN: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                            || !(initialEntity instanceof TropicalFish)) break;
                    TropicalFish tropicalFish = (TropicalFish) initialEntity;
                    entityList.removeIf(entity -> ((TropicalFish) entity).getPattern() != tropicalFish.getPattern());
                    break;
                }
                case TROPICALFISH_PATTERN_COLOR: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                            || !(initialEntity instanceof TropicalFish)) break;
                    TropicalFish tropicalFish = (TropicalFish) initialEntity;
                    entityList.removeIf(entity -> ((TropicalFish) entity).getPatternColor() != tropicalFish.getPatternColor());
                    break;
                }
                case TROPICALFISH_BODY_COLOR: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                            || !(initialEntity instanceof TropicalFish)) break;
                    TropicalFish tropicalFish = (TropicalFish) initialEntity;
                    entityList.removeIf(entity -> ((TropicalFish) entity).getBodyColor() != tropicalFish.getBodyColor());
                    break;
                }
                case PHANTOM_SIZE: {
                    if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                            || !(initialEntity instanceof Phantom)) break;
                    Phantom phantom = (Phantom) initialEntity;
                    entityList.removeIf(entity -> ((Phantom) entity).getSize() != phantom.getSize());
                    break;
                }
            }
        }

        if (initialEntity.hasMetadata("breedCooldown")) {
            entityList.removeIf(entity -> !entity.hasMetadata("breedCooldown"));
        }

        return entityList;
    }

    public boolean isEquipped(LivingEntity initialEntity) {
        return initialEntity.getEquipment() != null
                && (initialEntity.getEquipment().getItemInHand().getType() != Material.AIR
                || (initialEntity.getEquipment().getHelmet() != null
                && initialEntity.getEquipment().getHelmet().getType() != Material.AIR)
                || (initialEntity.getEquipment().getChestplate() != null
                && initialEntity.getEquipment().getChestplate().getType() != Material.AIR)
                || (initialEntity.getEquipment().getLeggings() != null
                && initialEntity.getEquipment().getLeggings().getType() != Material.AIR)
                || (initialEntity.getEquipment().getBoots() != null
                && initialEntity.getEquipment().getBoots().getType() != Material.AIR));
    }

    public void splitFromStack(LivingEntity entity) {
        UltimateStacker instance = plugin;
        EntityStack stack = instance.getEntityStackManager().getStack(entity);

        if (stack.getAmount() <= 1) return;

        LivingEntity newEntity = newEntity(entity);

        int newAmount = stack.getAmount() - 1;
        if (newAmount != 1)
            instance.getEntityStackManager().addStack(new EntityStack(newEntity, newAmount));
        stack.setAmount(1);
        instance.getEntityStackManager().removeStack(entity);
        entity.setVelocity(Methods.getRandomVector());
    }
}
