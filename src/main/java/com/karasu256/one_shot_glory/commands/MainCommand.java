package com.karasu256.one_shot_glory.commands;

import com.karasu256.one_shot_glory.commands.subcommands.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public class MainCommand {
    private final JavaPlugin plugin;
    private final CommandExecutor baseCommand;

    public MainCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.baseCommand = new BaseCommand();
        registerCommands();
    }

    private void registerCommands() {
        if (baseCommand instanceof BaseCommand base) {
            base.registerSubCommand("help", new HelpSubCommand());
            base.registerSubCommand("reload", new ReloadSubCommand());
            base.registerSubCommand("config", new ConfigSubCommand());
            base.registerSubCommand("start", new StartSubCommand());
            base.registerSubCommand("stop", new StopSubCommand());
        }

        String commandName = "osg";
        if (plugin.getCommand(commandName) != null) {
            plugin.getCommand(commandName).setExecutor(baseCommand);
            if (baseCommand instanceof TabCompleter tabCompleter) {
                plugin.getCommand(commandName).setTabCompleter(tabCompleter);
            }
        } else {
            plugin.getLogger().warning("Command '" + commandName + "' not found in plugin.yml!");
        }
    }
}