package com.karasu256.one_shot_glory.commands.subcommands;

import com.karasu256.one_shot_glory.One_Shot_Glory;
import com.karasu256.one_shot_glory.commands.SubCommand;
import org.bukkit.configuration.Configuration;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ConfigSubCommand implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("osg.config")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /osg config <property> [value]");
            return true;
        }

        String property = args[1];
        Configuration config = One_Shot_Glory.getPlugin(One_Shot_Glory.class).getConfig();
        
        if (args.length == 2) {
            // Get config value
            Object value = config.get(property);
            if (value == null) {
                sender.sendMessage("§cProperty '" + property + "' not found!");
            } else {
                sender.sendMessage("§a" + property + " = " + value);
            }
            return true;
        }

        String value = args[2];

        try {
            int intValue = Integer.parseInt(value);
            config.set(property, intValue);
        } catch (NumberFormatException e) {
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                boolean boolValue = Boolean.parseBoolean(value);
                config.set(property, boolValue);
            } else {
                config.set(property, value);
            }
        }

        One_Shot_Glory.getPlugin(One_Shot_Glory.class).saveConfig();
        sender.sendMessage("§aSuccessfully set " + property + " to " + value);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("osg.config")) {
            return new ArrayList<>();
        }

        if (args.length == 2) {
            // Return all config keys for property argument
            return new ArrayList<>(One_Shot_Glory.getPlugin(One_Shot_Glory.class).getConfig().getKeys(true));
        }

        return new ArrayList<>();
    }
}