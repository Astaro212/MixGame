package com.astaro.mixGame.services;

import com.astaro.mixGame.MixGame;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Collection;

public class NotificationService {
    private final MixGame plugin;

    public NotificationService(MixGame plugin) {
        this.plugin = plugin;
    }

    /**
     * Отправляет тайтл, используя ключи из файла локализации
     * @param path Ключ в конфиге (например, "Titles.roundStart")
     */
    public void sendTitle(Player player, String path, String... replacements) {
        String titleRaw = plugin.getChatService().getRaw(path + ".title").toString();
        String subtitleRaw = plugin.getChatService().getRaw(path + ".subtitle").toString();

        Object rawFadeIn = plugin.getChatService().getRaw(path + ".fadeIn");
        int fadeIn = (rawFadeIn instanceof Number) ? ((Number) rawFadeIn).intValue() : 10;

        Object rawStay = plugin.getChatService().getRaw(path + ".stay");
        int stay = (rawStay instanceof Number) ? ((Number) rawStay).intValue() : 40;

        Object rawFadeOut = plugin.getChatService().getRaw(path + ".fadeOut");
        int fadeOut = (rawFadeOut instanceof Number) ? ((Number) rawFadeOut).intValue() : 10;

        String finalTitle = replace(titleRaw, replacements);
        String finalSubtitle = replace(subtitleRaw, replacements);

        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L)
        );

        Title title = Title.title(
                ChatService.parse(finalTitle, player),
                ChatService.parse(finalSubtitle, player),
                times
        );

        player.showTitle(title);
    }

    private String replace(String text, String... replacements) {
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                text = text.replace(replacements[i], replacements[i + 1]);
            }
        }
        return text;
    }


}
