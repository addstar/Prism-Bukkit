package me.botsko.prism.utils;

import me.botsko.prism.Prism;
import me.botsko.prism.api.objects.MaterialState;
import me.botsko.prism.database.IdMapQuery;
import me.botsko.prism.database.sql.SqlIdMapQuery;
import me.botsko.prism.utils.block.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MaterialAliases {
    private static final int SUBID_WILDCARD = -1;
    private final Map<String, String> matCache = new HashMap<>();
    private final Map<String, String> idCache = new HashMap<>();
    private final Map<Material, Set<IntPair>> allIdsCache = new HashMap<>();

    private final HashMap<String, String> itemAliases = new HashMap<>();

    /**
     * Load the yml file and save config to hashmap.
     */
    public MaterialAliases() {
        FileConfiguration items = null;
        InputStream defConfigStream = this.getClass().getResourceAsStream("/items.yml");
        if (defConfigStream != null) {
            Prism.log("Elixr: Loaded items directory");
            items = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
        }


        if (items != null) {

            // Load all item ids/aliases
            ConfigurationSection sec = items.getConfigurationSection("items");
            Map<String, Object> itemAliases = null;

            if (sec != null) {
                itemAliases = sec.getValues(false);
            }
            // Cache the values for easier lookup
            if (itemAliases != null) {
                for (Map.Entry<String, Object> entry : itemAliases.entrySet()) {
                    @SuppressWarnings("unchecked")
                    Iterable<String> aliases = (List<String>) entry.getValue();

                    for (String alias : aliases) {
                        this.itemAliases.put(entry.getKey().toLowerCase(), alias);
                    }
                }
            }

        } else {
            Prism.warn("ERROR: The Item library was unable to load an internal item alias list.");
        }
    }

    public void initAllMaterials() {
        initMaterials(Material.values());
    }

    /**
     * Initialize the library.
     *
     * @param materials Materials ...
     */
    public void initMaterials(Material... materials) {
        Bukkit.getScheduler().runTaskAsynchronously(Prism.getInstance(), () -> {
            SqlIdMapQuery query = new SqlIdMapQuery(Prism.getPrismDataSource());
            for (Material m : materials) {
                String matName = m.name().toLowerCase(Locale.ENGLISH);
                String dataString;

                try {
                    dataString = Utilities.dataString(Bukkit.createBlockData(m));
                } catch (IllegalArgumentException e) {
                    continue;
                }

                query.findIds(m.name().toLowerCase(Locale.ENGLISH), dataString,
                      (i, d) ->
                        storeCache(m, dataString, i, d), () -> {
                        int id = query.mapAutoId(matName, dataString);
                        storeCache(m, dataString, id, 0);
                      });
            }
        });

    }

    private Set<IntPair> getIdsOf(Material material) {
        Set<IntPair> ids = allIdsCache.get(material);
        if (ids != null) {
            return ids;
        }

        IdMapQuery query = new SqlIdMapQuery(Prism.getPrismDataSource());

        query.findAllIds(material.name().toLowerCase(Locale.ENGLISH), list -> allIdsCache.put(
                material, new HashSet<>(list)));
        return allIdsCache.get(material);
    }

    private void storeCache(Material material, String state, int blockId, int blockSubid) {
        String matKey = material.name().toLowerCase(Locale.ENGLISH) + ":" + state;
        String idKey = blockId + ":" + blockSubid;

        matCache.put(idKey, matKey);
        idCache.put(matKey, idKey);

        getIdsOf(material).add(new IntPair(blockId, blockSubid));
    }

    private MaterialState fromCache(int blockId, int blockSubid) {
        String value = matCache.get(blockId + ":" + blockSubid);

        if (value != null) {
            String[] parts = value.split(":", 2);

            return new MaterialState(Material.matchMaterial(parts[0]), parts[1]);
        }

        return null;
    }

    private IntPair fromCache(Material material, String state) {
        String value = idCache.get(material.name().toLowerCase(Locale.ENGLISH) + ":" + state);

        if (value != null) {
            String[] parts = value.split(":", 2);

            return new IntPair(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }

        return null;
    }

    /**
     * .
     *
     * @param blockId           int
     * @param blockSubId        logMaterialErrorsint
     * @param logMaterialErrors boolean.
     * @return MaterialState
     */
    public MaterialState idsToMaterial(int blockId, int blockSubId, Boolean logMaterialErrors) {
        MaterialState cachedMaterial = fromCache(blockId, blockSubId);

        if (cachedMaterial != null) {
            return cachedMaterial;
        }

        MaterialState result = new MaterialState();
        SqlIdMapQuery query = new SqlIdMapQuery(Prism.getPrismDataSource());

        query.findMaterial(blockId, blockSubId,
              (material, state) -> {
                  result.material = Material.matchMaterial(material.toUpperCase(Locale.ENGLISH));
                  result.state = state;
                  if (result.material != null) {
                      storeCache(result.material, result.state, blockId, blockSubId);
                  }
              }, () -> {
                  if (logMaterialErrors) {
                      Prism.log("matError: [" + blockId + ", " + blockSubId + "] -> ???");
                  }
              });

        if (result.material == null) {
            return null;
        }

        return result;
    }

    /**
     * Create IntPair.
     *
     * @param material Material
     * @param state    State
     * @return IntPair
     */
    public IntPair materialToIds(Material material, String state) {
        int blockSubId;

        // For tools, where durability doesn't mean a different item (different cached
        // value) but is still important
        int durability = 0;
        try {
            durability = Integer.parseInt(state);
        } catch (NumberFormatException ignored) {
            //ignored
        }

        if (material.getMaxDurability() > 0) {
            blockSubId = 0;
        } else {
            blockSubId = durability;
        }

        IntPair cachedIds = fromCache(material, blockSubId == 0 ? state : String.valueOf(blockSubId));

        if (cachedIds != null) {
            return cachedIds;
        }

        IntPair result = new IntPair(0, 0);
        SqlIdMapQuery query = new SqlIdMapQuery(Prism.getPrismDataSource());
        String materialName = material.name().toLowerCase(Locale.ENGLISH);

        synchronized (this) {
            query.findIds(materialName, state,
                  (queryId, querySubId) -> {
                    result.first = queryId;
                    result.second = querySubId;
                    storeCache(material, state, queryId, querySubId); },
                  () -> {
                    int blockId = query.mapAutoId(materialName, state);
                    result.first = blockId;
                    storeCache(material, state, blockId, 0); });
        }

        if (blockSubId != durability) {
            result.second = durability;
        }
        return result;
    }

    public Set<IntPair> materialToAllIds(Material material) {
        return Collections.unmodifiableSet(getIdsOf(material));
    }

    /**
     * .
     *
     * @param material         Material
     * @param partialBlockData String
     * @return Set of IntPair
     * @throws IllegalArgumentException exception
     */
    public Set<IntPair> partialBlockDataIds(Material material, String partialBlockData)
            throws IllegalArgumentException {
        String fullBlockData = Utilities.dataString(Bukkit.createBlockData(material, partialBlockData));

        String[] parts = fullBlockData.substring(1, fullBlockData.length() - 1).toLowerCase(Locale.ENGLISH).split(",");

        StringBuilder likeString = new StringBuilder("%");

        for (String string : parts) {
            if (partialBlockData.contains(string)) {
                likeString.append(string).append('%');
            }
        }

        String stateLike = likeString.toString();

        IdMapQuery query = new SqlIdMapQuery(Prism.getPrismDataSource());

        Set<IntPair> ids = new HashSet<>();
        query.findAllIdsPartial(material.name().toLowerCase(Locale.ENGLISH), stateLike, ids::addAll);

        return ids;
    }

    /**
     * .
     *
     * @param material Material
     * @param state    String
     * @return IntPair
     */
    @Deprecated
    public IntPair materialToIdsWildcard(Material material, String state) {
        IntPair ids = materialToIds(material, state);

        if (material.getMaxDurability() > 0) {
            ids.second = SUBID_WILDCARD;
        }
        return ids;
    }

    /**
     * Returns the loaded list of item aliases/ids.
     *
     * @return HashMap
     */
    @Deprecated
    public HashMap<String, String> getItemAliases() {
        return itemAliases;
    }

    /**
     * .
     *
     * @param material Material
     * @param data     BlockData
     * @return String
     */
    public String getAlias(Material material, BlockData data) {
        String dataString = Utilities.dataString(data);

        String itemName = null;
        if (!itemAliases.isEmpty()) {
            String key = material.name().toLowerCase() + dataString;
            itemName = itemAliases.get(key);
        }
        if (itemName == null) {
            itemName = material.name().toLowerCase().replace("_", " ");
        }
        return itemName;
    }

    /**
     * Returns the proper name given an item stack.
     *
     * @param i ItemStack
     * @return String
     */
    public String getAlias(ItemStack i) {
        return i.getType().name().toLowerCase().replace("_", " ");
    }

    /**
     * .
     *
     * @param alias String
     * @return ArrayList
     */
    // TODO: Break this in 1.13. Woooo.
    public ArrayList<Material> getMaterialsByAlias(String alias) {
        ArrayList<Material> itemIds = new ArrayList<>();
        if (!itemAliases.isEmpty()) {
            for (Entry<String, String> entry : itemAliases.entrySet()) {
                if (entry.getValue().equals(alias)) {
                    itemIds.add(Material.matchMaterial(entry.getKey()));
                }
            }
        }
        return itemIds;
    }

}