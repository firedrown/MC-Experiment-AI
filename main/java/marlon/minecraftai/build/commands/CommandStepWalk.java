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
import marlon.minecraftai.ai.strategy.AIStrategy;
import marlon.minecraftai.ai.strategy.PathFinderStrategy;
import marlon.minecraftai.build.ForBuildPathFinder;
import marlon.minecraftai.build.blockbuild.BuildTask;
import net.minecraft.util.BlockPos;

@AICommand(helpText = "Go to next building site.", name = "minebuild")
public class CommandStepWalk {

	@AICommandInvocation(safeRule = SafeStrategyRule.DEFEND)
	public static AIStrategy run(
			AIHelper helper,
			@AICommandParameter(type = ParameterType.FIXED, fixedName = "step", description = "") String nameArg2,
			@AICommandParameter(type = ParameterType.FIXED, fixedName = "walk", description = "") String nameArg3) {

		final BuildTask task = helper.buildManager.peekNextTask();
		if (task == null) {
			AIChatController.addChatLine("No more build tasks.");
			return null;
		} else {
			final ForBuildPathFinder pf = new ForBuildPathFinder(task);
			return new PathFinderStrategy(pf, "Going to building site.") {
				@Override
				public void searchTasks(AIHelper helper) {
					final BlockPos atTarget = CommandBuild
							.isAroundSite(helper, task);
					if (atTarget == null) {
						super.searchTasks(helper);
					}
				}
			};
		}
	}
}
