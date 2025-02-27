package com.karasu256.one_shot_glory.util;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public enum BuffType {
    SPEED(Material.FEATHER, List.of(PotionEffectType.SPEED)),
    STRENGTH(Material.IRON_SWORD, List.of(PotionEffectType.STRENGTH)),
    REGENERATION(Material.GOLDEN_APPLE, List.of(PotionEffectType.REGENERATION)),
    RESISTANCE(Material.SHIELD, List.of(PotionEffectType.RESISTANCE)),
    JUMP(Material.SLIME_BALL, List.of(PotionEffectType.JUMP_BOOST)),
    FIRE(Material.BLAZE_POWDER, List.of(PotionEffectType.FIRE_RESISTANCE)),;

    private Material itemType;
    private List<PotionEffectType> potionEffectTypes;

    public ItemStack getItemStack() {
        return new ItemStack(itemType);
    }

    public List<PotionEffectType> getPotionEffectTypes() {
        return potionEffectTypes;
    }

    BuffType(Material itemType, List<PotionEffectType> potionEffectTypes) {
            this.itemType = itemType;
            this.potionEffectTypes = potionEffectTypes;
    }

    public static BuffType getBuffTypeByItemStack(ItemStack itemStack) {
        for (var buffType : values()) {
            if (buffType.itemType == itemStack.getType()) {
                return buffType;
            }
        }
        return null;
    }
}
