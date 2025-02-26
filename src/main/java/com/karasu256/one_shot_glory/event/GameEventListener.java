package com.karasu256.one_shot_glory.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;

public class GameEventListener implements Listener {
    private final Objective objective;

    public GameEventListener(Objective objective) {
        this.objective = objective;
    }

    public void unRegister() {
        PlayerDeathEvent.getHandlerList().unregister(this);
        PlayerRespawnEvent.getHandlerList().unregister(this);
    }

    @EventHandler()
    private void onPlayerDeath(PlayerDeathEvent event) {
        var player = event.getEntity();
        var killer = player.getKiller();

        if (killer != null) {
            var score = objective.getScore(killer.getName());
            score.setScore(score.getScore() + 1);

            //play a sound which is experience orb to the killer
            killer.playSound(killer.getLocation(), "entity.experience_orb.pickup", 1, 1);
        }
    }

    @EventHandler()
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        var server = event.getPlayer().getServer();
        var plugin = server.getPluginManager().getPlugin("One_Shot_Glory");

        //give a effect of resistance to the player
        var player = event.getPlayer();
        if (plugin != null) {
            int delay = plugin.getConfig().getInt("respawn_set_health_delay");

            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, (delay * 3) + 100, 255));

            setPlayerHealth(player, 20);

            server.getScheduler().runTaskLater(plugin, () -> {
                setPlayerHealth(player, 10);

                server.getScheduler().runTaskLater(plugin, () -> {
                    setPlayerHealth(player, 5);

                    server.getScheduler().runTaskLater(plugin, () -> {
                        setPlayerHealth(player, 1);
                    }, delay);
                }, delay);
            }, delay);
        }
    }

    private void setPlayerHealth(Player player, double health) {
        player.setMaxHealth(health);
        player.setHealth(player.getMaxHealth());
        player.playSound(player.getLocation(), "ui.button.click", 1, 1);
    }
}
