package com.flasharc.junit.stability.metrics;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.flasharc.junit.stability.Metric;

public class MemoryMetric implements Metric {
	private final Timer timer;
	private TimerTask timerTask;
	Runtime runtime = Runtime.getRuntime();
	
	private long maxMemoryUsage;
	private long avgMemoryUsage;
	private int count;
	
	public MemoryMetric() {
		timer = new Timer(true);
	}

	@Override
	public void onRunStart() {
		timerTask = new TimerTask() {
			@Override
			public void run() {
				long memory = runtime.totalMemory();
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
	public List<MetricResult> getResults(int loops) {
		double maxMem = maxMemoryUsage;
		double avgMem = avgMemoryUsage;
		
		maxMemoryUsage = avgMemoryUsage = count = 0;
		
		return Arrays.<MetricResult>asList(
				new MemoryMetricResult("Max_Memory", maxMem/1024),
				new MemoryMetricResult("Avg_Memory", avgMem/1024));
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
