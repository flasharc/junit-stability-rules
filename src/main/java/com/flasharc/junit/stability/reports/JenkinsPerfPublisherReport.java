package com.flasharc.junit.stability.reports;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.flasharc.junit.stability.Metric;

public class JenkinsPerfPublisherReport {
	
	private final Writer reportWriter;
	private final XmlSerializer xmlSerializer;
	private final String environment;
	
	public JenkinsPerfPublisherReport(File reportFile, String environment) throws IOException, XmlPullParserException {
		this(new FileWriter(reportFile), environment);
	}
	
	public JenkinsPerfPublisherReport(Writer reportWriter, String environment) throws XmlPullParserException, IllegalArgumentException, IllegalStateException, IOException {
		xmlSerializer = new KXmlSerializer();;
		xmlSerializer.setOutput(reportWriter);
		this.environment = environment;
		this.reportWriter = reportWriter;
	}
	
	public void startReport(String reportName) throws Exception {
		xmlSerializer.startDocument(null, null);
		xmlSerializer.startTag(null, "report");
		xmlSerializer.attribute(null, "name", reportName);
		xmlSerializer.attribute(null, "categ", environment);
	}
	
	public void finishReport() throws Exception {
		try {
			xmlSerializer.endTag(null, "report");
			xmlSerializer.endDocument();
			xmlSerializer.flush();
		} finally {
			reportWriter.close();
		}
	}

	public void reportTest(String testName, List<Metric.MetricResult> metrics) throws Exception {
		xmlSerializer.startTag(null, "test");
		try {
			xmlSerializer.attribute(null, "name", testName).attribute(null, "executed", "yes");
			
			xmlSerializer.startTag(null, "result");
			xmlSerializer.startTag(null, "success");
			xmlSerializer.attribute(null, "passed", "yes").attribute(null, "state", "100");
			xmlSerializer.endTag(null, "success");
			
			xmlSerializer.startTag(null, "metrics");
			for (Metric.MetricResult metric : metrics) {
				xmlSerializer.startTag(null, metric.getMetricType())
							 .attribute(null, "unit", metric.getMetricUnit())
							 .attribute(null, "mesure", Double.toString(metric.getMetricValue()))
							 .attribute(null, "isRelevant", "true")
							 .endTag(null, metric.getMetricType());
			}
			xmlSerializer.endTag(null, "metrics");
			
			xmlSerializer.endTag(null, "result");
		} finally {
			xmlSerializer.endTag(null, "test");
		}
	}
	
}
