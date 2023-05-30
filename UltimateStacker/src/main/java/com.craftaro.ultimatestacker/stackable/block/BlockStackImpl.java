package com.craftaro.ultimatestacker.stackable.block;

import com.craftaro.ultimatestacker.UltimateStacker;
import com.craftaro.ultimatestacker.api.stack.block.BlockStack;
import com.craftaro.ultimatestacker.settings.Settings;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.utils.TextUtils;
import org.bukkit.Location;

import java.util.UUID;

public class BlockStackImpl implements BlockStack {

    // This is the unique identifier for this stack.
    // It is reset on every plugin load.
    // Used for holograms.
    private final UUID uniqueId = UUID.randomUUID();

    // The id that identifies this stack in the database.
    private int id;

    private int amount = 1;
    private final CompatibleMaterial material;
    private final Location location;

    public BlockStackImpl(CompatibleMaterial material, Location location) {
        this.material = material;
        this.location = location;
    }

    public BlockStackImpl(CompatibleMaterial material, Location location, int amount) {
        this.amount = amount;
        this.material = material;
        this.location = location;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public void setAmount(int amount) {
        if (amount < 1) {
            destroy();
            return;
        }
        this.amount = amount;
    }

    @Override
    public boolean isValid() {
        return CompatibleMaterial.getMaterial(location.getBlock()) == material;
    }

    @Override
    public void add(int amount) {
        this.amount = this.amount + amount;
    }

    @Override
    public void take(int amount) {
        this.amount = this.amount - amount;
    }

    @Override
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

    @Override
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

    @Override
    public String getHologramId() {
        return "UltimateStacker-" + uniqueId;
    }

    @Override
    public String toString() {
        return "BlockStackImpl{" +
                "id=" + id +
                ", amount=" + amount +
                ", material=" + material +
                ", location=" + location +
                '}';
    }
}
