package me.botsko.prism.api.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created for Prism.
 *
 * @author Narimm on 22/02/2021
 * @since 2.1.8
 */
@SuppressWarnings("ALL")
public enum ActionType {

    BLOCK_BREAK("block-break"),
    BLOCK_BURN("block-burn"),
    BLOCK_DISPENSE("block-dispense"),
    BLOCK_FADE("block-fade"),
    BLOCK_FALL("block-fall"),
    BLOCK_FORM("block-form"),
    BLOCK_PLACE("block-place"),
    BLOCK_SHIFT("block-shift"),
    BLOCK_SPREAD("block-spread"),
    BLOCK_USE("block-use"),
    BONEMEAL_USE("bonemeal-use"),
    BUCKET_FILL("bucket-fill"),
    CAKE_EAT("cake-eat"),
    CONTAINER_ACCESS("container-access"),
    CRAFT_ITEM("craft-item"),
    ENCHANT_ITEM("enchant-item"),
    CREEPER_EXPLODE("creeper-explode"),
    CROP_TRAMPLE("crop-trample"),
    DRAGON_EAT("dragon-eat"),
    ENDERMAN_PICKUP("enderman-pickup"),
    ENDERMAN_PLACE("enderman-place"),
    ENTITY_BREAK("entity-break"),
    ENTITY_DYE("entity-dye"),
    ENTITY_EXPLODE("entity-explode"),
    ENTITY_FOLLOW("entity-follow"),
    ENTITY_FORM("entity-form"),
    ENTITY_SPAWN("entity-spawn"),
    ENTITY_KILL("entity-kill"),
    ENTITY_LEASH("entity-leash"),
    ENTITY_SHEAR("entity-shear"),
    ENTITY_UNLEASH("entity-unleash"),
    FIREBALL("fireball"),
    FIRE_SPREAD("fire-spread"),
    FIREWORK_LAUNCH("firework-launch"),
    HANGINGITEM_BREAK("hangingitem-break"),
    HANGINGITEM_PLACE("hangingitem-place"),
    ITEM_DROP("item-drop"),
    ITEM_INSERT("item-insert"),
    ITEM_PICKUP("item-pickup"),
    ITEM_REMOVE("item-remove"),
    ITEM_BREAK("item-break"),
    ITEM_ROTATE("item-rotate"),
    LAVA_BREAK("lava-break"),
    LAVA_BUCKET("lava-bucket"),
    LAVA_FLOW("lava-flow"),
    LAVA_IGNITE("lava-ignite"),
    LEAF_DECAY("leaf-decay"),
    LIGHTER("lighter"),
    LIGHTNING("lightning"),
    MUSHROOM_GROW("mushroom-grow"),
    PLAYER_CHAT("player-chat"),
    PLAYER_COMMAND("player-command"),
    PLAYER_DEATH("player-death"),
    PLAYER_JOIN("player-join"),
    PLAYER_QUIT("player-quit"),
    PLAYER_KILL("player-kill"),
    PLAYER_GAMEMODECHANGE("player-gamemodechange"),
    PLAYER_TELEPORT("player-teleport"),
    POTION_SPLASH("potion-splash"),
    PRISM_DRAIN("prism-drain"),
    PRISM_EXTINGUISH("prism-extinguish"),
    PRISM_PROCESS("prism-process"),
    PRISM_ROLLBACK("prism-rollback"),
    PRISM_RESTORE("prism-restore"),
    PRISM_UNDO("prism-undo"),
    PRISM_DELETE("prism-delete"),
    SHEEP_EAT("sheep-eat"),
    SIGN_CHANGE("sign-change"),
    PORTAL_CREATE("portal-create"),
    SPAWNEGG_USE("spawnegg-use"),
    TNT_EXPLODE("tnt-explode"),
    BED_EXPLODE("bed-explode"),
    TNT_PRIME("tnt-prime"),
    TREE_GROW("tree-grow"),
    VEHICLE_BREAK("vehicle-break"),
    VEHICLE_ENTER("vehicle-enter"),
    VEHICLE_EXIT("vehicle-exit"),
    VEHICLE_PLACE("vehicle-place"),
    WATER_BREAK("water-break"),
    WATER_FLOW("water-flow"),
    WATER_BUCKET("water-bucket"),
    WORLD_EDIT("world-edit"),
    XP_PICKUP("xp-pickup"),
    TARGET_HIT("target-hit"),
    PLAYER_TRADE("player-trade"),
    ITEM_RECEIVE("item-receive"),
    CUSTOM_ACTION("custom-action");

    private static final Map<String, List<ActionType>> registeredShortNames = new HashMap<>();
    private static final Map<String, List<ActionType>> registeredFamilyNames = new HashMap<>();
    private static final EnumMap<ActionType, StringPair> names = new EnumMap<>(ActionType.class);

    static {
        for (ActionType type : ActionType.values()) {
            StringPair pair = stringToName(type.name);
            if (pair != null) {
                String shortName = pair.shortName;
                List<ActionType> shortType = registeredShortNames.getOrDefault(shortName, new ArrayList<>());
                shortType.add(type);
                registeredShortNames.put(shortName, shortType);
                String familyName = pair.familyName;
                List<ActionType> familyType = registeredFamilyNames.getOrDefault(familyName, new ArrayList<>());
                familyType.add(type);
                registeredFamilyNames.put(familyName, familyType);
                names.put(type, pair);
            }
        }
    }

    public String name;

    ActionType(String name) {
        this.name = name;
    }

    public static Set<ActionType> getTypes() {
        return names.keySet();
    }

    /**
     *  Returns the exact ActionType if one exists.
     *
     * @param name String
     * @return ActionType or Null
     */
    public @Nullable static ActionType getByName(@NotNull String name) {
        try {
            if (name.contains("-")) {
                return ActionType.valueOf(name.replace('-', '_').toUpperCase());
            } else {
                return ActionType.valueOf(name.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @NotNull
    public static List<ActionType> getByShortName(String name) {
        return registeredShortNames.getOrDefault(name, Collections.emptyList());
    }

    public static Set<String> getShortNames() {
        return registeredShortNames.keySet();
    }

    @NotNull
    public static List<ActionType> getByFamilyName(String name) {
        return registeredFamilyNames.getOrDefault(name, Collections.emptyList());
    }

    /**
     * Convert a String to a pair.
     *
     * @param name Paired name
     * @return StringPair.
     */
    public static StringPair stringToName(String name) {
        final String[] _tmp = name.toLowerCase().split("-(?!.*-.*)");
        if (_tmp.length == 2) {
            return new StringPair(_tmp[0], _tmp[1]);
        }
        return null;
    }

    /**
     * Returns the family name  ie block-break (block).
     * @return String
     */
    public String getFamilyName() {
        StringPair result = names.get(this);
        if (result == null) {
            return this.name;
        }
        return result.familyName;
    }

    /**
     * Returns the short name  ie block-break (break).
     * @return String
     */
    public String getShortName() {
        StringPair result = names.get(this);
        if (result == null) {
            return this.name;
        }
        return result.shortName;
    }

    @Override
    public String toString() {
        return name;
    }

    private static class StringPair {
        protected String familyName;
        protected String shortName;

        public StringPair(String familyName, String shortName) {
            this.familyName = familyName;
            this.shortName = shortName;
        }
    }

}
