package com.yu.exception;

public class WrongArgumentException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public WrongArgumentException() {
		super();
	}
	
	public WrongArgumentException(String msg){
		super(msg);
	}
	
}
