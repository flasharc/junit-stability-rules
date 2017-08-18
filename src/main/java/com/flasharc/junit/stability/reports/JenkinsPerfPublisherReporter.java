package com.flasharc.junit.stability.reports;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.flasharc.junit.stability.Metric;
import com.flasharc.junit.stability.Reporter;

public class JenkinsPerfPublisherReporter implements Reporter {

	private final File reportFile;
	private final Transformer transformer;
	private final Document document;

	public JenkinsPerfPublisherReporter(File reportFile) throws IOException {
		this.reportFile = reportFile;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "no");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "html"); // To
																		// avoid
																		// self-closing
																		// tags.

			DocumentBuilderFactory iFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = iFactory.newDocumentBuilder();
			document = builder.newDocument();
		} catch (TransformerConfigurationException e) {
			throw new IOException(e);
		} catch (ParserConfigurationException e) {
			throw new IOException(e);
		}
	}

	private void writeToReport(Node content) throws IOException, TransformerException {
		RandomAccessFile raf = new RandomAccessFile(reportFile, "rwd");
		final String finishTag = "</report>";
		try {
			long len = raf.length();
			if (len == 0) {
				raf.writeBytes("<?xml version='1.0' ?>");
			} else {
				raf.seek(len - finishTag.length());
			}

			StringWriter stringWriter = new StringWriter();
			StreamResult result = new StreamResult(stringWriter);
			DOMSource source = new DOMSource(content);
			transformer.transform(source, result);

			if (len != 0) {
				stringWriter.append(finishTag);
			}

			raf.writeBytes(stringWriter.toString());
		} finally {
			raf.close();
		}
	}

	@Override
	public void startReport(String reportName, String environment) throws Exception {
		if (reportFile.length() == 0) {
			Element element = document.createElement("report");
			element.setAttribute("name", reportName);
			element.setAttribute("categ", environment);
			writeToReport(element);
		}
	}

	@Override
	public void reportTest(String testName, List<Metric.MetricResult> metrics) throws Exception {
		Element testElement = document.createElement("test");
		testElement.setAttribute("name", testName);
		testElement.setAttribute("executed", "yes");

		Element resultElement = document.createElement("result");
		testElement.appendChild(resultElement);

		Element successElement = document.createElement("success");
		successElement.setAttribute("state", "100");
		successElement.setAttribute("passed", "yes");
		resultElement.appendChild(successElement);

		Element metricsElement = document.createElement("metrics");
		resultElement.appendChild(metricsElement);
		if (metrics != null) {
			for (Metric.MetricResult metric : metrics) {
				Element metricElement = document.createElement(metric.getMetricType());
				metricsElement.appendChild(metricElement);
				metricElement.setAttribute("isRelevant", "true");
				metricElement.setAttribute("mesure", Double.toString(metric.getMetricValue()));
				metricElement.setAttribute("unit", metric.getMetricUnit());
			}
		}

		writeToReport(testElement);
	}

}
