package com.flasharc.junit.stability.reports;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.flasharc.junit.stability.Metric;

public class JenkinsPerfPublisherReporterTest {
	
	private String readFileContent(File file) throws IOException {
		StringBuilder content = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line).append("\n");
			}
		} finally {
			reader.close();
		}
		return content.toString();
	}

	@Test
	public void testReportGeneration() throws Exception {
		File file = File.createTempFile("jmh", "test");
		JenkinsPerfPublisherReporter publisher = new JenkinsPerfPublisherReporter(file);
		publisher.startReport("junittestReport", "Junit Testing");
		
		String expectedString = "<?xml version='1.0' ?><report categ=\"Junit Testing\" name=\"junittestReport\"></report>\n";
		assertEquals(expectedString, readFileContent(file));
	}

	@Test
	public void testReportWithNoMetrics() throws Exception {
		File file = File.createTempFile("jmh", "test");
		JenkinsPerfPublisherReporter publisher = new JenkinsPerfPublisherReporter(file);
		publisher.startReport("junittestReport", "Junit Testing");
		publisher.reportTest("nometricstest", Collections.<Metric.MetricResult>emptyList());

		String expectedString = "<?xml version='1.0' ?><report categ=\"Junit Testing\" name=\"junittestReport\"><test executed=\"yes\" name=\"nometricstest\"><result><success passed=\"yes\" state=\"100\"></success><metrics></metrics></result></test></report>\n";
		assertEquals(expectedString, readFileContent(file));
	}
	
	@Test
	public void testReportWithNullMetrics() throws Exception {
		File file = File.createTempFile("jmh", "test");
		JenkinsPerfPublisherReporter publisher = new JenkinsPerfPublisherReporter(file);
		publisher.startReport("junittestReport","Junit Testing");
		publisher.reportTest("nometricstest", null);

		String expectedString = "<?xml version='1.0' ?><report categ=\"Junit Testing\" name=\"junittestReport\"><test executed=\"yes\" name=\"nometricstest\"><result><success passed=\"yes\" state=\"100\"></success><metrics></metrics></result></test></report>\n";
		assertEquals(expectedString, readFileContent(file));
	}

	@Test
	public void testReportWithMetrics() throws Exception {
		File file = File.createTempFile("jmh", "test");
		JenkinsPerfPublisherReporter publisher = new JenkinsPerfPublisherReporter(file);
		publisher.startReport("junittestReport","Junit Testing");
		publisher.reportTest("nometricstest", Arrays.<Metric.MetricResult>asList(new MetricResultImpl("time", "ns", 12440)));

		String expectedString = "<?xml version='1.0' ?><report categ=\"Junit Testing\" name=\"junittestReport\"><test executed=\"yes\" name=\"nometricstest\"><result><success passed=\"yes\" state=\"100\"></success><metrics><time isRelevant=\"true\" mesure=\"12440.0\" unit=\"ns\"></time></metrics></result></test></report>\n";
		assertEquals(expectedString, readFileContent(file));
	}
	
	@Test
	public void testReportWithMultipleTests() throws Exception {
		File file = File.createTempFile("jmh", "test");
		JenkinsPerfPublisherReporter publisher = new JenkinsPerfPublisherReporter(file);
		publisher.startReport("junittestReport","Junit Testing");
		publisher.reportTest("nometricstest", Arrays.<Metric.MetricResult>asList(new MetricResultImpl("time", "ns", 12440)));
		publisher.reportTest("secondtest", Arrays.<Metric.MetricResult>asList(new MetricResultImpl("time", "ns", 821)));

		String expectedString = "<?xml version='1.0' ?><report categ=\"Junit Testing\" name=\"junittestReport\"><test executed=\"yes\" name=\"nometricstest\"><result><success passed=\"yes\" state=\"100\"></success><metrics><time isRelevant=\"true\" mesure=\"12440.0\" unit=\"ns\"></time></metrics></result></test><test executed=\"yes\" name=\"secondtest\"><result><success passed=\"yes\" state=\"100\"></success><metrics><time isRelevant=\"true\" mesure=\"821.0\" unit=\"ns\"></time></metrics></result></test></report>\n";
		assertEquals(expectedString, readFileContent(file));
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
