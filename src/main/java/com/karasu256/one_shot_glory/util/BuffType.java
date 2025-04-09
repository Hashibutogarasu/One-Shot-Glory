package com.karasu256.one_shot_glory.util;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

/**
 * プラグイン内で使用される様々なバフタイプを定義する列挙型
 * <p>
 * 各バフタイプには、対応するマテリアルタイプとポーション効果のリストが関連付けられています。
 * これらのバフは、ゲームプレイ中にプレイヤーに適用される効果を表します。
 * </p>
 * 
 * @author Hashibutogarasu
 * @version 1.0
 */
public enum BuffType {
    /** 移動速度を上昇させるバフ */
    SPEED(Material.FEATHER, List.of(PotionEffectType.SPEED), "移動速度上昇"),
    /** 攻撃力を強化するバフ */
    STRENGTH(Material.IRON_SWORD, List.of(PotionEffectType.STRENGTH), "攻撃力増加"),
    /** 体力を回復するバフ */
    REGENERATION(Material.GOLDEN_APPLE, List.of(PotionEffectType.REGENERATION), "再生"),
    /** ダメージ耐性を付与するバフ */
    RESISTANCE(Material.SHIELD, List.of(PotionEffectType.RESISTANCE), "耐性"),
    /** ジャンプ力を強化するバフ */
    JUMP(Material.SLIME_BALL, List.of(PotionEffectType.JUMP_BOOST), "跳躍力上昇"),
    /** 火炎耐性を付与するバフ */
    FIRE(Material.BLAZE_POWDER, List.of(PotionEffectType.FIRE_RESISTANCE), "火炎耐性"),
    /** 弱体化を付与するデバフ */
    WEAKNESS(Material.STRAY_SPAWN_EGG, List.of(PotionEffectType.WEAKNESS), "弱体化");

    /** このバフタイプに関連付けられたマテリアルタイプ */
    private Material itemType;
    /** このバフタイプで付与されるポーション効果のリスト */
    private List<PotionEffectType> potionEffectTypes;

    /** バフの名前 */
    private final String name;

    /**
     * このバフタイプに関連付けられたアイテムスタックを取得するメソッド
     * 
     * @return このバフタイプのマテリアルから生成されたアイテムスタック
     */
    public ItemStack getItemStack() {
        return new ItemStack(itemType);
    }

    /**
     * このバフタイプに関連付けられたポーション効果タイプのリストを取得するメソッド
     * 
     * @return ポーション効果タイプのリスト
     */
    public List<PotionEffectType> getPotionEffectTypes() {
        return potionEffectTypes;
    }

    /**
     * バフの名前を取得するメソッド
     * 
     * @return バフの名前
     */
    public String getName(){
        return this.name;
    }

    /**
     * BuffTypeの列挙型コンストラクタ
     * 
     * @param itemType このバフタイプに関連付けるマテリアルタイプ
     * @param potionEffectTypes このバフタイプで付与するポーション効果のリスト
     */
    BuffType(Material itemType, List<PotionEffectType> potionEffectTypes, String name) {
            this.itemType = itemType;
            this.potionEffectTypes = potionEffectTypes;
            this.name = name;
    }

    /**
     * アイテムスタックからそれに対応するバフタイプを取得する静的メソッド
     * <p>
     * 指定されたアイテムスタックのマテリアルタイプに基づいて、対応するBuffTypeを返します。
     * 一致するバフタイプがない場合はnullを返します。
     * </p>
     * 
     * @param itemStack 調査するアイテムスタック
     * @return 対応するBuffType、または一致するものがない場合はnull
     */
    public static BuffType getBuffTypeByItemStack(ItemStack itemStack) {
        for (var buffType : values()) {
            if (buffType.itemType == itemStack.getType()) {
                return buffType;
            }
        }
        return null;
    }
}
