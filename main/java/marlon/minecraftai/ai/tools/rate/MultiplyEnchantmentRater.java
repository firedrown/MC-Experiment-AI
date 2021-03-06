package marlon.minecraftai.ai.tools.rate;

import marlon.minecraftai.ai.path.world.BlockFloatMap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;

public class MultiplyEnchantmentRater extends EnchantmentRater {
	public MultiplyEnchantmentRater(int enchantmentId, String name,
			BlockFloatMap values) {
		super(enchantmentId, name, values);
	}

	@Override
	protected double getPow(ItemStack item, int forBlockAndMeta) {
		return Math.max(1,
				EnchantmentHelper.getEnchantmentLevel(enchantmentId, item));
	}

	@Override
	public String toString() {
		return "MultiplyEnchantmentRater [enchantmentId=" + enchantmentId
				+ ", name=" + name + "]";
	}
}