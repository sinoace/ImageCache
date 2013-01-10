package com.example.threadQueue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;

import android.util.Log;

public class ThreadQueue {
	public static final int PRIORITY_BACKGROUND = 1;
	public static final int PRIORITY_LOW = 2;
	public static final int PRIORITY_MEDIUM = 3;
	public static final int PRIORITY_HIGH = 4;
	public static final int PRIORITY_TOP = 5;

	ThreadPoolExecutor threadPool;
	public boolean commandSortByLatest = true;
	private Vector<String> actionList = new Vector<String>();

	private ThreadQueue(boolean commandSortByLatest) {
		this.commandSortByLatest = commandSortByLatest;
		threadPool = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS,
				new PriorityBlockingQueue<Runnable>(),
				new ThreadPoolExecutor.DiscardOldestPolicy());

	}

	public static ThreadQueue newInstance(boolean commandSortByLatest) {
		return new ThreadQueue(commandSortByLatest);
	}

	public void put(String action, int priority, MockRunnable runnable) {
		if (!actionList.contains(action)) {
			Command task = new Command(action, priority, runnable);
			threadPool.execute(task);
			actionList.add(action);
		}
	}

	public void stopQueue() {
		removeTasks();
		threadPool.shutdown();
	}

	public void removeTasks() {
		BlockingQueue<Runnable> commands = threadPool.getQueue();
		synchronized (commands) {
			Iterator<Runnable> keys = commands.iterator();
			Runnable cmd;
			ArrayList<Runnable> buffer = new ArrayList<Runnable>();
			while (keys.hasNext()) {
				cmd = keys.next();
				buffer.add(cmd);
			}
			commands.removeAll(buffer);
		}

	}

	class Command implements Runnable, Comparable<Command> {

		String action;
		int priority = PRIORITY_MEDIUM;
		MockRunnable runnable;
		long time = System.currentTimeMillis();

		public Command(String action, int priority, MockRunnable runnable) {
			this.action = action;
			this.priority = priority;
			this.runnable = runnable;
		}

		public void run() {
			try {
				runnable.run();
				actionList.remove(action);
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public int compareTo(Command another) {
			int result = another.priority - priority;
			if (result == 0) {
				result = (int) (commandSortByLatest ? (another.time - time)
						: (time - another.time));
			}
			return result;
		}
	}

}
