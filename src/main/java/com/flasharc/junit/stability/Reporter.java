package com.flasharc.junit.stability;

import java.util.List;

public interface Reporter {
	public void startReport(String reportName, String environment) throws Exception;
	public void reportTest(String testName, List<Metric.MetricResult> metrics) throws Exception;
}
