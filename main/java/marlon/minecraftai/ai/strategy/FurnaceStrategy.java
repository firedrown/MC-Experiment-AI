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

import java.util.ArrayList;
import java.util.HashMap;

import marlon.minecraftai.ai.AIHelper;
import marlon.minecraftai.ai.task.CloseScreenTask;
import marlon.minecraftai.ai.scanner.BlockRangeFinder;
import marlon.minecraftai.ai.scanner.BlockRangeScanner;
import marlon.minecraftai.ai.scanner.FurnaceBlockHandler;
import marlon.minecraftai.ai.scanner.FurnaceBlockHandler.FurnaceData;
import marlon.minecraftai.ai.strategy.CraftStrategy.CraftingTableData;
import marlon.minecraftai.ai.task.AITask;
import marlon.minecraftai.ai.task.ConditionalWaitTask;
import marlon.minecraftai.ai.task.ConditionalWaitTask.WaitCondition;
import marlon.minecraftai.ai.task.RunOnceTask;
import marlon.minecraftai.ai.task.TaskOperations;
import marlon.minecraftai.ai.task.UseItemOnBlockAtTask;
import marlon.minecraftai.ai.task.WaitTask;
import marlon.minecraftai.ai.task.error.StringTaskError;
import marlon.minecraftai.ai.task.inventory.ItemWithSubtype;
import marlon.minecraftai.ai.task.inventory.MoveInInventoryTask;
import marlon.minecraftai.ai.task.inventory.TakeResultItem;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

public class FurnaceStrategy extends PathFinderStrategy {
	
	public static boolean active = true; //added Marlon.C	

	public static class FurnaceTaskList {
		private boolean putItem;
		private boolean putFuel;
		private boolean take;

		public FurnaceTaskList(boolean putItem, boolean putFuel, boolean take) {
			this.putItem = putItem;
			this.putFuel = putFuel;
			this.take = take;
		}

		@Override
		public String toString() {
			return "FurnaceTaskList [putItem=" + putItem + ", putFuel="
					+ putFuel + ", take=" + take + "]";
		}

		public boolean hasSomePutTasks(FurnaceData f, ItemStack s) {
			return couldPutFuel(f, s) || couldPutItem(f, s);
		}

		public boolean couldTake(FurnaceData f) {
			return f.couldTake();
		}

		public boolean couldPutItem(FurnaceData f, ItemStack s) {
			if (s == null) {
				return false;
			}
			return putItem && f.couldPut(new ItemWithSubtype(s));
		}

		public boolean couldPutFuel(FurnaceData f, ItemStack s) {
			if (s == null) {
				return false;
			}
			return putItem && f.couldPutFuel(new ItemWithSubtype(s));
		}
	}

	private static abstract class MoveToWhatever extends MoveInInventoryTask
			implements WaitCondition {
		protected int stackIndex;
		protected FurnaceTaskList list;
		protected FurnaceData f;
		private boolean moved;

		public MoveToWhatever(int stackIndex, FurnaceTaskList list,
				FurnaceData f) {
			super();
			this.stackIndex = stackIndex;
			this.list = list;
			this.f = f;
		}

		protected abstract boolean shouldMove(AIHelper h);

		@Override
		public boolean shouldWait() {
			return moved;
		}

		@Override
		protected int getMissingAmount(AIHelper h, int currentCount) {
			return 64;
		}

		@Override
		protected int getFromStack(AIHelper h) {
			return convertPlayerInventorySlot(stackIndex) + 3;
		}

		@Override
		public void runTick(AIHelper h, TaskOperations o) {
			f.update(h);
			if (shouldMove(h)) {
				moved = true;
			}
			super.runTick(h, o);
			f.update(h);
		}

		protected ItemStack getStack(AIHelper h) {
			return h.getMinecraft().thePlayer.inventory
					.getStackInSlot(stackIndex);
		}

		@Override
		public boolean isFinished(AIHelper h) {
			return !shouldMove(h) || super.isFinished(h);
		}
	}

	private static final class MoveToBurnable extends MoveToWhatever {

		public MoveToBurnable(int stackIndex, FurnaceTaskList list,
				FurnaceData f) {
			super(stackIndex, list, f);
		}

		@Override
		protected int getToStack(AIHelper h) {
			return 0;
		}

		@Override
		protected boolean shouldMove(AIHelper h) {
			return list.couldPutItem(f, getStack(h));
		}
	}

	private static final class MoveToFuel extends MoveToWhatever {
		public MoveToFuel(int stackIndex, FurnaceTaskList list, FurnaceData f) {
			super(stackIndex, list, f);
		}

		@Override
		protected int getToStack(AIHelper h) {
			return 1;
		}

		@Override
		protected boolean shouldMove(AIHelper h) {
			return list.couldPutFuel(f, getStack(h));
		}

	}

