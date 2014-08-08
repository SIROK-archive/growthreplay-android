package com.growthreplay;

import com.growthbeat.GrowthbeatException;

public class GrowthReplayException extends GrowthbeatException {

	private static final long serialVersionUID = 1L;

	public GrowthReplayException() {
		super();
	}

	public GrowthReplayException(String message) {
		super(message);
	}

	public GrowthReplayException(Throwable throwable) {
		super(throwable);
	}

	public GrowthReplayException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
