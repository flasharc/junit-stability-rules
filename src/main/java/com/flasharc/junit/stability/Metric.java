package com.flasharc.junit.stability;

public interface Metric {
	void onRunStart();
	void onRunFinish();
	void processResults();
}
