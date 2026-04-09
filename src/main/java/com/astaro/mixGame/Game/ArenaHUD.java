package com.astaro.mixGame.Game;

import com.astaro.mixGame.services.ChatService;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ArenaHUD {


    private final ArenaController controller;
    private BossBar bossBar;

    public ArenaHUD(ArenaController controller) {
        this.controller = controller;
    }


    public void updateBossBar(String text, float progress, BossBar.Color color) {
        if (bossBar == null) {
            bossBar = BossBar.bossBar(Component.empty(), progress, color, BossBar.Overlay.PROGRESS);
        }

        bossBar.name(ChatService.parse(text, null));
        bossBar.progress(Math.max(0.0f, Math.min(1.0f, progress)));
        bossBar.color(getBlinkColor());

        for (Player p : controller.getBukkitPlayers()) {
            p.showBossBar(bossBar);
        }
    }

    private BossBar.Color getBlinkColor() {
        return BossBar.Color.values()[(int) (System.currentTimeMillis() / 500 % BossBar.Color.values().length)];
    }


    public void removeBossBar() {
        if (bossBar != null) {
            for (Player p : controller.getBukkitPlayers()) {
                p.hideBossBar(bossBar);
            }
            bossBar = null;
        }
    }

    // --- SCOREBOARD LOGIC ---

    public void updateSidebar() {
        String title = "<gradient:#ff55ff:#55ffff><bold>MixGame</bold></gradient>";

        for (Player p : controller.getBukkitPlayers()) {
            Scoreboard board = p.getScoreboard();

            if (board == Bukkit.getScoreboardManager().getMainScoreboard()) {
                board = Bukkit.getScoreboardManager().getNewScoreboard();
                p.setScoreboard(board);
            }

            Objective obj = board.getObjective("arena_info");
            if (obj == null) {
                obj = board.registerNewObjective("arena_info", Criteria.DUMMY, ChatService.parse(title, p));
                obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            }

            replaceScore(obj, "&7Арена: &f" + controller.getSettings().arenaName(), 3);
            replaceScore(obj, "&7Игроки: &a" + controller.getBukkitPlayers().size() + "/" + controller.getSettings().maxPlayers(), 2);
            replaceScore(obj, "&7Раунд: &e" + controller.getCurrentRound(), 1);
            replaceScore(obj, "&8--------------", 0);
        }
    }

    private void replaceScore(Objective obj, String text, int score) {
        Component component = ChatService.parse(text, null);
        String legacyText = LegacyComponentSerializer.legacySection()
                .serialize(component);
        obj.getScore(legacyText).resetScore();
        obj.getScore(legacyText).setScore(score);
    }

    public void clearHUD(Player player) {
        player.hideBossBar(bossBar);
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}
