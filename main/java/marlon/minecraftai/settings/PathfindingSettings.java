package marlon.minecraftai.settings;

import marlon.minecraftai.ai.path.world.BlockSet;
import marlon.minecraftai.ai.path.world.BlockSets;
import net.minecraft.init.Blocks;

public class PathfindingSettings {
	private static final BlockSet defaultUpwardsBlocks = new BlockSet(
			Blocks.dirt, Blocks.stone, Blocks.cobblestone, Blocks.sand,
			Blocks.netherrack);
	private static final BlockSet allowedForUpwards = BlockSets.SAFE_GROUND
			.unionWith(BlockSets.FEET_CAN_WALK_THROUGH);
	private static final BlockSet destructableBlocks = BlockSets.SAFE_AFTER_DESTRUCTION
			.unionWith(BlockSets.SAFE_CEILING).unionWith(BlockSets.FALLING)
			.intersectWith(BlockSets.INDESTRUCTABLE.invert());

	private PathfindingSetting destructive = new PathfindingSetting(
			BlockSets.SAFE_GROUND, allowedForUpwards, destructableBlocks,
			destructableBlocks, defaultUpwardsBlocks);
	private PathfindingSetting nonDestructiveLumberjack = new PathfindingSetting(
			BlockSets.SAFE_GROUND, BlockSets.SAFE_GROUND,
			BlockSets.FEET_CAN_WALK_THROUGH.unionWith(BlockSets.TREE_BLOCKS),
			BlockSets.HEAD_CAN_WALK_TRHOUGH.unionWith(BlockSets.TREE_BLOCKS),
			defaultUpwardsBlocks);
	private PathfindingSetting nonDestructive = new PathfindingSetting(
			BlockSets.SAFE_GROUND, BlockSets.SAFE_GROUND,
			BlockSets.FEET_CAN_WALK_THROUGH, BlockSets.HEAD_CAN_WALK_TRHOUGH,
			defaultUpwardsBlocks);
	private PathfindingSetting planting = new PathfindingSetting(
			BlockSets.SAFE_GROUND, BlockSets.SAFE_GROUND,
			BlockSets.FEET_CAN_WALK_THROUGH, BlockSets.HEAD_CAN_WALK_TRHOUGH,
			defaultUpwardsBlocks);
	private PathfindingSetting construction = new PathfindingSetting(
			BlockSets.SAFE_GROUND, BlockSets.SAFE_GROUND,
			BlockSets.FEET_CAN_WALK_THROUGH, BlockSets.HEAD_CAN_WALK_TRHOUGH,
			defaultUpwardsBlocks);
	private PathfindingSetting walking = new PathfindingSetting(
			BlockSets.SAFE_GROUND, BlockSets.SAFE_GROUND,
			BlockSets.FEET_CAN_WALK_THROUGH, BlockSets.HEAD_CAN_WALK_TRHOUGH,
			defaultUpwardsBlocks);

	public PathfindingSetting getDestructivePathfinder() {
		return destructive;
	}

	public PathfindingSetting getConstructionPathfinder() {
		return construction;
	}

	public PathfindingSetting getNonDestructiveLumberjack() {
		return nonDestructiveLumberjack;
	}

	public PathfindingSetting getPlanting() {
		return planting;
	}

	public PathfindingSetting getWalking() {
		return walking;
	}

	public PathfindingSetting getNonDestructive() {
		return nonDestructive;
	}
}
