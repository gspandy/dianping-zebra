package com.dianping.zebra.group.exception;

public class SingleDataSourceException extends RuntimeException {

	private static final long serialVersionUID = 6890440268267847181L;

	public SingleDataSourceException() {
		super();
	}

	public SingleDataSourceException(String message) {
		super(message);
	}

	public SingleDataSourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public SingleDataSourceException(Throwable cause) {
		super(cause);
	}
}