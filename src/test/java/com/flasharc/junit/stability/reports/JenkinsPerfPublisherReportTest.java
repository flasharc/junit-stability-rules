package com.flasharc.junit.stability.reports;

import static org.junit.Assert.*;

import java.io.StringWriter;

import org.junit.Test;

public class JenkinsPerfPublisherReportTest {

	@Test
	public void testReportGeneration() throws Exception {
		StringWriter writer = new StringWriter();
		JenkinsPerfPublisherReport publisher = new JenkinsPerfPublisherReport(writer, "Junit Testing");
		publisher.startReport("junittestReport");
		publisher.finishReport();
		
		String expectedString = "<?xml version='1.0' ?><report name=\"junittestReport\" categ=\"Junit Testing\" />";
		
		assertEquals(expectedString, writer.toString());
	}

}
