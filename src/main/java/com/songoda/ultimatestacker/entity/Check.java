package com.songoda.ultimatestacker.entity;

public enum Check {

    SPAWN_REASON(false), // ToDo: This
    NERFED(true),
    AGE(true),
    TICK_AGE(false),
    IS_TAMED(true),
    ANIMAL_OWNER(true),
    SKELETON_TYPE(true),
    ZOMBIE_BABY(true),
    SLIME_SIZE(true),
    ENDERMAN_CARRY_BLOCK(true),
    PIG_SADDLE(true),
    SHEEP_SHEERED(true),
    SHEEP_COLOR(true),
    WOLF_COLLAR_COLOR(true),
    OCELOT_TYPE(true),
    HORSE_COLOR(true),
    HORSE_STYLE(true),
    HORSE_CARRYING_CHEST(true),
    HORSE_HAS_ARMOR(true),
    HORSE_HAS_SADDLE(true),
    HORSE_JUMP(true),
    RABBIT_TYPE(true),
    VILLAGER_PROFESSION(true),
    LLAMA_COLOR(true),
    LLAMA_STRENGTH(true),
    PARROT_TYPE(true),
    PUFFERFISH_STATE(true),
    TROPICALFISH_PATTERN(true),
    TROPICALFISH_BODY_COLOR(true),
    TROPICALFISH_PATTERN_COLOR(true),
    PHANTOM_SIZE(true),
    CAT_TYPE(true);

    private boolean isEnabledByDefault;

    Check(boolean isEnabledByDefault) {
        this.isEnabledByDefault = isEnabledByDefault;
    }

    public boolean isEnabledByDefault() {
        return isEnabledByDefault;
    }
}
