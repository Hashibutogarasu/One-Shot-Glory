package com.karasu256.one_shot_glory.util;

import com.karasu256.one_shot_glory.One_Shot_Glory;
import org.bukkit.World;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

public class GameManager {
    public static void spawnTarget(World world) {
        List<Player> players = world.getPlayers();

        for (var player : players) {
            spawnTarget(world, player);
        }
    }

    public static void spawnTarget(World world, Player player) {
        BuffSystem randomBuff = BuffSystem.getRandomBuff(player);
        var ItemStack = randomBuff.getBuffType().getItemStack();

        var entity = spawnArmorStand(world, player, ItemStack);

        player.addPassenger(entity);
    }

    public static ArmorStand spawnArmorStand(World world, Player player, ItemStack itemStack) {
        ArmorStand armorStand = (ArmorStand) world.spawnEntity(
                player.getLocation().add(0, 2, 0),
                EntityType.ARMOR_STAND);

        armorStand.setInvisible(true);
        armorStand.setAI(false);
        armorStand.setGravity(false);
        armorStand.setRemoveWhenFarAway(true);
        armorStand.setSmall(true);

        setOwnerMetadata(armorStand, player);

        armorStand.setItem(EquipmentSlot.HEAD, itemStack);

        return armorStand;
    }

    private static <T extends Entity> void setOwnerMetadata(T entity, Player player) {
        Plugin plugin = One_Shot_Glory.getPlugin();
        String id = player.getUniqueId().toString();
        entity.setMetadata("owner", new FixedMetadataValue(plugin, id));
    }
}
