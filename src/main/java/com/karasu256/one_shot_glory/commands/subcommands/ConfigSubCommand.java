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
        One_Shot_Glory plugin = One_Shot_Glory.getPlugin(One_Shot_Glory.class);
        var langManager = plugin.getLanguageManager();

        if (!sender.hasPermission("osg.config")) {
            sender.sendMessage(langManager.getMessage("commands.no-permission", null));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(langManager.getMessage("commands.config.usage", null));
            return true;
        }

        String property = args[1];
        Configuration config = plugin.getConfig();
        
        if (args.length == 2) {
            Object value = config.get(property);
            if (value == null) {
                sender.sendMessage(langManager.getMessage("commands.config.not-found", null)
                    .replace("{property}", property));
            } else {
                sender.sendMessage(langManager.getMessage("commands.config.current-value", null)
                    .replace("{property}", property)
                    .replace("{value}", value.toString()));
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

        plugin.saveConfig();
        sender.sendMessage(langManager.getMessage("commands.config.set-value", null)
            .replace("{property}", property)
            .replace("{value}", value));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("osg.config")) {
            return new ArrayList<>();
        }

        if (args.length == 2) {
            One_Shot_Glory plugin = One_Shot_Glory.getPlugin(One_Shot_Glory.class);
            return new ArrayList<>(plugin.getConfig().getKeys(true));
        }

        return new ArrayList<>();
    }
}