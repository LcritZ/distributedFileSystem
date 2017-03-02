package com.yu.exception;

public class StorageServerInvalibException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public StorageServerInvalibException() {
		super();
	}
	
	public StorageServerInvalibException(String msg) {
		super(msg);
	}

}
