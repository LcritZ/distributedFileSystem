package com.yu.test;

import java.util.Timer;
import java.util.TimerTask;

public class TestTimer {
	Timer timer;

	public TestTimer(int seconds) {
		timer = new Timer();
		timer.schedule(new TimerTestTask(), seconds * 1000);
	}

	class TimerTestTask extends TimerTask {
		public void run() {
			System.out.println("sss");
			timer.cancel();
			timer.schedule(new TimerTestTask(), 2 * 1000);
			timer.cancel();
		}
	}

	public static void main(String args[]) {
		System.out.println("Prepare to schedule task.");
		new TestTimer(5);
		System.out.println("Task scheduled.");
	}
}