package com.flasharc.junit.stability;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import com.flasharc.junit.stability.reports.JenkinsPerfPublisherReport;

public final class Factory {
	private static final String PROPERTIES_FILE = "/stability-test-config.prop";
	private static final String PROP_REPORT_PATH = "reportPath";
	private static final String PROP_REPORT_NAME = "reportName";
	
	private static final String ENVIRONMENT = android.os.Build.MODEL;

	private Factory(){}
	private static Properties properties = new Properties();
	private static Reporter reporter;
	
	static {
		InputStream propertiesStream = Factory.class.getResourceAsStream(PROPERTIES_FILE);
		try {
			try {
				properties.load(propertiesStream);
			} finally {
				propertiesStream.close();
			}
		} catch (Exception e) {
			// Consume all exception.
		}
	}
	
	@SafeVarargs
	public static MetricsCollector collectMetrics(Class<? extends Metric>... metricsClasses) {
		if (reporter == null) {
			String reportPath = properties.getProperty(PROP_REPORT_PATH);
			if (reportPath == null) {
				throw new IllegalStateException("Report path not specified in Properties file");
			}
			File reportFile = new File(reportPath);
			reportFile.getParentFile().mkdirs();
			
			try {
				reporter = new JenkinsPerfPublisherReport(reportFile);
				reporter.startReport(properties.getProperty(PROP_REPORT_NAME), ENVIRONMENT);
			} catch (Exception e) {
				throw new IllegalStateException("Unable to write to report file", e);
			}
		}
		
		Metric[] metrics = new Metric[metricsClasses.length];
		for (int i = 0; i < metrics.length; i++) {
			try {
				metrics[i] = metricsClasses[i].newInstance();
			} catch (Exception e) {
				throw new IllegalArgumentException("Unable to instantiate all provided classes", e);
			}
		}
		return new MetricsCollector(reporter, metrics);
	}
	
}
