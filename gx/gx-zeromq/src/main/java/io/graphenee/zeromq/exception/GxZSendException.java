package io.graphenee.zeromq.exception;

@SuppressWarnings("serial")
public class GxZSendException extends Exception {

	private int errorCode;

	public GxZSendException(int errorCode, String message) {
		super();
		this.errorCode = errorCode;
	}

	public GxZSendException(int errorCode, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.errorCode = errorCode;
	}

	public GxZSendException(int errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

}
