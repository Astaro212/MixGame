package com.astaro.mixGame.data;

import com.astaro.mixGame.MixGame;

public record PlayerStats(int points, int won, int lost) {


    public int currentLevel() {
        int pointsPerLevel = MixGame.instance.getSettings().pointsToLevel();
        return (int) ((Math.sqrt(8.0 * points / pointsPerLevel + 1) - 1) / 2);
    }

    public int getPointsForLevel(int level) {
        int step = 25;
        return (level * (level + 1) / 2) * step;
    }

    public int getPlayedGames() {
        return won + lost;
    }

    public double getWLR() {
        return lost == 0 ? won : (double) won / lost;
    }
}