	public static class FurnacePathFinder extends BlockRangeFinder { //changed Marlon.C from private to public

		private static final class UpdateFurnaceDataTask extends RunOnceTask {
			private final FurnaceData f;

			private UpdateFurnaceDataTask(FurnaceData f) {
				this.f = f;
			}

			@Override
			protected void runOnce(AIHelper h, TaskOperations o) {
				GuiScreen gui = h.getMinecraft().currentScreen;
				if (!(gui instanceof GuiFurnace)) {
					o.desync(new StringTaskError("No furnace open"));
				} else {
					f.update((GuiFurnace) gui);
				}
			}
		}	

		private final class TakeFurnaceResult extends TakeResultItem {
			private final FurnaceData f;

			private TakeFurnaceResult(FurnaceData f) {
				super(GuiFurnace.class, 2);
				this.f = f;
			}

			@Override
			public boolean isFinished(AIHelper h) {
				return !list.couldTake(f) || super.isFinished(h);
			}

			@Override
			public void runTick(AIHelper h, TaskOperations o) {
				super.runTick(h, o);
				f.update(h);
			}
		}

		private FurnaceBlockHandler blockHandler;
		private final FurnaceTaskList list;

		public FurnacePathFinder(FurnaceTaskList list) {
			super();
			this.list = list;
		}	
		
		public HashMap<BlockPos, FurnaceData> getFurnace() //added Marlon.C
		{
			return this.blockHandler.getFurnace();
		}			

		@Override
		public BlockRangeScanner constructScanner(BlockPos playerPosition) { //changed Marlon.C from protected to public
			BlockRangeScanner scanner = super.constructScanner(playerPosition);
			blockHandler = new FurnaceBlockHandler();
			scanner.addHandler(blockHandler);
			return scanner;
		}

		@Override
		protected float rateDestination(int distance, int x, int y, int z) {
			ArrayList<FurnaceData> furnaces = blockHandler
					.getReachableForPos(new BlockPos(x, y, z));
			if (furnaces != null) {
				for (FurnaceData f : furnaces) {
					if (list.couldTake(f)) {
						return distance;
					}
					for (ItemStack s : helper.getMinecraft().thePlayer.inventory.mainInventory) {
						if (list.hasSomePutTasks(f, s)) {
							return distance;
						}
					}
				}
			}
			return -1;
		}

		@Override
		protected void addTasksForTarget(BlockPos currentPos) {
			ArrayList<FurnaceData> furnaces = blockHandler
					.getReachableForPos(currentPos);
			for (FurnaceData f : furnaces) {
				addFurnaceTasks(f);
			}

		}

		private void addFurnaceTasks(final FurnaceData f) {
			ArrayList<AITask> furnaceTasks = new ArrayList<AITask>();
			ItemStack[] mainInventory = helper.getMinecraft().thePlayer.inventory.mainInventory;
			for (int i = 0; i < mainInventory.length; i++) {
				ItemStack s = mainInventory[i];
				if (list.couldPutItem(f, s)) {
					furnaceTasks.add(new MoveToBurnable(i, list, f));
				}
				if (list.couldPutFuel(f, s)) {
					furnaceTasks.add(new MoveToFuel(i, list, f));
				}
			}
			
			if (list.couldTake(f)) {
				furnaceTasks.add(new TakeFurnaceResult(f));
			}

			if (!furnaceTasks.isEmpty()) {
				addTask(new UseItemOnBlockAtTask(f.getPos()) {
					@Override
					public boolean isFinished(AIHelper h) {
						return h.getMinecraft().currentScreen instanceof GuiFurnace;
					}
				});
				addTask(new WaitTask(5));
				addTask(new UpdateFurnaceDataTask(f));

				for (AITask t : furnaceTasks) {
					addTask(t);
					if (t instanceof WaitCondition) {
						addTask(new ConditionalWaitTask(5, (WaitCondition) t));
					}
				}				
					
				
				////////// Wait for raw material to smelt  //added Marlon.C
				if (list.couldTake(f)) {
					addTask(new WaitTask(400));
				}
				/////////				
				
				addTask(new UpdateFurnaceDataTask(f));	
				addTask(new CloseScreenTask());		
				
				active = false;	//added Marlon.C			
			}			
		}

	}

	public FurnaceStrategy(FurnaceTaskList list) {
		super(new FurnacePathFinder(list), null);
	}

	@Override
	public void searchTasks(AIHelper helper) {
		// If gui open, close it.
		if (helper.getMinecraft().currentScreen instanceof GuiFurnace) {
			addTask(new CloseScreenTask());
		}
		super.searchTasks(helper);
	}
	
	@Override
	public boolean checkShouldTakeOver(AIHelper helper) {	//added Marlon.C		
		if (active) {
			return true;
		}
		return false;
	}

	@Override
	public String getDescription(AIHelper helper) {
		return "Handle furnace.";
	}
}
