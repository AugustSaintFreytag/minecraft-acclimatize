package net.saint.acclimatize;

import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.saint.acclimatize.recipe.LeatherArmorWoolRecipe;

public class ModRecipeSerializers {

	public static final RecipeSerializer<LeatherArmorWoolRecipe> LEATHER_ARMOR_WOOL_RECIPE_SERIALIZER = new SpecialRecipeSerializer<LeatherArmorWoolRecipe>(
			LeatherArmorWoolRecipe::new);

	public static void registerRecipeSerializers() {
		Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(Mod.MOD_ID, "crafting_special_leather_armor_wool"),
				LEATHER_ARMOR_WOOL_RECIPE_SERIALIZER);
	}

}
