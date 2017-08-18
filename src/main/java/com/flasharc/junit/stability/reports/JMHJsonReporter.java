package com.flasharc.junit.stability.reports;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.flasharc.junit.stability.Metric.MetricResult;
import com.flasharc.junit.stability.Reporter;
import com.google.gson.Gson;

public class JMHJsonReporter implements Reporter {
	
	private final File reportFile;
	
	public JMHJsonReporter(File reportFile) throws IOException {
		this.reportFile = reportFile;
	}
	
	private void writeToReport(String content) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(reportFile, "rwd");
		try {
			long len = raf.length();
			if (len == 0) {
				content = "[" + content + "]";
			} else {
				raf.seek(len - 1);
				content = "," + content + "]";
			}
			raf.writeBytes(content);
		} finally {
			raf.close();
		}
	}
	
	@Override
	public void startReport(String reportName, String environment) throws Exception {
		// Do nothing.
	}

	@Override
	public void reportTest(String testName, List<MetricResult> metrics) throws Exception {
		if (metrics != null && metrics.size() > 0) {
			MetricResult primaryMetric = metrics.get(0);
			ReportPOJO report = new ReportPOJO();
			report.benchmark = getBenchmark(testName);
			report.primaryMetric = new ReportPOJO.Metric();
			report.primaryMetric.score = primaryMetric.getMetricValue();
			report.primaryMetric.scoreUnit = primaryMetric.getMetricUnit();
			
			for (int i = 1; i < metrics.size(); i++) {
				MetricResult secondaryMetric = metrics.get(i);
				ReportPOJO.Metric pojoMetric = new ReportPOJO.Metric();
				pojoMetric.score = secondaryMetric.getMetricValue();
				pojoMetric.scoreUnit = secondaryMetric.getMetricUnit();
				report.secondaryMetrics.put(secondaryMetric.getMetricType(), pojoMetric);
			}
			
			Gson gson = new Gson();
			writeToReport(gson.toJson(report));
		}
	}
	
	private String getBenchmark(String testName) {
		try {
			Pattern pattern = Pattern.compile("(.*)\\((.*)\\)");
			Matcher matcher = pattern.matcher(testName);
			matcher.find();
			testName = matcher.group(2).trim() + "." + matcher.group(1).trim();
		} catch (Exception e) {
			// catch all exception.
		}
		return testName;
	}

	@SuppressWarnings("unused")
	private static class ReportPOJO {
		public String benchmark;
		public String mode = "count";
		public Metric primaryMetric;
		public Map<String, Metric> secondaryMetrics = new HashMap<>();
				
		public static class Metric {
			public double score;
			public List<Double> scoreConfidence = new ArrayList<>();
			public String scoreUnit;
		}
	}
}
