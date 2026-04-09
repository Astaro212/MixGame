package com.astaro.mixGame.Game;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class FloorManager {
    private final List<Section> sections = new ArrayList<>();
    private final World world;
    private final BlockData airData = Material.AIR.createBlockData();
    private final Location l1, l2;
    private int columnsZ;


    private static class Section {
        final int minX, maxX, minZ, maxZ, y;
        Material currentMaterial;

        Section(int minX, int maxX, int minZ, int maxZ, int y) {
            this.minX = minX;
            this.maxX = maxX;
            this.minZ = minZ;
            this.maxZ = maxZ;
            this.y = y;
        }
    }

    public FloorManager(Location loc1, Location loc2, int sectionSize) {
        this.world = loc1.getWorld();
        this.l1 = loc1;
        this.l2 = loc2;
        calculateSections(loc1, loc2, sectionSize);
    }

    public void updateSectionSize(int newSize) {
        sections.clear();
        calculateSections(l1, l2, newSize);
    }

    private void calculateSections(Location l1, Location l2, int size) {
        int minX = Math.min(l1.getBlockX(), l2.getBlockX());
        int maxX = Math.max(l1.getBlockX(), l2.getBlockX());
        int minZ = Math.min(l1.getBlockZ(), l2.getBlockZ());
        int maxZ = Math.max(l1.getBlockZ(), l2.getBlockZ());
        int y = l1.getBlockY();

        this.columnsZ = 0;
        for (int z = minZ; z <= maxZ; z += size) columnsZ++;

        sections.clear();
        for (int x = minX; x <= maxX; x += size) {
            for (int z = minZ; z <= maxZ; z += size) {
                int endX = Math.min(x + size - 1, maxX);
                int endZ = Math.min(z + size - 1, maxZ);
                sections.add(new Section(x, endX, z, endZ, y));
            }
        }
    }

    public void generateNewFloor(Material selectedMaterial, List<Material> pool, int correctCount) {
        List<Material> wrongMaterials = new ArrayList<>(pool);
        wrongMaterials.remove(selectedMaterial);

        for (int i = 0; i < sections.size(); i++) {
            Section current = sections.get(i);

            Material topNeighbor = (i % columnsZ > 0) ? sections.get(i - 1).currentMaterial : null;
            Material leftNeighbor = (i >= columnsZ) ? sections.get(i - columnsZ).currentMaterial : null;

            Material mat = getUniqueMaterial(wrongMaterials, leftNeighbor, topNeighbor);
            current.currentMaterial = mat;
            fillArea(current, mat.createBlockData());
        }

        List<Section> shuffled = new ArrayList<>(sections);
        Collections.shuffle(shuffled);

        int limit = Math.max(1, Math.min(correctCount, sections.size()));
        for (int i = 0; i < limit; i++) {
            Section lucky = shuffled.get(i);
            lucky.currentMaterial = selectedMaterial;
            fillArea(lucky, selectedMaterial.createBlockData());
        }
    }

    private Material getUniqueMaterial(List<Material> pool, Material m1, Material m2) {
        List<Material> available = new ArrayList<>(pool);
        if (available.size() > 1) {
            available.remove(m1);
            available.remove(m2);
        }
        if (available.isEmpty()) return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));

        return available.get(ThreadLocalRandom.current().nextInt(available.size()));
    }




    public void clearIncorrectBlocks(Material correctMaterial) {
        for (Section section : sections) {
            if (section.currentMaterial != correctMaterial) {
                fillArea(section, airData);
                section.currentMaterial = Material.AIR;
            }
        }
    }

    private void fillArea(Section area, BlockData data) {
        for (int x = area.minX; x <= area.maxX; x++) {
            for (int z = area.minZ; z <= area.maxZ; z++) {
                world.setBlockData(x, area.y, z, data);
            }
        }
    }

    public void reset() {
        for (Section section : sections) {
            fillArea(section, Material.WHITE_WOOL.createBlockData());
            section.currentMaterial = Material.WHITE_WOOL;
        }
    }

    public int getSectionsCount(){
        return sections.size();
    }
}