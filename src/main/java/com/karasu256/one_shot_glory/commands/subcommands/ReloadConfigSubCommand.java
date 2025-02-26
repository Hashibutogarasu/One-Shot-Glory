package com.karasu256.one_shot_glory.commands.subcommands;

import com.karasu256.one_shot_glory.One_Shot_Glory;
import com.karasu256.one_shot_glory.commands.SubCommand;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.List;

public class ReloadConfigSubCommand implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("osg.reload")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        try {
            One_Shot_Glory.config = One_Shot_Glory.getPlugin(One_Shot_Glory.class).getConfig();
            One_Shot_Glory.getPlugin(One_Shot_Glory.class).loadConfig();
            One_Shot_Glory.getPlugin(One_Shot_Glory.class).saveConfig();
            One_Shot_Glory.getPlugin(One_Shot_Glory.class).reloadConfig();
            sender.sendMessage("§aConfiguration reloaded successfully!");
        } catch (Exception e) {
            sender.sendMessage("§cFailed to reload configuration: " + e.getMessage());
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}