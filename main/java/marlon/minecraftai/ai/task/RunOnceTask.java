package marlon.minecraftai.ai.task;

import marlon.minecraftai.ai.AIHelper;

public abstract class RunOnceTask extends AITask {

	private boolean run;

	@Override
	public boolean isFinished(AIHelper h) {
		return run;
	}

	@Override
	public void runTick(AIHelper h, TaskOperations o) {
		runOnce(h, o);
		run = true;
	}

	protected abstract void runOnce(AIHelper h, TaskOperations o);

}
