package com.karasu256.one_shot_glory.commands.subcommands;

import com.karasu256.one_shot_glory.commands.SubCommand;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.List;

public class HelpSubCommand implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("§aAvailable commands:");
        sender.sendMessage("§a/osg help - Show this help message");
        sender.sendMessage("§a/osg reload - Show reload commands");
        sender.sendMessage("§a/osg reload config - Reload plugin configuration");
        sender.sendMessage("§a/osg config <property> [value] - Get or set configuration values");
        sender.sendMessage("§a/osg start - Enable One Shot Glory functionality");
        sender.sendMessage("§a/osg stop - Disable One Shot Glory functionality");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}