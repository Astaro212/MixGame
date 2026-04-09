package com.astaro.mixGame.Game;

import com.astaro.mixGame.MixGame;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GameTask extends BukkitRunnable {
    private final MixGame plugin;
    private final ArenaController controller;
    private int phaseTimer;
    private GamePhase phase = GamePhase.LOBBY;


    public GameTask(MixGame plugin, ArenaController controller) {
        this.plugin = plugin;
        this.controller = controller;
        this.phaseTimer = plugin.getSettings().lobbyCountdown();
    }

    @Override
    public void run() {
        if (controller.getStatus() == ArenaController.ArenaStatus.WAITING) {
            this.cancel();
            return;
        }

        if (phaseTimer <= 0) {
            nextPhase();
        }

        controller.updateProgress(phase, phaseTimer);
        phaseTimer--;

    }

    private void nextPhase() {
        switch (phase) {
            case LOBBY -> {
                controller.startGame();

                phase = GamePhase.IDLE;
                phaseTimer = 2;
            }
            case IDLE -> startShowingColor();
            case SHOWING_COLOR -> clearFloor();
            case WAITING_FOR_FALL -> startNewRound();
        }
    }

    private void startShowingColor() {
        phase = GamePhase.SHOWING_COLOR;

        String type = controller.getSettings().floorMaterial();

        Material luckyMaterial = plugin.getFloorService().getRandomMaterial(controller.getSettings().floorMaterial());
        controller.setCurrentMaterial(luckyMaterial);
        List<Material> pool = switch (type.toUpperCase()) {
            case "WOOL" -> plugin.getFloorService().getWoolList();
            case "CONCRETE" -> plugin.getFloorService().getConcreteList();
            case "TERRACOTTA" -> plugin.getFloorService().getTerracottaList();
            default -> plugin.getFloorService().getMixedList();
        };

        int totalSections = controller.getFloorManager().getSectionsCount();
        int correctCount = Math.max(1, (totalSections / 6)  - (controller.getCurrentRound() / 2));
        if (controller.getCurrentRound() > 10) {
            correctCount = ThreadLocalRandom.current().nextInt(1, 3);
        }

        controller.getFloorManager().generateNewFloor(luckyMaterial, pool, correctCount);

        ItemStack item = new ItemStack(luckyMaterial);
        controller.getBukkitPlayers().forEach(p -> {
            for (int i = 0; i < 9; i++) {
                p.getInventory().setItem(i, item);
            }
        });

        int baseTime = 7;

        if (controller.getCurrentRound() >= 5) {
            int reduction = (controller.getCurrentRound() - 3) / 2;
            baseTime -= reduction;
        }

        phaseTimer = Math.max(2, baseTime);

    }

    private void clearFloor() {
        phase = GamePhase.WAITING_FOR_FALL;
        controller.getFloorManager().clearIncorrectBlocks(controller.getCurrentMaterial());

        controller.getBukkitPlayers().forEach(p -> {
            p.getInventory().clear();
            p.getInventory().setItem(8, plugin.getItemService().getLeaveItem());
        });

        phaseTimer = 3;
    }


    private void startNewRound() {
        phase = GamePhase.IDLE;
        controller.nextRound();
        controller.getBukkitPlayers().forEach(p -> {
            plugin.getNotificationService().sendTitle(p, "Titles.roundStart", "%round%", String.valueOf(controller.getCurrentRound()));
        });
        phaseTimer = 1;
    }

    enum GamePhase {LOBBY, IDLE, SHOWING_COLOR, WAITING_FOR_FALL}
}

