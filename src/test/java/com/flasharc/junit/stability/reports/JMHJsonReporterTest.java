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
	public void testReportGeneration() throws Exception {
		File file = File.createTempFile("jmh", "test");
		JMHJsonReporter publisher = new JMHJsonReporter(file);
		publisher.startReport("","");
		
		String expectedString = "<?xml version='1.0' ?><report name=\"junittestReport\" categ=\"Junit Testing\" />";
		assertEquals(expectedString, file.toString());
	}

	@Test
	public void testReportWithNoMetrics() throws Exception {
		File file = File.createTempFile("jmh", "test");
		JMHJsonReporter publisher = new JMHJsonReporter(file);
		publisher.startReport("junittestReport", "Junit Testing");
		publisher.reportTest("nometricstest", Collections.<Metric.MetricResult>emptyList());

		String expectedString = "<?xml version='1.0' ?><report name=\"junittestReport\" categ=\"Junit Testing\"><test name=\"nometricstest\" executed=\"yes\"><result><success passed=\"yes\" state=\"100\" /><metrics /></result></test></report>";
		assertEquals(expectedString, file.toString());
	}
	
	@Test
	public void testReportWithNullMetrics() throws Exception {
		File file = File.createTempFile("jmh", "test");
		JMHJsonReporter publisher = new JMHJsonReporter(file);
		publisher.startReport("junittestReport","Junit Testing");
		publisher.reportTest("nometricstest", null);

		String expectedString = "<?xml version='1.0' ?><report name=\"junittestReport\" categ=\"Junit Testing\"><test name=\"nometricstest\" executed=\"yes\"><result><success passed=\"yes\" state=\"100\" /><metrics /></result></test></report>";
		assertEquals(expectedString, file.toString());
	}

	@Test
	public void testReportWithMetrics() throws Exception {
		File file = File.createTempFile("jmh", "test");
		JMHJsonReporter publisher = new JMHJsonReporter(file);
		publisher.startReport("junittestReport","Junit Testing");
		publisher.reportTest("nometricstest", Arrays.<Metric.MetricResult>asList(new MetricResultImpl("time", "ns", 12440)));

		String expectedString = "<?xml version='1.0' ?><report name=\"junittestReport\" categ=\"Junit Testing\"><test name=\"nometricstest\" executed=\"yes\"><result><success passed=\"yes\" state=\"100\" /><metrics><time unit=\"ns\" mesure=\"12440.0\" isRelevant=\"true\" /></metrics></result></test></report>";
		assertEquals(expectedString, file.toString());
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