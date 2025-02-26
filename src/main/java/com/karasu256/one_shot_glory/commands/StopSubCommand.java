package com.karasu256.one_shot_glory.commands;

import com.karasu256.one_shot_glory.One_Shot_Glory;
import com.karasu256.one_shot_glory.game.Initializer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public class StopSubCommand implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("osg.stop")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        var plugin = One_Shot_Glory.getPlugin(One_Shot_Glory.class);

        // Disable the plugin functionality
        One_Shot_Glory.config.set("enabled", false);
        plugin.saveConfig();

        PlayerInteractEvent.getHandlerList().unregister(plugin);

        sender.sendMessage("§aOne Shot Glory has been disabled!");
        return Initializer.unRegister();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}