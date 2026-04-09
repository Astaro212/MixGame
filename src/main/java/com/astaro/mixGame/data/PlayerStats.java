package com.astaro.mixGame.data;

public record PlayerStats(int points, int won, int lost) {
    public int getPlayedGames() { return won + lost; }
    public double getWLR() { return lost == 0 ? won : (double) won / lost; }
}

