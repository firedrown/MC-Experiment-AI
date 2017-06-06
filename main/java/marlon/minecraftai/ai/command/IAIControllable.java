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
package marlon.minecraftai.ai.command;

import marlon.minecraftai.ai.AIController;
import marlon.minecraftai.ai.AIHelper;
import marlon.minecraftai.ai.strategy.AIStrategy;
import net.minecraft.client.Minecraft;

/**
 * This is something that could be controlled by the bot.
 * 
 * @see AIController
 * 
 *
 */
public interface IAIControllable {

	/**
	 * Get the current minecraft instance.
	 * 
	 * @return
	 */
	Minecraft getMinecraft();

	AIHelper getAiHelper();

	/**
	 * Request to pass control to that strategy.
	 * 
	 * @param strategy
	 *            The new strategy.
	 */
	void requestUseStrategy(AIStrategy strategy);

}
