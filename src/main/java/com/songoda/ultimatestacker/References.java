package com.songoda.ultimatestacker;

public class References {

    private String prefix;

    public References() {
        prefix = UltimateStacker.getInstance().getLocale().getMessage("general.nametag.prefix") + " ";
    }

    public String getPrefix() {
        return this.prefix;
    }
}