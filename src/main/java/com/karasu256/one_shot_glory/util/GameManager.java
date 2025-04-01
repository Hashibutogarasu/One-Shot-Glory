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

/**
 * ゲームのメカニクスを管理するユーティリティクラス
 * <p>
 * このクラスは、ゲームのターゲットの生成やArmorStandの操作など、
 * ゲーム内のさまざまなメカニクスを管理するための静的メソッドを提供します。
 * </p>
 * 
 * @author Hashibutogarasu
 * @version 1.0
 */
public class GameManager {

    /**
     * GameManagerのデフォルトコンストラクタ
     * <p>
     * 新しいGameManagerインスタンスを初期化します。
     * </p>
     */
    private GameManager() {

    }

    /**
     * 指定されたワールド内のすべてのプレイヤーにターゲットを生成するメソッド
     * <p>
     * ワールド内のすべてのプレイヤーに対して個別にターゲットを生成します。
     * </p>
     * 
     * @param world ターゲットを生成するワールド
     */
    public static void spawnTarget(World world) {
        List<Player> players = world.getPlayers();

        for (var player : players) {
            spawnTarget(world, player);
        }
    }

    /**
     * 指定されたプレイヤーにターゲットを生成するメソッド
     * <p>
     * ランダムなバフを持つターゲットをプレイヤーに生成し、乗せます。
     * </p>
     * 
     * @param world  ターゲットを生成するワールド
     * @param player ターゲットを生成する対象のプレイヤー
     */
    public static void spawnTarget(World world, Player player) {
        BuffSystem randomBuff = BuffSystem.getRandomBuff(player);
        var ItemStack = randomBuff.getBuffType().getItemStack();

        var entity = spawnArmorStand(world, player, ItemStack);

        player.addPassenger(entity);
    }

    /**
     * ArmorStandエンティティを生成して設定するメソッド
     * <p>
     * 指定されたプレイヤーの位置に不可視のArmorStandを生成し、
     * 基本的な設定を適用して頭にアイテムを装備させます。
     * </p>
     * 
     * @param world     ArmorStandを生成するワールド
     * @param player    ArmorStandの位置の基準となるプレイヤー
     * @param itemStack ArmorStandの頭部に装備するアイテム
     * @return 生成および設定されたArmorStandエンティティ
     */
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

    /**
     * エンティティに所有者のメタデータを設定するメソッド
     * <p>
     * 指定されたエンティティに、プレイヤーを所有者として特定するメタデータを設定します。
     * これにより、後でエンティティの所有者を識別できます。
     * </p>
     * 
     * @param <T>    エンティティの型
     * @param entity メタデータを設定するエンティティ
     * @param player 所有者として設定するプレイヤー
     */
    private static <T extends Entity> void setOwnerMetadata(T entity, Player player) {
        Plugin plugin = One_Shot_Glory.getPlugin();
        String id = player.getUniqueId().toString();
        entity.setMetadata("owner", new FixedMetadataValue(plugin, id));
    }
}
