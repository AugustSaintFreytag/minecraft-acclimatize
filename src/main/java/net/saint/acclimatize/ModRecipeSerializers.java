package net.saint.acclimatize;

import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.saint.acclimatize.recipe.LeatherArmorWoolRecipe;

public class ModRecipeSerializers {

	public static final RecipeSerializer<LeatherArmorWoolRecipe> LEATHER_ARMOR_WOOL_RECIPE_SERIALIZER = RecipeSerializer.register(
			"crafting_special_leather_armor_wool", new SpecialRecipeSerializer<LeatherArmorWoolRecipe>(LeatherArmorWoolRecipe::new));

}
