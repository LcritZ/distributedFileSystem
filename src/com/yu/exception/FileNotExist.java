package com.yu.exception;

public class FileNotExist extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public FileNotExist() {
		super();
	}
	public FileNotExist(String msg) {
		super(msg);
	}
}
