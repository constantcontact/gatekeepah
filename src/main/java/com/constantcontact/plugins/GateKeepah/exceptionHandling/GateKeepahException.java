package com.constantcontact.plugins.GateKeepah.exceptionHandling;

public class GateKeepahException extends InterruptedException {

	private static final long serialVersionUID = 2901455233461977156L;
	private String message = null;
	private final String PREFIX = "GateKeepah:     ";

	public GateKeepahException() {
		super();
	}
	
	public GateKeepahException(String message) {	
		super(message);
		if(message.contains(PREFIX)){
			this.message = message;
		}else {
			this.message = PREFIX + message;			
		}
	}

	@Override
	public String toString() {
		return message;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
