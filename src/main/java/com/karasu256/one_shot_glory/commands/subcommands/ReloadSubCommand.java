package com.karasu256.one_shot_glory.commands.subcommands;

import com.karasu256.one_shot_glory.commands.SubCommand;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReloadSubCommand implements SubCommand {
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public ReloadSubCommand() {
        subCommands.put("config", new ReloadConfigSubCommand());
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("osg.reload")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length > 1) {
            SubCommand subCommand = subCommands.get(args[1].toLowerCase());
            if (subCommand != null) {
                return subCommand.execute(sender, args);
            }
        }

        // Default reload behavior
        sender.sendMessage("§aReload subcommands:");
        sender.sendMessage("§a/osg reload config - Reload plugin configuration");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("osg.reload")) {
            return new ArrayList<>();
        }

        if (args.length == 2) {
            return new ArrayList<>(subCommands.keySet());
        }

        if (args.length > 2) {
            SubCommand subCommand = subCommands.get(args[1].toLowerCase());
            if (subCommand != null) {
                return subCommand.tabComplete(sender, args);
            }
        }

        return new ArrayList<>();
    }
}