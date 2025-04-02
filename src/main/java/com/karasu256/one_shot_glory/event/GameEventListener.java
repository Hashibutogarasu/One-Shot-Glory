package com.karasu256.one_shot_glory.event;

import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;

import com.karasu256.one_shot_glory.One_Shot_Glory;
import com.karasu256.one_shot_glory.util.ArmorStandUtils;
import com.karasu256.one_shot_glory.util.BuffSystem;
import com.karasu256.one_shot_glory.util.BuffType;
import com.karasu256.one_shot_glory.util.GameManager;
import com.karasu256.one_shot_glory.util.OSGPlayerUtils;

/**
 * One-Shot-Gloryゲームでのイベントを処理するリスナークラス
 * <p>
 * このクラスは、ゲームのメカニクスに関連するサーバーイベントをリッスンし、
 * 適切な処理を行います。矢の命中、エンティティのダメージ、プレイヤーの移動、
 * プレイヤーの死亡、リスポーンなどのイベントを処理します。
 * </p>
 * 
 * @author Hashibutogarasu
 * @version 1.0
 */
public class GameEventListener implements Listener {
    /** スコア管理のための目標オブジェクト */
    private final Objective objective;

    /**
     * GameEventListenerのコンストラクタ
     * <p>
     * スコア管理のための目標オブジェクトを設定します。
     * </p>
     * 
     * @param objective スコアを記録するための目標オブジェクト
     */
    public GameEventListener(Objective objective) {
        this.objective = objective;
    }

    /**
     * このリスナーの登録を解除するメソッド
     * <p>
     * 関連するすべてのイベントハンドラの登録を解除します。
     * このメソッドは、ゲームが停止したときや、プラグインが無効化されるときに呼び出されます。
     * </p>
     */
    public void unRegister() {
        ProjectileHitEvent.getHandlerList().unregister(this);
        PlayerMoveEvent.getHandlerList().unregister(this);
        EntityDamageEvent.getHandlerList().unregister(this);
        PlayerDeathEvent.getHandlerList().unregister(this);
        PlayerRespawnEvent.getHandlerList().unregister(this);
    }

    /**
     * 矢が何かに当たったときのイベントハンドラ
     * <p>
     * 矢がヒットしたときに、矢のエンティティを削除します。
     * </p>
     * 
     * @param event 矢のヒットイベント
     */
    @EventHandler()
    private void onArrowHit(ProjectileHitEvent event) {
        if (event.getEntityType() == EntityType.ARROW) {
            var entity = event.getEntity();
            
            // 矢の発射者が有効なプレイヤーの場合のみ矢を消去
            if (entity.getShooter() instanceof Player shooter && 
                OSGPlayerUtils.getEnabledList().contains(shooter)) {
                entity.remove();
            }
        }
    }

    /**
     * エンティティがダメージを受けたときのイベントハンドラ
     * <p>
     * ArmorStandがダメージを受けた場合、その所有者プレイヤーにダメージを与え、
     * 攻撃したプレイヤーにスコアを加算します。また、ArmorStandの頭部アイテムに基づいて
     * 攻撃者にバフ効果を付与します。
     * </p>
     * 
     * @param event エンティティダメージイベント
     */
    @EventHandler()
    private void onEntityDamage(EntityDamageEvent event) {
        // cancel the damage to the player
        // if the damaged entity is a armor stand, the owner will kill
        if (event.getEntity() instanceof ArmorStand) {
            var armorStand = (ArmorStand) event.getEntity();
            
            // メタデータが存在し、空でないことを確認
            if (!armorStand.hasMetadata("owner") || armorStand.getMetadata("owner").isEmpty()) {
                return;
            }
            
            var player = armorStand.getWorld().getEntitiesByClass(Player.class).stream()
                    .filter(entity -> entity.getUniqueId().toString()
                            .equals(armorStand.getMetadata("owner").get(0).asString()))
                    .findFirst().orElse(null);

            //if the owner and the killer are the same, do nothing
            if (player == event.getDamageSource().getCausingEntity()) {
                return;
            }

            if (player != null) {
                player.damage(1000, DamageSource.builder(DamageType.OUT_OF_WORLD).build());
            }

            armorStand.remove();

            // get damage source player
            var entity = event.getDamageSource().getCausingEntity();

            if (entity instanceof Player) {
                objective.getScore(entity.getName()).setScore(objective.getScore(entity.getName()).getScore() + 1);
            }

            // get armor stand head item
            if (armorStand != null) {
                var itemStack = armorStand.getEquipment().getHelmet();

                BuffType buffType = BuffType.getBuffTypeByItemStack(itemStack);

                // apply the buff to the killer
                if (entity instanceof Player killer) {
                    BuffSystem buffSystem = new BuffSystem(buffType);
                    buffSystem.applyBuff(killer);
                }
            }
        }
    }

    /**
     * プレイヤーが移動したときのイベントハンドラ
     * <p>
     * プレイヤーが移動したとき、そのプレイヤーに関連付けられたArmorStandの位置を更新します。
     * ArmorStandはプレイヤーの頭上2ブロックの位置に配置されます。
     * </p>
     * 
     * @param event プレイヤー移動イベント
     */
    @EventHandler()
    private void onPlayerMove(PlayerMoveEvent event) {
        var player = event.getPlayer();
        var location = player.getLocation();

        // ArmorStandUtilsを使用してプレイヤーのアーマースタンドを取得
        ArmorStand armorStand = ArmorStandUtils.getPlayerArmorStand(player);

        location.setY(location.getY() + 2);

        if (armorStand != null) {
            armorStand.teleport(location);
        }
    }

    /**
     * プレイヤーが死亡したときのイベントハンドラ
     * <p>
     * プレイヤーが死亡したとき、そのプレイヤーに関連付けられたArmorStandを削除します。
     * </p>
     * 
     * @param event プレイヤー死亡イベント
     */
    @EventHandler()
    private void onPlayerDeath(PlayerDeathEvent event) {
        // プレイヤーが死亡したときに、そのプレイヤーのアーマースタンドを削除する
        Player player = event.getEntity();
        
        // ArmorStandUtilsを使用してプレイヤーのアーマースタンドを削除
        ArmorStandUtils.removePlayerArmorStand(player);
    }

    /**
     * プレイヤーがリスポーンしたときのイベントハンドラ
     * <p>
     * プレイヤーがリスポーンしたとき、新しいターゲットを生成し、
     * 一時的な耐性効果をプレイヤーに付与します。耐性の持続時間は
     * 設定ファイルのrespawn_set_health_delayの値に基づいて決定されます。
     * </p>
     * 
     * @param event プレイヤーリスポーンイベント
     */
    @EventHandler()
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        var plugin = One_Shot_Glory.getPlugin();

        // spawn new item display
        if (plugin != null) {
            var player = event.getPlayer();
            GameManager.spawnTarget(player.getWorld(), player);
        }

        // give a effect of resistance to the player
        var player = event.getPlayer();
        if (plugin != null) {
            int delay = plugin.getConfig().getInt("respawn_set_health_delay");

            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, (delay * 3) + 100, 255));
        }
    }
}
