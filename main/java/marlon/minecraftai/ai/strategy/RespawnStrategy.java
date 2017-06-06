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
package marlon.minecraftai.ai.strategy;

import marlon.minecraftai.ai.AIHelper;

/**
 * Clicks the respawn button (if the respawn screen is shown)
 * 
 * 
 *
 */
public class RespawnStrategy extends AIStrategy {
	@Override
	protected TickResult onGameTick(AIHelper helper) {
		if (!helper.isAlive()) {
			helper.respawn();
			return TickResult.TICK_HANDLED;
		} else {
			return TickResult.NO_MORE_WORK;
		}
	}
}
