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
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

/**
 * An {@link AIStrategy} tells the bot what to do next. It should recover from
 * any state that might happen while the bot executes.
 * 
 * 
 * @see RunOnceStrategy
 * @see StackStrategy
 * 
 */
public abstract class AIStrategy {
	public enum TickResult {
		/**
		 * To do this tick again, either calling the same strategy again or
		 * deactivating it.
		 */
		TICK_AGAIN,
		/**
		 * The tick is handled, the player was moved and did some work.
		 */
		TICK_HANDLED,
		/**
		 * This stategy did not do anything and has nothing more to do
		 * currently. Deactivate it.
		 */
		NO_MORE_WORK,
		ABORT
	};

	private boolean active;

	/**
	 * (de) Activates the strategy. Always called in a game tick. This can be
	 * called any time, especially deactivate.
	 * 
	 * @param active
	 */
	public final void setActive(boolean active, AIHelper helper) {
		this.active = active;
		if (active) {
			onActivate(helper);
		} else {
			onDeactivate(helper);
		}
	}

	public final boolean isActive() {
		return active;
	}

	protected void onDeactivate(AIHelper helper) {
	}

	protected void onActivate(AIHelper helper) {
	}

	/**
	 * Tests if this strategy needs to take over immediately. Chances are good
	 * that you are standing on a cliff / ... if you get activated.
	 * 
	 * @return <code>true</code> if {@link #checkShouldTakeOver(AIHelper)} needs
	 *         to be called every game tick, <code>false</code> to only call on
	 *         good/safe times.
	 */
	public boolean takesOverAnyTime() {
		return false;
	}

	/**
	 * Checks if this should take over.
	 * 
	 * @param helper
	 *            TODO
	 * 
	 * @return
	 */
	public boolean checkShouldTakeOver(AIHelper helper) {
		if (active) {
			throw new IllegalStateException();
		}
		return true;
	}

	/**
	 * Runs a game tick.
	 * 
	 * @param helper
	 *            The helper.
	 * @return
	 */
	public final TickResult gameTick(AIHelper helper) {
		if (!active) {
			throw new IllegalStateException();
		}

		return onGameTick(helper);
	}

	protected abstract TickResult onGameTick(AIHelper helper);

	/**
	 * 
	 * @param helper
	 *            TODO
	 * @return A String to display in the top right hand corner of the screen.
	 */
	public String getDescription(AIHelper helper) {
		return "No description so far... " + getClass().getSimpleName();
	}

	/**
	 * A flag indicating that this strategy has failed. Most strategies cannot
	 * fail, they just do as good as they can and then finish. This flag should
	 * only be set if the strategy has no more work.
	 * 
	 * @return
	 */
	public boolean hasFailed() {
		return false;
	}

	public void drawMarkers(RenderTickEvent event, AIHelper helper) {
		
	}

}
