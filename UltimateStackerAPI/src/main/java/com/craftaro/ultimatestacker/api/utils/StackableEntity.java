package com.craftaro.ultimatestacker.api.utils;

public interface StackableEntity {

    int getAmount();

    void setAmount(int amount);

    void add(int amount);

    void take(int amount);

    //int getMaxStackSize();
}
