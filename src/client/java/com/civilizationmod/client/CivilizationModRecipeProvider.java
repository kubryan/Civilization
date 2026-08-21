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

                    shaped(RecipeCategory.MISC, CivilizationItems.RESIDENTIAL_MARKER_1)
                        .pattern("b")
                        .pattern("i")
                        .define('b', ItemTags.BEDS)
                        .define('i', Items.IRON_INGOT)
                        .unlockedBy("has_bed", has(ItemTags.BEDS))
                        .save(output);

                    shaped(RecipeCategory.MISC, CivilizationItems.RESIDENTIAL_MARKER_2)
                        .pattern("bb")
                        .pattern("i ")
                        .define('b', ItemTags.BEDS)
                        .define('i', Items.IRON_INGOT)
                        .unlockedBy("has_bed", has(ItemTags.BEDS))
                        .save(output);

                    shaped(RecipeCategory.MISC, CivilizationItems.RESIDENTIAL_MARKER_4)
                        .pattern("bb")
                        .pattern("bb")
                        .pattern("i ")
                        .define('b', ItemTags.BEDS)
                        .define('i', Items.IRON_INGOT)
                        .unlockedBy("has_bed", has(ItemTags.BEDS))
                        .save(output);

                    shaped(RecipeCategory.MISC, CivilizationItems.RESIDENTIAL_MARKER_6)
                        .pattern("bbb")
                        .pattern("bbb")
                        .pattern("i  ")
                        .define('b', ItemTags.BEDS)
                        .define('i', Items.IRON_INGOT)
                        .unlockedBy("has_bed", has(ItemTags.BEDS))
                        .save(output);
            }
        };
    }

    @Override
    public String getName() {
        return "Civitas Building Marker Recipes";
    }
}
