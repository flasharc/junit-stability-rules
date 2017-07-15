package com.flasharc.junit.stability.metrics.android;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collections;
import java.util.List;

import com.flasharc.junit.stability.Metric;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

public class CPUMetric implements Metric {
	
	private final int pid;
	
	private long startCpuJiffies;
	private long endCpuJiffies;
	
	public CPUMetric() {
		ActivityManager manager = (ActivityManager) InstrumentationRegistry.getTargetContext().getSystemService(Context.ACTIVITY_SERVICE);
		int pid = -1;
		for (RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
			if (processInfo.processName.equals(InstrumentationRegistry.getTargetContext().getPackageName())) {
				pid = processInfo.pid;
				break;
			}
		}
		this.pid = pid;
	}
	
	private long getCurrentCpuJiffies() throws Exception {
		try (BufferedReader reader = new BufferedReader(new FileReader("/proc/" + pid + "/stat"))) {
			String[] cpuStat = reader.readLine().split("\\s+");
			return Long.parseLong(cpuStat[13]) + Long.parseLong(cpuStat[14]) + Long.parseLong(cpuStat[15]) + Long.parseLong(cpuStat[16]);
		}
	}

	@Override
	public void onRunStart() throws Exception {
		startCpuJiffies = getCurrentCpuJiffies();
	}

	@Override
	public void onRunFinish() throws Exception {
		endCpuJiffies = getCurrentCpuJiffies();
	}

	@Override
	public List<MetricResult> getResults() {
		return Collections.<MetricResult>singletonList(new CpuMetricResult(endCpuJiffies - startCpuJiffies));
	}

	private static class CpuMetricResult implements MetricResult {
		
		private static final String METRIC_TYPE = "CPU Jiffies";
		private static final String METRIC_UNIT = "jiffy";
		
		private final double cpuJiffes;
		
		private CpuMetricResult(double cpuJiffes) {
			this.cpuJiffes = cpuJiffes;
		}

		@Override
		public String getMetricType() {
			return METRIC_TYPE;
		}
		
		@Override
		public String getMetricUnit() {
			return METRIC_UNIT;
		}

		@Override
		public double getMetricValue() {
			return cpuJiffes;
		}
	}
}
