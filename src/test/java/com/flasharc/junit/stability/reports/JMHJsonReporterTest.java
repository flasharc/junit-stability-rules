package com.flasharc.junit.stability.reports;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.flasharc.junit.stability.Metric;

public class JMHJsonReporterTest {

	@Test
	//TODO: Write proper tests.
	public void testPlaceholder() throws Exception {
		File file = File.createTempFile("jmh", "test");
		JMHJsonReporter publisher = new JMHJsonReporter(file);
		publisher.startReport("","");
	}
	
	private static class MetricResultImpl implements Metric.MetricResult {
		private final String metricType;
		private final String metricUnit;
		private final double metricValue;
		
		private MetricResultImpl(String metricType, String metricUnit, double metricValue) {
			this.metricType = metricType;
			this.metricUnit = metricUnit;
			this.metricValue = metricValue;
		}
		

		@Override
		public String getMetricType() {
			return metricType;
		}

		@Override
		public String getMetricUnit() {
			return metricUnit;
		}

		@Override
		public double getMetricValue() {
			return metricValue;
		}
	}

}
