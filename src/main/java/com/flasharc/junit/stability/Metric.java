package com.flasharc.junit.stability;

import java.util.List;

public interface Metric {
	void onRunStart() throws Exception;
	void onRunFinish() throws Exception;
	List<MetricResult> getResults() throws Exception;
	
	interface MetricResult {
		String getMetricType();
		String getMetricUnit();
		double getMetricValue();
	}
}
