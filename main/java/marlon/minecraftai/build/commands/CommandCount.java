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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map.Entry;

import marlon.minecraftai.ai.AIHelper;
import marlon.minecraftai.ai.HumanReadableItemFilter;
import marlon.minecraftai.ai.ItemFilter;
import marlon.minecraftai.ai.command.AIChatController;
import marlon.minecraftai.ai.command.AICommand;
import marlon.minecraftai.ai.command.AICommandInvocation;
import marlon.minecraftai.ai.command.AICommandParameter;
import marlon.minecraftai.ai.command.ParameterType;
import marlon.minecraftai.ai.strategy.AIStrategy;
import marlon.minecraftai.build.blockbuild.BuildTask;

import com.google.common.base.Function;

@AICommand(helpText = "Count all stuff required for building.", name = "minebuild")
public class CommandCount {

	@AICommandInvocation()
	public static AIStrategy run(
			AIHelper helper,
			@AICommandParameter(type = ParameterType.FIXED, fixedName = "count", description = "") String nameArg2) {

		final Hashtable<ItemFilter, Integer> counts = new Hashtable<ItemFilter, Integer>();
		for (final BuildTask s : helper.buildManager.getScheduled()) {
			final ItemFilter required = s.getRequiredItem();
			final Integer count = counts.get(required);
			counts.put(required, count == null ? 1 : count + 1);
		}

		final ArrayList<Entry<ItemFilter, Integer>> list = new ArrayList<Entry<ItemFilter, Integer>>(
				counts.entrySet());
		Collections.sort(list, new Comparator<Entry<ItemFilter, Integer>>() {
			@Override
			public int compare(Entry<ItemFilter, Integer> o1,
					Entry<ItemFilter, Integer> o2) {
				return -o1.getValue().compareTo(o2.getValue());
			}
		});

		AIChatController.addToChatPaged("Required items", 1, list,
				new Function<Entry<ItemFilter, Integer>, String>() {
					@Override
					public String apply(Entry<ItemFilter, Integer> item) {
						final ItemFilter filter = item.getKey();
						final String key = niceFilterName(filter);
						return key + ": " + item.getValue();
					}

				});
		return null;
	}

	public static String niceFilterName(ItemFilter filter) {
		return filter instanceof HumanReadableItemFilter ? ((HumanReadableItemFilter) filter)
				.getDescription() : filter.toString();
	}
}
