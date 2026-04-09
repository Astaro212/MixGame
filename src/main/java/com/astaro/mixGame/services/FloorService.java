package com.astaro.mixGame.services;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class FloorService {
    private final List<Material> wools = new ArrayList<>();
    private final List<Material> concretes = new ArrayList<>();
    private final List<Material> terracottas = new ArrayList<>();
    private final List<Material> mixed = new ArrayList<>();


    public FloorService() {
        for (Material m : Material.values()) {
            if (!m.isBlock()) continue;

            String name = m.name();

            if (name.endsWith("_WOOL")) wools.add(m);
            else if (name.endsWith("_CONCRETE")) concretes.add(m);
            else if (name.endsWith("_TERRACOTTA") && !name.contains("GLAZED")) terracottas.add(m);
        }
        mixed.addAll(wools);
        mixed.addAll(concretes);
        mixed.addAll(terracottas);
    }

    public Material getRandomMaterial(String type) {
        List<Material> source = switch (type.toUpperCase()) {
            case "WOOL" -> new ArrayList<>(wools);
            case "CONCRETE" -> new ArrayList<>(concretes);
            case "TERRACOTTA" -> new ArrayList<>(terracottas);
            default -> mixed;
        };
        return source.get(ThreadLocalRandom.current().nextInt(source.size()));
    }

    public List<Material> getMixedList() {
       return this.mixed;
    }
    public List<Material> getWoolList() { return this.wools; }
    public List<Material> getConcreteList() { return this.concretes; }
    public List<Material> getTerracottaList() { return this.terracottas; }
}
