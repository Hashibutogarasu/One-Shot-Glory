package com.karasu256.one_shot_glory.event;

import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.ArmorStand;
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
import com.karasu256.one_shot_glory.util.BuffSystem;
import com.karasu256.one_shot_glory.util.BuffType;
import com.karasu256.one_shot_glory.util.GameManager;

public class GameEventListener implements Listener {
    private final Objective objective;

    public GameEventListener(Objective objective) {
        this.objective = objective;
    }

    public void unRegister() {
        ProjectileHitEvent.getHandlerList().unregister(this);
        PlayerMoveEvent.getHandlerList().unregister(this);
        EntityDamageEvent.getHandlerList().unregister(this);
        PlayerDeathEvent.getHandlerList().unregister(this);
        PlayerRespawnEvent.getHandlerList().unregister(this);
    }

    @EventHandler()
    private void onArrowHit(ProjectileHitEvent event) {
        if (event.getEntityType() == EntityType.ARROW) {
            var entity = event.getEntity();

            entity.remove();
        }
    }

    @EventHandler()
    private void onEntityDamage(EntityDamageEvent event) {
        // cancel the damage to the player
        // if the damaged entity is a armor stand, the owner will kill
        if (event.getEntity() instanceof ArmorStand) {
            var armorStand = (ArmorStand) event.getEntity();
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

    @EventHandler()
    private void onPlayerMove(PlayerMoveEvent event) {
        var player = event.getPlayer();
        var location = player.getLocation();

        // get item display from the player
        var uuid = player.getUniqueId();
        ArmorStand armorStand = player.getWorld().getEntitiesByClass(ArmorStand.class).stream()
                .filter(entity -> entity.getMetadata("owner").get(0).asString().equals(uuid.toString()))
                .findFirst().orElse(null);

        location.setY(location.getY() + 2);

        if (armorStand != null) {
            armorStand.teleport(location);
        }
    }

    @EventHandler()
    private void onPlayerDeath(PlayerDeathEvent event) {

    }

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
