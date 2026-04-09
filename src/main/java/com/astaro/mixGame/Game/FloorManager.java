package com.astaro.mixGame.managers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class FloorManager {
    private final List<BlockArea> sections = new ArrayList<>();
    private final World world;


    private record BlockArea(int minX, int maxX, int minZ, int maxZ, int y) {
    }

    public FloorManager(Location loc1, Location loc2, int sectionSize) {
        this.world = loc1.getWorld();
        calculateSections(loc1, loc2, sectionSize);
    }

    private void calculateSections(Location l1, Location l2, int size) {
        int minX = Math.min(l1.getBlockX(), l2.getBlockX());
        int maxX = Math.max(l1.getBlockX(), l2.getBlockX());
        int minZ = Math.min(l1.getBlockZ(), l2.getBlockZ());
        int maxZ = Math.max(l1.getBlockZ(), l2.getBlockZ());
        int y = l1.getBlockY();

        for (int x = minX; x <= maxX; x += size) {
            for (int z = minZ; z <= maxZ; z += size) {
                sections.add(new BlockArea(x, Math.min(x + size - 1, maxX),
                        z, Math.min(z + size - 1, maxZ), y));
            }
        }
    }

    public void generateNewFloor(Material selectedMaterial, Set<Material> pool) {
        int luckyIndex = ThreadLocalRandom.current().nextInt(sections.size());

        for (int i = 0; i < sections.size(); i++) {
            BlockArea area = sections.get(i);
            Material mat = (i == luckyIndex) ? selectedMaterial : getRandomFromSet(pool, selectedMaterial);
            BlockData data = mat.createBlockData();

            for (int x = area.minX; x <= area.maxX; x++) {
                for (int z = area.minZ; z <= area.maxZ; z++) {
                    world.setBlockData(x, area.y, z, data);
                }
            }
        }
    }

    private Material getRandomFromSet(Set<Material> pool, Material exclude) {
        List<Material> list = new ArrayList<>(pool);
        list.remove(exclude);
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }
}