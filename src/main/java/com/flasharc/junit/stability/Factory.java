package com.flasharc.junit.stability;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Factory {
	private static final String PROPERTIES_FILE = "/stability-test-config.prop";
	private static final String PROP_REPORT_PATH = "reportPath";
	private static final String PROP_REPORT_NAME = "reportName";
	private static final String PROP_REPORTER_CLASS = "reporterClass";
	
	private static final String DEFAULT_REPORT_PATH = "performance.report";
	private static final String DEFAULT_REPORT_NAME = "PerformanceTest";
	private static final String DEFAULT_REPORTER_CLASS = "com.flasharc.junit.stability.reports.JenkinsPerfPublisherReporter";
	
	private Factory(){}
	private static Properties properties;
	private static Reporter reporter;
	
	private synchronized static void initProperties() {
		if (properties == null) {
			properties = new Properties();
			InputStream propertiesStream = Factory.class.getResourceAsStream(PROPERTIES_FILE);
			if (propertiesStream != null) {
				try {
					properties.load(propertiesStream);
				} catch (Exception e) {
					throw new IllegalStateException("Unable to read properties file", e);
				} finally {
					try {
						propertiesStream.close();
					} catch (IOException e) {
						throw new IllegalStateException("Unable to close resource stream", e);
					}
				}
			}
		}
	}
	
	private synchronized static void initReporter() {
		if (reporter == null) {
			String reportPath = properties.getProperty(PROP_REPORT_PATH, DEFAULT_REPORT_PATH);
			File reportFile = new File(reportPath);
			if (reportFile.isDirectory()) {
				throw new IllegalStateException("Report path " + reportPath + " denotes a directory");
			}
			File reportDir = reportFile.getParentFile();
			if (!reportDir.exists() && !reportDir.mkdirs()) {
				throw new IllegalStateException("Unable to create the report directory: " + reportDir.getAbsolutePath());
			}
			
			String reporterClass = properties.getProperty(PROP_REPORTER_CLASS, DEFAULT_REPORTER_CLASS);
			
			try {
				reporter = (Reporter) Class.forName(reporterClass).getConstructor(File.class).newInstance(reportFile);
				String reportName = properties.getProperty(PROP_REPORT_NAME, DEFAULT_REPORT_NAME);
				String environment = getEnvironment();
				reporter.startReport(reportName, environment);
			} catch (Exception e) {
				throw new IllegalStateException("Unable to initialize reporter", e);
			}
		}
	}
	
	private static String getEnvironment() {
		try {
			Class.forName("android.app.Activity");
			// This is Android.
			return "Android " + getAndroidVersion() + "_" + getAndroidModel();
		} catch (ClassNotFoundException e) {
			// This is PC.
			String osName = System.getProperty("os.name");
			String osArch = System.getProperty("os.arch");
			String javaVersion = System.getProperty("java.runtime.version");
			return osName + " " + osArch + "_" + javaVersion;
		}
	}
	
	private static String getAndroidVersion() {
		try {
			Class<?> versionClass = Class.forName("android.os.Build$VERSION");
			return (String) versionClass.getField("RELEASE").get(null);
		} catch (Exception e) {
			return "X.X";
		}
	}
	
	private static String getAndroidModel() {
		String manufacturer = "";
		Class<?> buildClass = null;
		try {
			buildClass = Class.forName("android.os.Build");
			manufacturer = (String) buildClass.getField("MANUFACTURER").get(null);
		} catch (Exception e) {}
		
		String model = "UNKNOWN";
		try {
			model = (String) buildClass.getField("MODEL").get(null);
		} catch (Exception e) {}
		
		return model.startsWith(manufacturer)? model : manufacturer + " " + model;
	}
	
	@SafeVarargs
	public static MetricsCollector collectMetrics(Class<? extends Metric>... metricsClasses) {
		initProperties();
		initReporter();
		
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
