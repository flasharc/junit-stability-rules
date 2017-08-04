package com.flasharc.junit.stability.metrics.android;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.flasharc.junit.stability.Metric;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Debug.MemoryInfo;
import android.support.test.InstrumentationRegistry;

public class MemoryMetric implements Metric {
	private final MemoryInfo memInfo;
	private final Timer timer;
	private TimerTask timerTask;
	
	private int maxMemoryUsage;
	private int avgMemoryUsage;
	private int count;
	
	public MemoryMetric() {
		ActivityManager manager = (ActivityManager) InstrumentationRegistry.getTargetContext().getSystemService(Context.ACTIVITY_SERVICE);
		int pid = -1;
		for (RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
			if (processInfo.processName.equals(InstrumentationRegistry.getTargetContext().getPackageName())) {
				pid = processInfo.pid;
				break;
			}
		}
		
		if (pid == -1) {
			throw new IllegalStateException("Cannot retrieve the PID of the target app");
		}
		memInfo = manager.getProcessMemoryInfo(new int[] {pid})[0];
		timer = new Timer(true);
	}

	@Override
	public void onRunStart() {
		timerTask = new TimerTask() {
			@Override
			public void run() {
				int memory = memInfo.getTotalPss();
				if (memory > maxMemoryUsage) {
					maxMemoryUsage = memory;
				}
				count++;
				avgMemoryUsage += (memory - avgMemoryUsage)/count;
			}
		};
		timer.scheduleAtFixedRate(timerTask, 0, 1000);
	}

	@Override
	public void onRunFinish() {
		timerTask.cancel();
	}

	@Override
	public List<MetricResult> getResults() {
		double maxMem = maxMemoryUsage;
		double avgMem = avgMemoryUsage;
		
		maxMemoryUsage = avgMemoryUsage = count = 0;
		
		return Arrays.<MetricResult>asList(
				new MemoryMetricResult("Max_Memory", maxMem),
				new MemoryMetricResult("Avg_Memory", avgMem));
	}

	private static class MemoryMetricResult implements MetricResult {
		
		private static final String METRIC_UNIT = "kB";
		
		private final String metricType;
		private final double kiloBytes;
		
		private MemoryMetricResult(String metricType, double kiloBytes) {
			this.kiloBytes = kiloBytes;
			this.metricType = metricType;
		}

		@Override
		public String getMetricType() {
			return metricType;
		}
		
		@Override
		public String getMetricUnit() {
			return METRIC_UNIT;
		}

		@Override
		public double getMetricValue() {
			return kiloBytes;
		}
	}
}
