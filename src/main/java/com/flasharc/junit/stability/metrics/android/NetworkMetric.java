package com.flasharc.junit.stability.metrics.android;

import java.util.Arrays;
import java.util.List;

import com.flasharc.junit.stability.Metric;
import android.net.TrafficStats;
import android.support.test.InstrumentationRegistry;

public class NetworkMetric implements Metric {
	private final int targetUid;
	
	private long startRxBytes;
	private long startTxBytes;
	private long endRxBytes;
	private long endTxBytes;
	
	public NetworkMetric() {
		targetUid = InstrumentationRegistry.getTargetContext().getApplicationInfo().uid;
	}
	
	@Override
	public void onRunStart() {
		startRxBytes = TrafficStats.getUidRxBytes(targetUid);
		startTxBytes = TrafficStats.getUidTxBytes(targetUid);
	}

	@Override
	public void onRunFinish() {
		endRxBytes = TrafficStats.getUidRxBytes(targetUid);
		endTxBytes = TrafficStats.getUidTxBytes(targetUid);
	}

	@Override
	public List<MetricResult> getResults() {
		return Arrays.<MetricResult>asList(
				new NetworkMetricResult("Data_Transmitted", (endTxBytes - startTxBytes)),
				new NetworkMetricResult("Data_Received", (endRxBytes - startRxBytes)));
	}

	private static class NetworkMetricResult implements MetricResult {
		
		private static final String METRIC_UNIT = "bytes";
		
		private final String metricType;
		private final double bytes;
		
		private NetworkMetricResult(String metricType, double bytes) {
			this.bytes = bytes;
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
			return bytes;
		}
	}
}
