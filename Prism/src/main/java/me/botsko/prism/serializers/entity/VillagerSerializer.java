package me.botsko.prism.serializers.entity;

import me.botsko.prism.serializers.items.ItemStackSerializer;
import me.botsko.prism.utils.MiscUtils;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VillagerSerializer extends AbstractVillagerSerializer<Villager> {

    protected String type = null;
    protected String profession = null;
    protected Integer villagerXp = 0;
    protected Collection<VillagerRecipe> recipes = new ArrayList<>();

    @Override
    public void serialize(Villager entity) {
        super.serialize(entity);
        type = entity.getVillagerType().name().toLowerCase();
        profession = entity.getProfession().name().toLowerCase();
        villagerXp = entity.getVillagerExperience();
        for (MerchantRecipe recipe : entity.getRecipes()) {
            final VillagerRecipe r = new VillagerRecipe();
            r.result = ItemStackSerializer.createItemStackSerialized(recipe.getResult());
            r.ingredient = new ArrayList<>();
            recipe.getIngredients().forEach(rec -> r.ingredient.add(
                    ItemStackSerializer.createItemStackSerialized(rec)));
            r.experienceReward = recipe.hasExperienceReward();
            r.maxUses = recipe.getMaxUses();
            r.currentUses = recipe.getUses();
            r.villagerXp = recipe.getVillagerExperience();
            r.priceMultiplier = recipe.getPriceMultiplier();
            recipes.add(r);
        }
    }

    @Override
    public void deserialize(Villager entity) {
        super.deserialize(entity);
        entity.setProfession(MiscUtils.getEnum(profession, Profession.FARMER));
        entity.setVillagerExperience(villagerXp);
        entity.setVillagerType(MiscUtils.getEnum(type, Villager.Type.PLAINS));
        List<MerchantRecipe> bukkitRecipes = new ArrayList<>();
        recipes.forEach(villagerRecipe -> {
            MerchantRecipe recipe = new MerchantRecipe(villagerRecipe.result.toBukkit(), villagerRecipe.currentUses,
                    villagerRecipe.maxUses, villagerRecipe.experienceReward, villagerRecipe.villagerXp,
                    villagerRecipe.priceMultiplier);
            villagerRecipe.ingredient.forEach(
                  itemStackSerializer -> recipe.addIngredient(itemStackSerializer.toBukkit()));
            bukkitRecipes.add(recipe);

        });
        entity.setRecipes(bukkitRecipes);
    }

    @Override
    protected String getPrefix() {
        return super.getPrefix() + MiscUtils.niceName(type);
    }

    @Override
    protected String getSuffix() {
        return super.getSuffix() + "(" + MiscUtils.niceName(profession) + ")";
    }

    public static class VillagerRecipe {

        Float priceMultiplier;
        ItemStackSerializer result;
        List<ItemStackSerializer> ingredient;
        Boolean experienceReward;
        Integer maxUses;
        Integer currentUses;
        Integer villagerXp;
    }

}
