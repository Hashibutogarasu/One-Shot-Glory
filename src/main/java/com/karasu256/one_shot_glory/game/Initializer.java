package com.karasu256.one_shot_glory.game;

import com.karasu256.one_shot_glory.One_Shot_Glory;
import com.karasu256.one_shot_glory.event.GameEventListener;
import com.karasu256.one_shot_glory.util.GameManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

public class Initializer {
    private static GameEventListener gameEventListener;

    @SuppressWarnings("deprecation")
    public static boolean init(CommandSender sender) {
        sender.sendMessage("§aInitializing One Shot Glory...");

        // unregister the previous event listener
        var pluginManager = sender.getServer().getPluginManager();
        var plugin = One_Shot_Glory.getPlugin();

        if (plugin != null) {
            PlayerInteractEvent.getHandlerList().unregister(plugin);
        }

        var server = sender.getServer();

        // create a team which named "Members" and add all players to it
        var scoreboard = server.getScoreboardManager().getMainScoreboard();

        var team = scoreboard.getTeam("Members");

        if (team == null) {
            team = scoreboard.registerNewTeam("Members");
        }

        team.setColor(ChatColor.GREEN);

        Team finalTeam = team;
        var players = server.getOnlinePlayers();
        players.forEach(player -> finalTeam.addEntry(player.getName()));

        var playersInTeam = team.getEntries();

        // create or get a score board objective of "Score"
        var objective = scoreboard.getObjective("Score");

        if (objective == null) {
            objective = scoreboard.registerNewObjective("Score", "dummy", "Score");
        }

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName("§aScore");

        // show the score to tab list
        objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);

        // reset the score of all players
        Objective finalObjective = objective;
        playersInTeam.forEach(playerName -> {
            var player = server.getPlayer(playerName);
            if (player != null) {
                finalObjective.getScore(playerName).setScore(0);
            }
        });

        GameManager.spawnTarget(server.getWorld("world"));

        // register a event listener when the player killed by another player
        if (plugin != null) {
            gameEventListener = new GameEventListener(objective);
            pluginManager.registerEvents(gameEventListener, plugin);
        }

        sender.sendMessage("§aOne Shot Glory has been initialized!");

        return true;
    }

    public static boolean stop() {
        if (gameEventListener != null) {
            gameEventListener.unRegister();

            return true;
        }

        return false;
    }

    public static boolean unRegister() {
        if (gameEventListener != null) {
            gameEventListener.unRegister();

            return true;
        }

        return false;
    }
}
