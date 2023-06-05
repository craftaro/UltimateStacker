package com.craftaro.ultimatestacker.api.stack.block;

import com.craftaro.ultimatestacker.api.utils.Hologramable;
import com.craftaro.ultimatestacker.api.utils.Stackable;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.database.Data;

public interface BlockStack extends Stackable, Hologramable, Data {


    int getId();

    void destroy();

    CompatibleMaterial getMaterial();
}
