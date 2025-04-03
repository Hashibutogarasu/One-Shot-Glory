package com.karasu256.one_shot_glory.util;

import com.karasu256.one_shot_glory.One_Shot_Glory;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * アーマースタンドの生成・管理・取得を行うユーティリティクラス
 * <p>
 * このクラスは、プレイヤーに関連付けられたアーマースタンドの生成、取得、
 * および管理のための静的メソッドとキャッシュを提供します。
 * </p>
 * 
 * @author Hashibutogarasu
 * @version 1.0
 */
public class ArmorStandUtils {
    
    /** プレイヤーIDとアーマースタンドのマッピングを保持するキャッシュ */
    private static final Map<UUID, ArmorStand> playerArmorStandMap = new HashMap<>();
    
    /** 全てのプラグインによって管理されるアーマースタンドのリスト */
    private static final List<ArmorStand> managedArmorStands = new ArrayList<>();

    /**
     * ArmorStandUtilsのデフォルトコンストラクタ
     * <p>
     * このクラスはユーティリティクラスであり、インスタンス化は不要です。
     * </p>
     */
    private ArmorStandUtils() {
        // ユーティリティクラスのためインスタンス化しない
    }

    /**
     * 指定されたプレイヤーのアーマースタンドを取得するメソッド
     * <p>
     * プレイヤーに関連付けられたアーマースタンドをキャッシュから取得し、
     * キャッシュにない場合はワールド内を検索します。
     * プラグイン固有のIDメタデータを確認して、このプラグインが生成した
     * アーマースタンドのみを対象とします。
     * </p>
     * 
     * @param player アーマースタンドを取得するプレイヤー
     * @return プレイヤーに関連付けられたアーマースタンド、存在しない場合はnull
     */
    public static ArmorStand getPlayerArmorStand(Player player) {
        // キャッシュにある場合はキャッシュから返す
        UUID playerUuid = player.getUniqueId();
        if (playerArmorStandMap.containsKey(playerUuid)) {
            ArmorStand cached = playerArmorStandMap.get(playerUuid);
            // 既に削除されていないか確認
            if (cached != null && !cached.isDead()) {
                return cached;
            } else {
                // 削除されていれば、キャッシュから削除
                playerArmorStandMap.remove(playerUuid);
            }
        }
        
        // ワールド内を検索
        World world = player.getWorld();
        ArmorStand armorStand = world.getEntitiesByClass(ArmorStand.class).stream()
                .filter(entity -> entity.hasMetadata("owner") && 
                        !entity.getMetadata("owner").isEmpty() && 
                        entity.getMetadata("owner").get(0).asString().equals(playerUuid.toString()) &&
                        entity.hasMetadata("id") &&
                        !entity.getMetadata("id").isEmpty() &&
                        entity.getMetadata("id").get(0).asString().equals("armor_stand"))
                .findFirst().orElse(null);
        
        // キャッシュに追加
        if (armorStand != null) {
            playerArmorStandMap.put(playerUuid, armorStand);
        }
        
        return armorStand;
    }

