package com.karasu256.one_shot_glory.util;

import com.karasu256.one_shot_glory.One_Shot_Glory;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * アイテムフレームの生成・管理・取得を行うユーティリティクラス
 * <p>
 * このクラスは、プレイヤーに関連付けられたアイテムフレームの生成、取得、
 * および管理のための静的メソッドとキャッシュを提供します。
 * </p>
 * 
 * @author Hashibutogarasu
 * @version 1.0
 */
public class ItemFrameUtils {

    /** プレイヤーIDとアイテムフレームのマッピングを保持するキャッシュ */
    private static final Map<UUID, ItemFrame> playerItemFrameMap = new HashMap<>();

    /** 全てのプラグインによって管理されるアイテムフレームのリスト */
    private static final List<ItemFrame> managedItemFrames = new ArrayList<>();

    /**
     * ItemFrameUtilsのデフォルトコンストラクタ
     * <p>
     * このクラスはユーティリティクラスであり、インスタンス化は不要です。
     * </p>
     */
    private ItemFrameUtils() {
        // ユーティリティクラスのためインスタンス化しない
    }

    /**
     * アイテムフレームエンティティを生成して設定するメソッド
     * <p>
     * 指定されたプレイヤーの位置に不可視のアイテムフレームを生成し、
     * 基本的な設定を適用してアイテムを装備させます。
     * プレイヤーに既にアイテムフレームが関連付けられている場合は、それを削除します。
     * </p>
     * 
     * @param world     アイテムフレームを生成するワールド
     * @param player    アイテムフレームの位置の基準となるプレイヤー
     * @param itemStack アイテムフレームに表示するアイテム
     * @return 生成および設定されたアイテムフレームエンティティ
     */
    public static ItemFrame spawnItemFrame(World world, Player player, ItemStack itemStack) {
        // 既存のアイテムフレームを削除
        removePlayerItemFrame(player);

        // 新しいアイテムフレームを生成
        ItemFrame itemFrame = (ItemFrame) world.spawnEntity(
                player.getLocation().add(0, 2, 0),
                EntityType.ITEM_FRAME);

        // アイテムフレームの基本設定
        itemFrame.setVisible(false); // フレームを透明に
        itemFrame.setFixed(true); // 破壊できないように
        itemFrame.setPersistent(true); // 自然消滅しないように
        itemFrame.setInvulnerable(true); // ダメージを受けないように
        itemFrame.setRotation(itemFrame.getRotation().rotateClockwise()); // 回転をリセット

        // メタデータの設定
        itemFrame.setMetadata("id", new FixedMetadataValue(One_Shot_Glory.getPlugin(), "item_frame"));
        itemFrame.setMetadata("owner",
                new FixedMetadataValue(One_Shot_Glory.getPlugin(), player.getUniqueId().toString()));

        // アイテムの設定
        itemFrame.setItem(itemStack);

        // キャッシュとリストに追加
        playerItemFrameMap.put(player.getUniqueId(), itemFrame);
        managedItemFrames.add(itemFrame);

        return itemFrame;
    }

    /**
     * 指定されたプレイヤーのアイテムフレームを取得するメソッド
     * <p>
     * プレイヤーに関連付けられたアイテムフレームをキャッシュから取得し、
     * キャッシュにない場合はワールド内を検索します。
     * </p>
     * 
     * @param player アイテムフレームを取得するプレイヤー
     * @return プレイヤーに関連付けられたアイテムフレーム、存在しない場合はnull
     */
    public static ItemFrame getPlayerItemFrame(Player player) {
        UUID playerUuid = player.getUniqueId();
        if (playerItemFrameMap.containsKey(playerUuid)) {
            ItemFrame cached = playerItemFrameMap.get(playerUuid);
            if (cached != null && !cached.isDead()) {
                return cached;
            } else {
                playerItemFrameMap.remove(playerUuid);
            }
        }

        World world = player.getWorld();
        ItemFrame itemFrame = world.getEntitiesByClass(ItemFrame.class).stream()
                .filter(entity -> entity.hasMetadata("owner") &&
                        !entity.getMetadata("owner").isEmpty() &&
                        entity.getMetadata("owner").get(0).asString().equals(playerUuid.toString()) &&
                        entity.hasMetadata("id") &&
                        !entity.getMetadata("id").isEmpty() &&
                        entity.getMetadata("id").get(0).asString().equals("item_frame"))
                .findFirst().orElse(null);

        if (itemFrame != null) {
            playerItemFrameMap.put(playerUuid, itemFrame);
        }

        return itemFrame;
    }

    /**
     * 指定したプレイヤーのアイテムフレームを削除するメソッド
     * 
     * @param player アイテムフレームを削除するプレイヤー
     * @return 削除に成功した場合はtrue、アイテムフレームが存在しなかった場合はfalse
     */
    public static boolean removePlayerItemFrame(Player player) {
        ItemFrame itemFrame = getPlayerItemFrame(player);
        if (itemFrame != null && !itemFrame.isDead()) {
            itemFrame.setItem(ItemStack.of(Material.AIR));
            itemFrame.remove();
            playerItemFrameMap.remove(player.getUniqueId());
            managedItemFrames.remove(itemFrame);
            return true;
        }
        return false;
    }

    /**
     * 全てのプレイヤーのアイテムフレームを削除するメソッド
     * 
     * @return 削除されたアイテムフレームの数
     */
    public static int removeAllItemFrames() {
        int count = 0;

        for (ItemFrame itemFrame : new ArrayList<>(managedItemFrames)) {
            if (!itemFrame.isDead()) {
                itemFrame.remove();
                count++;
            }
        }

        managedItemFrames.clear();
        playerItemFrameMap.clear();

        return count;
    }

    /**
     * エンティティがこのプラグインによって作成されたアイテムフレームかどうかを判定するメソッド
     * 
     * @param entity 判定するエンティティ
     * @return このプラグインによって作成されたアイテムフレームであればtrue、そうでなければfalse
     */
    public static boolean isPluginItemFrame(Entity entity) {
        if (!(entity instanceof ItemFrame)) {
            return false;
        }

        if (managedItemFrames.contains(entity)) {
            return true;
        }

        if (entity.hasMetadata("id") &&
                !entity.getMetadata("id").isEmpty() &&
                entity.getMetadata("id").get(0).asString().equals("item_frame")) {

            if (!managedItemFrames.contains(entity)) {
                managedItemFrames.add((ItemFrame) entity);
            }
            return true;
        }

        return false;
    }

    /**
     * エンティティがこのプラグインによって作成され、指定されたプレイヤーが所有するアイテムフレームかどうかを判定するメソッド
     * 
     * @param entity 判定するエンティティ
     * @param player 所有者と想定されるプレイヤー
     * @return このプラグインによって作成され、指定されたプレイヤーが所有するアイテムフレームであればtrue、そうでなければfalse
     */
    public static boolean isPlayerOwnedItemFrame(Entity entity, Player player) {
        if (!isPluginItemFrame(entity)) {
            return false;
        }

        return entity.hasMetadata("owner") &&
                !entity.getMetadata("owner").isEmpty() &&
                entity.getMetadata("owner").get(0).asString().equals(player.getUniqueId().toString());
    }
}