package choonster.testmod3.item;

import choonster.testmod3.api.capability.maxhealth.IMaxHealth;
import choonster.testmod3.capability.maxhealth.MaxHealthCapability;
import choonster.testmod3.text.TestMod3Lang;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * An item that tells the player the current max health and the bonus max health provided by the entity's {@link IMaxHealth} when right clicked on an entity.
 *
 * @author Choonster
 */
public class MaxHealthGetterItem extends Item {
	public MaxHealthGetterItem(final Item.Properties properties) {
		super(properties);
	}

	@Override
	public ActionResultType itemInteractionForEntity(final ItemStack stack, final PlayerEntity player, final LivingEntity target, final Hand hand) {
		if (!player.world.isRemote) {
			MaxHealthCapability.getMaxHealth(target).ifPresent(maxHealth -> {
				player.sendMessage(new TranslationTextComponent(TestMod3Lang.MESSAGE_MAX_HEALTH_GET.getTranslationKey(), target.getDisplayName(), target.getMaxHealth(), maxHealth.getBonusMaxHealth()), Util.DUMMY_UUID);
			});
		}

		return ActionResultType.SUCCESS;
	}
}
