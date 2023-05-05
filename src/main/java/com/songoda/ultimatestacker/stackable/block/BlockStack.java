package com.songoda.ultimatestacker.stackable.block;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.utils.TextUtils;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.settings.Settings;
import com.songoda.ultimatestacker.stackable.Hologramable;
import com.songoda.ultimatestacker.utils.Stackable;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class BlockStack implements Stackable, Hologramable {

    // This is the unique identifier for this stack.
    // It is reset on every plugin load.
    // Used for holograms.
    private final UUID uniqueId = UUID.randomUUID();

    // The id that identifies this stack in the database.
    private int id;

    private int amount = 1;
    private final CompatibleMaterial material;
    private final Location location;

    public BlockStack(CompatibleMaterial material, Location location) {
        this.material = material;
        this.location = location;
    }

    public BlockStack(CompatibleMaterial material, Location location, int amount) {
        this.amount = amount;
        this.material = material;
        this.location = location;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public boolean isValid() {
        return CompatibleMaterial.getMaterial(location.getBlock()) == material;
    }

    public void add(int amount) {
        this.amount = this.amount + amount;
    }

    public void take(int amount) {
        this.amount = this.amount - amount;
    }

    public int getX() {
        return location.getBlockX();
    }

    public int getY() {
        return location.getBlockY();
    }

    public int getZ() {
        return location.getBlockZ();
    }

    public World getWorld() {
        return location.getWorld();
    }

    public void destroy() {
        amount = 0;
        UltimateStacker plugin = UltimateStacker.getInstance();
        plugin.getBlockStackManager().removeBlock(location);
        plugin.removeHologram(this);
        plugin.getDataManager().deleteBlock(this);
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public CompatibleMaterial getMaterial() {
        return material;
    }

    @Override
    public String getHologramName() {
        String nameFormat = Settings.NAME_FORMAT_BLOCK.getString();
        String displayName = TextUtils.formatText(material.name().toLowerCase().replace("_", " "), true);

        nameFormat = nameFormat.replace("{TYPE}", displayName);
        nameFormat = nameFormat.replace("{AMT}", Integer.toString(amount));

        return TextUtils.formatText(nameFormat).trim();
    }

    @Override
    public boolean areHologramsEnabled() {
        return Settings.BLOCK_HOLOGRAMS.getBoolean();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getHologramId() {
        return "UltimateStacker-" + uniqueId;
    }

    @Override
    public String toString() {
        return "BlockStack{" +
                "id=" + id +
                ", amount=" + amount +
                ", material=" + material +
                ", location=" + location +
                '}';
    }
}
