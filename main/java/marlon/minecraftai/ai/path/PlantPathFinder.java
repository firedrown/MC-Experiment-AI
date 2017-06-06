/*******************************************************************************
 * This file is part of Minebot.
 *
 * Minebot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Minebot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Minebot.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package marlon.minecraftai.ai.path;

import marlon.minecraftai.ai.AIHelper;
import marlon.minecraftai.ai.ClassItemFilter;
import marlon.minecraftai.ai.ItemFilter;
import marlon.minecraftai.ai.path.world.BlockMetaSet;
import marlon.minecraftai.ai.path.world.BlockSet;
import marlon.minecraftai.ai.path.world.BlockSets;
import marlon.minecraftai.ai.path.world.WorldData;
import marlon.minecraftai.ai.path.world.WorldWithDelta;
import marlon.minecraftai.ai.task.UseItemOnBlockAtTask;
import marlon.minecraftai.ai.task.place.DestroyBlockTask;
import marlon.minecraftai.ai.task.place.PlaceBlockAtFloorTask;
import marlon.minecraftai.settings.MinebotSettingsRoot;
import marlon.minecraftai.settings.PathfindingSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

public class PlantPathFinder extends MovePathFinder {
	private static final BlockSet FARMLAND = new BlockSet(Blocks.farmland);
	private static final BlockSet NETHERWART_FARMLAND = new BlockSet(
			Blocks.soul_sand);

	private static final BlockSet FARMLANDABLE = new BlockSet(Blocks.dirt,
			Blocks.grass);

	private final class PlaceSeedsTask extends PlaceBlockAtFloorTask {
		private final SeedFilter seedFilter;

		private PlaceSeedsTask(BlockPos pos, SeedFilter filter) {
			super(pos, filter);
			seedFilter = filter;
		}

		@Override
		protected boolean isAtDesiredHeight(AIHelper h) {
			return true;
		}

		@Override
		public boolean applyToDelta(WorldWithDelta world) {
			Item anyPlaceItem = seedFilter.type.items[0];
			Block b = Block.getBlockFromItem(anyPlaceItem);
			world.setBlock(pos, Block.getIdFromBlock(b), 0);
			return true;
		}
	}

	public enum PlantType {
		NORMAL(FARMLAND, Items.wheat_seeds, Items.carrot, Items.potato), WHEAT(
				FARMLAND, Items.wheat_seeds), CARROT(FARMLAND, Items.carrot), POTATO(
				FARMLAND, Items.potato), NETHERWART(NETHERWART_FARMLAND,
				Items.nether_wart);

		public final BlockSet farmland;

		private final Item[] items;

		private PlantType(BlockSet farmland, Item... items) {
			this.farmland = farmland;
			this.items = items;
		}

		public boolean canPlantItem(Item item) {
			for (final Item i : items) {
				if (item == i) {
					return true;
				}
			}
			return false;
		}
	}

	private final class SeedFilter implements ItemFilter {
		private final PlantType type;

		public SeedFilter(PlantType type) {
			super();
			this.type = type;
		}

		@Override
		public boolean matches(ItemStack itemStack) {
			return itemStack != null && type.canPlantItem(itemStack.getItem());
		}
	}

	private static class UseHoeTask extends UseItemOnBlockAtTask {

		public UseHoeTask(BlockPos farmlandPos) {
			super(new ClassItemFilter(ItemHoe.class), farmlandPos);
		}

		@Override
		public boolean applyToDelta(WorldWithDelta world) {
			if (FARMLANDABLE.isAt(world, getPos())) {
				world.setBlock(getPos(), Blocks.farmland);
			}
			return true;
		}
	}

	private final PlantType type;

	public PlantPathFinder(PlantType type) {
		this.type = type;
	}

	@Override
	protected PathfindingSetting loadSettings(MinebotSettingsRoot settingsRoot) {
		return settingsRoot.getPathfinding().getPlanting();
	}

	@Override
	protected float rateDestination(int distance, int x, int y, int z) {
		if (isGrown(world, x, y, z)) {
			return distance + 1;
		} else if (BlockSets.AIR.isAt(world, x, y, z)
				&& type.farmland.isAt(world, x, y - 1, z)
				&& helper.canSelectItem(new SeedFilter(type))) {
			return distance + 1;
		} else if (type.farmland == FARMLAND
				&& BlockSets.AIR.isAt(world, x, y, z)
				&& FARMLANDABLE.contains(helper.getBlock(x, y - 1, z))
				&& helper.canSelectItem(new SeedFilter(type))
				&& helper.canSelectItem(new ClassItemFilter(ItemHoe.class))) {
			return distance + 10;
		} else {
			return -1;
		}
	}

	private boolean isGrown(WorldData world, int x, int y, int z) {
		final int blockWithMeta = world.getBlockIdWithMeta(x, y, z);
		if (Block.getBlockById(blockWithMeta >> 4) instanceof BlockCrops) {
			final int metadata = blockWithMeta & 0xf;
			return metadata >= 7;
		} else if (new BlockMetaSet(Blocks.nether_wart, 3).isAt(world, x, y, z)) {
			return true;
		}
		return false;
	}

	@Override
	protected void addTasksForTarget(BlockPos currentPos) {
		if (BlockSets.AIR.isAt(world, currentPos)) {
			BlockPos farmlandPos = currentPos.add(0, -1, 0);
			if (!type.farmland.isAt(world, farmlandPos)) {
				addTask(new UseHoeTask(farmlandPos));
			}
			addTask(new PlaceSeedsTask(currentPos, new SeedFilter(type)));
		} else {
			addTask(new DestroyBlockTask(currentPos));
		}
	}
}
