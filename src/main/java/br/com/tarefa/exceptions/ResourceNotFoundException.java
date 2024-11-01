package br.com.tarefa.exceptions;

public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -1731434106607832506L;

	public ResourceNotFoundException(String message) {
		super(message);
	}

	public ResourceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ResourceNotFoundException(String message, Object ...args) {
		super(String.format(message, args));
	}

}
