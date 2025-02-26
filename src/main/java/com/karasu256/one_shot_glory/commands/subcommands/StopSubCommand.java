package com.karasu256.one_shot_glory.commands.subcommands;

import com.karasu256.one_shot_glory.One_Shot_Glory;
import com.karasu256.one_shot_glory.commands.SubCommand;
import com.karasu256.one_shot_glory.game.Initializer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.ArrayList;
import java.util.List;

public class StopSubCommand implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        One_Shot_Glory plugin = One_Shot_Glory.getPlugin(One_Shot_Glory.class);
        var langManager = plugin.getLanguageManager();

        if (!sender.hasPermission("osg.stop")) {
            sender.sendMessage(langManager.getMessage("commands.no-permission", null));
            return true;
        }

        One_Shot_Glory.config.set("enabled", false);
        plugin.saveConfig();

        PlayerInteractEvent.getHandlerList().unregister(plugin);

        sender.sendMessage(langManager.getMessage("commands.stop.success", null));
        return Initializer.unRegister();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}