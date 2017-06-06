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
package marlon.minecraftai.build.commands;

import marlon.minecraftai.ai.AIHelper;
import marlon.minecraftai.ai.command.AIChatController;
import marlon.minecraftai.ai.command.AICommand;
import marlon.minecraftai.ai.command.AICommandInvocation;
import marlon.minecraftai.ai.command.AICommandParameter;
import marlon.minecraftai.ai.command.ParameterType;
import marlon.minecraftai.ai.command.SafeStrategyRule;
import marlon.minecraftai.ai.path.world.BlockSets;
import marlon.minecraftai.ai.strategy.AIStrategy;
import marlon.minecraftai.ai.strategy.TaskStrategy;
import marlon.minecraftai.ai.task.AITask;
import marlon.minecraftai.build.blockbuild.BuildTask;
import net.minecraft.util.BlockPos;

@AICommand(helpText = "Place the next block.\n Use the walk command before this command or walk there yourself.", name = "minebuild")
public class CommandStepPlace {

	@AICommandInvocation(safeRule = SafeStrategyRule.DEFEND)
	public static AIStrategy run(
			AIHelper helper,
			@AICommandParameter(type = ParameterType.FIXED, fixedName = "step", description = "") String nameArg2,
			@AICommandParameter(type = ParameterType.FIXED, fixedName = "place", description = "") String nameArg3) {
		return new TaskStrategy() {

			@Override
			protected void searchTasks(AIHelper helper) {
				final BuildTask task = helper.buildManager.peekNextTask();
				if (task == null) {
					AIChatController.addChatLine("No more build tasks.");
				} else {
					addStep(helper, task);
				}
			}

			private void addStep(AIHelper helper, final BuildTask task) {
				final BlockPos forPosition = task.getForPosition();
				final BlockPos fromPos = getFromPos(helper, task, forPosition);
				if (fromPos == null) {
					AIChatController.addChatLine("Not at starting position.");
				} else if (BlockSets.AIR.isAt(helper.getWorld(),
						task.getForPosition())) {
					final AITask t = task.getPlaceBlockTask(fromPos);
					if (t != null) {
						addTask(t);
					}
				} else {
					AIChatController
							.addChatLine("Could not place the block: There is already something!");
				}
			}

			@Override
			public String getDescription(AIHelper helper) {
				return "Place the block.";
			}
		};
	}

	private static BlockPos getFromPos(AIHelper helper, final BuildTask task,
			BlockPos forPosition) {
		BlockPos fromPos = null;
		for (final BlockPos p : task.getStandablePlaces()) {
			if (helper.isStandingOn(p.getX() + forPosition.getX(), p.getY() + forPosition.getY(),
					p.getZ() + forPosition.getZ())) {
				fromPos = p;
			}
		}
		return fromPos;
	}
}
