package com.astaro.mixGame.services;

import com.astaro.mixGame.MixGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatService {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private YamlConfiguration lang;

    public void saveDefaultLangs() {
        List<String> defaultLangs = List.of("ru.yml", "en.yml");

        for (String langName : defaultLangs) {
            String path = "Language/" + langName;
            File file = new File(MixGame.instance.getDataFolder(), path);

            if (!file.exists()) {
                MixGame.instance.saveResource(path, false);
            }
        }
    }


    public void load(String langCode) {
        String fileName = "Language/" + langCode.toLowerCase() + ".yml";
        File langFile = new File(MixGame.instance.getDataFolder(), fileName);

        if (!langFile.exists()) {
            MixGame.loggerService.error("Localization file not found: " + fileName);
            langFile = new File(MixGame.instance.getDataFolder(), "en.yml");
        }

        this.lang = YamlConfiguration.loadConfiguration(langFile);
    }


    public Object getRaw(String path) {
        return lang.get(path, "Missing message: " + path);
    }

    public void sendMessage(Player player, String path, String... replacements) {
        Object rawValue = getRaw(path);

        if (rawValue instanceof List<?>) {
            for (Object line : (List<?>) rawValue) {
                sendProcessedMessage(player, line.toString(), replacements);
            }
        } else {
            sendProcessedMessage(player, rawValue.toString(), replacements);
        }
    }

    private void sendProcessedMessage(Player player, String text, String... replacements) {
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                text = text.replace(replacements[i], replacements[i + 1]);
            }
        }
        player.sendMessage(parse(text, player));
    }

    public void broadcast(String path, String... replacements) {
        Object rawValue = getRaw(path);
        String text = (rawValue instanceof List) ? String.join("\n", (List<String>) rawValue) : rawValue.toString();
        String processedText = replacePlaceholders(text, replacements);
        Bukkit.getOnlinePlayers().forEach(player -> {
            sendProcessedMessage(player, processedText);
            sendActionBar(player, processedText);
        });
    }

    private String replacePlaceholders(String text, String... replacements) {
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                text = text.replace(replacements[i], replacements[i + 1]);
            }
        }
        return text;
    }

    public void sendActionBar(Player player, String path, String... replacements) {
        String text = getRaw(path).toString();
        String processedText = replacePlaceholders(text, replacements);
        player.sendActionBar(parse(processedText, player));
    }


    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static Component parse(String text, Player player) {
        if (text == null || text.isEmpty()) return Component.empty();

        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "<#" + matcher.group(1) + ">");
        }
        matcher.appendTail(sb);
        String processed = sb.toString();
        return processColors(processed);
    }

    public void updateTabList(Player player) {
        String headerRaw = getRaw("tablist.header").toString();
        String footerRaw = getRaw("tablist.footer").toString();

        Component header = parse(headerRaw, player);
        Component footer = parse(footerRaw, player);

        player.sendPlayerListHeaderAndFooter(header, footer);
    }

    public void sendTabList(Player player) {
        Component header = parse("<gradient:#FF55FF:#FFFF55:#FF5555><bold>MixGame</bold></gradient>\n", player);
        Component footer = parse("\n<gray>Версия: <white>2.3.2</white></gray>\n<aqua>://yourserver.com</aqua>", player);

        player.sendPlayerListHeaderAndFooter(header, footer);
    }

    private static Component processColors(String processed) {
        processed = processed
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>");

        return MM.deserialize(processed);
    }

}

