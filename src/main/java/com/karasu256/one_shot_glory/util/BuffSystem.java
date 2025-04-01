package com.karasu256.one_shot_glory.util;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

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
     * プレイヤーにバフ効果を適用するメソッド
     * <p>
     * このバフシステムに関連付けられたバフタイプのポーション効果を
     * プレイヤーとその関連ArmorStandに適用します。
     * </p>
     * 
     * @param player バフを適用するプレイヤー
     */
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

    /**
     * プレイヤーからバフ効果を削除するメソッド
     * <p>
     * このバフシステムに関連付けられたバフタイプのポーション効果を
     * プレイヤーとその関連ArmorStandから削除します。
     * </p>
     * 
     * @param player バフを削除するプレイヤー
     */
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
     * プレイヤーに関連付けられたArmorStandを取得する内部メソッド
     * <p>
     * プレイヤーのUUIDをメタデータとして持つArmorStandを
     * プレイヤーが所属するワールドから検索し、最初に見つかったものを返します。
     * </p>
     * 
     * @param player ArmorStandの所有者となるプレイヤー
     * @return プレイヤーに関連付けられたArmorStand、または見つからない場合はnull
     */
    private static ArmorStand geArmorStand(Player player) {
        var world = player.getWorld();

        return world.getEntitiesByClass(ArmorStand.class).stream()
                .filter(entity -> entity.getMetadata("owner").get(0).asString().equals(player.getUniqueId().toString()))
                .findFirst().orElse(null);
    }
}
