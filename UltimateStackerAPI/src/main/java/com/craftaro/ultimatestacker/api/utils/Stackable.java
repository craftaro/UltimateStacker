package com.craftaro.ultimatestacker.api.utils;


public interface Stackable {

    int getAmount();

    void setAmount(int amount);

    void add(int amount);

    void take(int amount);

    boolean isValid();
}
