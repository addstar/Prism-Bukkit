package me.botsko.prism.utils.block;

import me.botsko.prism.utils.MaterialTag;
import me.botsko.prism.utils.MaterialTag.MatchMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.data.BlockData;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 23/05/2020.
 */
public class TabLibraryHelper {
    // Material at a location that are commonly acceptable to replace.
    protected static final Tag<Material> replaceableMaterials = new MaterialTag(
            Material.AIR,
            Material.CAVE_AIR,
            Material.VOID_AIR,
            Material.FIRE,
            Material.GRAVEL,
            Material.LAVA,
            Material.TALL_GRASS,
            Material.SAND,
            Material.SNOW,
            Material.SNOW_BLOCK,
            Material.WATER,
            Material.GRASS,
            Material.SOUL_SOIL,
            Material.SOUL_SAND,
            Material.NETHERRACK);

    // Material that has gravity (will fall, not break, when placed on the side of a wall or breaking
    // the block under it)
    protected static final MaterialTag fallingMaterials = new MaterialTag(
            Material.GRAVEL,
            Material.ANVIL,
            Material.DRAGON_EGG
    )
            .append("_CONCRETE_POWDER", MatchMode.SUFFIX)
            .append(Tag.SAND);

    // Material that will detach from the side of a block when that block is broken
    protected static final MaterialTag fallsOffWall = new MaterialTag(
            Material.POWERED_RAIL,
            Material.DETECTOR_RAIL,
            Material.STICKY_PISTON,
            Material.PISTON,
            Material.PISTON_HEAD,
            Material.MOVING_PISTON,
            Material.WALL_TORCH,
            Material.SOUL_WALL_TORCH,
            Material.LADDER,
            Material.LEVER,
            Material.REDSTONE_WALL_TORCH,
            Material.NETHER_PORTAL,
            Material.VINE,
            Material.COCOA,
            Material.TRIPWIRE_HOOK,
            Material.ACTIVATOR_RAIL,
            Material.BELL,
            Material.ITEM_FRAME,
            Material.PAINTING)
            .append(Tag.RAILS, Tag.BUTTONS, Tag.WALL_SIGNS)
            .append(MaterialTag.WALL_BANNERS);

    // Material that will detach from the top of a block when that block is broken
    protected static final MaterialTag fallsOffTop = new MaterialTag(
            Material.STICKY_PISTON,
            Material.DEAD_BUSH,
            Material.PISTON,
            Material.PISTON_HEAD,
            Material.MOVING_PISTON,
            Material.TORCH,
            Material.SOUL_TORCH,
            Material.REDSTONE,
            Material.WHEAT,
            Material.LEVER,
            Material.STONE_PRESSURE_PLATE,
            Material.REDSTONE_TORCH,
            Material.SNOW,
            Material.CACTUS,
            Material.SUGAR_CANE,
            Material.NETHER_PORTAL,
            Material.REPEATER,
            Material.PUMPKIN_STEM,
            Material.MELON_STEM,
            Material.LILY_PAD,
            Material.NETHER_WART,
            Material.CARROTS,
            Material.POTATOES,
            Material.BEETROOTS,
            Material.COMPARATOR,
            Material.BAMBOO,
            Material.TURTLE_EGG,
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
            Material.BEACON,
            Material.ITEM_FRAME,
            Material.CONDUIT,
            Material.BELL
    )
            .append(Tag.DOORS,
                    Tag.RAILS,
                    Tag.SAPLINGS,
                    MaterialTag.BANNERS,
                    Tag.STANDING_SIGNS
            )
            .append(
                    Tag.WOODEN_PRESSURE_PLATES,
                    Tag.BUTTONS,
                    Tag.CARPETS,
                    Tag.FLOWER_POTS)
            .append(MaterialTag.ALL_PLANTS);

    // Material that will be detached by flowing water/lava
    protected static final MaterialTag flowBreaks =
            new MaterialTag(
                    MaterialTag.ALL_PLANTS,
                    MaterialTag.CROPS,
                    MaterialTag.SKULLS
            )
                    .append(
                            Material.CACTUS,
                            Material.REPEATER,
                            Material.COMPARATOR,
                            Material.REDSTONE_WIRE,
                            Material.LEVER
                    )
                    .append(
                            Material.REDSTONE_TORCH,
                            Material.SUGAR_CANE,
                            Material.TORCH,
                            Material.SOUL_TORCH,
                            Material.WALL_TORCH,
                            Material.SOUL_WALL_TORCH,
                            Material.REDSTONE_WALL_TORCH,
                            Material.TRIPWIRE)
                    .append(
                            Material.TRIPWIRE_HOOK,
                            Material.VINE,
                            Material.END_ROD
                    )
                    .append(
                            Tag.BUTTONS,
                            Tag.SAPLINGS,
                            Tag.RAILS,
                            Tag.FLOWER_POTS);

    // Material that can grow/spread to another location
    protected static final MaterialTag growableStructure =
            new MaterialTag(Tag.LEAVES, Tag.LOGS)
                    .append(
                            Material.RED_MUSHROOM_BLOCK,
                            Material.BROWN_MUSHROOM_BLOCK,
                            Material.MUSHROOM_STEM,
                            Material.CHORUS_FLOWER)
                    .append(MaterialTag.HYPHAE);


    // Material that could possibly cause other material to detach from another block
    protected static final EnumSet<Material> detachingBlocks = EnumSet.of(
            Material.AIR,
            Material.FIRE,
            Material.WATER,
            Material.LAVA);
    /**
     * There are several items that are officially different ItemStacks, but for the
     * purposes of what we're doing are really considered one core item. This
     * attempts to be a little lenient on matching the ids.
     *
     * @param material Material 1
     * @param material Material 2
     */
    protected static final Map<Material, Material> baseMaterials = new EnumMap<>(Material.class);

    static {
        baseMaterials.put(Material.GRASS_BLOCK, Material.DIRT);
        baseMaterials.put(Material.MYCELIUM, Material.DIRT);
        baseMaterials.put(Material.PODZOL, Material.DIRT);
        baseMaterials.put(Material.FARMLAND, Material.DIRT);
        baseMaterials.put(Material.GRASS_PATH, Material.DIRT);
    }

    /**
     * Create a string from BlockData.
     *
     * @param data BlockData
     * @return String
     */
    public static String dataString(BlockData data) {
        if (data != null) {
            return data.getAsString().replace("minecraft:"
                    + data.getMaterial().name().toLowerCase(Locale.ENGLISH), "");
        }

        return "";
    }
}
