package br.com.tarefa.exceptions;

public class RevokeTokenException extends RuntimeException {

	private static final long serialVersionUID = 637035049234801576L;

	public RevokeTokenException(String message) {
		super(message);
	}

	public RevokeTokenException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public RevokeTokenException(String message, Object ...args) {
		super(String.format(message, args));
	}

}
