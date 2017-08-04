package com.flasharc.junit.stability;

import java.util.ArrayList;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class MetricsCollector implements TestRule {
	private final Metric[] metrics;
	private final Reporter reporter;

	public MetricsCollector(Reporter reporter, Metric... metrics) {
		if (metrics == null) {
			throw new NullPointerException("No metrics defined");
		}
		this.metrics = metrics;
		this.reporter = reporter;
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new MetricsCollectedStatement(base, description.getDisplayName());
	}
	
	private class MetricsCollectedStatement extends Statement {
		private final Statement base;
		private final String testName;

		private MetricsCollectedStatement(Statement base, String testName) {
			this.base = base;
			this.testName = testName;
		}

		@Override
		public void evaluate() throws Throwable {
			for (Metric metric : metrics) {
				try {
					metric.onRunStart();
				} catch (Throwable e) {
					// Ignore.
				}
			}
			try {
				base.evaluate();
			} finally {
				for (int i = metrics.length; i > 0; i--) { // This is to iterate in the reverse order.
					try {
						metrics[i - 1].onRunFinish();
					} catch (Throwable e) {
						// Ignore.
					}
				}
				ArrayList<Metric.MetricResult> results = new ArrayList<>();
				for (Metric metric : metrics) {
					try {
						results.addAll(metric.getResults());
					} catch (Throwable e) {
						// Ignore.
					}
				}
				try {
					reporter.reportTest(testName, results);
				} catch (Throwable e) {
					// Ignore.
				}
			}
		}
	}
}
