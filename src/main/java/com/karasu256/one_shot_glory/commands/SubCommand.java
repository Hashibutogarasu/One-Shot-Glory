package com.karasu256.one_shot_glory.commands;

import org.bukkit.command.CommandSender;
import java.util.List;

public interface SubCommand {
    boolean execute(CommandSender sender, String[] args);
    List<String> tabComplete(CommandSender sender, String[] args);
}