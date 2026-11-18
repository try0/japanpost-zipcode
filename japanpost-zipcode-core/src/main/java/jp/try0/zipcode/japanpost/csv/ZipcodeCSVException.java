package jp.try0.zipcode.japanpost.csv;

public class ZipcodeCSVException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ZipcodeCSVException(Throwable cause) {
		super(cause);
	}

	public ZipcodeCSVException(String message) {
		super(message);
	}
}
