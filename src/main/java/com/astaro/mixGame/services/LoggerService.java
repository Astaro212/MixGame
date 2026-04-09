package com.astaro.mixGame.services;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class LoggerService {

    private final MiniMessage mm = MiniMessage.miniMessage();
    private final ConsoleCommandSender console = Bukkit.getConsoleSender();
    private String prefix = "<gray>[<gradient:#FF5555:#FFAA00>MixGame</gradient>] </gray>";

    public void setPrefix(String newPrefix) {
        this.prefix = newPrefix;
    }

    public void info(String message) {
        console.sendMessage(mm.deserialize(prefix + "<white>" + message + "</white>"));
    }

    public void warn(String message) {
        console.sendMessage(mm.deserialize(prefix + "<yellow>⚠ " + message + "</yellow>"));
    }

    public void error(String message) {
        console.sendMessage(mm.deserialize(prefix + "<red>✖ " + message + "</red>"));
    }

    public void debug(String message, boolean isDebugEnabled) {
        if (isDebugEnabled) {
            console.sendMessage(mm.deserialize(prefix + "<aqua>[DEBUG]</aqua> <gray>" + message + "</gray>"));
        }
    }
}
