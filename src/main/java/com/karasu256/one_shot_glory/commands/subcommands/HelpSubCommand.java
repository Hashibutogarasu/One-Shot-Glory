package com.karasu256.one_shot_glory.commands.subcommands;

import com.karasu256.one_shot_glory.One_Shot_Glory;
import com.karasu256.one_shot_glory.commands.SubCommand;
import com.karasu256.one_shot_glory.util.LanguageManager;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.List;

public class HelpSubCommand implements SubCommand {
    private final LanguageManager langManager;

    public HelpSubCommand() {
        this.langManager = One_Shot_Glory.getPlugin(One_Shot_Glory.class).getLanguageManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage(langManager.getMessage("commands.help.title", null));
        sender.sendMessage(langManager.getMessage("commands.help.help", null));
        sender.sendMessage(langManager.getMessage("commands.help.reload", null));
        sender.sendMessage(langManager.getMessage("commands.help.reload-config", null));
        sender.sendMessage(langManager.getMessage("commands.help.config", null));
        sender.sendMessage(langManager.getMessage("commands.help.start", null));
        sender.sendMessage(langManager.getMessage("commands.help.stop", null));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}