package dev.aurelium.auramobs.entities;

import dev.aurelium.auramobs.AuraMobs;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ScaleManager {

    private final List<EntityScale> entries = new ArrayList<>();
    private Attribute scaleAttribute;
    private final AuraMobs plugin;

    public ScaleManager(AuraMobs plugin) {
        this.plugin = plugin;
    }

    public void loadConfiguration() {
        try {
            entries.clear();

            for (Attribute attribute : Attribute.values()) {
                if (attribute.name().toLowerCase().contains("scale")) {
                    scaleAttribute = attribute;
                    break;
                }
            }

            if (scaleAttribute == null) return;

            ConfigurationSection section = plugin.getConfig().getConfigurationSection("scales");

            for (String entry : section.getKeys(false)) {
                ConfigurationSection entrySection = section.getConfigurationSection(entry);

                int levelStart = Integer.parseInt(entry.split("-")[0]);
                int levelEnd = Integer.parseInt(entry.split("-")[1]);
                double chance = entrySection.getDouble("chance");

                String scale = entrySection.getString("scale");

                if(scale == null) {
                    plugin.getLogger().warning("Scale entry " + entry + " is missing the scale value!");
                    continue;
                }

                if (scale.contains("-")) {

                    if(scale.split("-").length != 2) {
                        plugin.getLogger().warning("Scale entry " + entry + " has an invalid scale value!");
                        continue;
                    }

                    double intervalStart = Double.parseDouble(scale.split("-")[0]);
                    double intervalEnd = Double.parseDouble(scale.split("-")[1]);
                    entries.add(new EntityScale(levelStart, levelEnd, new double[0], intervalStart, intervalEnd, chance));
                } else {
                    double[] fixed = Arrays.stream(scale.replace(" ", "").split(",")).mapToDouble(Double::parseDouble).toArray();
                    entries.add(new EntityScale(levelStart, levelEnd, fixed, 0, 0, chance));
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load scale configuration: " + e.getMessage());
        }
    }

    public void applyScale(LivingEntity entity, int level) {
        for (EntityScale entry : entries) {
            if (level >= entry.getLevelStart() && level <= entry.getLevelEnd()) {
                if (Math.random() < entry.getChance()) {
                    if (entry.getFixed().length > 0) {
                        double random = entry.getFixed()[ThreadLocalRandom.current().nextInt(entry.getFixed().length)];
                        entity.getAttribute(scaleAttribute).setBaseValue(Math.max(.00625, Math.min(16, random)));
                    } else {
                        double random = entry.getIntervalStart() + (entry.getIntervalEnd() - entry.getIntervalStart()) * Math.random();
                        entity.getAttribute(scaleAttribute).setBaseValue(Math.max(.00625, Math.min(16, random)));
                    }
                }
            }
        }
    }

    public boolean hasScaleAttribute() {
        return scaleAttribute != null;
    }
}
