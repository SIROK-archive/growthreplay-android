package com.growthreplay;

public class GrowthReplayException extends RuntimeException {

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
