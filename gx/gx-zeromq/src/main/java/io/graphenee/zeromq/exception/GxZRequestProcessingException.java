package io.graphenee.zeromq.exception;

@SuppressWarnings("serial")
public class GxZRequestProcessingException extends Exception {

	public GxZRequestProcessingException() {
		super();
	}

	public GxZRequestProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public GxZRequestProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

	public GxZRequestProcessingException(String message) {
		super(message);
	}

	public GxZRequestProcessingException(Throwable cause) {
		super(cause);
	}

}
