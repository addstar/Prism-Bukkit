package me.botsko.prism.utils;

import me.botsko.prism.Prism;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public class MaterialTag implements Tag<Material> {
    public static final Tag<Material> DYES = new MaterialTag("_DYE",MatchMode.SUFFIX);
    public static final MaterialTag CORAL_WALL_FANS = new MaterialTag(Material.DEAD_BRAIN_CORAL_WALL_FAN,
            Material.DEAD_BUBBLE_CORAL_WALL_FAN, Material.DEAD_FIRE_CORAL_WALL_FAN, Material.DEAD_HORN_CORAL_WALL_FAN,
            Material.DEAD_TUBE_CORAL_WALL_FAN, Material.DEAD_BRAIN_CORAL_WALL_FAN,
            Material.BRAIN_CORAL_WALL_FAN, Material.BUBBLE_CORAL_WALL_FAN, Material.FIRE_CORAL_WALL_FAN,
            Material.HORN_CORAL_WALL_FAN, Material.TUBE_CORAL_WALL_FAN);
    public static final MaterialTag PLANTS = new MaterialTag(Material.GRASS, Material.FERN, Material.DEAD_BUSH,
            Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM, Material.AZURE_BLUET,
            Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP, Material.OXEYE_DAISY,
            Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.LILY_PAD, Material.KELP, Material.KELP_PLANT,
            Material.SWEET_BERRY_BUSH)
            .append(CORAL_WALL_FANS)
            .append(Tag.CORALS);
    public static final MaterialTag TALL_PLANTS = new MaterialTag(Material.SUNFLOWER, Material.LILAC,
            Material.ROSE_BUSH, Material.PEONY, Material.TALL_GRASS, Material.LARGE_FERN, Material.TALL_SEAGRASS,
            Material.KELP, Material.KELP_PLANT);
    public static final MaterialTag CROPS = new MaterialTag(Material.WHEAT, Material.POTATOES, Material.CARROTS,
            Material.BEETROOTS, Material.MELON_STEM, Material.ATTACHED_MELON_STEM, Material.NETHER_WART,
            Material.PUMPKIN_STEM, Material.ATTACHED_PUMPKIN_STEM);
    public static final Tag<Material> SOIL_CANDIDATES = new MaterialTag(Material.AIR, Material.WATER, Material.LAVA,
            Material.DIRT, Material.GRASS_BLOCK, Material.PODZOL, Material.MYCELIUM, Material.COARSE_DIRT,
            Material.FARMLAND, Material.GRASS_PATH, Material.WARPED_NYLIUM, Material.CRIMSON_NYLIUM);
    public static final MaterialTag WALL_SKULLS = new MaterialTag(Material.SKELETON_WALL_SKULL,
            Material.WITHER_SKELETON_WALL_SKULL, Material.CREEPER_WALL_HEAD, Material.DRAGON_WALL_HEAD,
            Material.PLAYER_WALL_HEAD, Material.ZOMBIE_WALL_HEAD);
    public static final MaterialTag FLOOR_SKULLS = new MaterialTag(Material.SKELETON_SKULL,
            Material.WITHER_SKELETON_SKULL, Material.CREEPER_HEAD, Material.DRAGON_HEAD,
            Material.PLAYER_HEAD, Material.ZOMBIE_HEAD);
    public static final MaterialTag HYPHAE = new MaterialTag("_HYPHAE",MatchMode.SUFFIX);
    public static final MaterialTag SKULLS = new MaterialTag(WALL_SKULLS).append(FLOOR_SKULLS);
    public static final MaterialTag ALL_PLANTS = new MaterialTag(PLANTS).append(TALL_PLANTS);
    public static final Tag<Material> BOATS = new MaterialTag(Tag.ITEMS_BOATS);
    public static final Tag<Material> SPAWN_EGGS = new MaterialTag("_SPAWN_EGG", MatchMode.SUFFIX);
    public static final MaterialTag ALL_BANNERS = new MaterialTag(Tag.BANNERS);
    public static final MaterialTag BANNERS = new MaterialTag(ALL_BANNERS).exclude("_WALL_", MatchMode.CONTAINS);
    public static final MaterialTag WALL_BANNERS = new MaterialTag(Tag.BANNERS).exclude(BANNERS);
    public static final MaterialTag BEDS = new MaterialTag(Tag.BEDS);
    public static final MaterialTag CRAFTING = new MaterialTag(Material.CRAFTING_TABLE,Material.ANVIL,Material.JIGSAW,
            Material.SMITHING_TABLE,Material.BREWING_STAND,Material.ENCHANTING_TABLE,Material.SMOKER,Material.FURNACE,
            Material.BLAST_FURNACE);
    public static final MaterialTag CONTAINERS = new MaterialTag(CRAFTING).append(
            Material.CHEST,Material.BARREL,Material.ENDER_CHEST,Material.TRAPPED_CHEST,Material.CHEST_MINECART,
            Material.DROPPER,Material.DISPENSER, Material.HOPPER,Material.HOPPER_MINECART
    );
    public static final MaterialTag USABLE = new MaterialTag(Tag.BUTTONS).append(Tag.DOORS).append(Tag.TRAPDOORS)
            .append(Material.LEVER).append(Tag.FENCE_GATES);
    // Affected by bone meal.
    public static final MaterialTag GROWABLE = new MaterialTag(CROPS).append(PLANTS).append(Material.CACTUS)
            .append(Tag.SAPLINGS);
    private final EnumSet<Material> materials;
    private final NamespacedKey key;

    /**
     * Constructor.
     * @param materials Set
     */
    @SuppressWarnings("unused")
    public MaterialTag(EnumSet<Material> materials) {
        this.materials = materials.clone();
        key = new NamespacedKey(Prism.getInstance(),"material_tags");
    }

    /**
     * Constructor.
     * @param materialTags Tag
     */
    @SafeVarargs
    public MaterialTag(Tag<Material>... materialTags) {
        this.materials = EnumSet.noneOf(Material.class);
        append(materialTags);
        key = new NamespacedKey(Prism.getInstance(),"material_tags");
    }

    /**
     * Constructor.
     * add new Material to the group.
     * @param materials Materials
     */
    public MaterialTag(Material... materials) {
        this.materials = EnumSet.noneOf(Material.class);
        append(materials);
        key = new NamespacedKey(Prism.getInstance(),"material_tags");

    }

    /**
     * Constructor.
     * add  Material via matching.
     * @param segment Sting
     * @param mode MatchMode
     */
    public MaterialTag(String segment, MatchMode mode) {
        this.materials = EnumSet.noneOf(Material.class);
        append(segment, mode);
        key = new NamespacedKey(Prism.getInstance(),"material_tags");

    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return key;
    }

    public MaterialTag append(Material... materials) {
        this.materials.addAll(Arrays.asList(materials));
        return this;
    }

    /**
     * add new Tags to the group.
     * @param materialTags Tag
     * @return MaterialTag
     */
    @SafeVarargs
    public final MaterialTag append(Tag<Material>... materialTags) {
        for (Tag<Material> materialTag : materialTags) {
            this.materials.addAll(materialTag.getValues());
        }

        return this;
    }

    /**
     * Append a segment and mode.
     *
     * @param segment String
     * @param mode    MatchMode
     * @return MaterialTag
     */
    public MaterialTag append(String segment, MatchMode mode) {
        segment = segment.toUpperCase();

        switch (mode) {
            case PREFIX:
                for (Material m : Material.values()) {
                    if (m.name().startsWith(segment)) {
                        materials.add(m);
                    }
                }
                break;

            case SUFFIX:
                for (Material m : Material.values()) {
                    if (m.name().endsWith(segment)) {
                        materials.add(m);
                    }
                }
                break;

            case CONTAINS:
                for (Material m : Material.values()) {
                    if (m.name().contains(segment)) {
                        materials.add(m);
                    }
                }
                break;
            default:
                throw new IllegalArgumentException(mode.name() + " is NOT a valid rule");
        }

        return this;
    }

    /**
     * Exclude certain materials.
     *
     * @param materials Materials to exclude
     * @return MaterialTag.
     */
    public MaterialTag exclude(Material... materials) {
        for (Material m : materials) {
            this.materials.remove(m);
        }

        return this;
    }

    /**
     * Exclude certain materials.
     *
     * @param materialTags Materials to exclude
     * @return MaterialTag.
     */
    @SafeVarargs
    public final MaterialTag exclude(Tag<Material>... materialTags) {
        for (Tag<Material> materialTag : materialTags) {
            this.materials.removeAll(materialTag.getValues());
        }

        return this;
    }

    /**
     * Exclude Tags from this group.
     * @param segment String
     * @param mode MatchMode
     * @return MaterialTag
     */
    public MaterialTag exclude(String segment, MatchMode mode) {

        segment = segment.toUpperCase();

        switch (mode) {
            case PREFIX:
                for (Material m : Material.values()) {
                    if (m.name().startsWith(segment)) {
                        materials.remove(m);
                    }
                }
                break;

            case SUFFIX:
                for (Material m : Material.values()) {
                    if (m.name().endsWith(segment)) {
                        materials.remove(m);
                    }
                }
                break;

            case CONTAINS:
                for (Material m : Material.values()) {
                    if (m.name().contains(segment)) {
                        materials.remove(m);
                    }
                }
                break;
            default:
                throw new IllegalArgumentException(mode.name() + " is NOT a valid rule");
        }

        return this;
    }

    @NotNull
    @Override
    public Set<Material> getValues() {
        return materials;
    }

    @Override
    public boolean isTagged(@NotNull Material material) {
        return materials.contains(material);
    }

    @Override
    public String toString() {
        return materials.toString();
    }

    public enum MatchMode {
        PREFIX,
        SUFFIX,
        CONTAINS
    }

}
