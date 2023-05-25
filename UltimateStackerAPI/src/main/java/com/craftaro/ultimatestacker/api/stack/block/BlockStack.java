package com.craftaro.ultimatestacker.api.stack.block;

import com.craftaro.ultimatestacker.api.utils.Hologramable;
import com.craftaro.ultimatestacker.api.utils.Stackable;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.hooks.stackers.UltimateStacker;
import com.songoda.core.utils.TextUtils;
import org.bukkit.Location;
import org.bukkit.World;

public interface BlockStack extends Stackable, Hologramable {


    int getId();

    void destroy();

    CompatibleMaterial getMaterial();
}
