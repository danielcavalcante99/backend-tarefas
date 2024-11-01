package br.com.tarefa.exceptions;

public class AuthorizationException extends RuntimeException {

	private static final long serialVersionUID = 6197545074116542558L;

	public AuthorizationException(String message) {
		super(message);
	}

	public AuthorizationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public AuthorizationException(String message, Object ...args) {
		super(String.format(message, args));
	}

}
