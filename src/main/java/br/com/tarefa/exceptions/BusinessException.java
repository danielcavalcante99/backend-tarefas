package br.com.tarefa.exceptions;

public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 6197545074116542558L;

	public BusinessException(String message) {
		super(message);
	}

	public BusinessException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public BusinessException(String message, Object ...args) {
		super(String.format(message, args));
	}

}
