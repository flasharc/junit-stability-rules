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
		return new MetricsCollectedStatement(base, description);
	}
	
	private class MetricsCollectedStatement extends Statement {
		private final Statement base;
		private final String testName;
		private final int rampUps;
		private final int loops;

		private MetricsCollectedStatement(Statement base, Description description) {
			this.base = base;
			this.testName = description.getDisplayName();
			Repeat repeat = description.getAnnotation(Repeat.class);
			if (repeat != null) {
				this.rampUps = repeat.rampUps();
				this.loops = repeat.loops();
			} else {
				this.rampUps = 0;
				this.loops = 1;
			}
		}

		@Override
		public void evaluate() throws Throwable {
			for (int i = 0; i < rampUps; i++) {
				base.evaluate();
			}
			
			for (Metric metric : metrics) {
				try {
					metric.onRunStart();
				} catch (Throwable e) {
					// Ignore.
				}
			}
			try {
				for (int i = 0; i < loops; i++) {
					base.evaluate();
				}
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
						results.addAll(metric.getResults(loops));
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
