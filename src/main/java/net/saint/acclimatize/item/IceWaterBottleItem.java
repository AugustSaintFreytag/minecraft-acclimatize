package net.saint.acclimatize.item;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.saint.acclimatize.Mod;
import net.saint.acclimatize.ModStatusEffects;

public class IceWaterBottleItem extends Item {

	public IceWaterBottleItem(Settings settings) {
		super(settings);
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.DRINK;
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		return 32;
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		super.finishUsing(stack, world, user);

		if (!(user instanceof PlayerEntity player)) {
			return ItemStack.EMPTY;
		}

		if (user instanceof ServerPlayerEntity serverPlayer) {
			Criteria.CONSUME_ITEM.trigger(serverPlayer, stack);
			serverPlayer.incrementStat(Stats.USED.getOrCreateStat(this));

			var serverWorld = (ServerWorld) world;
			serverWorld.playSound(null, serverPlayer.getBlockPos(), SoundEvents.ENTITY_WANDERING_TRADER_DRINK_POTION, SoundCategory.PLAYERS,
					1.0F, 1.0F);

			serverPlayer.addStatusEffect(
					new StatusEffectInstance(ModStatusEffects.HEAT_DISSIPATION, Mod.CONFIG.iceWaterEffectDuration, 0, false, true));
		}

		if (!player.isCreative()) {
			stack.decrement(1);
			var emptyBottleStack = new ItemStack(Items.GLASS_BOTTLE);

			if (!player.getInventory().insertStack(emptyBottleStack)) {
				player.dropItem(emptyBottleStack, false);
			}
		}

		return stack;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		return ItemUsage.consumeHeldItem(world, user, hand);
	}

}