    /**
     * アーマースタンドエンティティを生成して設定するメソッド
     * <p>
     * 指定されたプレイヤーの位置に不可視のアーマースタンドを生成し、
     * 基本的な設定を適用して頭にアイテムを装備させます。
     * プレイヤーに既にアーマースタンドが関連付けられている場合は、それを削除します。
     * </p>
     * 
     * @param world     アーマースタンドを生成するワールド
     * @param player    アーマースタンドの位置の基準となるプレイヤー
     * @param itemStack アーマースタンドの頭部に装備するアイテム
     * @return 生成および設定されたアーマースタンドエンティティ
     */
    public static ArmorStand spawnArmorStand(World world, Player player, ItemStack itemStack) {
        // 既存のアーマースタンドを削除
        removePlayerArmorStand(player);
        
        // プレイヤーに乗っているPassengerをチェック
        for (Entity passenger : player.getPassengers()) {
            if (passenger instanceof ArmorStand) {
                ArmorStand passengerArmorStand = (ArmorStand) passenger;
                if (passengerArmorStand.hasMetadata("owner") && 
                    !passengerArmorStand.getMetadata("owner").isEmpty() &&
                    passengerArmorStand.getMetadata("owner").get(0).asString().equals(player.getUniqueId().toString()) &&
                    passengerArmorStand.hasMetadata("id") &&
                    !passengerArmorStand.getMetadata("id").isEmpty() &&
                    passengerArmorStand.getMetadata("id").get(0).asString().equals("armor_stand")) {
                    
                    // プラグインが所有するアーマースタンドであれば削除
                    player.removePassenger(passengerArmorStand);
                    passengerArmorStand.remove();
                    
                    // キャッシュとリストからも削除
                    playerArmorStandMap.remove(player.getUniqueId());
                    managedArmorStands.remove(passengerArmorStand);
                }
            }
        }
        
        // ワールド内のすべてのアーマースタンドをチェックして、このプレイヤーに関連付けられたものを削除
        world.getEntitiesByClass(ArmorStand.class).stream()
                .filter(armorStand -> armorStand.hasMetadata("owner") && 
                        !armorStand.getMetadata("owner").isEmpty() && 
                        armorStand.getMetadata("owner").get(0).asString().equals(player.getUniqueId().toString()) &&
                        armorStand.hasMetadata("id") &&
                        !armorStand.getMetadata("id").isEmpty() &&
                        armorStand.getMetadata("id").get(0).asString().equals("armor_stand"))
                .forEach(armorStand -> {
                    armorStand.remove();
                    managedArmorStands.remove(armorStand);
                });
        
        // 新しいアーマースタンドを生成
        ArmorStand armorStand = (ArmorStand) world.spawnEntity(
                player.getLocation().add(0, 2, 0),
                EntityType.ARMOR_STAND);

        armorStand.setInvisible(true);
        armorStand.setAI(false);
        armorStand.setGravity(false);
        armorStand.setRemoveWhenFarAway(true);
        armorStand.setSmall(true);
        armorStand.setMetadata("id", new FixedMetadataValue(One_Shot_Glory.getPlugin(), "armor_stand"));
        armorStand.setMetadata("owner", new FixedMetadataValue(One_Shot_Glory.getPlugin(), player.getUniqueId().toString()));

        setOwnerMetadata(armorStand, player);
        armorStand.setItem(EquipmentSlot.HEAD, itemStack);
        
        // キャッシュとリストに追加
        playerArmorStandMap.put(player.getUniqueId(), armorStand);
        managedArmorStands.add(armorStand);
        
        return armorStand;
    }

    /**
     * 指定したプレイヤーのアーマースタンドを削除するメソッド
     * 
     * @param player アーマースタンドを削除するプレイヤー
     * @return 削除に成功した場合はtrue、アーマースタンドが存在しなかった場合はfalse
     */
    public static boolean removePlayerArmorStand(Player player) {
        ArmorStand armorStand = getPlayerArmorStand(player);
        if (armorStand != null && !armorStand.isDead()) {
            armorStand.remove();
            playerArmorStandMap.remove(player.getUniqueId());
            managedArmorStands.remove(armorStand);
            return true;
        }
        return false;
    }

    /**
     * 全てのプレイヤーのアーマースタンドを削除するメソッド
     * 
     * @return 削除されたアーマースタンドの数
     */
    public static int removeAllArmorStands() {
        int count = 0;
        
        // 管理リストから削除
        for (ArmorStand armorStand : new ArrayList<>(managedArmorStands)) {
            if (!armorStand.isDead()) {
                armorStand.remove();
                count++;
            }
        }
        
        // リストとキャッシュをクリア
        managedArmorStands.clear();
        playerArmorStandMap.clear();
        
        return count;
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

    /**
     * エンティティがこのプラグインによって作成されたアーマースタンドかどうかを判定するメソッド
     * <p>
     * エンティティがアーマースタンドであり、かつプラグイン固有のIDメタデータを持っているかを確認します。
     * </p>
     * 
     * @param entity 判定するエンティティ
     * @return このプラグインによって作成されたアーマースタンドであればtrue、そうでなければfalse
     */
    public static boolean isPluginArmorStand(Entity entity) {
        if (!(entity instanceof ArmorStand)) {
            return false;
        }
        
        return entity.hasMetadata("id") && 
               !entity.getMetadata("id").isEmpty() && 
               entity.getMetadata("id").get(0).asString().equals("armor_stand");
    }

    /**
     * エンティティがこのプラグインによって作成され、指定されたプレイヤーが所有するアーマースタンドかどうかを判定するメソッド
     * <p>
     * エンティティがアーマースタンドであり、プラグイン固有のIDメタデータを持ち、
     * かつ指定されたプレイヤーが所有者として設定されているかを確認します。
     * </p>
     * 
     * @param entity 判定するエンティティ
     * @param player 所有者と想定されるプレイヤー
     * @return このプラグインによって作成され、指定されたプレイヤーが所有するアーマースタンドであればtrue、そうでなければfalse
     */
    public static boolean isPlayerOwnedArmorStand(Entity entity, Player player) {
        if (!isPluginArmorStand(entity)) {
            return false;
        }
        
        return entity.hasMetadata("owner") && 
               !entity.getMetadata("owner").isEmpty() && 
               entity.getMetadata("owner").get(0).asString().equals(player.getUniqueId().toString());
    }
}