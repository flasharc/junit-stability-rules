package com.flasharc.junit.stability;

import java.util.List;

public interface Metric {
	void onRunStart();
	void onRunFinish();
	List<MetricResult> getResults();
	
	interface MetricResult {
		String getMetricType();
		String getMetricUnit();
		double getMetricValue();
	}
}
