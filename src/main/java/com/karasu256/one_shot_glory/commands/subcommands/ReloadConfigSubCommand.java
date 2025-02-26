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
            sender.sendMessage(One_Shot_Glory.getPlugin(One_Shot_Glory.class).getLanguageManager().getMessage("commands.no-permission", null));
            return true;
        }

        try {
            One_Shot_Glory plugin = One_Shot_Glory.getPlugin(One_Shot_Glory.class);
            One_Shot_Glory.config = plugin.getConfig();
            plugin.loadConfig();
            plugin.saveConfig();
            plugin.reloadConfig();
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.reload.config.success", null));
        } catch (Exception e) {
            One_Shot_Glory plugin = One_Shot_Glory.getPlugin(One_Shot_Glory.class);
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.reload.config.error", null)
                .replace("{error}", e.getMessage()));
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}