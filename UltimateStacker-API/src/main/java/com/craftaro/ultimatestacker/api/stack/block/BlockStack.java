package com.craftaro.ultimatestacker.api.stack.block;

import com.craftaro.core.database.Data;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.ultimatestacker.api.utils.Hologramable;
import com.craftaro.ultimatestacker.api.utils.Stackable;

public interface BlockStack extends Stackable, Hologramable, Data {


    int getId();

    void destroy();

    XMaterial getMaterial();
}
