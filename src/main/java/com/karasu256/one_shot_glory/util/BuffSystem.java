package com.karasu256.one_shot_glory.util;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class BuffSystem {
    private final BuffType buffType;

    public BuffSystem(BuffType buffType) {
        this.buffType = buffType;
    }

    public BuffType getBuffType() {
        return buffType;
    }

    public void applyBuff(Player player) {
        if (buffType == null) {
            return;
        }

        var potionEffectTypes = buffType.getPotionEffectTypes();
        var armorStand = geArmorStand(player);
        for (var potionEffectType : potionEffectTypes) {
            player.addPotionEffect(potionEffectType.createEffect(200, 1));
            armorStand.addPotionEffect(potionEffectType.createEffect(200, 1));
        }
    }

    public void removeBuff(Player player) {
        if (buffType == null) {
            return;
        }

        var potionEffectTypes = buffType.getPotionEffectTypes();
        var armorStand = geArmorStand(player);
        for (var potionEffectType : potionEffectTypes) {
            player.removePotionEffect(potionEffectType);
            armorStand.removePotionEffect(potionEffectType);
        }
    }

    public static BuffSystem getRandomBuff(Player player) {
        var buffTypes = BuffType.values();
        var randomIndex = (int) (Math.random() * buffTypes.length);
        var buffType = buffTypes[randomIndex];
        return new BuffSystem(buffType);
    }

    private static ArmorStand geArmorStand(Player player) {
        var world = player.getWorld();

        return world.getEntitiesByClass(ArmorStand.class).stream()
                .filter(entity -> entity.getMetadata("owner").get(0).asString().equals(player.getUniqueId().toString()))
                .findFirst().orElse(null);
    }
}
