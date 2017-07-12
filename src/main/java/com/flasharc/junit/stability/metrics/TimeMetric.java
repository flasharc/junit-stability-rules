package com.flasharc.junit.stability.metrics;

import java.util.Collections;
import java.util.List;

import com.flasharc.junit.stability.Metric;

public class TimeMetric implements Metric {
	
	long startTime;
	long endTime;

	@Override
	public void onRunStart() {
		startTime = System.nanoTime();
	}

	@Override
	public void onRunFinish() {
		endTime = System.nanoTime();
	}

	@Override
	public List<MetricResult> getResults() {
		long timeTaken = endTime - startTime;
		return Collections.<MetricResult>singletonList(new TimeMetricResult(timeTaken));
	}
	
	private static class TimeMetricResult implements MetricResult {
		
		private static final String METRIC_TYPE = "TimeTaken";
		private static final String METRIC_UNIT = "ns";
		
		private final double timeTakenNs;
		
		private TimeMetricResult(double timeTakenNs) {
			this.timeTakenNs = timeTakenNs;
		}

		@Override
		public String getMetricType() {
			return METRIC_TYPE;
		}
		
		@Override
		public String getMetricUnit() {
			return METRIC_UNIT;
		}

		@Override
		public double getMetricValue() {
			return timeTakenNs;
		}
	}
}
