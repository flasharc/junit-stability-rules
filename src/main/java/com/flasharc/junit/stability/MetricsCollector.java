package com.flasharc.junit.stability;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class MetricsCollector implements TestRule {
	private final Metric[] metrics;

	public MetricsCollector(Metric... metrics) {
		if (metrics == null) {
			throw new NullPointerException("No metrics defined");
		}
		this.metrics = metrics;
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new MetricsCollectedStatement(base);
	}

	private class MetricsCollectedStatement extends Statement {
		private final Statement base;

		private MetricsCollectedStatement(Statement base) {
			this.base = base;
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
				for (Metric metric : metrics) {
					try {
						metric.onRunFinish();
					} catch (Throwable e) {
						// Ignore.
					}
				}
				for (Metric metric : metrics) {
					try {
						metric.getResults();
					} catch (Throwable e) {
						// Ignore.
					}
				}
			}
		}
	}
}
