package br.com.awesome.exception;

public class AnnotationNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public AnnotationNotFoundException(String errorMessage) {
		super(errorMessage);
	}

}
