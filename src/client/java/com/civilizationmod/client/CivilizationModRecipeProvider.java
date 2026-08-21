package com.civilizationmod.client;

import com.civilizationmod.CivilizationItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

/** Generates player-facing crafting recipes for Civitas building markers. */
public final class CivilizationModRecipeProvider extends FabricRecipeProvider {
    public CivilizationModRecipeProvider(
            FabricPackOutput output,
            CompletableFuture<HolderLookup.Provider> registriesFuture
    ) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(
            HolderLookup.Provider registryLookup,
            RecipeOutput exporter
    ) {
        return new RecipeProvider(registryLookup, exporter) {
            @Override
            public void buildRecipes() {
                    shaped(RecipeCategory.MISC, CivilizationItems.WAREHOUSE_MARKER)
                        .pattern("ccc")
                        .pattern("cic")
                        .pattern("ccc")
                        .define('c', Items.CHEST)
                        .define('i', Items.IRON_INGOT)
                        .unlockedBy(getHasName(Items.CHEST), has(Items.CHEST))
                        .save(output);

                    shaped(RecipeCategory.MISC, CivilizationItems.CIVITAS_BINDING_DEVICE)
                        .pattern("sss")
                        .pattern("sis")
                        .pattern("sss")
                        .define('s', Items.STICK)
                        .define('i', Items.IRON_INGOT)
                        .unlockedBy(getHasName(Items.STICK), has(Items.STICK))
                        .save(output);

                    shaped(RecipeCategory.MISC, CivilizationItems.TOWN_HALL_MARKER)
                        .pattern("esp")
                        .pattern("ah ")
                        .define('e', Items.EMERALD)
                        .define('s', Items.IRON_SWORD)
                        .define('p', Items.IRON_PICKAXE)
                        .define('a', Items.IRON_AXE)
                        .define('h', Items.IRON_SHOVEL)
                        .unlockedBy(getHasName(Items.EMERALD), has(Items.EMERALD))
                        .save(output);

                    shaped(RecipeCategory.MISC, CivilizationItems.RESIDENTIAL_MARKER_1)
                        .pattern("www")
                        .pattern("wiw")
                        .pattern("www")
                        .define('w', ItemTags.WOOL)
                        .define('i', Items.IRON_INGOT)
                        .unlockedBy("has_wool", has(ItemTags.WOOL))
                        .save(output);

                    shapeless(RecipeCategory.MISC, CivilizationItems.RESIDENTIAL_MARKER_2)
                        .requires(CivilizationItems.RESIDENTIAL_MARKER_1, 2)
                        .unlockedBy(getHasName(CivilizationItems.RESIDENTIAL_MARKER_1), has(CivilizationItems.RESIDENTIAL_MARKER_1))
                        .save(output);

                    shapeless(RecipeCategory.MISC, CivilizationItems.RESIDENTIAL_MARKER_4)
                        .requires(CivilizationItems.RESIDENTIAL_MARKER_1, 4)
                        .unlockedBy(getHasName(CivilizationItems.RESIDENTIAL_MARKER_1), has(CivilizationItems.RESIDENTIAL_MARKER_1))
                        .save(output);

                    shapeless(RecipeCategory.MISC, CivilizationItems.RESIDENTIAL_MARKER_6)
                        .requires(CivilizationItems.RESIDENTIAL_MARKER_1, 6)
                        .unlockedBy(getHasName(CivilizationItems.RESIDENTIAL_MARKER_1), has(CivilizationItems.RESIDENTIAL_MARKER_1))
                        .save(output);
            }
        };
    }

    @Override
    public String getName() {
        return "Civitas Building Marker Recipes";
    }
}
