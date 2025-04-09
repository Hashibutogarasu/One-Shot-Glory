package com.karasu256.one_shot_glory.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.karasu256.one_shot_glory.One_Shot_Glory;

/**
 * プレイヤーへのバフ効果の適用と管理を行うクラス
 * <p>
 * このクラスは、様々なバフタイプに基づいてプレイヤーとArmorStandに
 * ポーション効果を適用したり削除したりする機能を提供します。
 * また、ランダムなバフの生成機能も含まれています。
 * </p>
 * 
 * @author Hashibutogarasu
 * @version 1.0
 * @see BuffType
 */
public class BuffSystem {
    /** このバフシステムが管理するバフタイプ */
    private final BuffType buffType;
    /** メタデータのキー */
    public static final String BUFF_METADATA_KEY = "active_buffs";

    /**
     * 特定のバフタイプを持つBuffSystemインスタンスを作成するコンストラクタ
     * 
     * @param buffType このインスタンスが管理するバフタイプ
     */
    public BuffSystem(BuffType buffType) {
        this.buffType = buffType;
    }

    /**
     * このバフシステムが管理するバフタイプを取得するメソッド
     * 
     * @return 関連付けられたBuffType
     */
    public BuffType getBuffType() {
        return buffType;
    }

    /**
     * プレイヤーが何らかのバフを持っているかチェックするメソッド
     * 
     * @param player チェックするプレイヤー
     * @return プレイヤーが何らかのバフを持っている場合はtrue
     */
    public static boolean hasAnyBuff(Player player) {
        return !getActiveBuffs(player).isEmpty();
    }

    /**
     * プレイヤーにバフ効果を適用するメソッド
     * <p>
     * このバフシステムに関連付けられたバフタイプのポーション効果を
     * プレイヤーとその関連ArmorStandに適用し、メタデータに保存します。
     * プレイヤーが既に他のバフを持っている場合は適用しません。
     * </p>
     * 
     * @param player バフを適用するプレイヤー
     * @return バフを適用できた場合はtrue、既に他のバフがあった場合はfalse
     */
    public boolean applyBuff(Player player) {
        if (buffType == null) {
            return false;
        }

        // 既に他のバフが適用されているか確認
        List<BuffType> activeBuffs = getActiveBuffs(player);
        if (!activeBuffs.isEmpty() && !activeBuffs.contains(buffType) && hasAnyBuff(player)) {
            // 既に別のバフを持っている場合は新しいバフを適用しない
            return false;
        }

        // 現在のバフがなければ追加
        if (!activeBuffs.contains(buffType)) {
            activeBuffs.add(buffType);
            player.setMetadata(BUFF_METADATA_KEY, new FixedMetadataValue(One_Shot_Glory.getPlugin(), activeBuffs));
        }

        var potionEffectTypes = buffType.getPotionEffectTypes();

        for (var potionEffectType : potionEffectTypes) {
            player.addPotionEffect(potionEffectType.createEffect(600, 1));
        }

        return true;
    }

    /**
     * プレイヤーから指定したバフを取り除きます
     * 
     * @param player   バフを取り除くプレイヤー
     * @param buffType 取り除くバフの種類
     */
    public static void removeBuff(Player player, BuffType buffType) {
        new BuffSystem(buffType).removeBuff(player);
    }

    /**
     * プレイヤーからすべてのバフを取り除きます
     * 
     * @param player バフを取り除くプレイヤー
     */
    public static void removeAllBuffs(Player player) {
        List<BuffType> activeBuffs = getAllBuffs(player);
        for (BuffType buff : activeBuffs) {
            removeBuff(player, buff);
        }
    }

    /**
     * プレイヤーからバフ効果を削除するメソッド
     * <p>
     * このバフシステムに関連付けられたバフタイプのポーション効果を
     * プレイヤーとその関連ArmorStandから削除し、メタデータも更新します。
     * </p>
     * 
     * @param player バフを削除するプレイヤー
     */
    public void removeBuff(Player player) {
        if (buffType == null) {
            return;
        }

        // バフリストから削除
        List<BuffType> activeBuffs = getActiveBuffs(player);
        if (activeBuffs.remove(buffType)) {
            // バフが実際に削除された場合のみメタデータを更新
            if (activeBuffs.isEmpty()) {
                // リストが空になった場合はメタデータを完全に削除
                player.removeMetadata(BUFF_METADATA_KEY, One_Shot_Glory.getPlugin());
            } else {
                // まだバフが残っている場合は更新したリストを保存
                player.setMetadata(BUFF_METADATA_KEY, new FixedMetadataValue(One_Shot_Glory.getPlugin(), activeBuffs));
            }
        }

        var potionEffectTypes = buffType.getPotionEffectTypes();

        for (var potionEffectType : potionEffectTypes) {
            player.removePotionEffect(potionEffectType);
        }
    }

    /**
     * プレイヤーの現在アクティブなバフリストを取得します
     * 
     * @param player 対象のプレイヤー
     * @return アクティブなバフのリスト
     */
    @SuppressWarnings("unchecked")
    public static List<BuffType> getActiveBuffs(Player player) {
        if (player.hasMetadata(BUFF_METADATA_KEY)) {
            List<MetadataValue> values = player.getMetadata(BUFF_METADATA_KEY);
            if (!values.isEmpty()) {
                return (List<BuffType>) values.get(0).value();
            }
        }
        return new ArrayList<>();
    }

    /**
     * プレイヤーにランダムなバフを提供するための静的メソッド
     * <p>
     * 利用可能なすべてのバフタイプからランダムに一つを選択し、
     * それを使用して新しいBuffSystemインスタンスを作成します。
     * </p>
     * 
     * @param player 関連付けるプレイヤー（メタデータ関連のため）
     * @return ランダムに生成されたBuffSystemインスタンス
     */
    public static BuffSystem getRandomBuff(Player player) {
        var buffTypes = BuffType.values();
        var randomIndex = (int) (Math.random() * buffTypes.length);
        var buffType = buffTypes[randomIndex];
        return new BuffSystem(buffType);
    }

    /**
     * プレイヤーが指定されたバフを持っているかを確認します
     * 
     * @param player   確認するプレイヤー
     * @param buffType 確認するバフの種類
     * @return プレイヤーが指定されたバフを持っている場合はtrue
     */
    public static boolean hasBuff(Player player, BuffType buffType) {
        return getActiveBuffs(player).contains(buffType);
    }

    /**
     * プレイヤーが現在持っているすべてのバフを取得します
     * 
     * @param player 確認するプレイヤー
     * @return プレイヤーが持っているバフのリスト
     */
    public static List<BuffType> getAllBuffs(Player player) {
        return new ArrayList<>(getActiveBuffs(player));
    }
}
