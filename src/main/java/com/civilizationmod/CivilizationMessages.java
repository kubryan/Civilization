package com.civilizationmod;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Builds every player-facing CivilizationMod message with the project brand.
 *
 * <p>The locale-specific prefix is gold, while the separator and translated
 * message are reset to the normal chat style. There is intentionally no
 * trailing brand suffix.</p>
 */
public final class CivilizationMessages {
	private static final String PREFIX_KEY = "civilizationmod.message.prefix";

	private CivilizationMessages() {
	}

	public static Component translatable(String messageKey, Object... arguments) {
		MutableComponent prefix = Component.literal("|")
				.withStyle(ChatFormatting.GOLD)
				.append(Component.translatable(PREFIX_KEY).withStyle(ChatFormatting.GOLD))
				.append(Component.literal("|").withStyle(ChatFormatting.GOLD));
		return prefix
				.append(Component.literal(" : ").withStyle(ChatFormatting.RESET))
				.append(Component.translatable(messageKey, arguments).withStyle(ChatFormatting.RESET));
	}
}
