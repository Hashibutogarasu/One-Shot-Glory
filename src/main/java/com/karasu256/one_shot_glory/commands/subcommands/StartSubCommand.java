package com.karasu256.one_shot_glory.commands.subcommands;

import com.karasu256.one_shot_glory.One_Shot_Glory;
import com.karasu256.one_shot_glory.commands.SubCommand;
import com.karasu256.one_shot_glory.game.Initializer;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.List;

public class StartSubCommand implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("osg.start")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        // Enable the plugin functionality
        One_Shot_Glory.config.set("enabled", true);
        One_Shot_Glory.getPlugin(One_Shot_Glory.class).saveConfig();

        return Initializer.init(sender);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}